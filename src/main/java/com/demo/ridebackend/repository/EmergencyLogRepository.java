package com.demo.ridebackend.repository;

import com.demo.ridebackend.entity.EmergencyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyLogRepository extends JpaRepository<EmergencyLog, Long> {

    List<EmergencyLog> findAllByOrderByTimestampDesc();
}