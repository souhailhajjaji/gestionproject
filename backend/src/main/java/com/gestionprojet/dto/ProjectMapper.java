package com.gestionprojet.dto;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.User;
import org.mapstruct.*;

import java.util.UUID;

/**
 * MapStruct mapper for converting between Project entity and DTOs.
 * Handles bidirectional conversion and partial updates.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {UserMapper.class})
public interface ProjectMapper {

    /**
     * Converts a ProjectRequestDTO to a Project entity.
     *
     * @param projectDTO the project request DTO to convert
     * @return the project entity
     */
    Project toEntity(ProjectRequestDTO projectDTO);

    /**
     * Converts a Project entity to a ProjectResponseDTO.
     *
     * @param project the project entity to convert
     * @return the project response DTO
     */
    ProjectResponseDTO toResponseDto(Project project);

    /**
     * Performs a partial update of a project entity from a RequestDTO.
     * Ignores ID, timestamps, and relationships.
     *
     * @param project the project entity to update
     * @param projectDTO the DTO containing update values
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "responsable", ignore = true)
    void partialUpdate(@MappingTarget Project project, ProjectRequestDTO projectDTO);

    /**
     * Creates a Project entity from a UUID ID.
     * Used for mapping relationships by ID only.
     *
     * @param id the project ID
     * @return a project entity with only the ID set, or null if ID is null
     */
    default Project fromId(UUID id) {
        if (id == null) {
            return null;
        }
        Project project = new Project();
        project.setId(id);
        return project;
    }

    /**
     * Converts a User entity to a UserBriefDTO.
     *
     * @param user the user entity to convert
     * @return the user brief DTO, or null if user is null
     */
    default UserBriefDTO toUserBriefDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserBriefDTO.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .build();
    }
}
