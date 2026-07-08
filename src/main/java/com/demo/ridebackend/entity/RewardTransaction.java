package com.demo.ridebackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @Column(nullable = false)
    private Integer pointsEarned;

    @Column(nullable = false)
    private Integer balanceAfter;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}