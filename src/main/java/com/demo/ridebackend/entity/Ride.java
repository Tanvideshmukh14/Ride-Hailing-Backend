package com.demo.ridebackend.entity;

import com.demo.ridebackend.enums.RideStatus;
import com.demo.ridebackend.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double pickupLatitude;

    private Double pickupLongitude;

    private Double dropLatitude;

    private Double dropLongitude;

    private Double distance;

    private Double fare;

    private LocalDateTime requestedTime;

    private LocalDateTime acceptedTime;

    private LocalDateTime startedTime;

    private LocalDateTime completedTime;

    @Enumerated(EnumType.STRING)
    private RideStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id")
    private User rider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    @OneToOne(mappedBy = "ride", cascade = CascadeType.ALL)
    private Payment payment;

    // ---- Ride OTP Verification (V6 - Feature 1) ----

    @Column(name = "ride_otp", length = 4)
    private String rideOtp;

    @Column(name = "otp_generated_time")
    private LocalDateTime otpGeneratedTime;

    @Builder.Default
    @Column(name = "otp_verified")
    private Boolean otpVerified = false;

    // ---- Driver Matching Preference ----

    @Column(name = "requested_vehicle_type")
    private String requestedVehicleType; // e.g., "SEDAN", "SUV", "BIKE"

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_driver_gender")
    private Gender preferredDriverGender; // Optional filter if rider requests a specific gender driver
}