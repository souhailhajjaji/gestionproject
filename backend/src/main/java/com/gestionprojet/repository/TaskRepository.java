package com.gestionprojet.repository;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.Task;
import com.gestionprojet.model.User;
import com.gestionprojet.model.enums.Priority;
import com.gestionprojet.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Task entity operations.
 * Provides CRUD operations and custom query methods for tasks.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Finds all tasks belonging to a specific project.
     * @param projet the project
     * @return list of tasks for the project
     */
    List<Task> findByProjet(Project projet);

    /**
     * Finds all tasks assigned to a specific user.
     * @param assigne the assignee user
     * @return list of tasks assigned to the user
     */
    List<Task> findByAssigne(User assigne);

    /**
     * Finds all tasks with a specific status.
     * @param statut the task status
     * @return list of tasks with the status
     */
    List<Task> findByStatut(TaskStatus statut);

    /**
     * Finds all tasks with a specific priority.
     * @param priorite the task priority
     * @return list of tasks with the priority
     */
    List<Task> findByPriorite(Priority priorite);

    /**
     * Finds a task by ID with project and assignee details eagerly fetched.
     * @param id the task UUID
     * @return Optional containing the task with details if found
     */
    @Query("SELECT t FROM Task t JOIN FETCH t.projet p JOIN FETCH p.responsable WHERE t.id = :id")
    Optional<Task> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Finds all tasks for a project with details eagerly fetched.
     * @param projet the project
     * @return list of tasks with details
     */
    @Query("SELECT t FROM Task t JOIN FETCH t.projet p JOIN FETCH p.responsable LEFT JOIN FETCH t.assigne WHERE t.projet = :projet")
    List<Task> findByProjetWithDetails(@Param("projet") Project projet);

    /**
     * Finds all tasks for a project with a specific status.
     * @param projet the project
     * @param statut the task status
     * @return list of tasks with the status
     */
    @Query("SELECT t FROM Task t JOIN FETCH t.projet p JOIN FETCH p.responsable LEFT JOIN FETCH t.assigne " +
           "WHERE t.projet = :projet AND t.statut = :statut")
    List<Task> findByProjetAndStatut(@Param("projet") Project projet, @Param("statut") TaskStatus statut);

    /**
     * Finds tasks matching the provided filter criteria.
     * Null parameters are ignored in the filter.
     * @param assigneId optional filter by assignee UUID
     * @param statut optional filter by task status
     * @param projetId optional filter by project UUID
     * @return list of matching tasks
     */
    @Query("SELECT t FROM Task t JOIN FETCH t.projet p JOIN FETCH p.responsable LEFT JOIN FETCH t.assigne " +
           "WHERE (:assigneId IS NULL OR t.assigne.id = :assigneId) " +
           "AND (:statut IS NULL OR t.statut = :statut) " +
           "AND (:projetId IS NULL OR t.projet.id = :projetId)")
    List<Task> findByFilters(
        @Param("assigneId") UUID assigneId,
        @Param("statut") TaskStatus statut,
        @Param("projetId") UUID projetId
    );

    /**
     * Counts tasks by project and status.
     * @param projet the project
     * @param statut the task status
     * @return count of tasks
     */
    long countByProjetAndStatut(Project projet, TaskStatus statut);
}
