package com.gestionprojet.controller;

import com.gestionprojet.dto.UserDTO;
import com.gestionprojet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing users.
 * Provides endpoints for CRUD operations, role management, and document uploads.
 * All endpoints require Bearer token authentication. Admin-only endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Retrieves all users from the system.
     *
     * @return list of all users
     */
    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * Retrieves a specific user by their ID.
     *
     * @param id the UUID of the user to retrieve
     * @return the user if found
     * @throws com.gestionprojet.exception.ResourceNotFoundException if user not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * Creates a new user. Requires ADMIN role.
     *
     * @param userDTO the user data to create
     * @param password the initial password for the user
     * @return the created user
     */
    @PostMapping
    @Operation(summary = "Create a new user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody UserDTO userDTO,
            @RequestParam String password) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(userDTO, password));
    }

    /**
     * Updates an existing user. Requires ADMIN role.
     *
     * @param id the UUID of the user to update
     * @param userDTO the updated user data
     * @return the updated user
     * @throws com.gestionprojet.exception.ResourceNotFoundException if user not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    /**
     * Deletes a user by their ID. Requires ADMIN role.
     *
     * @param id the UUID of the user to delete
     * @return no content response
     * @throws com.gestionprojet.exception.ResourceNotFoundException if user not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads an identity document for a user.
     *
     * @param id the UUID of the user
     * @param file the identity document file to upload
     * @return the updated user with document URL
     * @throws Exception if file upload fails
     */
    @PostMapping("/{id}/document")
    @Operation(summary = "Upload identity document")
    public ResponseEntity<UserDTO> uploadIdentityDocument(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(userService.uploadIdentityDocument(id, file));
    }

    /**
     * Assigns a role to a user. Requires ADMIN role.
     *
     * @param id the UUID of the user
     * @param role the role to assign (e.g., ADMIN, USER)
     * @return the updated user
     * @throws com.gestionprojet.exception.ResourceNotFoundException if user not found
     */
    @PutMapping("/{id}/roles/{role}")
    @Operation(summary = "Assign role to user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> assignRole(
            @PathVariable UUID id,
            @PathVariable String role) {
        return ResponseEntity.ok(userService.assignRole(id, role));
    }

    /**
     * Removes a role from a user. Requires ADMIN role.
     *
     * @param id the UUID of the user
     * @param role the role to remove
     * @return the updated user
     * @throws com.gestionprojet.exception.ResourceNotFoundException if user not found
     */
    @DeleteMapping("/{id}/roles/{role}")
    @Operation(summary = "Remove role from user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> removeRole(
            @PathVariable UUID id,
            @PathVariable String role) {
        return ResponseEntity.ok(userService.removeRole(id, role));
    }
}
