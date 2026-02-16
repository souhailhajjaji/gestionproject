package com.gestionprojet.repository;

import com.gestionprojet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 * Provides CRUD operations and custom query methods for users.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their Keycloak ID.
     * @param keycloakId the Keycloak UUID
     * @return Optional containing the user if found
     */
    Optional<User> findByKeycloakId(@Param("keycloakId") UUID keycloakId);

    /**
     * Finds a user by their email address.
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Checks if a user exists with the given email address.
     * @param email the email address
     * @return true if a user with this email exists
     */
    boolean existsByEmail(@Param("email") String email);

    /**
     * Finds a user by ID with their roles eagerly fetched.
     * @param id the user UUID
     * @return Optional containing the user with roles if found
     */
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);
}
