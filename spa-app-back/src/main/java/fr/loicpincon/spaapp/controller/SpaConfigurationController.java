package fr.loicpincon.spaapp.controller;

import fr.loicpincon.spaapp.model.SpaConfiguration;
import fr.loicpincon.spaapp.service.SpaConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/configuration")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class SpaConfigurationController {

    private final SpaConfigurationService configurationService;

    @GetMapping
    public ResponseEntity<SpaConfiguration> getConfiguration() {
        SpaConfiguration config = configurationService.getConfiguration();
        return ResponseEntity.ok(config);
    }

    @PutMapping
    public ResponseEntity<SpaConfiguration> updateConfiguration(@Valid @RequestBody SpaConfiguration configuration) {
        try {
            SpaConfiguration updated = configurationService.updateConfiguration(configuration);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation lors de la mise à jour de la configuration: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<SpaConfiguration> resetToDefaults() {
        configurationService.resetToDefaults();
        SpaConfiguration defaultConfig = configurationService.getConfiguration();
        return ResponseEntity.ok(defaultConfig);
    }

    @GetMapping("/mqtt/status")
    public ResponseEntity<Boolean> isMqttConfigured() {
        boolean configured = configurationService.isMqttConfigured();
        return ResponseEntity.ok(configured);
    }

    @PutMapping("/mqtt")
    public ResponseEntity<SpaConfiguration> updateMqttConfiguration(
            @RequestParam String brokerUrl,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password) {
        try {
            SpaConfiguration updated = configurationService.updateMqttConfiguration(brokerUrl, username, password);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la mise à jour de la configuration MQTT: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/notifications")
    public ResponseEntity<SpaConfiguration> updateNotificationSettings(
            @RequestParam boolean enabled,
            @RequestParam(defaultValue = "true") boolean sessionStart,
            @RequestParam(defaultValue = "true") boolean sessionEnd,
            @RequestParam(defaultValue = "true") boolean temperatureReached,
            @RequestParam(defaultValue = "true") boolean error) {

        SpaConfiguration updated = configurationService.updateNotificationSettings(
            enabled, sessionStart, sessionEnd, temperatureReached, error
        );
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/temperatures")
    public ResponseEntity<SpaConfiguration> updateTemperatureDefaults(
            @RequestParam int relax,
            @RequestParam int eco,
            @RequestParam int heat) {
        try {
            SpaConfiguration updated = configurationService.updateTemperatureDefaults(relax, eco, heat);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la mise à jour des températures par défaut: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/spa-name/{name}")
    public ResponseEntity<SpaConfiguration> findBySpaName(@PathVariable String name) {
        return configurationService.findBySpaName(name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/auto-shutdown/enabled")
    public ResponseEntity<Boolean> isAutoShutdownEnabled() {
        boolean enabled = configurationService.isAutoShutdownEnabled();
        return ResponseEntity.ok(enabled);
    }

    @GetMapping("/auto-shutdown/duration")
    public ResponseEntity<Integer> getAutoShutdownDuration() {
        Integer duration = configurationService.getAutoShutdownDuration();
        return ResponseEntity.ok(duration);
    }

    @GetMapping("/notifications/enabled")
    public ResponseEntity<Boolean> areNotificationsEnabled() {
        boolean enabled = configurationService.areNotificationsEnabled();
        return ResponseEntity.ok(enabled);
    }
}