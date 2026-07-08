package com.demo.ridebackend.controller;

import com.demo.ridebackend.dto.request.OtpVerificationRequest;
import com.demo.ridebackend.dto.request.RideRequestDTO;
import com.demo.ridebackend.dto.request.SosRequest;
import com.demo.ridebackend.dto.response.EmergencyResponse;
import com.demo.ridebackend.dto.response.RideResponse;
import com.demo.ridebackend.service.EmergencyService;
import com.demo.ridebackend.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;
    private final EmergencyService emergencyService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<RideResponse> requestRide(
            @Valid @RequestBody RideRequestDTO request) {

        return ResponseEntity.ok(rideService.requestRide(request));
    }

    @GetMapping("/my-rides")
    public ResponseEntity<List<RideResponse>> getMyRides() {
        return ResponseEntity.ok(rideService.getMyRides());
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<RideResponse>> getAvailableRides() {
        return ResponseEntity.ok(rideService.getAvailableRides());
    }

    @PutMapping("/{rideId}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideResponse> acceptRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.acceptRide(rideId));
    }

    @PutMapping("/{rideId}/verify-otp")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideResponse> verifyOtp(
            @PathVariable Long rideId,
            @Valid @RequestBody OtpVerificationRequest request) {

        return ResponseEntity.ok(rideService.verifyOtp(rideId, request));
    }

    @PutMapping("/{rideId}/regenerate-otp")
    @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<RideResponse> regenerateOtp(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.regenerateOtp(rideId));
    }

    @PutMapping("/{rideId}/start")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideResponse> startRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.startRide(rideId));
    }

    @PutMapping("/{rideId}/complete")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideResponse> completeRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.completeRide(rideId));
    }

    @PostMapping("/{rideId}/sos")
    public ResponseEntity<EmergencyResponse> raiseSos(
            @PathVariable Long rideId,
            @Valid @RequestBody SosRequest request) {

        return ResponseEntity.ok(emergencyService.raiseSos(rideId, request));
    }
}