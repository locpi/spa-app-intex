package fr.loicpincon.spaapp.service;

import fr.loicpincon.spaapp.model.Scenario;
import fr.loicpincon.spaapp.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;

    public List<Scenario> getAllScenarios() {
        return scenarioRepository.findAllByOrderByExecutionTime();
    }

    public List<Scenario> getActiveScenarios() {
        return scenarioRepository.findByIsActiveTrueOrderByExecutionTime();
    }

    public Optional<Scenario> getScenarioById(Long id) {
        return scenarioRepository.findById(id);
    }

    public Scenario createScenario(Scenario scenario) {
        validateScenario(scenario);

        if (hasTimeConflict(scenario.getExecutionTime(), scenario.getId())) {
            throw new IllegalArgumentException("Un scénario est déjà programmé à cette heure");
        }

        scenario.setIsActive(true);
        Scenario saved = scenarioRepository.save(scenario);
        log.info("Scénario créé: {} à {}", saved.getName(), saved.getExecutionTime());

        return saved;
    }

    public Scenario updateScenario(Long id, Scenario scenarioUpdate) {
        return scenarioRepository.findById(id)
            .map(existing -> {
                validateScenario(scenarioUpdate);

                if (!existing.getExecutionTime().equals(scenarioUpdate.getExecutionTime()) &&
                    hasTimeConflict(scenarioUpdate.getExecutionTime(), id)) {
                    throw new IllegalArgumentException("Un scénario est déjà programmé à cette heure");
                }

                existing.setName(scenarioUpdate.getName());
                existing.setExecutionTime(scenarioUpdate.getExecutionTime());
                existing.setTargetTemperature(scenarioUpdate.getTargetTemperature());
                existing.setBubblesEnabled(scenarioUpdate.getBubblesEnabled());
                existing.setDurationMinutes(scenarioUpdate.getDurationMinutes());
                existing.setIsActive(scenarioUpdate.getIsActive());

                Scenario saved = scenarioRepository.save(existing);
                log.info("Scénario mis à jour: {}", saved.getName());

                return saved;
            })
            .orElseThrow(() -> new IllegalArgumentException("Scénario non trouvé avec l'ID: " + id));
    }

    public void deleteScenario(Long id) {
        scenarioRepository.findById(id)
            .ifPresentOrElse(
                scenario -> {
                    scenarioRepository.deleteById(id);
                    log.info("Scénario supprimé: {}", scenario.getName());
                },
                () -> {
                    throw new IllegalArgumentException("Scénario non trouvé avec l'ID: " + id);
                }
            );
    }

    public Scenario toggleScenarioStatus(Long id) {
        return scenarioRepository.findById(id)
            .map(scenario -> {
                scenario.setIsActive(!scenario.getIsActive());
                Scenario saved = scenarioRepository.save(scenario);
                log.info("Statut du scénario '{}' changé: {}",
                    saved.getName(),
                    saved.getIsActive() ? "activé" : "désactivé");
                return saved;
            })
            .orElseThrow(() -> new IllegalArgumentException("Scénario non trouvé avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Scenario> getScenariosToExecute(LocalTime currentTime) {
        return scenarioRepository.findByExecutionTimeAndIsActiveTrue(currentTime);
    }

    @Transactional(readOnly = true)
    public List<Scenario> getScenariosForNextHour() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime nextHour = now.plusHours(1);
        return scenarioRepository.findScenariosForNextHour(now, nextHour);
    }

    @Transactional(readOnly = true)
    public List<Scenario> searchScenarios(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllScenarios();
        }
        return scenarioRepository.findByNameContainingIgnoreCase(searchTerm.trim());
    }

    public long countActiveScenarios() {
        return scenarioRepository.countByIsActiveTrue();
    }

    public List<Scenario> getScenariosByTemperatureRange(int minTemp, int maxTemp) {
        return scenarioRepository.findByTemperatureRange(minTemp, maxTemp);
    }

    public List<Scenario> getScenariosWithBubbles() {
        return scenarioRepository.findByBubblesEnabledTrueAndIsActiveTrue();
    }

    @Transactional
    public void cleanupInactiveScenarios() {
        long deleted = scenarioRepository.count() - scenarioRepository.countByIsActiveTrue();
        scenarioRepository.deleteByIsActiveFalse();
        if (deleted > 0) {
            log.info("{} scénarios inactifs supprimés", deleted);
        }
    }

    private void validateScenario(Scenario scenario) {
        if (scenario.getName() == null || scenario.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du scénario est obligatoire");
        }

        if (scenario.getExecutionTime() == null) {
            throw new IllegalArgumentException("L'heure d'exécution est obligatoire");
        }

        if (scenario.getTargetTemperature() == null ||
            scenario.getTargetTemperature() < 20 ||
            scenario.getTargetTemperature() > 42) {
            throw new IllegalArgumentException("La température cible doit être entre 20°C et 42°C");
        }

        if (scenario.getDurationMinutes() != null &&
            (scenario.getDurationMinutes() < 15 || scenario.getDurationMinutes() > 480)) {
            throw new IllegalArgumentException("La durée doit être entre 15 et 480 minutes");
        }
    }

    private boolean hasTimeConflict(LocalTime executionTime, Long excludeId) {
        return scenarioRepository.existsTimeConflict(executionTime, excludeId != null ? excludeId : -1L);
    }
}