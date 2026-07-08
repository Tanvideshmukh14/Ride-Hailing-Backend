package com.demo.ridebackend.service;

import com.demo.ridebackend.dto.response.PaymentResponse;
import com.demo.ridebackend.entity.Payment;
import com.demo.ridebackend.enums.PaymentStatus;
import com.demo.ridebackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponse createPayment(Long rideId) {

        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new RuntimeException("Payment not found for this ride"));

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        return mapToPaymentResponse(payment);
    }

    public PaymentResponse getPayment(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return mapToPaymentResponse(payment);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {

        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentTime(payment.getPaymentTime())
                .paymentStatus(payment.getPaymentStatus())
                .rideId(payment.getRide().getId())
                .build();
    }
}