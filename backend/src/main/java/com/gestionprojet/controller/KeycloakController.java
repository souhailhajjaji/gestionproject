package com.gestionprojet.controller;

import com.gestionprojet.service.KeycloakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Keycloak IAM management operations.
 * Provides endpoints for role management and admin user creation.
 */
@Slf4j
@RestController
@RequestMapping("/keycloak")
@RequiredArgsConstructor
@Tag(name = "Keycloak Management", description = "APIs for managing Keycloak users and roles")
public class KeycloakController {

    private final KeycloakService keycloakService;

    /**
     * Initializes Keycloak realm roles (ADMIN, USER).
     * Can be called manually if automatic initialization failed.
     */
    @PostMapping("/init-roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Initialize realm roles", description = "Creates ADMIN and USER realm roles in Keycloak")
    public ResponseEntity<Map<String, String>> initializeRoles() {
        log.info("Manual initialization of Keycloak realm roles requested");
        keycloakService.initializeRealmRoles();
        return ResponseEntity.ok(Map.of("message", "Realm roles initialized successfully"));
    }

    /**
     * Creates an admin user with full privileges.
     */
    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create admin user", description = "Creates a new admin user in Keycloak and local database")
    public ResponseEntity<Map<String, String>> createAdminUser(
            @RequestBody CreateAdminRequest request) {
        log.info("Creating admin user: {}", request.getEmail());
        
        UserRepresentation user = keycloakService.createAdminUser(
                request.getEmail(),
                request.getPassword(),
                request.getPrenom(),
                request.getNom()
        );
        
        return ResponseEntity.ok(Map.of(
                "message", "Admin user created successfully",
                "userId", user.getId(),
                "email", user.getEmail()
        ));
    }

    /**
     * Assigns a role to a user.
     */
    @PostMapping("/users/{keycloakId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign role to user", description = "Assigns a realm role to a user")
    public ResponseEntity<Map<String, String>> assignRole(
            @PathVariable UUID keycloakId,
            @PathVariable String roleName) {
        log.info("Assigning role {} to user {}", roleName, keycloakId);
        keycloakService.assignRole(keycloakId, roleName);
        return ResponseEntity.ok(Map.of(
                "message", "Role assigned successfully",
                "role", roleName,
                "userId", keycloakId.toString()
        ));
    }

    /**
     * Removes a role from a user.
     */
    @DeleteMapping("/users/{keycloakId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove role from user", description = "Removes a realm role from a user")
    public ResponseEntity<Map<String, String>> removeRole(
            @PathVariable UUID keycloakId,
            @PathVariable String roleName) {
        log.info("Removing role {} from user {}", roleName, keycloakId);
        keycloakService.removeRole(keycloakId, roleName);
        return ResponseEntity.ok(Map.of(
                "message", "Role removed successfully",
                "role", roleName,
                "userId", keycloakId.toString()
        ));
    }

    /**
     * Request DTO for creating admin user.
     */
    public static class CreateAdminRequest {
        private String email;
        private String password;
        private String prenom;
        private String nom;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
    }
}
