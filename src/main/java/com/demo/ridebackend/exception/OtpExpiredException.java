package com.demo.ridebackend.exception;

public class OtpExpiredException extends RuntimeException {

    public OtpExpiredException(String message) {
        super(message);
    }
}
