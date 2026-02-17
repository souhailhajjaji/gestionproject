package com.gestionprojet.repository;

import com.gestionprojet.model.Project;
import com.gestionprojet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Project entity operations.
 * Provides CRUD operations and custom query methods for projects.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Finds all projects managed by a specific responsible user.
     * @param responsable the responsible user
     * @return list of projects managed by the user
     */
    List<Project> findByResponsable(User responsable);

    /**
     * Finds a project by ID with its responsible user eagerly fetched.
     * @param id the project UUID
     * @return Optional containing the project with responsable if found
     */
    @Query("SELECT p FROM Project p JOIN FETCH p.responsable WHERE p.id = :id")
    Optional<Project> findByIdWithResponsable(@Param("id") UUID id);

    /**
     * Finds all projects with their responsible users eagerly fetched.
     * @return list of all projects with responsables
     */
    @Query("SELECT p FROM Project p JOIN FETCH p.responsable ORDER BY p.createdAt DESC")
    List<Project> findAllWithResponsable();

    /**
     * Finds all projects managed by a specific user with details eagerly fetched.
     * @param responsable the responsible user
     * @return list of projects with responsables
     */
    @Query("SELECT p FROM Project p JOIN FETCH p.responsable WHERE p.responsable = :responsable")
    List<Project> findByResponsableWithDetails(@Param("responsable") User responsable);
}
