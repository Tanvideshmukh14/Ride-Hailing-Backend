package com.demo.ridebackend.exception;

public class InvalidRideStateException extends RuntimeException {

    public InvalidRideStateException(String message) {
        super(message);
    }
}