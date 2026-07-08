package com.demo.ridebackend.exception;

public class InsufficientRewardPointsException extends RuntimeException {

    public InsufficientRewardPointsException(String message) {
        super(message);
    }
}