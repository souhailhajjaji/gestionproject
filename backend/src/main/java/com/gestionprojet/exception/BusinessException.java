package com.gestionprojet.exception;

/**
 * Exception thrown when a business rule validation fails.
 * Used for 400 Bad Request responses.
 */
public class BusinessException extends RuntimeException {

    /**
     * Creates a new BusinessException with the specified message.
     * @param message the detail message
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Creates a new BusinessException with the specified message and cause.
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
