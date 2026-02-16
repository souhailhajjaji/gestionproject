package com.gestionprojet.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Error response object returned by the API when an error occurs.
 * Contains error details including timestamp, status, message, and path.
 */
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;

    /**
     * Creates a standard error response without validation errors.
     * @param status the HTTP status code
     * @param error the error type
     * @param message the error message
     * @param path the request path
     * @return a new ErrorResponse instance
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, null);
    }

    /**
     * Creates an error response with validation errors.
     * @param status the HTTP status code
     * @param error the error type
     * @param message the error message
     * @param path the request path
     * @param validationErrors map of field names to error messages
     * @return a new ErrorResponse instance with validation errors
     */
    public static ErrorResponse withValidation(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, path, validationErrors);
    }
}
