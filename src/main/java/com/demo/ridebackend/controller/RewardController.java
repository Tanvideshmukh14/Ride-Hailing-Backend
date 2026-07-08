package com.demo.ridebackend.controller;

import com.demo.ridebackend.dto.request.CouponRedemptionRequest;
import com.demo.ridebackend.dto.response.CouponRedemptionResponse;
import com.demo.ridebackend.dto.response.RewardCouponResponse;
import com.demo.ridebackend.dto.response.RewardPointsResponse;
import com.demo.ridebackend.service.RewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @GetMapping("/points")
    public ResponseEntity<RewardPointsResponse> getMyRewardPoints() {
        return ResponseEntity.ok(rewardService.getMyRewardPoints());
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<RewardCouponResponse>> getMyCoupons() {
        return ResponseEntity.ok(rewardService.getMyCoupons());
    }

    @PostMapping("/coupons/redeem")
    public ResponseEntity<CouponRedemptionResponse> redeemCoupon(
            @Valid @RequestBody CouponRedemptionRequest request) {

        return ResponseEntity.ok(rewardService.redeemCoupon(request));
    }
}
