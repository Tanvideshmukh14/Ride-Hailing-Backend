package com.demo.ridebackend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRedemptionResponse {

    private String couponCode;

    private Long rideId;

    private Double originalFare;

    private Double discountApplied;

    private Double payableAmount;

    private Integer remainingRewardPoints;

    private String message;
}