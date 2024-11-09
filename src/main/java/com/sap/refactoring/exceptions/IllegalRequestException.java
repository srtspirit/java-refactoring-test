package com.sap.refactoring.exceptions;

/**
 * Exception indicating a general error with request
 */
public class IllegalRequestException extends RuntimeException{
    public IllegalRequestException(String message) {
        super(message);
    }
}
