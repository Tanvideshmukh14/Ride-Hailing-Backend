package com.demo.ridebackend.controller;

import com.demo.ridebackend.dto.response.EmergencyResponse;
import com.demo.ridebackend.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    // Admin-only dashboard of every emergency ever raised.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/emergencies")
    public ResponseEntity<List<EmergencyResponse>> getAllEmergencies() {
        return ResponseEntity.ok(emergencyService.getAllEmergencies());
    }

    // Accessible by an admin, or the rider/driver involved in that specific emergency.
    @GetMapping("/emergencies/{id}")
    public ResponseEntity<EmergencyResponse> getEmergencyById(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyService.getEmergencyById(id));
    }
}