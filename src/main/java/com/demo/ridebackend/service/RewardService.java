package com.demo.ridebackend.service;

import com.demo.ridebackend.dto.request.CouponRedemptionRequest;
import com.demo.ridebackend.dto.response.CouponRedemptionResponse;
import com.demo.ridebackend.dto.response.RewardCouponResponse;
import com.demo.ridebackend.dto.response.RewardPointsResponse;
import com.demo.ridebackend.entity.Ride;

import java.util.List;

public interface RewardService {

    /**
     * Awards reward points for a just-completed ride and, if the rider's
     * cumulative balance crosses a 100-point milestone, automatically
     * issues a new Free Ride Coupon. Meant to be called from the ride
     * completion flow.
     *
     * @return number of reward points earned for this specific ride
     */
    int awardPointsForCompletedRide(Ride ride);

    RewardPointsResponse getMyRewardPoints();

    List<RewardCouponResponse> getMyCoupons();

    CouponRedemptionResponse redeemCoupon(CouponRedemptionRequest request);
}