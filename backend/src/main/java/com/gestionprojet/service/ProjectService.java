package com.gestionprojet.service;

import com.gestionprojet.dto.ProjectDTO;
import com.gestionprojet.dto.ProjectBriefDTO;
import com.gestionprojet.dto.UserBriefDTO;
import com.gestionprojet.exception.BusinessException;
import com.gestionprojet.exception.ResourceNotFoundException;
import com.gestionprojet.model.Project;
import com.gestionprojet.model.User;
import com.gestionprojet.repository.ProjectRepository;
import com.gestionprojet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing projects.
 * Handles business logic for project CRUD operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new project with the given data.
     * Validates that the responsible user exists.
     *
     * @param projectDTO the project data transfer object containing project information
     * @return the created project as DTO
     * @throws ResourceNotFoundException if responsible user not found
     */
    @Transactional
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        log.info("Creating project: {}", projectDTO.getNom());

        User responsable = userRepository.findById(projectDTO.getResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("User", projectDTO.getResponsableId().toString()));

        Project project = new Project();
        project.setNom(projectDTO.getNom());
        project.setDescription(projectDTO.getDescription());
        project.setDateDebut(projectDTO.getDateDebut());
        project.setDateFin(projectDTO.getDateFin());
        project.setResponsable(responsable);

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully: {}", savedProject.getId());

        return convertToDto(savedProject);
    }

    /**
     * Retrieves all projects from the database with their responsible users.
     *
     * @return list of all projects as DTOs
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> findAll() {
        log.info("Finding all projects");
        return projectRepository.findAllWithResponsable().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds a project by its ID.
     *
     * @param id the UUID of the project
     * @return the project as DTO
     * @throws ResourceNotFoundException if project not found
     */
    @Transactional(readOnly = true)
    public ProjectDTO findById(UUID id) {
        log.info("Finding project by ID: {}", id);
        Project project = projectRepository.findByIdWithResponsable(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));
        return convertToDto(project);
    }

    /**
     * Finds all projects managed by a specific responsible user.
     *
     * @param responsableId the UUID of the responsible user
     * @return list of projects as DTOs
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> findByResponsable(UUID responsableId) {
        log.info("Finding projects by responsable: {}", responsableId);
        User responsable = userRepository.findById(responsableId)
                .orElseThrow(() -> new ResourceNotFoundException("User", responsableId.toString()));
        return projectRepository.findByResponsableWithDetails(responsable).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing project with new data.
     *
     * @param id the UUID of the project to update
     * @param projectDTO the new project data
     * @return the updated project as DTO
     * @throws ResourceNotFoundException if project or responsible user not found
     */
    @Transactional
    public ProjectDTO updateProject(UUID id, ProjectDTO projectDTO) {
        log.info("Updating project: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));

        project.setNom(projectDTO.getNom());
        project.setDescription(projectDTO.getDescription());
        project.setDateDebut(projectDTO.getDateDebut());
        project.setDateFin(projectDTO.getDateFin());

        if (projectDTO.getResponsableId() != null) {
            User responsable = userRepository.findById(projectDTO.getResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", projectDTO.getResponsableId().toString()));
            project.setResponsable(responsable);
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated successfully: {}", updatedProject.getId());

        return convertToDto(updatedProject);
    }

    /**
     * Deletes a project by its ID.
     *
     * @param id the UUID of the project to delete
     * @throws ResourceNotFoundException if project not found
     */
    @Transactional
    public void deleteProject(UUID id) {
        log.info("Deleting project: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));

        projectRepository.delete(project);
        log.info("Project deleted successfully: {}", id);
    }

    /**
     * Converts a Project entity to a ProjectDTO.
     * Includes nested responsible user information.
     *
     * @param project the project entity to convert
     * @return the project DTO
     */
    private ProjectDTO convertToDto(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .nom(project.getNom())
                .description(project.getDescription())
                .dateDebut(project.getDateDebut())
                .dateFin(project.getDateFin())
                .responsableId(project.getResponsable().getId())
                .responsable(UserBriefDTO.builder()
                        .id(project.getResponsable().getId())
                        .nom(project.getResponsable().getNom())
                        .prenom(project.getResponsable().getPrenom())
                        .email(project.getResponsable().getEmail())
                        .build())
                .build();
    }
}
