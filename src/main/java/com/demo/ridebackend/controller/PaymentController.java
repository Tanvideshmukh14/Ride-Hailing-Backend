package com.demo.ridebackend.controller;

import com.demo.ridebackend.dto.response.PaymentResponse;
import com.demo.ridebackend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{rideId}")
    public ResponseEntity<PaymentResponse> createPayment(@PathVariable Long rideId) {
        return ResponseEntity.ok(paymentService.createPayment(rideId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }
}