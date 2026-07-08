package com.demo.ridebackend.service.impl;
import com.demo.ridebackend.enums.Role;
import com.demo.ridebackend.enums.RideStatus;
import com.demo.ridebackend.dto.request.SosRequest;
import com.demo.ridebackend.dto.response.EmergencyResponse;
import com.demo.ridebackend.entity.EmergencyLog;
import com.demo.ridebackend.entity.Ride;
import com.demo.ridebackend.entity.User;
import com.demo.ridebackend.exception.EmergencyAccessDeniedException;
import com.demo.ridebackend.exception.EmergencyNotFoundException;
import com.demo.ridebackend.exception.InvalidRideStateException;
import com.demo.ridebackend.exception.RideNotFoundException;
import com.demo.ridebackend.repository.EmergencyLogRepository;
import com.demo.ridebackend.repository.RideRepository;
import com.demo.ridebackend.repository.UserRepository;
import com.demo.ridebackend.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demo.ridebackend.enums.EmergencyStatus;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyServiceImpl implements EmergencyService {

    private static final Logger log = LoggerFactory.getLogger(EmergencyServiceImpl.class);

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final EmergencyLogRepository emergencyLogRepository;

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public EmergencyResponse raiseSos(Long rideId, SosRequest request) {

        User rider = getLoggedInUser();

        if (rider.getRole() != Role.RIDER) {
            throw new RuntimeException("Only riders can raise an SOS.");
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + rideId));

        if (!ride.getRider().getId().equals(rider.getId())) {
            throw new RuntimeException("You can only raise an SOS on your own ride.");
        }

        if (ride.getStatus() != RideStatus.ONGOING) {
            throw new InvalidRideStateException(
                    "SOS can only be raised during an ongoing ride. Current status: " + ride.getStatus());
        }

        EmergencyLog emergencyLog = EmergencyLog.builder()
                .ride(ride)
                .rider(ride.getRider())
                .driver(ride.getDriver())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .timestamp(LocalDateTime.now())
                .status(EmergencyStatus.ACTIVE)
                .build();

        EmergencyLog savedLog = emergencyLogRepository.save(emergencyLog);

        // Update ride status so drivers/riders and dashboards immediately reflect the emergency.
        ride.setStatus(RideStatus.EMERGENCY);
        rideRepository.save(ride);

        notifyAdmin(savedLog);
        notifyEmergencyContacts(savedLog);

        return mapToResponse(savedLog);
    }

    /**
     * Placeholder integration point for a real-time admin alert channel
     * (e.g. push notification, WebSocket broadcast, or SMS gateway).
     * Wired here so the flow is complete end-to-end; swap the log line
     * for an actual notification provider when one is available.
     */
    private void notifyAdmin(EmergencyLog emergencyLog) {
        log.warn(
                "[SOS ALERT] Emergency #{} raised on ride #{} by rider '{}' at ({}, {}). Admin notified.",
                emergencyLog.getId(), emergencyLog.getRide().getId(), emergencyLog.getRider().getName(),
                emergencyLog.getLatitude(), emergencyLog.getLongitude());
    }

    /**
     * Placeholder integration point for notifying the rider's saved
     * emergency contact (SMS/call gateway). Falls back to a log warning
     * when no contact number is on file.
     */
    private void notifyEmergencyContacts(EmergencyLog emergencyLog) {

        String contactPhone = emergencyLog.getRider().getEmergencyContactPhone();

        if (contactPhone == null || contactPhone.isBlank()) {
            log.warn(
                    "[SOS ALERT] Rider '{}' has no emergency contact configured.",
                    emergencyLog.getRider().getName());
        } else {
            log.warn(
                    "[SOS ALERT] Notifying emergency contact {} for rider '{}'.",
                    contactPhone, emergencyLog.getRider().getName());
        }
    }

    @Override
    public EmergencyResponse getEmergencyById(Long emergencyId) {

        User user = getLoggedInUser();

        EmergencyLog log = emergencyLogRepository.findById(emergencyId)
                .orElseThrow(() -> new EmergencyNotFoundException("Emergency not found with id: " + emergencyId));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isRider = log.getRider().getId().equals(user.getId());
        boolean isDriver = log.getDriver() != null && log.getDriver().getId().equals(user.getId());

        if (!isAdmin && !isRider && !isDriver) {
            throw new EmergencyAccessDeniedException("You are not authorized to view this emergency record.");
        }

        return mapToResponse(log);
    }

    @Override
    public List<EmergencyResponse> getAllEmergencies() {

        return emergencyLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private EmergencyResponse mapToResponse(EmergencyLog log) {

        return EmergencyResponse.builder()
                .emergencyId(log.getId())
                .rideId(log.getRide().getId())
                .rideStatus(log.getRide().getStatus())
                .riderName(log.getRider().getName())
                .riderPhone(log.getRider().getPhone())
                .driverName(log.getDriver() != null ? log.getDriver().getName() : null)
                .driverPhone(log.getDriver() != null ? log.getDriver().getPhone() : null)
                .latitude(log.getLatitude())
                .longitude(log.getLongitude())
                .timestamp(log.getTimestamp())
                .status(log.getStatus())
                .build();
    }
}