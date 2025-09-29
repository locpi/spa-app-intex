package fr.loicpincon.spaapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Configuration persistante du spa (préférences utilisateur, limites, etc.)
 * Une seule instance par système - Singleton pattern
 */
@Entity
@Table(name = "spa_configuration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Limites de température
    @Min(value = 15, message = "La température minimum absolue ne peut pas être inférieure à 15°C")
    @Max(value = 25, message = "La température minimum absolue ne peut pas dépasser 25°C")
    @Builder.Default
    @Column(name = "min_temperature", nullable = false)
    private Integer minTemperature = 20;

    @Min(value = 35, message = "La température maximum absolue ne peut pas être inférieure à 35°C")
    @Max(value = 45, message = "La température maximum absolue ne peut pas dépasser 45°C")
    @Builder.Default
    @Column(name = "max_temperature", nullable = false)
    private Integer maxTemperature = 42;

    // Températures par défaut pour les modes rapides
    @Builder.Default
    @Column(name = "default_relax_temperature", nullable = false)
    private Integer defaultRelaxTemperature = 37;

    @Builder.Default
    @Column(name = "default_eco_temperature", nullable = false)
    private Integer defaultEcoTemperature = 28;

    @Builder.Default
    @Column(name = "default_heat_temperature", nullable = false)
    private Integer defaultHeatTemperature = 40;

    // Durées par défaut
    @Builder.Default
    @Column(name = "default_session_duration", nullable = false)
    private Integer defaultSessionDurationMinutes = 60;

    @Builder.Default
    @Column(name = "max_session_duration", nullable = false)
    private Integer maxSessionDurationMinutes = 480; // 8 heures

    // Paramètres de sécurité
    @Builder.Default
    @Column(name = "auto_shutdown_enabled", nullable = false)
    private Boolean autoShutdownEnabled = true;

    @Builder.Default
    @Column(name = "auto_shutdown_duration", nullable = false)
    private Integer autoShutdownDurationMinutes = 240; // 4 heures

    // Paramètres MQTT
    @Size(max = 200)
    @Column(name = "mqtt_broker_url", length = 200)
    private String mqttBrokerUrl;

    @Size(max = 100)
    @Column(name = "mqtt_client_id", length = 100)
    private String mqttClientId;

    @Size(max = 100)
    @Column(name = "mqtt_username", length = 100)
    private String mqttUsername;

    @Size(max = 100)
    @Column(name = "mqtt_password", length = 100)
    private String mqttPassword;

    // Topics MQTT
    @Size(max = 200)
    @Builder.Default
    @Column(name = "mqtt_command_topic", length = 200)
    private String mqttCommandTopic = "spa/command";

    @Size(max = 200)
    @Builder.Default
    @Column(name = "mqtt_status_topic", length = 200)
    private String mqttStatusTopic = "spa/status";

    @Size(max = 200)
    @Builder.Default
    @Column(name = "mqtt_temperature_topic", length = 200)
    private String mqttTemperatureTopic = "spa/temperature";

    // Notifications
    @Builder.Default
    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = true;

    @Builder.Default
    @Column(name = "session_start_notification", nullable = false)
    private Boolean sessionStartNotification = true;

    @Builder.Default
    @Column(name = "session_end_notification", nullable = false)
    private Boolean sessionEndNotification = true;

    @Builder.Default
    @Column(name = "temperature_reached_notification", nullable = false)
    private Boolean temperatureReachedNotification = true;

    // Métadonnées
    @Size(max = 100)
    @Column(name = "spa_name", length = 100)
    private String spaName;

    @Size(max = 50)
    @Column(name = "spa_model", length = 50)
    private String spaModel;

    @Column(name = "installation_date")
    private LocalDateTime installationDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Méthodes utilitaires
    public boolean isTemperatureValid(int temperature) {
        return temperature >= minTemperature && temperature <= maxTemperature;
    }

    public boolean isDurationValid(int durationMinutes) {
        return durationMinutes > 0 && durationMinutes <= maxSessionDurationMinutes;
    }

    public boolean isMqttConfigured() {
        return mqttBrokerUrl != null && !mqttBrokerUrl.trim().isEmpty();
    }

    public boolean requiresAuthentication() {
        return mqttUsername != null && !mqttUsername.trim().isEmpty();
    }
}