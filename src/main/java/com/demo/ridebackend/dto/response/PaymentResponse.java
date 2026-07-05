package com.demo.ridebackend.dto.response;

import com.demo.ridebackend.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse
{

    private Long id;

    private Double amount;

    private LocalDateTime paymentTime;

    private PaymentStatus paymentStatus;

    private Long rideId;
}