package com.demo.ridebackend.service.impl;

import com.demo.ridebackend.dto.request.LoginRequest;
import com.demo.ridebackend.dto.request.RegisterRequest;
import com.demo.ridebackend.dto.response.AuthResponse;
import com.demo.ridebackend.entity.User;
import com.demo.ridebackend.repository.UserRepository;
import com.demo.ridebackend.security.JwtService;
import com.demo.ridebackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already exists.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .available(true)

                // ============================================================
                // START OF VERSION 5 CHANGES: DRIVER PREFERENCES
                // ============================================================
                .gender(request.getGender()) // Maps driver's gender for matching
                .vehicleType(request.getVehicleType()) // Maps vehicle category (e.g. SEDAN)
                // ============================================================
                // END OF VERSION 5 CHANGES
                // ============================================================

                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .message("Registration Successful")
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found."));

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .message("Login Successful")
                .build();
    }
}