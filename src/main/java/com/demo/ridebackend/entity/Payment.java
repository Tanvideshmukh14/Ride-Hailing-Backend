package com.demo.ridebackend.entity;

import com.demo.ridebackend.enums.PaymentStatus;
import jakarta.persistence.*;
import com.demo.ridebackend.entity.Ride;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private LocalDateTime paymentTime;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @OneToOne
    @JoinColumn(name = "ride_id")
    private Ride ride;
}