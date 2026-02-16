package com.gestionprojet.model;

import com.gestionprojet.model.enums.Priority;
import com.gestionprojet.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Task entity representing a task within a project.
 * Tracks task status, priority, assignment, and timestamps.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "statut", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus statut = TaskStatus.TODO;

    @Column(name = "priorite", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priorite = Priority.MOYENNE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projet_id", nullable = false)
    private Project projet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigne_id")
    private User assigne;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
