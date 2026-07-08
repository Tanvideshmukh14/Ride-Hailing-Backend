package com.demo.ridebackend.exception;

public class CouponExpiredException extends RuntimeException {

    public CouponExpiredException(String message) {
        super(message);
    }
}