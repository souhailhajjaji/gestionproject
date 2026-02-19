package com.gestionprojet.service;

import com.gestionprojet.dto.UserDTO;
import com.gestionprojet.exception.BusinessException;
import com.gestionprojet.exception.ResourceNotFoundException;
import com.gestionprojet.model.User;
import com.gestionprojet.model.enums.Role;
import com.gestionprojet.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
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

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

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

    /**
     * Creates a realm role in Keycloak if it doesn't exist.
     *
     * @param roleName the name of the role to create
     * @param description the description of the role
     * @return true if role was created, false if it already existed
     */
    public boolean createRealmRoleIfNotExists(String roleName, String description) {
        RealmResource realmResource = keycloak.realm(realm);
        RolesResource rolesResource = realmResource.roles();
        
        try {
            rolesResource.get(roleName).toRepresentation();
            log.debug("Role {} already exists in Keycloak", roleName);
            return false;
        } catch (Exception e) {
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            role.setDescription(description);
            role.setComposite(false);
            role.setClientRole(false);
            
            rolesResource.create(role);
            log.info("Created realm role {} in Keycloak", roleName);
            return true;
        }
    }

    /**
     * Creates all required realm roles (ADMIN, USER) in Keycloak.
     * This method ensures the roles exist before they can be assigned to users.
     */
    public void initializeRealmRoles() {
        log.info("Initializing Keycloak realm roles...");
        
        createRealmRoleIfNotExists("ADMIN", "Administrator with full access to all features");
        createRealmRoleIfNotExists("USER", "Standard user with limited access");
        
        log.info("Keycloak realm roles initialized successfully");
    }

    /**
     * Creates an admin user in Keycloak and local database.
     *
     * @param email the admin email
     * @param password the admin password
     * @param prenom the admin first name
     * @param nom the admin last name
     * @return the created UserRepresentation
     */
    public UserRepresentation createAdminUser(String email, String password, String prenom, String nom) {
        log.info("Creating admin user in Keycloak: {}", email);
        
        Optional<UserRepresentation> existingUser = getUserByEmail(email);
        if (existingUser.isPresent()) {
            log.info("Admin user {} already exists in Keycloak", email);
            return existingUser.get();
        }
        
        UserDTO adminDTO = new UserDTO();
        adminDTO.setEmail(email);
        adminDTO.setPrenom(prenom);
        adminDTO.setNom(nom);
        
        UserRepresentation userRepresentation = createUserInKeycloak(adminDTO, password);
        UUID keycloakId = UUID.fromString(userRepresentation.getId());
        
        assignRole(keycloakId, "ADMIN");
        
        Optional<User> localUser = userRepository.findByEmail(email);
        if (localUser.isEmpty()) {
            User user = new User();
            user.setKeycloakId(keycloakId);
            user.setEmail(email);
            user.setPrenom(prenom);
            user.setNom(nom);
            user.setRoles(Set.of(Role.ADMIN));
            userRepository.save(user);
            log.info("Created admin user in local database: {}", email);
        }
        
        log.info("Admin user {} created successfully with ADMIN role", email);
        return userRepresentation;
    }

    /**
     * Initializes Keycloak setup including roles and admin user.
     * Called automatically after bean construction.
     * Wrapped in try-catch to prevent startup failure if Keycloak is unavailable.
     */
    @PostConstruct
    public void initializeKeycloakSetup() {
        try {
            log.info("Starting Keycloak initialization...");
            
            // Test if Keycloak is reachable before attempting initialization
            keycloak.serverInfo().getInfo();
            
            initializeRealmRoles();
            
            log.info("Keycloak initialization completed successfully");
        } catch (jakarta.ws.rs.ForbiddenException e) {
            log.warn("Keycloak initialization skipped: Insufficient permissions (403 Forbidden). ");
            log.warn("Please ensure the backend-api client in Keycloak has 'manage-realm' or 'manage-users' service account roles.");
            log.warn("The application will continue to work, but realm roles must be created manually in Keycloak.");
        } catch (jakarta.ws.rs.ProcessingException e) {
            log.warn("Keycloak initialization skipped: Cannot connect to Keycloak server at {}.", authServerUrl);
            log.warn("Please ensure Keycloak is running and accessible.");
            log.warn("The application will start without Keycloak integration. User management features will be unavailable.");
        } catch (Exception e) {
            log.error("Failed to initialize Keycloak: {}", e.getMessage(), e);
        }
    }
}
