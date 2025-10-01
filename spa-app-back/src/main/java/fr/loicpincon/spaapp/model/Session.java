package fr.loicpincon.spaapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Sessions programmées ponctuelles avec date et heure précises
 * Exemple: "Session détente dimanche" le 30/09/2025 à 15h00
 */
@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la session ne peut pas être vide")
    @Size(max = 150, message = "Le nom de la session ne peut pas dépasser 150 caractères")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotNull(message = "La date et heure de début sont obligatoires")
    @Future(message = "La session doit être programmée dans le futur")
    @Column(name = "scheduled_start_time", nullable = false)
    private LocalDateTime scheduledStartTime;

    @NotNull(message = "La température cible est obligatoire")
    @Min(value = 20, message = "La température minimum est 20°C")
    @Max(value = 42, message = "La température maximum est 42°C")
    @Column(name = "target_temperature", nullable = false)
    private Integer targetTemperature;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 15, message = "La durée minimum est 15 minutes")
    @Max(value = 480, message = "La durée maximum est 8 heures")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Builder.Default
    @Column(name = "bubbles_enabled", nullable = false)
    private Boolean bubblesEnabled = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    @Column(name = "notes", length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Méthodes utilitaires
    public LocalDateTime getCalculatedEndTime() {
        LocalDateTime startTime = actualStartTime != null ? actualStartTime : scheduledStartTime;
        return startTime.plusMinutes(durationMinutes);
    }

    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }

    public boolean isScheduled() {
        return status == SessionStatus.SCHEDULED;
    }

    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == SessionStatus.CANCELLED;
    }

    public boolean shouldActivateBubbles() {
        return Boolean.TRUE.equals(bubblesEnabled);
    }

    public long getActualDurationMinutes() {
        if (actualStartTime != null && actualEndTime != null) {
            return java.time.Duration.between(actualStartTime, actualEndTime).toMinutes();
        }
        return 0;
    }

    // Méthodes pour changer d'état
    public void start() {
        if (status == SessionStatus.SCHEDULED) {
            status = SessionStatus.ACTIVE;
            actualStartTime = LocalDateTime.now();
        }
    }

    public void complete() {
        if (status == SessionStatus.ACTIVE) {
            status = SessionStatus.COMPLETED;
            actualEndTime = LocalDateTime.now();
        }
    }

    public void cancel() {
        if (status == SessionStatus.SCHEDULED || status == SessionStatus.ACTIVE) {
            status = SessionStatus.CANCELLED;
            if (status == SessionStatus.ACTIVE && actualEndTime == null) {
                actualEndTime = LocalDateTime.now();
            }
        }
    }
}