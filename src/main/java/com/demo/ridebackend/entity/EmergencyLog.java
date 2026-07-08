package com.demo.ridebackend.entity;
import com.demo.ridebackend.enums.EmergencyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyStatus status;
}