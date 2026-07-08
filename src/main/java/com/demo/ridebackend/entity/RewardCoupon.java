package com.demo.ridebackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String couponCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

    @Column(nullable = false)
    private Double maxDiscount;

    @Column(nullable = false)
    private Double minEligibleFare;

    @Column(nullable = false)
    private Double maxEligibleFare;

    @Column(nullable = false)
    private LocalDateTime issuedDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean redeemed = false;

    private LocalDateTime redeemedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redeemed_ride_id")
    private Ride redeemedRide;
}
