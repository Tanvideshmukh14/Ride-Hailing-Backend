package com.demo.ridebackend.dto.request;

import com.demo.ridebackend.enums.Gender; // Import your Gender enum
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequestDTO {

    @NotNull
    private Double pickupLatitude;

    @NotNull
    private Double pickupLongitude;

    @NotNull
    private Double dropLatitude;

    @NotNull
    private Double dropLongitude;

    // --- Added for Version 5: Feature of Preference ---
    private String requestedVehicleType; // e.g., "SEDAN", "SUV", "BIKE"

    private Gender preferredDriverGender; // Optional user preference filter
}