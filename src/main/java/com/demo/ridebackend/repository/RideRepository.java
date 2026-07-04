package com.demo.ridebackend.repository;

import com.demo.ridebackend.entity.Ride;
import com.demo.ridebackend.entity.User;
import com.demo.ridebackend.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByRider(User rider);

    List<Ride> findByDriver(User driver);

    List<Ride> findByStatus(RideStatus status);

}