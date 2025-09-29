package fr.loicpincon.spaapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Scénarios récurrents (quotidiens) pour le contrôle automatique du spa
 * Exemple: "Chauffage du soir" tous les jours à 23h00
 */
@Entity
@Table(name = "scenarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du scénario ne peut pas être vide")
    @Size(max = 100, message = "Le nom du scénario ne peut pas dépasser 100 caractères")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "L'heure d'exécution est obligatoire")
    @Column(name = "execution_time", nullable = false)
    private LocalTime executionTime;

    @NotNull(message = "La température cible est obligatoire")
    @Min(value = 20, message = "La température minimum est 20°C")
    @Max(value = 42, message = "La température maximum est 42°C")
    @Column(name = "target_temperature", nullable = false)
    private Integer targetTemperature;

    @Min(value = 20, message = "La température de maintien minimum est 20°C")
    @Max(value = 42, message = "La température de maintien maximum est 42°C")
    @Column(name = "maintain_temperature")
    private Integer maintainTemperature;

    @Builder.Default
    @Column(name = "bubbles_enabled", nullable = false)
    private Boolean bubblesEnabled = false;

    @Min(value = 15, message = "La durée minimum est 15 minutes")
    @Max(value = 480, message = "La durée maximum est 8 heures")
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = true;

    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Méthodes utilitaires
    public boolean shouldActivateBubbles() {
        return Boolean.TRUE.equals(bubblesEnabled);
    }

    public boolean hasMaintenanceTemperature() {
        return maintainTemperature != null;
    }

    public boolean hasDurationLimit() {
        return durationMinutes != null && durationMinutes > 0;
    }
}