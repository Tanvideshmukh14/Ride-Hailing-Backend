package com.demo.ridebackend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardCouponResponse {

    private String couponCode;

    private Double maxDiscount;

    private Double minEligibleFare;

    private Double maxEligibleFare;

    private LocalDateTime issuedDate;

    private LocalDateTime expiryDate;

    private Boolean redeemed;

    private LocalDateTime redeemedDate;
}