package com.demo.ridebackend.service.impl;
import com.demo.ridebackend.exception.CouponExpiredException;
import com.demo.ridebackend.dto.request.CouponRedemptionRequest;
import com.demo.ridebackend.dto.response.CouponRedemptionResponse;
import com.demo.ridebackend.dto.response.RewardCouponResponse;
import com.demo.ridebackend.dto.response.RewardPointsResponse;
import com.demo.ridebackend.entity.Ride;
import com.demo.ridebackend.entity.RewardCoupon;
import com.demo.ridebackend.entity.RewardTransaction;
import com.demo.ridebackend.entity.User;
import com.demo.ridebackend.enums.Role;
import com.demo.ridebackend.exception.CouponAlreadyRedeemedException;
import com.demo.ridebackend.exception.CouponNotEligibleException;
import com.demo.ridebackend.exception.CouponNotFoundException;
import com.demo.ridebackend.exception.InsufficientRewardPointsException;
import com.demo.ridebackend.exception.RideNotFoundException;
import com.demo.ridebackend.repository.RewardCouponRepository;
import com.demo.ridebackend.repository.RewardTransactionRepository;
import com.demo.ridebackend.repository.RideRepository;
import com.demo.ridebackend.repository.UserRepository;
import com.demo.ridebackend.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final RewardCouponRepository rewardCouponRepository;
    private final RewardTransactionRepository rewardTransactionRepository;

    @Value("${reward.fare-unit:100}")
    private double fareUnit;

    @Value("${reward.points-per-unit:5}")
    private int pointsPerUnit;

    @Value("${reward.coupon.milestone:100}")
    private int couponMilestone;

    @Value("${reward.coupon.validity-days:10}")
    private long couponValidityDays;

    @Value("${reward.coupon.max-discount:500}")
    private double couponMaxDiscount;

    @Value("${reward.coupon.min-eligible-fare:100}")
    private double couponMinEligibleFare;

    @Value("${reward.coupon.max-eligible-fare:500}")
    private double couponMaxEligibleFare;

    private User getLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public int awardPointsForCompletedRide(Ride ride) {

        User rider = ride.getRider();
        double fare = ride.getFare() != null ? ride.getFare() : 0.0;

        int pointsEarned = (int) (Math.floor(fare / fareUnit) * pointsPerUnit);

        if (pointsEarned <= 0) {
            return 0;
        }

        int balanceBefore = rider.getRewardPoints() != null ? rider.getRewardPoints() : 0;
        int balanceAfter = balanceBefore + pointsEarned;

        rider.setRewardPoints(balanceAfter);
        userRepository.save(rider);

        RewardTransaction transaction = RewardTransaction.builder()
                .rider(rider)
                .ride(ride)
                .pointsEarned(pointsEarned)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .build();

        rewardTransactionRepository.save(transaction);

        // Issue one coupon for every 100-point milestone crossed by this ride
        // (handles the rare case where a single ride's points span more than
        // one milestone).
        int milestonesBefore = balanceBefore / couponMilestone;
        int milestonesAfter = balanceAfter / couponMilestone;

        for (int i = milestonesBefore + 1; i <= milestonesAfter; i++) {
            issueFreeRideCoupon(rider);
        }

        return pointsEarned;
    }

    private void issueFreeRideCoupon(User rider) {

        LocalDateTime now = LocalDateTime.now();

        RewardCoupon coupon = RewardCoupon.builder()
                .couponCode(generateCouponCode())
                .rider(rider)
                .maxDiscount(couponMaxDiscount)
                .minEligibleFare(couponMinEligibleFare)
                .maxEligibleFare(couponMaxEligibleFare)
                .issuedDate(now)
                .expiryDate(now.plusDays(couponValidityDays))
                .redeemed(false)
                .build();

        rewardCouponRepository.save(coupon);
    }

    private String generateCouponCode() {
        return "RIDE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public RewardPointsResponse getMyRewardPoints() {

        User rider = getLoggedInUser();
        int total = rider.getRewardPoints() != null ? rider.getRewardPoints() : 0;
        int remainder = total % couponMilestone;
        int pointsToNext = remainder == 0 ? 0 : couponMilestone - remainder;

        return RewardPointsResponse.builder()
                .riderId(rider.getId())
                .totalRewardPoints(total)
                .pointsToNextCoupon(pointsToNext)
                .build();
    }

    @Override
    public List<RewardCouponResponse> getMyCoupons() {

        User rider = getLoggedInUser();

        return rewardCouponRepository.findByRider(rider)
                .stream()
                .map(this::mapToCouponResponse)
                .toList();
    }

    @Override
    @Transactional
    public CouponRedemptionResponse redeemCoupon(CouponRedemptionRequest request) {

        User rider = getLoggedInUser();

        if (rider.getRole() != Role.RIDER) {
            throw new RuntimeException("Only riders can redeem reward coupons.");
        }

        RewardCoupon coupon = rewardCouponRepository
                .findByCouponCodeAndRider(request.couponCode(), rider)
                .orElseThrow(() -> new CouponNotFoundException(
                        "No coupon found with code: " + request.couponCode()));

        if (Boolean.TRUE.equals(coupon.getRedeemed())) {
            throw new CouponAlreadyRedeemedException("This coupon has already been redeemed.");
        }

        if (LocalDateTime.now().isAfter(coupon.getExpiryDate())) {
            throw new CouponExpiredException("This coupon expired on " + coupon.getExpiryDate());
        }

        Ride ride = rideRepository.findById(request.rideId())
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + request.rideId()));

        if (!ride.getRider().getId().equals(rider.getId())) {
            throw new CouponNotEligibleException("This ride does not belong to you.");
        }

        double fare = ride.getFare() != null ? ride.getFare() : 0.0;

        if (fare < coupon.getMinEligibleFare() || fare > coupon.getMaxEligibleFare()) {
            throw new CouponNotEligibleException(
                    "This coupon is only valid for rides with fare between "
                            + coupon.getMinEligibleFare() + " and " + coupon.getMaxEligibleFare());
        }

        int currentPoints = rider.getRewardPoints() != null ? rider.getRewardPoints() : 0;

        if (currentPoints < couponMilestone) {
            throw new InsufficientRewardPointsException(
                    "You need at least " + couponMilestone + " reward points to redeem a coupon.");
        }

        double discount = Math.min(fare, coupon.getMaxDiscount());
        double payable = fare - discount;

        coupon.setRedeemed(true);
        coupon.setRedeemedDate(LocalDateTime.now());
        coupon.setRedeemedRide(ride);
        rewardCouponRepository.save(coupon);

        // Redemption always costs a flat 100 points; any surplus points stay.
        int remainingPoints = currentPoints - couponMilestone;
        rider.setRewardPoints(remainingPoints);
        userRepository.save(rider);

        return CouponRedemptionResponse.builder()
                .couponCode(coupon.getCouponCode())
                .rideId(ride.getId())
                .originalFare(fare)
                .discountApplied(discount)
                .payableAmount(payable)
                .remainingRewardPoints(remainingPoints)
                .message("Coupon redeemed successfully. " + couponMilestone + " points deducted.")
                .build();
    }

    private RewardCouponResponse mapToCouponResponse(RewardCoupon coupon) {

        return RewardCouponResponse.builder()
                .couponCode(coupon.getCouponCode())
                .maxDiscount(coupon.getMaxDiscount())
                .minEligibleFare(coupon.getMinEligibleFare())
                .maxEligibleFare(coupon.getMaxEligibleFare())
                .issuedDate(coupon.getIssuedDate())
                .expiryDate(coupon.getExpiryDate())
                .redeemed(coupon.getRedeemed())
                .redeemedDate(coupon.getRedeemedDate())
                .build();
    }
}