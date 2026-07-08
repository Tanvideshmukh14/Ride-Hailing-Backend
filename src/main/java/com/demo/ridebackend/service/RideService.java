package com.demo.ridebackend.service;

import com.demo.ridebackend.dto.request.RideRequestDTO;
import com.demo.ridebackend.dto.request.OtpVerificationRequest;
import com.demo.ridebackend.dto.response.RideResponse;
import com.demo.ridebackend.entity.Payment;
import com.demo.ridebackend.entity.Ride;
import com.demo.ridebackend.entity.User;
import com.demo.ridebackend.enums.PaymentStatus;
import com.demo.ridebackend.enums.Role;
import com.demo.ridebackend.enums.RideStatus;
import com.demo.ridebackend.exception.InvalidOtpException;
import com.demo.ridebackend.exception.InvalidRideStateException;
import com.demo.ridebackend.exception.OtpExpiredException;
import com.demo.ridebackend.exception.OtpNotVerifiedException;
import com.demo.ridebackend.exception.RideNotFoundException;
import com.demo.ridebackend.repository.PaymentRepository;
import com.demo.ridebackend.repository.RideRepository;
import com.demo.ridebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final RewardService rewardService;
    private final PaymentRepository paymentRepository;

    @Value("${ride.otp.expiry-minutes:5}")
    private long otpExpiryMinutes;

    private static final double MATCH_RADIUS_KM = 5.0;

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public RideResponse requestRide(RideRequestDTO request) {

        User rider = getLoggedInUser();

        if (rider.getRole() != Role.RIDER) {
            throw new RuntimeException("Only riders can request rides.");
        }

        double distance = calculateDistance(
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDropLatitude(),
                request.getDropLongitude()
        );

        Ride ride = Ride.builder()
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .dropLatitude(request.getDropLatitude())
                .dropLongitude(request.getDropLongitude())
                .distance(distance)
                .fare(distance * 20)
                .requestedTime(LocalDateTime.now())
                .status(RideStatus.REQUESTED)
                .rider(rider)
                .requestedVehicleType(request.getRequestedVehicleType())
                .preferredDriverGender(request.getPreferredDriverGender())
                .build();

        ride = rideRepository.save(ride);

        // Driver matching: notify nearby drivers who match vehicle/gender preference.
        List<User> nearbyDrivers = userRepository.findDrivers(
                ride.getPickupLatitude(),
                ride.getPickupLongitude(),
                ride.getRequestedVehicleType(),
                ride.getPreferredDriverGender(),
                MATCH_RADIUS_KM,
                Role.DRIVER
        );

        log.info("--- [NOTIFICATION] --- Sent ride request notification to {} nearby drivers matching preferences.",
                nearbyDrivers.size());

        return mapToRideResponse(ride);
    }

    public List<RideResponse> getMyRides() {

        User user = getLoggedInUser();

        List<Ride> rides = (user.getRole() == Role.DRIVER)
                ? rideRepository.findByDriver(user)
                : rideRepository.findByRider(user);

        return rides.stream()
                .map(this::mapToRideResponse)
                .toList();
    }

    public List<RideResponse> getAvailableRides() {

        User driver = getLoggedInUser();

        if (driver.getRole() != Role.DRIVER) {
            throw new RuntimeException("Only drivers can view available rides.");
        }

        return rideRepository.findByStatus(RideStatus.REQUESTED)
                .stream()
                .filter(ride -> calculateDistance(
                        driver.getLatitude(),
                        driver.getLongitude(),
                        ride.getPickupLatitude(),
                        ride.getPickupLongitude()
                ) <= MATCH_RADIUS_KM)
                .map(this::mapToRideResponse)
                .toList();
    }

    @Transactional
    public RideResponse acceptRide(Long rideId) {

        User driver = getLoggedInUser();

        if (driver.getRole() != Role.DRIVER) {
            throw new RuntimeException("Only drivers can accept rides.");
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + rideId));

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidRideStateException("Ride cannot be accepted from status: " + ride.getStatus());
        }

        driver.setAvailable(false);
        userRepository.save(driver);

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedTime(LocalDateTime.now());

        // Generate a fresh 4-digit OTP only at the point of acceptance.
        ride.setRideOtp(generateOtp());
        ride.setOtpGeneratedTime(LocalDateTime.now());
        ride.setOtpVerified(false);

        Ride savedRide = rideRepository.save(ride);

        log.info("--- [NOTIFICATION] --- Ride status updated: ACCEPTED. Notifying Rider: {}",
                savedRide.getRider().getName());

        RideResponse response = mapToRideResponse(savedRide);
        response.setMessage("Ride accepted. Share the OTP " + savedRide.getRideOtp() + " with your driver to start the ride.");
        return response;
    }

    /**
     * Driver-side verification of the OTP told to them by the passenger.
     * On success the ride is marked otpVerified = true so that startRide()
     * is allowed to proceed. The OTP itself is not reusable once verified.
     */
    @Transactional
    public RideResponse verifyOtp(Long rideId, OtpVerificationRequest request) {

        User driver = getLoggedInUser();

        if (driver.getRole() != Role.DRIVER) {
            throw new RuntimeException("Only drivers can verify ride OTP.");
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + rideId));

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStateException(
                    "OTP can only be verified for a ride that has been accepted. Current status: " + ride.getStatus());
        }

        if (Boolean.TRUE.equals(ride.getOtpVerified())) {
            throw new InvalidOtpException("OTP has already been verified for this ride.");
        }

        if (ride.getRideOtp() == null || ride.getOtpGeneratedTime() == null) {
            throw new InvalidOtpException("No OTP has been generated for this ride.");
        }

        boolean expired = Duration.between(ride.getOtpGeneratedTime(), LocalDateTime.now())
                .toMinutes() >= otpExpiryMinutes;

        if (expired) {
            throw new OtpExpiredException("OTP has expired. Please ask the passenger to regenerate it.");
        }

        if (!ride.getRideOtp().equals(request.otp())) {
            throw new InvalidOtpException("Invalid OTP entered.");
        }

        ride.setOtpVerified(true);

        Ride savedRide = rideRepository.save(ride);

        RideResponse response = mapToRideResponse(savedRide);
        response.setMessage("OTP verified successfully. You can now start the ride.");
        return response;
    }

    /**
     * Rider-side regeneration of the ride OTP. Allowed only while the ride
     * is ACCEPTED and not yet verified — covers expiry, or driver simply
     * not receiving it. Old OTP discarded, fresh 4-digit code + timestamp issued.
     */
    @Transactional
    public RideResponse regenerateOtp(Long rideId) {

        User rider = getLoggedInUser();

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + rideId));

        if (!ride.getRider().getId().equals(rider.getId())) {
            throw new RuntimeException("Only the rider who booked this ride can regenerate its OTP.");
        }

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStateException(
                    "OTP can only be regenerated for a ride that has been accepted. Current status: " + ride.getStatus());
        }

        if (Boolean.TRUE.equals(ride.getOtpVerified())) {
            throw new InvalidOtpException("OTP has already been verified for this ride and cannot be regenerated.");
        }

        ride.setRideOtp(generateOtp());
        ride.setOtpGeneratedTime(LocalDateTime.now());
        ride.setOtpVerified(false);

        Ride savedRide = rideRepository.save(ride);

        RideResponse response = mapToRideResponse(savedRide);
        response.setMessage("New OTP " + savedRide.getRideOtp() + " generated. Share it with your driver.");
        return response;
    }

    @Transactional
    public RideResponse startRide(Long rideId) {

        User driver = getLoggedInUser();

        if (driver.getRole() != Role.DRIVER) {
            throw new RuntimeException("Only drivers can start rides.");
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + rideId));

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStateException("Ride cannot be started from status: " + ride.getStatus());
        }

        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("Only the assigned driver can start this ride.");
        }

        if (!Boolean.TRUE.equals(ride.getOtpVerified())) {
            throw new OtpNotVerifiedException("Ride cannot start until the OTP has been verified by the driver.");
        }

        ride.setStatus(RideStatus.ONGOING);
        ride.setStartedTime(LocalDateTime.now());

        // OTP is single-use: invalidate it once the ride has actually started.
        ride.setRideOtp(null);
        ride.setOtpGeneratedTime(null);

        Ride savedRide = rideRepository.save(ride);

        log.info("--- [NOTIFICATION] --- Ride status updated: ONGOING. Trip started.");

        return mapToRideResponse(savedRide);
    }

    @Transactional
    public RideResponse completeRide(Long rideId) {

        User loggedInDriver = getLoggedInUser();

        if (loggedInDriver.getRole() != Role.DRIVER) {
            throw new RuntimeException("Only drivers can complete rides.");
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + rideId));

        if (ride.getStatus() != RideStatus.ONGOING) {
            throw new InvalidRideStateException("Ride cannot be completed from status: " + ride.getStatus());
        }

        if (ride.getDriver() == null || !ride.getDriver().getId().equals(loggedInDriver.getId())) {
            throw new RuntimeException("Only the assigned driver can complete this ride.");
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedTime(LocalDateTime.now());

        // Driver becomes available again for new ride requests.
        loggedInDriver.setAvailable(true);
        userRepository.save(loggedInDriver);

        Ride savedRide = rideRepository.save(ride);

        // Create a pending payment entry for this ride.
        Payment payment = Payment.builder()
                .amount(savedRide.getFare())
                .paymentStatus(PaymentStatus.PENDING)
                .paymentTime(LocalDateTime.now())
                .ride(savedRide)
                .build();
        paymentRepository.save(payment);

        // Reward & Loyalty Program: award points automatically on completion.
        int pointsEarned = rewardService.awardPointsForCompletedRide(savedRide);

        log.info("--- [NOTIFICATION] --- Ride status updated: COMPLETED. Total Fare: {}", savedRide.getFare());

        RideResponse response = mapToRideResponse(savedRide);
        response.setRewardPointsEarned(pointsEarned);
        response.setMessage(pointsEarned > 0
                ? "Ride completed. Payment pending. You earned " + pointsEarned + " reward points."
                : "Ride completed. Payment pending.");

        return response;
    }

    private String generateOtp() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
    }

    private RideResponse mapToRideResponse(Ride ride) {

        return RideResponse.builder()
                .rideId(ride.getId())
                .riderName(ride.getRider().getName())
                .driverName(
                        ride.getDriver() != null
                                ? ride.getDriver().getName()
                                : null
                )
                .status(ride.getStatus())
                .fare(ride.getFare())
                .build();
    }

    private double calculateDistance(double lat1, double lon1,
                                     double lat2, double lon2) {

        final int EARTH_RADIUS = 6371; // km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
