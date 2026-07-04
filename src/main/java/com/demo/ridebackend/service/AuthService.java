package com.demo.ridebackend.service;

import com.demo.ridebackend.dto.request.LoginRequest;
import com.demo.ridebackend.dto.request.RegisterRequest;
import com.demo.ridebackend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

}