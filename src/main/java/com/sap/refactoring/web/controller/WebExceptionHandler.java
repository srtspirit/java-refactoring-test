package com.sap.refactoring.web.controller;

import com.sap.refactoring.exceptions.ConstraintViolationException;
import com.sap.refactoring.exceptions.IllegalRequestException;
import com.sap.refactoring.exceptions.NotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        final Map<String, Object> body = new HashMap<>();
        final Map<String, String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> (FieldError) error)
                .collect(Collectors
                        .toMap(FieldError::getField,
                                err -> err.getDefaultMessage() != null? err.getDefaultMessage(): ""));

        body.put("error", "Validation failed");
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFoundExceptions(NotFoundException ex) {
    }

    @ExceptionHandler({ConstraintViolationException.class,
            IllegalRequestException.class,
            DataAccessException.class})
    public ResponseEntity<Map<String, String>> handleConstraintViolationExceptions(Exception ex) {
        final Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());

        return ResponseEntity.badRequest().body(body);
    }
}
