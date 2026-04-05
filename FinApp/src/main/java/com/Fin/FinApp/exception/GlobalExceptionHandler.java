package com.Fin.FinApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Tells Spring to watch all Controllers for thrown exceptions
public class GlobalExceptionHandler {

    // 1. Handle our custom RuntimeExceptions (like "Invalid password" or "User not found")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeExceptions(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());

        // If it's an auth error, return 401 Unauthorized. Otherwise, 400 Bad Request.
        HttpStatus status = (ex.getMessage().contains("password") || ex.getMessage().contains("User not found"))
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(errorResponse, status);
    }

    // 2. Handle @Valid validation failures (like a blank email or missing amount)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}