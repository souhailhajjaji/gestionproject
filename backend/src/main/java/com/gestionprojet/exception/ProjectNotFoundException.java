package com.gestionprojet.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested project is not found.
 * Used for 404 Not Found responses specific to Project entity.
 */
public class ProjectNotFoundException extends RuntimeException {

    /**
     * Creates a new ProjectNotFoundException with the project ID.
     * @param id the UUID of the project that was not found
     */
    public ProjectNotFoundException(UUID id) {
        super("Project not found with id: " + id);
    }

    /**
     * Creates a new ProjectNotFoundException with a custom message.
     * @param message the detail message
     */
    public ProjectNotFoundException(String message) {
        super(message);
    }
}
