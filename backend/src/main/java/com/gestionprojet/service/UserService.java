package com.gestionprojet.service;

import com.gestionprojet.dto.UserDTO;
import com.gestionprojet.exception.BusinessException;
import com.gestionprojet.exception.ResourceNotFoundException;
import com.gestionprojet.model.User;
import com.gestionprojet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 * Handles business logic for user CRUD operations, role management, and document uploads.
 * Integrates with Keycloak for authentication and RustFsService for file storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final RustFsService rustFsService;

    /**
     * Creates a new user with the given data.
     * First creates the user in Keycloak, then stores local user data.
     * If Keycloak is unavailable or returns 403, creates local user only (development mode).
     *
     * @param userDTO the user data transfer object containing user information
     * @param password the initial password for the user
     * @return the created user as DTO
     * @throws BusinessException if a user with the same email already exists
     */
    @Transactional
    public UserDTO createUser(UserDTO userDTO, String password) {
        log.info("Creating user: {}", userDTO.getEmail());

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException("User with this email already exists");
        }

        UUID keycloakId;
        try {
            // Create user in Keycloak first
            var kcUser = keycloakService.createUserInKeycloak(userDTO, password);
            keycloakId = UUID.fromString(kcUser.getId());
            log.info("Created user in Keycloak: {}", keycloakId);
        } catch (jakarta.ws.rs.ForbiddenException e) {
            // Keycloak 403 - create local user only (development mode)
            log.warn("Keycloak returned 403, creating local user only. Consider configuring Keycloak permissions.");
            keycloakId = UUID.randomUUID();
        } catch (Exception e) {
            // Keycloak unavailable - create local user only (development mode)
            log.warn("Keycloak unavailable ({}), creating local user only", e.getMessage());
            keycloakId = UUID.randomUUID();
        }

        // Create local user record
        User user = new User();
        user.setKeycloakId(keycloakId);
        user.setEmail(userDTO.getEmail());
        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setDateNaissance(userDTO.getDateNaissance());
        user.setTelephone(userDTO.getTelephone());
        user.setRoles(userDTO.getRoles() != null ? userDTO.getRoles()
                : java.util.Set.of(com.gestionprojet.model.enums.Role.USER));

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getId());

        return convertToDto(savedUser);
    }

    /**
     * Retrieves all users from the database.
     *
     * @return list of all users as DTOs
     */
    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        log.info("Finding all users");
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds a user by their ID.
     *
     * @param id the UUID of the user
     * @return the user as DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDTO findById(UUID id) {
        log.info("Finding user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        return convertToDto(user);
    }

    /**
     * Finds a user by their Keycloak ID.
     *
     * @param keycloakId the Keycloak UUID of the user
     * @return the user as DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDTO findByKeycloakId(UUID keycloakId) {
        log.info("Finding user by Keycloak ID: {}", keycloakId);
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User", keycloakId.toString()));
        return convertToDto(user);
    }

    /**
     * Updates an existing user's information.
     * Updates both Keycloak and local database.
     *
     * @param id the UUID of the user to update
     * @param userDTO the new user data
     * @return the updated user as DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        // Update Keycloak
        keycloakService.updateUserInKeycloak(user.getKeycloakId(), userDTO);

        // Update local record
        user.setNom(userDTO.getNom());
        user.setPrenom(userDTO.getPrenom());
        user.setEmail(userDTO.getEmail());
        user.setDateNaissance(userDTO.getDateNaissance());
        user.setTelephone(userDTO.getTelephone());

        if (userDTO.getRoles() != null) {
            user.setRoles(userDTO.getRoles());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getId());

        return convertToDto(updatedUser);
    }

    /**
     * Deletes a user by their ID.
     * Deletes from both Keycloak and local database.
     *
     * @param id the UUID of the user to delete
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        // Delete from Keycloak
        keycloakService.deleteUserFromKeycloak(user.getKeycloakId());

        // Delete local record
        userRepository.delete(user);
        log.info("User deleted successfully: {}", id);
    }

    /**
     * Uploads an identity document for a user.
     * Deletes any existing document before uploading the new one.
     *
     * @param id the UUID of the user
     * @param file the identity document file to upload
     * @return the updated user with document URL
     * @throws Exception if file upload fails
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserDTO uploadIdentityDocument(UUID id, MultipartFile file) throws Exception {
        log.info("Uploading identity document for user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        // Delete existing document if present
        if (user.getPieceIdentiteUrl() != null) {
            rustFsService.deleteFile(user.getPieceIdentiteUrl());
        }

        // Upload new document
        String fileUrl = rustFsService.uploadFile(file, "identity-documents");
        user.setPieceIdentiteUrl(fileUrl);

        User updatedUser = userRepository.save(user);
        log.info("Identity document uploaded successfully for user: {}", id);

        return convertToDto(updatedUser);
    }

    /**
     * Assigns a role to a user.
     * Updates both Keycloak and local database.
     *
     * @param id the UUID of the user
     * @param role the role to assign (e.g., ADMIN, USER)
     * @return the updated user
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserDTO assignRole(UUID id, String role) {
        log.info("Assigning role {} to user: {}", role, id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        keycloakService.assignRole(user.getKeycloakId(), role);

        user.getRoles().add(com.gestionprojet.model.enums.Role.valueOf(role));
        userRepository.save(user);

        return convertToDto(user);
    }

    /**
     * Removes a role from a user.
     * Updates both Keycloak and local database.
     *
     * @param id the UUID of the user
     * @param role the role to remove
     * @return the updated user
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserDTO removeRole(UUID id, String role) {
        log.info("Removing role {} from user: {}", role, id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        keycloakService.removeRole(user.getKeycloakId(), role);

        user.getRoles().remove(com.gestionprojet.model.enums.Role.valueOf(role));
        userRepository.save(user);

        return convertToDto(user);
    }

    /**
     * Converts a User entity to a UserDTO.
     *
     * @param user the user entity to convert
     * @return the user DTO
     */
    private UserDTO convertToDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .dateNaissance(user.getDateNaissance())
                .telephone(user.getTelephone())
                .pieceIdentiteUrl(user.getPieceIdentiteUrl())
                .roles(user.getRoles())
                .build();
    }
}
