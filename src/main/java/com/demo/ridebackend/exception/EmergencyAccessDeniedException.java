package com.demo.ridebackend.exception;

public class EmergencyAccessDeniedException extends RuntimeException {

    public EmergencyAccessDeniedException(String message) {
        super(message);
    }
}