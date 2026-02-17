package com.gestionprojet.service;

import com.gestionprojet.dto.ProjectMapper;
import com.gestionprojet.dto.ProjectRequestDTO;
import com.gestionprojet.dto.ProjectResponseDTO;
import com.gestionprojet.dto.UserBriefDTO;
import com.gestionprojet.exception.BusinessException;
import com.gestionprojet.exception.ProjectNotFoundException;
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
 * Service implementation for managing projects.
 * Handles business logic for project CRUD operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    /**
     * Creates a new project with the given data.
     * Validates that the responsible user exists and dates are valid.
     *
     * @param projectDTO the project request data transfer object
     * @return the created project as response DTO
     * @throws BusinessException if validation fails (e.g., dateFin < dateDebut)
     * @throws com.gestionprojet.exception.ResourceNotFoundException if responsible user not found
     */
    @Override
    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO projectDTO) {
        log.info("Creating project: {}", projectDTO.getNom());

        validateDates(projectDTO.getDateDebut(), projectDTO.getDateFin());

        User responsable = userRepository.findById(projectDTO.getResponsableId())
                .orElseThrow(() -> new BusinessException(
                        "Responsable not found with id: " + projectDTO.getResponsableId()));

        Project project = projectMapper.toEntity(projectDTO);
        project.setResponsable(responsable);

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully: {}", savedProject.getId());

        return convertToResponseDto(savedProject);
    }

    /**
     * Retrieves all projects from the database with their responsible users.
     *
     * @return list of all projects as response DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> findAll() {
        log.info("Finding all projects");
        return projectRepository.findAllWithResponsable().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds a project by its ID.
     *
     * @param id the UUID of the project
     * @return the project as response DTO
     * @throws ProjectNotFoundException if project not found
     */
    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDTO findById(UUID id) {
        log.info("Finding project by ID: {}", id);
        Project project = projectRepository.findByIdWithResponsable(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        return convertToResponseDto(project);
    }

    /**
     * Finds all projects managed by a specific responsible user.
     *
     * @param responsableId the UUID of the responsible user
     * @return list of projects as response DTOs
     * @throws BusinessException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> findByResponsable(UUID responsableId) {
        log.info("Finding projects by responsable: {}", responsableId);
        User responsable = userRepository.findById(responsableId)
                .orElseThrow(() -> new BusinessException(
                        "Responsable not found with id: " + responsableId));
        return projectRepository.findByResponsableWithDetails(responsable).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing project with new data.
     *
     * @param id the UUID of the project to update
     * @param projectDTO the new project request data
     * @return the updated project as response DTO
     * @throws ProjectNotFoundException if project not found
     * @throws BusinessException if validation fails or responsible user not found
     */
    @Override
    @Transactional
    public ProjectResponseDTO updateProject(UUID id, ProjectRequestDTO projectDTO) {
        log.info("Updating project: {}", id);

        validateDates(projectDTO.getDateDebut(), projectDTO.getDateFin());

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        projectMapper.partialUpdate(project, projectDTO);

        if (projectDTO.getResponsableId() != null) {
            User responsable = userRepository.findById(projectDTO.getResponsableId())
                    .orElseThrow(() -> new BusinessException(
                            "Responsable not found with id: " + projectDTO.getResponsableId()));
            project.setResponsable(responsable);
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated successfully: {}", updatedProject.getId());

        return convertToResponseDto(updatedProject);
    }

    /**
     * Deletes a project by its ID.
     *
     * @param id the UUID of the project to delete
     * @throws ProjectNotFoundException if project not found
     */
    @Override
    @Transactional
    public void deleteProject(UUID id) {
        log.info("Deleting project: {}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        projectRepository.delete(project);
        log.info("Project deleted successfully: {}", id);
    }

    /**
     * Validates that the end date is not before the start date.
     *
     * @param dateDebut the start date
     * @param dateFin the end date
     * @throws BusinessException if dateFin is before dateDebut
     */
    private void validateDates(java.time.LocalDate dateDebut, java.time.LocalDate dateFin) {
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new BusinessException("La date de fin doit être postérieure à la date de début");
        }
    }

    /**
     * Converts a Project entity to a ProjectResponseDTO.
     *
     * @param project the project entity to convert
     * @return the project response DTO
     */
    private ProjectResponseDTO convertToResponseDto(Project project) {
        return ProjectResponseDTO.builder()
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
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
