package com.demo.ridebackend.dto.request;

import com.demo.ridebackend.enums.Role;
import com.demo.ridebackend.enums.Gender; // Added for Version 5
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String phone;

    @NotNull
    private Role role;

    private Double latitude;

    private Double longitude;

    // ============================================================
    // START OF VERSION 5 CHANGES: REGISTRATION PREFERENCES
    // ============================================================

    private Gender gender; // Captures driver's gender (MALE, FEMALE, OTHER)

    private String vehicleType; // Captures vehicle class (e.g., "SEDAN", "SUV")

    // ============================================================
    // END OF VERSION 5 CHANGES
    // ============================================================
}