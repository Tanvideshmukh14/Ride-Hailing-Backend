package com.demo.ridebackend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardPointsResponse {

    private Long riderId;

    private Integer totalRewardPoints;

    private Integer pointsToNextCoupon;
}