package dev.aimusic.backend.common.exceptions;

import dev.aimusic.backend.common.dto.RequestStatus;
import dev.aimusic.backend.common.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Response> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication error: {}", e.getMessage());
        var response = Response.builder().build();
        response.setRequestStatus(RequestStatus.builder()
                .error("AUTHENTICATION_ERROR")
                .errorMessage(e.getMessage())
                .build());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Response> handleNotFoundException(NotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        var response = Response.builder().build();
        response.setRequestStatus(RequestStatus.builder()
                .error("NOT_FOUND")
                .errorMessage(e.getMessage())
                .build());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(PaymentRequiredException.class)
    public ResponseEntity<Response> handlePaymentRequiredException(PaymentRequiredException e) {
        log.warn("Payment required: {}", e.getMessage());
        var response = Response.builder().build();
        response.setRequestStatus(RequestStatus.builder()
                .error("PAYMENT_REQUIRED")
                .errorMessage(e.getMessage())
                .build());
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Response> handleValidationException(ValidationException e) {
        log.warn("Validation error: {}", e.getMessage());
        var response = Response.builder().build();
        response.setRequestStatus(RequestStatus.builder()
                .error("VALIDATION_ERROR")
                .errorMessage(e.getMessage())
                .build());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        var response = Response.builder().build();
        response.setRequestStatus(RequestStatus.builder()
                .error("INTERNAL_SERVER_ERROR")
                .errorMessage("Internal server error")
                .build());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}