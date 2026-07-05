package com.demo.ridebackend.service;

import com.demo.ridebackend.dto.response.PaymentResponse;
import com.demo.ridebackend.entity.Payment;
import com.demo.ridebackend.entity.Ride;
import com.demo.ridebackend.enums.PaymentStatus;
import com.demo.ridebackend.repository.PaymentRepository;
import com.demo.ridebackend.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;

    public PaymentResponse createPayment(Long rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        Payment payment = Payment.builder()
                .amount(ride.getFare())
                .paymentTime(LocalDateTime.now())
                .paymentStatus(PaymentStatus.SUCCESS)
                .ride(ride)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return mapToPaymentResponse(savedPayment);
    }

    public PaymentResponse getPayment(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return mapToPaymentResponse(payment);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        response.setId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setPaymentTime(payment.getPaymentTime());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setRideId(payment.getRide().getId());

        return response;
    }
}