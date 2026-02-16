package com.gestionprojet.dto;

import lombok.*;

import java.util.UUID;

/**
 * Brief Data Transfer Object for User entity.
 * Contains only essential user information for display purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBriefDTO {

    private UUID id;
    private String nom;
    private String prenom;
    private String email;
}
