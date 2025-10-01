package fr.loicpincon.spaapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour vérifier la santé de l'application
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HealthController {

    /**
     * Endpoint de santé simple
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "spa-app-backend");
        health.put("version", "0.0.1-SNAPSHOT");

        // Statut des services
        Map<String, Boolean> services = new HashMap<>();
        services.put("api", true);
        services.put("database", true); // À améliorer avec une vraie vérification
        services.put("mqtt", false); // À améliorer avec une vraie vérification
        health.put("services", services);

        log.info("Health check requested");
        return ResponseEntity.ok(health);
    }

    /**
     * Ping simple pour tester la connectivité
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Informations sur l'application
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "SPA Control Application");
        info.put("description", "Application de contrôle de spa connecté");
        info.put("version", "0.0.1-SNAPSHOT");
        info.put("build", LocalDateTime.now());

        // Fonctionnalités disponibles
        Map<String, Boolean> features = new HashMap<>();
        features.put("spa-control", true);
        features.put("sessions", true);
        features.put("scenarios", true);
        features.put("configuration", true);
        features.put("real-time-monitoring", true);
        info.put("features", features);

        return ResponseEntity.ok(info);
    }
}