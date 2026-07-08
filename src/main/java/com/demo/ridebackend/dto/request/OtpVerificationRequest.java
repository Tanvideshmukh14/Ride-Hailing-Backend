package com.demo.ridebackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerificationRequest(

        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "\\d{4}", message = "OTP must be exactly 4 digits")
        String otp

) {
}