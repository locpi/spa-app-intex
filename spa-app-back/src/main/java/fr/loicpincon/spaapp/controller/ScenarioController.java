package fr.loicpincon.spaapp.controller;

import fr.loicpincon.spaapp.model.Scenario;
import fr.loicpincon.spaapp.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class ScenarioController {

    private final ScenarioService scenarioService;

    @GetMapping
    public ResponseEntity<List<Scenario>> getAllScenarios() {
        List<Scenario> scenarios = scenarioService.getAllScenarios();
        return ResponseEntity.ok(scenarios);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Scenario>> getActiveScenarios() {
        List<Scenario> activeScenarios = scenarioService.getActiveScenarios();
        return ResponseEntity.ok(activeScenarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Scenario> getScenarioById(@PathVariable Long id) {
        return scenarioService.getScenarioById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Scenario> createScenario(@Valid @RequestBody Scenario scenario) {
        try {
            Scenario created = scenarioService.createScenario(scenario);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la création du scénario: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Scenario> updateScenario(@PathVariable Long id, @Valid @RequestBody Scenario scenario) {
        try {
            Scenario updated = scenarioService.updateScenario(id, scenario);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la mise à jour du scénario {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScenario(@PathVariable Long id) {
        try {
            scenarioService.deleteScenario(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors de la suppression du scénario {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Scenario> toggleScenarioStatus(@PathVariable Long id) {
        try {
            Scenario toggled = scenarioService.toggleScenarioStatus(id);
            return ResponseEntity.ok(toggled);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur lors du changement de statut du scénario {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/execute/{time}")
    public ResponseEntity<List<Scenario>> getScenariosToExecute(@PathVariable String time) {
        try {
            LocalTime executionTime = LocalTime.parse(time);
            List<Scenario> scenarios = scenarioService.getScenariosToExecute(executionTime);
            return ResponseEntity.ok(scenarios);
        } catch (Exception e) {
            log.warn("Erreur lors de la récupération des scénarios à exécuter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/next-hour")
    public ResponseEntity<List<Scenario>> getScenariosForNextHour() {
        List<Scenario> scenarios = scenarioService.getScenariosForNextHour();
        return ResponseEntity.ok(scenarios);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Scenario>> searchScenarios(@RequestParam String q) {
        List<Scenario> scenarios = scenarioService.searchScenarios(q);
        return ResponseEntity.ok(scenarios);
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveScenarios() {
        long count = scenarioService.countActiveScenarios();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/temperature-range")
    public ResponseEntity<List<Scenario>> getScenariosByTemperatureRange(
            @RequestParam int minTemp,
            @RequestParam int maxTemp) {
        List<Scenario> scenarios = scenarioService.getScenariosByTemperatureRange(minTemp, maxTemp);
        return ResponseEntity.ok(scenarios);
    }

    @GetMapping("/with-bubbles")
    public ResponseEntity<List<Scenario>> getScenariosWithBubbles() {
        List<Scenario> scenarios = scenarioService.getScenariosWithBubbles();
        return ResponseEntity.ok(scenarios);
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupInactiveScenarios() {
        scenarioService.cleanupInactiveScenarios();
        return ResponseEntity.ok().build();
    }
}