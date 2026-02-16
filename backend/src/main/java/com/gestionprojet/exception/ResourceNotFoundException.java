package com.gestionprojet.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Used for 404 Not Found responses.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new ResourceNotFoundException with a formatted message.
     * @param resource the type of resource that was not found
     * @param identifier the identifier that was used to search for the resource
     */
    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s not found with %s", resource, identifier));
    }

    /**
     * Creates a new ResourceNotFoundException with the specified message.
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
