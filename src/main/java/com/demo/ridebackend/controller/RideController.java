package com.demo.ridebackend.controller;

import com.demo.ridebackend.dto.request.RideRequestDTO;
import com.demo.ridebackend.dto.response.RideResponse;
import com.demo.ridebackend.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping("/request")
    public ResponseEntity<RideResponse> requestRide(
            @Valid @RequestBody RideRequestDTO request) {

        return ResponseEntity.ok(rideService.requestRide(request));
    }

    @GetMapping("/my-rides")
    public ResponseEntity<List<RideResponse>> getMyRides() {
        return ResponseEntity.ok(rideService.getMyRides());
    }

    @GetMapping("/available")
    public ResponseEntity<List<RideResponse>> getAvailableRides() {
        return ResponseEntity.ok(rideService.getAvailableRides());
    }

    @PutMapping("/{rideId}/accept")
    public ResponseEntity<RideResponse> acceptRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.acceptRide(rideId));
    }

    @PutMapping("/{rideId}/start")
    public ResponseEntity<RideResponse> startRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.startRide(rideId));
    }

    @PutMapping("/{rideId}/complete")
    public ResponseEntity<RideResponse> completeRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.completeRide(rideId));
    }
}