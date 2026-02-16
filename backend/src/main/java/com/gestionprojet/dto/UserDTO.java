package com.gestionprojet.dto;

import com.gestionprojet.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for User entity.
 * Used for creating and updating users with validation constraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private UUID id;

    @NotBlank(message = "Nom est requis")
    private String nom;

    @NotBlank(message = "Prénom est requis")
    private String prenom;

    private LocalDate dateNaissance;

    @NotBlank(message = "Email est requis")
    @Email(message = "Email invalide")
    private String email;

    private String telephone;

    private String pieceIdentiteUrl;

    private Set<Role> roles;

    private UUID keycloakId;
}
