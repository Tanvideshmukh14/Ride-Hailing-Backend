package com.demo.ridebackend.service;

import com.demo.ridebackend.dto.request.RideRequestDTO;
import com.demo.ridebackend.dto.response.RideResponse;
import com.demo.ridebackend.entity.Ride;
import com.demo.ridebackend.entity.User;
import com.demo.ridebackend.enums.Role;
import com.demo.ridebackend.enums.RideStatus;
import com.demo.ridebackend.repository.RideRepository;
import com.demo.ridebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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

        // FIX: Added Role.DRIVER parameter to match the updated repository method signature
        List<User> nearbyDrivers = userRepository.findDrivers(
                ride.getPickupLatitude(),
                ride.getPickupLongitude(),
                ride.getRequestedVehicleType(),
                ride.getPreferredDriverGender(),
                5.0,
                Role.DRIVER
        );

        log.info("--- [NOTIFICATION] --- Sent ride request notification to {} nearby drivers matching preferences.", nearbyDrivers.size());

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
            throw new RuntimeException("Access denied: Only drivers can query available rides.");
        }

        return rideRepository.findByStatus(RideStatus.REQUESTED)
                .stream()
                .filter(ride -> calculateDistance(
                        driver.getLatitude(), driver.getLongitude(),
                        ride.getPickupLatitude(), ride.getPickupLongitude()) <= 5.0)
                .map(this::mapToRideResponse)
                .toList();
    }

    public RideResponse acceptRide(Long rideId) {
        User driver = getLoggedInUser();

        if (driver.getRole() != Role.DRIVER) {
            throw new RuntimeException("Only drivers can accept rides.");
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new RuntimeException("Ride cannot be accepted. Current status: " + ride.getStatus());
        }

        driver.setAvailable(false);
        userRepository.save(driver);

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);
        ride.setAcceptedTime(LocalDateTime.now());

        log.info("--- [NOTIFICATION] --- Ride status updated: ACCEPTED. Notifying Rider: {}", ride.getRider().getName());
        return mapToRideResponse(rideRepository.save(ride));
    }

    public RideResponse startRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new RuntimeException("Ride cannot be started. Must be ACCEPTED first.");
        }

        ride.setStatus(RideStatus.ONGOING);
        ride.setStartedTime(LocalDateTime.now());

        log.info("--- [NOTIFICATION] --- Ride status updated: ONGOING. Trip started.");
        return mapToRideResponse(rideRepository.save(ride));
    }

    public RideResponse completeRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.ONGOING) {
            throw new RuntimeException("Ride cannot be completed. Must be ONGOING first.");
        }

        User driver = ride.getDriver();
        if (driver != null) {
            driver.setAvailable(true);
            userRepository.save(driver);
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setCompletedTime(LocalDateTime.now());

        log.info("--- [NOTIFICATION] --- Ride status updated: COMPLETED. Total Fare: {}", ride.getFare());
        return mapToRideResponse(rideRepository.save(ride));
    }

    private RideResponse mapToRideResponse(Ride ride) {
        return RideResponse.builder()
                .rideId(ride.getId())
                .riderName(ride.getRider().getName())
                .driverName(ride.getDriver() != null ? ride.getDriver().getName() : null)
                .status(ride.getStatus())
                .fare(ride.getFare())
                .build();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371;
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