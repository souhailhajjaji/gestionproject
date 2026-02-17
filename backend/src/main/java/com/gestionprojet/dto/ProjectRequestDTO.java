package com.gestionprojet.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request Data Transfer Object for creating or updating a Project.
 * Contains validation constraints for input data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequestDTO {

    @NotBlank(message = "Nom du projet est requis")
    @Size(max = 200, message = "Nom trop long (max 200 caractères)")
    private String nom;

    @Size(max = 1000, message = "Description trop longue (max 1000 caractères)")
    private String description;

    @NotNull(message = "Date de début est requise")
    private LocalDate dateDebut;

    @FutureOrPresent(message = "Date de fin doit être dans le futur ou aujourd'hui")
    private LocalDate dateFin;

    @NotNull(message = "Responsable est requis")
    private UUID responsableId;
}
