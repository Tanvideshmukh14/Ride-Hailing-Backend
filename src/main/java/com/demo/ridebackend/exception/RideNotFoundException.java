package com.demo.ridebackend.exception;

public class RideNotFoundException extends RuntimeException {

    public RideNotFoundException(String message) {
        super(message);
    }
}