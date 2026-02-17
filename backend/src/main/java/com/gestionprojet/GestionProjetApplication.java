package com.gestionprojet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Main application class for the Gestion Projet (Project Management) application.
 * This is a Spring Boot application that provides REST APIs for managing projects, tasks, and users.
 */
@SpringBootApplication
public class GestionProjetApplication {

    /**
     * Main entry point for the Spring Boot application.
     * Initializes and launches the Spring application context.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(GestionProjetApplication.class, args);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
