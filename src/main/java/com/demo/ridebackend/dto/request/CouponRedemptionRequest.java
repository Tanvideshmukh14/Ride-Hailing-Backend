package com.demo.ridebackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CouponRedemptionRequest(

        @NotBlank(message = "Coupon code is required")
        String couponCode,

        @NotNull(message = "Ride id is required")
        Long rideId

) {
}