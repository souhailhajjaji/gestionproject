package com.gestionprojet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response Data Transfer Object for Project entity.
 * Contains all project information including timestamps for API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponseDTO {

    private UUID id;

    private String nom;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFin;

    private UUID responsableId;

    private UserBriefDTO responsable;

    private Instant createdAt;

    private Instant updatedAt;
}
