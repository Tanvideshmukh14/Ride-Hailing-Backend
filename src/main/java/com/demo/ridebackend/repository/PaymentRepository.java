package com.demo.ridebackend.repository;

import com.demo.ridebackend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRideId(Long rideId);

}