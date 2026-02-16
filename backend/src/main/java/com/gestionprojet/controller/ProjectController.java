package com.gestionprojet.controller;

import com.gestionprojet.dto.ProjectDTO;
import com.gestionprojet.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing projects.
 * Provides endpoints for CRUD operations on projects.
 * All endpoints require Bearer token authentication via Keycloak.
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Retrieves all projects from the system.
     *
     * @return list of all projects with their responsible users
     */
    @GetMapping
    @Operation(summary = "Get all projects")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.findAll());
    }

    /**
     * Retrieves a specific project by its ID.
     *
     * @param id the UUID of the project to retrieve
     * @return the project if found
     * @throws com.gestionprojet.exception.ResourceNotFoundException if project not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.findById(id));
    }

    /**
     * Retrieves all projects managed by a specific responsible user.
     *
     * @param responsableId the UUID of the responsible user
     * @return list of projects managed by the specified user
     */
    @GetMapping("/responsable/{responsableId}")
    @Operation(summary = "Get projects by responsible user")
    public ResponseEntity<List<ProjectDTO>> getProjectsByResponsable(@PathVariable UUID responsableId) {
        return ResponseEntity.ok(projectService.findByResponsable(responsableId));
    }

    /**
     * Creates a new project.
     *
     * @param projectDTO the project data to create
     * @return the created project
     */
    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(projectDTO));
    }

    /**
     * Updates an existing project.
     *
     * @param id the UUID of the project to update
     * @param projectDTO the updated project data
     * @return the updated project
     * @throws com.gestionprojet.exception.ResourceNotFoundException if project not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing project")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.updateProject(id, projectDTO));
    }

    /**
     * Deletes a project by its ID.
     *
     * @param id the UUID of the project to delete
     * @return no content response
     * @throws com.gestionprojet.exception.ResourceNotFoundException if project not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
