package com.example.insecurebank.controller;

import com.example.insecurebank.exception.AccountNotFoundException;
import com.example.insecurebank.exception.InsufficientFundsException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Validation error for request method={} uri={}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(400).body("Invalid request: " + ex.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<String> handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest request) {
        log.warn("Account not found for request method={} uri={}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(404).body("Account not found");
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<String> handleInsufficientFunds(InsufficientFundsException ex, HttpServletRequest request) {
        log.warn("Insufficient funds for request method={} uri={}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(400).body("Insufficient funds");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Runtime exception for request method={} uri={}",
                request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(500).body("Unable to process request. Please try again later.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for request method={} uri={}",
                request.getMethod(), request.getRequestURI(), ex);
        String body = "An unexpected error occurred. Please contact support if the problem persists.";
        return ResponseEntity.status(500).body(body);
    }
}
