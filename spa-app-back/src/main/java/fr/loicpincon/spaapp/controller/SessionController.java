package fr.loicpincon.spaapp.controller;

import fr.loicpincon.spaapp.model.Session;
import fr.loicpincon.spaapp.model.SessionStatus;
import fr.loicpincon.spaapp.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<Session>> getAllSessions() {
        List<Session> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Session>> getSessionsByStatus(@PathVariable SessionStatus status) {
        List<Session> sessions = sessionService.getSessionsByStatus(status);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Session> getSessionById(@PathVariable Long id) {
        return sessionService.getSessionById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<Session> getActiveSession() {
        Optional<Session> activeSession = sessionService.getActiveSession();
        return activeSession
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<Session> createSession(@Valid @RequestBody Session session) {
        try {
            Session created = sessionService.createSession(session);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la création de la session: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Session> updateSession(@PathVariable Long id, @Valid @RequestBody Session session) {
        try {
            Session updated = sessionService.updateSession(id, session);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la mise à jour de la session {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        try {
            sessionService.deleteSession(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la suppression de la session {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<Session> startSession(@PathVariable Long id) {
        try {
            Session started = sessionService.startSession(id);
            return ResponseEntity.ok(started);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors du démarrage de la session {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Session> completeSession(@PathVariable Long id) {
        try {
            Session completed = sessionService.completeSession(id);
            return ResponseEntity.ok(completed);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la finalisation de la session {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Session> cancelSession(@PathVariable Long id) {
        try {
            Session cancelled = sessionService.cancelSession(id);
            return ResponseEntity.ok(cancelled);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de l'annulation de la session {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/to-start")
    public ResponseEntity<List<Session>> getSessionsToStart() {
        List<Session> sessions = sessionService.getSessionsToStart();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/to-complete")
    public ResponseEntity<List<Session>> getActiveSessionsToComplete() {
        List<Session> sessions = sessionService.getActiveSessionsToComplete();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/today")
    public ResponseEntity<List<Session>> getTodaySessions() {
        List<Session> sessions = sessionService.getTodaySessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/tomorrow")
    public ResponseEntity<List<Session>> getTomorrowSessions() {
        List<Session> sessions = sessionService.getTomorrowSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Session>> getRecentCompletedSessions(@RequestParam(defaultValue = "7") int days) {
        List<Session> sessions = sessionService.getRecentCompletedSessions(days);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Session>> searchSessions(@RequestParam String q) {
        List<Session> sessions = sessionService.searchSessions(q);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/count/{status}")
    public ResponseEntity<Long> countSessionsByStatus(@PathVariable SessionStatus status) {
        long count = sessionService.countSessionsByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/average-duration")
    public ResponseEntity<Double> getAverageSessionDuration() {
        Optional<Double> average = sessionService.getAverageSessionDuration();
        return average
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/temperature/{temp}")
    public ResponseEntity<List<Session>> getSessionsByTemperature(@PathVariable Integer temp) {
        List<Session> sessions = sessionService.getSessionsByTemperature(temp);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/statistics/most-used")
    public ResponseEntity<List<Object[]>> getMostUsedConfigurations() {
        List<Object[]> configurations = sessionService.getMostUsedConfigurations();
        return ResponseEntity.ok(configurations);
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupOldSessions() {
        sessionService.cleanupOldSessions();
        return ResponseEntity.ok().build();
    }
}