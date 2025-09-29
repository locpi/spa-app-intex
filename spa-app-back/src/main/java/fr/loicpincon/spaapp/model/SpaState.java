package fr.loicpincon.spaapp.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * État temps réel du spa - NON PERSISTÉ en base (données volatiles via MQTT)
 * Cette classe représente l'état actuel reçu du spa via MQTT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaState {

    // État actuel de la température
    private Double currentTemperature;
    private Integer targetTemperature;
    private Boolean isHeating;

    // État des équipements
    private Boolean bubblesActive;
    private Boolean filterActive;
    private Boolean lightsActive; // Si disponible dans le futur

    // État général du système
    private Boolean isOnline;
    private Boolean isInErrorState;
    private String errorMessage;

    // Session en cours
    private Long activeSessionId;
    private LocalDateTime sessionStartTime;
    private Integer sessionRemainingMinutes;

    // Informations système
    private String firmwareVersion;
    private Double powerConsumption; // Si disponible
    private LocalDateTime lastUpdateTime;
    private Integer wifiSignalStrength; // Si disponible

    // Méthodes utilitaires
    public boolean isHeatingActive() {
        return Boolean.TRUE.equals(isHeating);
    }

    public boolean areBubblesActive() {
        return Boolean.TRUE.equals(bubblesActive);
    }

    public boolean isFilterActive() {
        return Boolean.TRUE.equals(filterActive);
    }

    public boolean hasActiveSession() {
        return activeSessionId != null && activeSessionId > 0;
    }

    public boolean isTemperatureReached() {
        if (currentTemperature == null || targetTemperature == null) {
            return false;
        }
        // Tolérance de ±0.5°C
        return Math.abs(currentTemperature - targetTemperature) <= 0.5;
    }

    public boolean requiresAttention() {
        return Boolean.TRUE.equals(isInErrorState) ||
               Boolean.FALSE.equals(isOnline) ||
               (errorMessage != null && !errorMessage.trim().isEmpty());
    }

    public String getStatusSummary() {
        if (!Boolean.TRUE.equals(isOnline)) {
            return "Hors ligne";
        }
        if (Boolean.TRUE.equals(isInErrorState)) {
            return "Erreur: " + (errorMessage != null ? errorMessage : "Inconnue");
        }
        if (hasActiveSession()) {
            return "Session en cours - " + sessionRemainingMinutes + "min restantes";
        }
        if (isHeatingActive()) {
            return "Chauffage en cours - " + currentTemperature + "°C → " + targetTemperature + "°C";
        }
        return "En veille - " + currentTemperature + "°C";
    }

    // Méthode pour créer un état par défaut (spa éteint/déconnecté)
    public static SpaState createOfflineState() {
        return SpaState.builder()
                .isOnline(false)
                .isHeating(false)
                .bubblesActive(false)
                .filterActive(false)
                .isInErrorState(false)
                .lastUpdateTime(LocalDateTime.now())
                .build();
    }

    // Méthode pour valider la cohérence des données
    public boolean isDataConsistent() {
        // Vérifications de base
        if (currentTemperature != null && (currentTemperature < -10 || currentTemperature > 50)) {
            return false;
        }
        if (targetTemperature != null && (targetTemperature < 15 || targetTemperature > 45)) {
            return false;
        }
        if (sessionRemainingMinutes != null && sessionRemainingMinutes < 0) {
            return false;
        }
        return true;
    }
}