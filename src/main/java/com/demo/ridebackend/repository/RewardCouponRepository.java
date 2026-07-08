package com.demo.ridebackend.repository;

import com.demo.ridebackend.entity.RewardCoupon;
import com.demo.ridebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RewardCouponRepository extends JpaRepository<RewardCoupon, Long> {

    List<RewardCoupon> findByRider(User rider);

    Optional<RewardCoupon> findByCouponCodeAndRider(String couponCode, User rider);

    List<RewardCoupon> findByRiderAndRedeemedFalse(User rider);
}