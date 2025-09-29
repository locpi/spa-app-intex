package fr.loicpincon.spaapp.controller;

import fr.loicpincon.spaapp.model.SpaState;
import fr.loicpincon.spaapp.service.SpaStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/spa/state")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SpaStateController {

    private final SpaStateService spaStateService;

    @GetMapping
    public ResponseEntity<SpaState> getCurrentState() {
        SpaState currentState = spaStateService.getCurrentState();
        return ResponseEntity.ok(currentState);
    }

    @GetMapping("/online")
    public ResponseEntity<Boolean> isSpaOnline() {
        boolean online = spaStateService.isSpaOnline();
        return ResponseEntity.ok(online);
    }

    @GetMapping("/heating")
    public ResponseEntity<Boolean> isSpaHeating() {
        boolean heating = spaStateService.isSpaHeating();
        return ResponseEntity.ok(heating);
    }

    @GetMapping("/session/active")
    public ResponseEntity<Boolean> hasActiveSession() {
        boolean hasSession = spaStateService.hasActiveSession();
        return ResponseEntity.ok(hasSession);
    }

    @GetMapping("/session/id")
    public ResponseEntity<Long> getActiveSessionId() {
        Long sessionId = spaStateService.getActiveSessionId();
        if (sessionId != null) {
            return ResponseEntity.ok(sessionId);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/temperature/current")
    public ResponseEntity<Double> getCurrentTemperature() {
        Double temperature = spaStateService.getCurrentTemperature();
        if (temperature != null) {
            return ResponseEntity.ok(temperature);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/temperature/target")
    public ResponseEntity<Integer> getTargetTemperature() {
        Integer temperature = spaStateService.getTargetTemperature();
        if (temperature != null) {
            return ResponseEntity.ok(temperature);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/temperature/reached")
    public ResponseEntity<Boolean> isTargetTemperatureReached() {
        boolean reached = spaStateService.isTargetTemperatureReached();
        return ResponseEntity.ok(reached);
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatusSummary() {
        String status = spaStateService.getStatusSummary();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/attention")
    public ResponseEntity<Boolean> requiresAttention() {
        boolean attention = spaStateService.requiresAttention();
        return ResponseEntity.ok(attention);
    }

    @GetMapping("/history")
    public ResponseEntity<ConcurrentHashMap<LocalDateTime, SpaState>> getRecentStates() {
        ConcurrentHashMap<LocalDateTime, SpaState> history = spaStateService.getRecentStates();
        return ResponseEntity.ok(history);
    }

    @PostMapping("/offline")
    public ResponseEntity<Void> markOffline() {
        spaStateService.markOffline();
        log.info("Spa marqué comme hors ligne via API");
        return ResponseEntity.ok().build();
    }

    // Endpoint pour la mise à jour via MQTT (usage interne)
    @PostMapping("/update")
    public ResponseEntity<Void> updateState(@RequestBody SpaState newState) {
        try {
            spaStateService.updateCurrentState(newState);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'état du spa: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoints pour des vérifications rapides (utiles pour monitoring)
    @GetMapping("/health")
    public ResponseEntity<String> getHealthStatus() {
        SpaState state = spaStateService.getCurrentState();
        if (state == null) {
            return ResponseEntity.status(503).body("État du spa indisponible");
        }

        if (spaStateService.requiresAttention()) {
            return ResponseEntity.status(503).body("Le spa nécessite une attention");
        }

        return ResponseEntity.ok("OK");
    }

    @GetMapping("/quick-status")
    public ResponseEntity<QuickStatusResponse> getQuickStatus() {
        SpaState state = spaStateService.getCurrentState();

        QuickStatusResponse response = QuickStatusResponse.builder()
            .online(spaStateService.isSpaOnline())
            .heating(spaStateService.isSpaHeating())
            .currentTemperature(spaStateService.getCurrentTemperature())
            .targetTemperature(spaStateService.getTargetTemperature())
            .hasActiveSession(spaStateService.hasActiveSession())
            .activeSessionId(spaStateService.getActiveSessionId())
            .requiresAttention(spaStateService.requiresAttention())
            .statusSummary(spaStateService.getStatusSummary())
            .lastUpdate(state != null ? state.getLastUpdateTime() : null)
            .build();

        return ResponseEntity.ok(response);
    }

    // Classe interne pour le statut rapide
    @lombok.Data
    @lombok.Builder
    public static class QuickStatusResponse {
        private boolean online;
        private boolean heating;
        private Double currentTemperature;
        private Integer targetTemperature;
        private boolean hasActiveSession;
        private Long activeSessionId;
        private boolean requiresAttention;
        private String statusSummary;
        private LocalDateTime lastUpdate;
    }
}