package com.gestionprojet.dto;

import com.gestionprojet.model.enums.Priority;
import com.gestionprojet.model.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Data Transfer Object for Task entity.
 * Used for creating and updating tasks with validation constraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDTO {

    private UUID id;

    @NotBlank(message = "Titre est requis")
    @Size(max = 200, message = "Titre trop long (max 200 caractères)")
    private String titre;

    @Size(max = 1000, message = "Description trop longue")
    private String description;

    @NotNull(message = "Statut est requis")
    private TaskStatus statut;

    @NotNull(message = "Priorité est requise")
    private Priority priorite;

    @NotNull(message = "Projet est requis")
    private UUID projetId;

    private ProjectBriefDTO projet;

    private UUID assigneId;

    private UserBriefDTO assigne;
}
