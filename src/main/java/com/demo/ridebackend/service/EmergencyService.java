package com.demo.ridebackend.service;

import com.demo.ridebackend.dto.request.SosRequest;
import com.demo.ridebackend.dto.response.EmergencyResponse;

import java.util.List;

public interface EmergencyService {

    EmergencyResponse raiseSos(Long rideId, SosRequest request);

    EmergencyResponse getEmergencyById(Long emergencyId);

    List<EmergencyResponse> getAllEmergencies();
}