package com.gestionprojet.dto;

import com.gestionprojet.model.User;
import org.mapstruct.*;

import java.util.UUID;

/**
 * MapStruct mapper for converting between User entity and UserDTO.
 * Handles bidirectional conversion and partial updates.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface UserMapper {

    /**
     * Converts a UserDTO to a User entity.
     *
     * @param userDTO the user DTO to convert
     * @return the user entity
     */
    User toEntity(UserDTO userDTO);

    /**
     * Converts a User entity to a UserDTO.
     *
     * @param user the user entity to convert
     * @return the user DTO
     */
    UserDTO toDto(User user);

    /**
     * Performs a partial update of a user entity from a DTO.
     * Ignores ID, Keycloak ID, and timestamps.
     *
     * @param user the user entity to update
     * @param userDTO the DTO containing update values
     * @return the updated user entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User partialUpdate(@MappingTarget User user, UserDTO userDTO);

    /**
     * Creates a User entity from a UUID ID.
     * Used for mapping relationships by ID only.
     *
     * @param id the user ID
     * @return a user entity with only the ID set, or null if ID is null
     */
    default User fromId(UUID id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }
}
