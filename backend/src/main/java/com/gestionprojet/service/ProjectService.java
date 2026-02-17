package com.gestionprojet.service;

import com.gestionprojet.dto.ProjectRequestDTO;
import com.gestionprojet.dto.ProjectResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing projects.
 * Defines business operations for project CRUD operations.
 */
public interface ProjectService {

    /**
     * Creates a new project with the given data.
     *
     * @param projectDTO the project request data transfer object
     * @return the created project as response DTO
     * @throws com.gestionprojet.exception.BusinessException if validation fails (e.g., dateFin < dateDebut)
     * @throws com.gestionprojet.exception.ResourceNotFoundException if responsible user not found
     */
    ProjectResponseDTO createProject(ProjectRequestDTO projectDTO);

    /**
     * Retrieves all projects from the database.
     *
     * @return list of all projects as response DTOs
     */
    List<ProjectResponseDTO> findAll();

    /**
     * Finds a project by its ID.
     *
     * @param id the UUID of the project
     * @return the project as response DTO
     * @throws com.gestionprojet.exception.ProjectNotFoundException if project not found
     */
    ProjectResponseDTO findById(UUID id);

    /**
     * Finds all projects managed by a specific responsible user.
     *
     * @param responsableId the UUID of the responsible user
     * @return list of projects as response DTOs
     * @throws com.gestionprojet.exception.ResourceNotFoundException if user not found
     */
    List<ProjectResponseDTO> findByResponsable(UUID responsableId);

    /**
     * Updates an existing project with new data.
     *
     * @param id the UUID of the project to update
     * @param projectDTO the new project request data
     * @return the updated project as response DTO
     * @throws com.gestionprojet.exception.ProjectNotFoundException if project not found
     * @throws com.gestionprojet.exception.BusinessException if validation fails
     * @throws com.gestionprojet.exception.ResourceNotFoundException if responsible user not found
     */
    ProjectResponseDTO updateProject(UUID id, ProjectRequestDTO projectDTO);

    /**
     * Deletes a project by its ID.
     *
     * @param id the UUID of the project to delete
     * @throws com.gestionprojet.exception.ProjectNotFoundException if project not found
     */
    void deleteProject(UUID id);
}
