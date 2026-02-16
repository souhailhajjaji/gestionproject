package com.gestionprojet.dto;

import com.gestionprojet.model.Task;
import org.mapstruct.*;

import java.util.UUID;

/**
 * MapStruct mapper for converting between Task entity and TaskDTO.
 * Handles bidirectional conversion and partial updates.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {UserMapper.class, ProjectMapper.class})
public interface TaskMapper {

    /**
     * Converts a TaskDTO to a Task entity.
     *
     * @param taskDTO the task DTO to convert
     * @return the task entity
     */
    Task toEntity(TaskDTO taskDTO);

    /**
     * Converts a Task entity to a TaskDTO.
     *
     * @param task the task entity to convert
     * @return the task DTO
     */
    TaskDTO toDto(Task task);

    /**
     * Performs a partial update of a task entity from a DTO.
     * Ignores ID, timestamps, and relationships.
     *
     * @param task the task entity to update
     * @param taskDTO the DTO containing update values
     * @return the updated task entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "projet", ignore = true)
    @Mapping(target = "assigne", ignore = true)
    Task partialUpdate(@MappingTarget Task task, TaskDTO taskDTO);

    /**
     * Creates a Task entity from a UUID ID.
     * Used for mapping relationships by ID only.
     *
     * @param id the task ID
     * @return a task entity with only the ID set, or null if ID is null
     */
    default Task fromId(UUID id) {
        if (id == null) {
            return null;
        }
        Task task = new Task();
        task.setId(id);
        return task;
    }
}
