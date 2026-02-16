package com.gestionprojet.controller;

import com.gestionprojet.dto.TaskDTO;
import com.gestionprojet.model.enums.TaskStatus;
import com.gestionprojet.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing tasks.
 * Provides endpoints for CRUD operations, filtering, and statistics on tasks.
 * All endpoints require Bearer token authentication via Keycloak.
 */
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management API")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    /**
     * Retrieves all tasks from the system.
     *
     * @return list of all tasks
     */
    @GetMapping
    @Operation(summary = "Get all tasks")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        return ResponseEntity.ok(taskService.findAll());
    }

    /**
     * Retrieves a specific task by its ID.
     *
     * @param id the UUID of the task to retrieve
     * @return the task if found
     * @throws com.gestionprojet.exception.ResourceNotFoundException if task not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    /**
     * Retrieves all tasks belonging to a specific project.
     *
     * @param projetId the UUID of the project
     * @return list of tasks for the specified project
     */
    @GetMapping("/projet/{projetId}")
    @Operation(summary = "Get tasks by project")
    public ResponseEntity<List<TaskDTO>> getTasksByProjet(@PathVariable UUID projetId) {
        return ResponseEntity.ok(taskService.findByProjet(projetId));
    }

    /**
     * Retrieves all tasks assigned to a specific user.
     *
     * @param assigneId the UUID of the assignee
     * @return list of tasks assigned to the specified user
     */
    @GetMapping("/assigne/{assigneId}")
    @Operation(summary = "Get tasks by assignee")
    public ResponseEntity<List<TaskDTO>> getTasksByAssigne(@PathVariable UUID assigneId) {
        return ResponseEntity.ok(taskService.findByAssigne(assigneId));
    }

    /**
     * Filters tasks based on optional criteria: assignee, status, and project.
     *
     * @param assigneId optional filter by assignee UUID
     * @param statut optional filter by task status
     * @param projetId optional filter by project UUID
     * @return list of tasks matching the filter criteria
     */
    @GetMapping("/filter")
    @Operation(summary = "Filter tasks")
    public ResponseEntity<List<TaskDTO>> filterTasks(
            @RequestParam(required = false) UUID assigneId,
            @RequestParam(required = false) TaskStatus statut,
            @RequestParam(required = false) UUID projetId) {
        return ResponseEntity.ok(taskService.findByFilters(assigneId, statut, projetId));
    }

    /**
     * Retrieves task count grouped by status for a specific project.
     *
     * @param projetId the UUID of the project
     * @return map of task status to count
     */
    @GetMapping("/projet/{projetId}/stats")
    @Operation(summary = "Get task count by status for a project")
    public ResponseEntity<Map<TaskStatus, Long>> getTaskStatsByProjet(@PathVariable UUID projetId) {
        return ResponseEntity.ok(taskService.getTaskCountByStatus(projetId));
    }

    /**
     * Creates a new task.
     *
     * @param taskDTO the task data to create
     * @return the created task
     */
    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(taskDTO));
    }

    /**
     * Updates an existing task.
     *
     * @param id the UUID of the task to update
     * @param taskDTO the updated task data
     * @return the updated task
     * @throws com.gestionprojet.exception.ResourceNotFoundException if task not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskDTO taskDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDTO));
    }

    /**
     * Updates only the status of a task.
     *
     * @param id the UUID of the task
     * @param statut the new status to set
     * @return the updated task
     * @throws com.gestionprojet.exception.ResourceNotFoundException if task not found
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable UUID id,
            @RequestParam TaskStatus statut) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, statut));
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id the UUID of the task to delete
     * @return no content response
     * @throws com.gestionprojet.exception.ResourceNotFoundException if task not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
