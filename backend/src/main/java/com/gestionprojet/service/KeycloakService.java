package com.gestionprojet.service;

import com.gestionprojet.dto.UserDTO;
import com.gestionprojet.exception.BusinessException;
import com.gestionprojet.exception.ResourceNotFoundException;
import com.gestionprojet.model.User;
import com.gestionprojet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing Keycloak IAM (Identity and Access Management).
 * Handles user creation, updates, deletion, and role management in Keycloak.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;
    private final UserRepository userRepository;

    @Value("${keycloak.realm}")
    private String realm;

    private static final Boolean PASSWORD_TEMPORARY = false;

    /**
     * Creates a new user in Keycloak with the given credentials.
     * Automatically assigns the default USER role.
     *
     * @param userDTO the user data containing email, name, etc.
     * @param password the initial password for the user
     * @return the created Keycloak UserRepresentation
     */
    public UserRepresentation createUserInKeycloak(UserDTO userDTO, String password) {
        log.info("Creating user in Keycloak: {}", userDTO.getEmail());

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userDTO.getEmail());
        userRepresentation.setEmail(userDTO.getEmail());
        userRepresentation.setFirstName(userDTO.getPrenom());
        userRepresentation.setLastName(userDTO.getNom());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(PASSWORD_TEMPORARY);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        userRepresentation.setCredentials(List.of(credential));

        UsersResource usersResource = keycloak.realm(realm).users();
        jakarta.ws.rs.core.Response response = usersResource.create(userRepresentation);

        String userId = response.getLocation().getPath().replaceAll(".*/", "");
        log.info("Created Keycloak user with ID: {}", userId);

        userRepresentation.setId(userId);

        // Assign default role
        RoleRepresentation userRole = keycloak.realm(realm).roles().get("USER").toRepresentation();
        keycloak.realm(realm).users().get(userId).roles().realmLevel().add(List.of(userRole));

        return userRepresentation;
    }

    /**
     * Updates an existing user's information in Keycloak.
     *
     * @param keycloakId the Keycloak UUID of the user
     * @param userDTO the updated user data
     */
    public void updateUserInKeycloak(UUID keycloakId, UserDTO userDTO) {
        log.info("Updating user in Keycloak: {}", keycloakId);

        UserResource userResource = keycloak.realm(realm).users().get(keycloakId.toString());
        UserRepresentation userRepresentation = userResource.toRepresentation();

        userRepresentation.setFirstName(userDTO.getPrenom());
        userRepresentation.setLastName(userDTO.getNom());
        userRepresentation.setEmail(userDTO.getEmail());

        userResource.update(userRepresentation);
    }

    /**
     * Deletes a user from Keycloak.
     *
     * @param keycloakId the Keycloak UUID of the user to delete
     */
    public void deleteUserFromKeycloak(UUID keycloakId) {
        log.info("Deleting user from Keycloak: {}", keycloakId);
        keycloak.realm(realm).users().delete(keycloakId.toString());
    }

    /**
     * Assigns a role to a user in Keycloak.
     *
     * @param keycloakId the Keycloak UUID of the user
     * @param roleName the name of the role to assign
     */
    public void assignRole(UUID keycloakId, String roleName) {
        log.info("Assigning role {} to user {}", roleName, keycloakId);

        RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
        keycloak.realm(realm).users().get(keycloakId.toString())
                .roles().realmLevel().add(List.of(role));
    }

    /**
     * Removes a role from a user in Keycloak.
     *
     * @param keycloakId the Keycloak UUID of the user
     * @param roleName the name of the role to remove
     */
    public void removeRole(UUID keycloakId, String roleName) {
        log.info("Removing role {} from user {}", roleName, keycloakId);

        RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
        keycloak.realm(realm).users().get(keycloakId.toString())
                .roles().realmLevel().remove(List.of(role));
    }

    /**
     * Retrieves a user's Keycloak representation by their Keycloak ID.
     *
     * @param keycloakId the Keycloak UUID of the user
     * @return the Keycloak UserRepresentation
     * @throws ResourceNotFoundException if user not found in Keycloak
     */
    public UserRepresentation getUserByKeycloakId(UUID keycloakId) {
        try {
            return keycloak.realm(realm).users().get(keycloakId.toString()).toRepresentation();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Keycloak user", keycloakId.toString());
        }
    }

    /**
     * Searches for a user by their email address.
     *
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    public Optional<UserRepresentation> getUserByEmail(String email) {
        List<UserRepresentation> users = keycloak.realm(realm).users()
                .searchByEmail(email, true);

        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

    /**
     * Retrieves all role names assigned to a user.
     *
     * @param keycloakId the Keycloak UUID of the user
     * @return set of role names
     */
    public Set<String> getUserRoles(UUID keycloakId) {
        UserResource userResource = keycloak.realm(realm).users().get(keycloakId.toString());
        return userResource.roles().realmLevel().listEffective().stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Synchronizes local user data with Keycloak data.
     * Updates the local user record with information from Keycloak.
     *
     * @param keycloakId the Keycloak UUID of the user to sync
     */
    public void syncUserFromKeycloak(UUID keycloakId) {
        UserRepresentation kcUser = getUserByKeycloakId(keycloakId);

        Optional<User> existingUser = userRepository.findByKeycloakId(keycloakId);

        User user = existingUser.orElseGet(() -> {
            User newUser = new User();
            newUser.setKeycloakId(keycloakId);
            return newUser;
        });

        user.setEmail(kcUser.getEmail());
        user.setNom(kcUser.getLastName());
        user.setPrenom(kcUser.getFirstName());

        Set<String> roles = getUserRoles(keycloakId);
        user.setRoles(roles.stream()
                .map(roleName -> com.gestionprojet.model.enums.Role.valueOf(roleName))
                .collect(Collectors.toSet()));

        userRepository.save(user);
        log.info("Synced user from Keycloak: {}", keycloakId);
    }
}
