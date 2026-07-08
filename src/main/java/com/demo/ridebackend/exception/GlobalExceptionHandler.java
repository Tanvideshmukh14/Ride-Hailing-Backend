package com.demo.ridebackend.exception;

import com.demo.ridebackend.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRideNotFound(RideNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOtpExpired(OtpExpiredException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OtpNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleOtpNotVerified(OtpNotVerifiedException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidRideStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRideState(InvalidRideStateException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCouponNotFound(CouponNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CouponExpiredException.class)
    public ResponseEntity<ErrorResponse> handleCouponExpired(CouponExpiredException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CouponAlreadyRedeemedException.class)
    public ResponseEntity<ErrorResponse> handleCouponAlreadyRedeemed(CouponAlreadyRedeemedException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CouponNotEligibleException.class)
    public ResponseEntity<ErrorResponse> handleCouponNotEligible(CouponNotEligibleException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InsufficientRewardPointsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientRewardPoints(InsufficientRewardPointsException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EmergencyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmergencyNotFound(EmergencyNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(EmergencyAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleEmergencyAccessDenied(EmergencyAccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    // Fallback for role checks and other existing service-level validations
    // that currently throw plain RuntimeException.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}