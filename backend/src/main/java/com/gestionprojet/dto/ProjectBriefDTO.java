package com.gestionprojet.dto;

import lombok.*;

import java.util.UUID;

/**
 * Brief Data Transfer Object for Project entity.
 * Contains only essential project information for display purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectBriefDTO {

    private UUID id;
    private String nom;
    private String description;
}
