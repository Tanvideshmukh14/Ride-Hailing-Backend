package com.demo.ridebackend.repository;

import com.demo.ridebackend.entity.RewardTransaction;
import com.demo.ridebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {

    List<RewardTransaction> findByRiderOrderByCreatedAtDesc(User rider);
}