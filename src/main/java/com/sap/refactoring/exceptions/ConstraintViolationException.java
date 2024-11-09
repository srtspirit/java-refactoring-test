package com.sap.refactoring.exceptions;

/**
 * Exception indicating a constraint or data integrity violation
 */
public class ConstraintViolationException extends RuntimeException {
    public ConstraintViolationException(String message) {
        super(message);
    }
}
