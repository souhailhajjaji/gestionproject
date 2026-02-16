package com.gestionprojet.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object for Project entity.
 * Used for creating and updating projects with validation constraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {

    private UUID id;

    @NotBlank(message = "Nom du projet est requis")
    @Size(max = 200, message = "Nom trop long (max 200 caractères)")
    private String nom;

    @Size(max = 1000, message = "Description trop longue")
    private String description;

    @NotNull(message = "Date de début est requise")
    private LocalDate dateDebut;

    @FutureOrPresent(message = "Date de fin doit être dans le futur")
    private LocalDate dateFin;

    @NotNull(message = "Responsable est requis")
    private UUID responsableId;

    private UserBriefDTO responsable;
}
