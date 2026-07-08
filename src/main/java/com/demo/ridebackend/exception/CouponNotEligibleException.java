package com.demo.ridebackend.exception;

public class CouponNotEligibleException extends RuntimeException {

    public CouponNotEligibleException(String message) {
        super(message);
    }
}