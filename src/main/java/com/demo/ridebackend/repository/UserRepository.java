package com.demo.ridebackend.repository;

import com.demo.ridebackend.entity.User;
import com.demo.ridebackend.enums.Role;
import com.demo.ridebackend.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByRole(Role role);

    // FIX: Replaced hardcoded Role.DRIVER with a dynamic parameter to prevent parsing crashes
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.available = true " +
            "AND (:vType IS NULL OR u.vehicleType = :vType) " +
            "AND (:gender IS NULL OR u.gender = :gender) " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(u.latitude)) * " +
            "cos(radians(u.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(u.latitude)))) <= :rad")
    List<User> findDrivers(@Param("lat") Double lat,
                           @Param("lng") Double lng,
                           @Param("vType") String vType,
                           @Param("gender") Gender gender,
                           @Param("rad") Double rad,
                           @Param("role") Role role); // Added Role param cleanly here
}