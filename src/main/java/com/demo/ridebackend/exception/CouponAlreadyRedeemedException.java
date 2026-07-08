package com.demo.ridebackend.exception;

public class CouponAlreadyRedeemedException extends RuntimeException {

    public CouponAlreadyRedeemedException(String message) {
        super(message);
    }
}