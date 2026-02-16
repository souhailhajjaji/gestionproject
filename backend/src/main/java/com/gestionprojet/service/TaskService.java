package com.gestionprojet.service;

import com.gestionprojet.dto.TaskDTO;
import com.gestionprojet.dto.ProjectBriefDTO;
import com.gestionprojet.dto.UserBriefDTO;
import com.gestionprojet.exception.BusinessException;
import com.gestionprojet.exception.ResourceNotFoundException;
import com.gestionprojet.model.Project;
import com.gestionprojet.model.Task;
import com.gestionprojet.model.User;
import com.gestionprojet.model.enums.TaskStatus;
import com.gestionprojet.repository.TaskRepository;
import com.gestionprojet.repository.ProjectRepository;
import com.gestionprojet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing tasks.
 * Handles business logic for task CRUD operations, filtering, and statistics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new task with the given data.
     * Validates that the project and assignee (if provided) exist.
     *
     * @param taskDTO the task data transfer object containing task information
     * @return the created task as DTO
     * @throws ResourceNotFoundException if project or assignee not found
     */
    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        log.info("Creating task: {}", taskDTO.getTitre());

        Project projet = projectRepository.findById(taskDTO.getProjetId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", taskDTO.getProjetId().toString()));

        Task task = new Task();
        task.setTitre(taskDTO.getTitre());
        task.setDescription(taskDTO.getDescription());
        task.setStatut(taskDTO.getStatut());
        task.setPriorite(taskDTO.getPriorite());
        task.setProjet(projet);

        if (taskDTO.getAssigneId() != null) {
            User assigne = userRepository.findById(taskDTO.getAssigneId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", taskDTO.getAssigneId().toString()));
            task.setAssigne(assigne);
        }

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully: {}", savedTask.getId());

        return convertToDto(savedTask);
    }

    /**
     * Retrieves all tasks from the database.
     *
     * @return list of all tasks as DTOs
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> findAll() {
        log.info("Finding all tasks");
        return taskRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds a task by its ID.
     *
     * @param id the UUID of the task
     * @return the task as DTO
     * @throws ResourceNotFoundException if task not found
     */
    @Transactional(readOnly = true)
    public TaskDTO findById(UUID id) {
        log.info("Finding task by ID: {}", id);
        Task task = taskRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id.toString()));
        return convertToDto(task);
    }

    /**
     * Finds all tasks belonging to a specific project.
     *
     * @param projetId the UUID of the project
     * @return list of tasks as DTOs
     * @throws ResourceNotFoundException if project not found
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> findByProjet(UUID projetId) {
        log.info("Finding tasks by project: {}", projetId);
        Project projet = projectRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projetId.toString()));
        return taskRepository.findByProjetWithDetails(projet).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds all tasks assigned to a specific user.
     *
     * @param assigneId the UUID of the assignee
     * @return list of tasks as DTOs
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> findByAssigne(UUID assigneId) {
        log.info("Finding tasks by assignee: {}", assigneId);
        User assigne = userRepository.findById(assigneId)
                .orElseThrow(() -> new ResourceNotFoundException("User", assigneId.toString()));
        return taskRepository.findByAssigne(assigne).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Finds tasks matching the provided filter criteria.
     * Null parameters are ignored in the filter.
     *
     * @param assigneId optional filter by assignee UUID
     * @param statut optional filter by task status
     * @param projetId optional filter by project UUID
     * @return list of matching tasks as DTOs
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> findByFilters(UUID assigneId, TaskStatus statut, UUID projetId) {
        log.info("Finding tasks by filters - assigneId: {}, statut: {}, projetId: {}", assigneId, statut, projetId);
        return taskRepository.findByFilters(assigneId, statut, projetId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing task with new data.
     *
     * @param id the UUID of the task to update
     * @param taskDTO the new task data
     * @return the updated task as DTO
     * @throws ResourceNotFoundException if task, project, or assignee not found
     */
    @Transactional
    public TaskDTO updateTask(UUID id, TaskDTO taskDTO) {
        log.info("Updating task: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id.toString()));

        task.setTitre(taskDTO.getTitre());
        task.setDescription(taskDTO.getDescription());
        task.setStatut(taskDTO.getStatut());
        task.setPriorite(taskDTO.getPriorite());

        if (taskDTO.getProjetId() != null) {
            Project projet = projectRepository.findById(taskDTO.getProjetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", taskDTO.getProjetId().toString()));
            task.setProjet(projet);
        }

        if (taskDTO.getAssigneId() != null) {
            User assigne = userRepository.findById(taskDTO.getAssigneId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", taskDTO.getAssigneId().toString()));
            task.setAssigne(assigne);
        } else {
            task.setAssigne(null);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully: {}", updatedTask.getId());

        return convertToDto(updatedTask);
    }

    /**
     * Updates only the status of a task.
     *
     * @param id the UUID of the task
     * @param statut the new status to set
     * @return the updated task as DTO
     * @throws ResourceNotFoundException if task not found
     */
    @Transactional
    public TaskDTO updateTaskStatus(UUID id, TaskStatus statut) {
        log.info("Updating task status: {} -> {}", id, statut);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id.toString()));

        task.setStatut(statut);

        Task updatedTask = taskRepository.save(task);
        log.info("Task status updated successfully: {}", updatedTask.getId());

        return convertToDto(updatedTask);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id the UUID of the task to delete
     * @throws ResourceNotFoundException if task not found
     */
    @Transactional
    public void deleteTask(UUID id) {
        log.info("Deleting task: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id.toString()));

        taskRepository.delete(task);
        log.info("Task deleted successfully: {}", id);
    }

    /**
     * Gets the count of tasks grouped by status for a specific project.
     *
     * @param projetId the UUID of the project
     * @return map of task status to count
     * @throws ResourceNotFoundException if project not found
     */
    @Transactional(readOnly = true)
    public Map<TaskStatus, Long> getTaskCountByStatus(UUID projetId) {
        log.info("Getting task count by status for project: {}", projetId);
        Project projet = projectRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projetId.toString()));

        return taskRepository.findByProjet(projet).stream()
                .collect(Collectors.groupingBy(Task::getStatut, Collectors.counting()));
    }

    /**
     * Converts a Task entity to a TaskDTO.
     * Includes nested project and assignee information.
     *
     * @param task the task entity to convert
     * @return the task DTO
     */
    private TaskDTO convertToDto(Task task) {
        TaskDTO.TaskDTOBuilder builder = TaskDTO.builder()
                .id(task.getId())
                .titre(task.getTitre())
                .description(task.getDescription())
                .statut(task.getStatut())
                .priorite(task.getPriorite())
                .projetId(task.getProjet().getId())
                .projet(ProjectBriefDTO.builder()
                        .id(task.getProjet().getId())
                        .nom(task.getProjet().getNom())
                        .description(task.getProjet().getDescription())
                        .build());

        if (task.getAssigne() != null) {
            builder.assigneId(task.getAssigne().getId())
                    .assigne(UserBriefDTO.builder()
                            .id(task.getAssigne().getId())
                            .nom(task.getAssigne().getNom())
                            .prenom(task.getAssigne().getPrenom())
                            .email(task.getAssigne().getEmail())
                            .build());
        }

        return builder.build();
    }
}
