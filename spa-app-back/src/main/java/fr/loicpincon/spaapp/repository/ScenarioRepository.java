package fr.loicpincon.spaapp.repository;

import fr.loicpincon.spaapp.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    /**
     * Trouve tous les scénarios actifs triés par heure d'exécution
     */
    List<Scenario> findByIsActiveTrueOrderByExecutionTime();

    /**
     * Trouve tous les scénarios (actifs et inactifs) triés par heure d'exécution
     */
    List<Scenario> findAllByOrderByExecutionTime();

    /**
     * Trouve les scénarios à exécuter à une heure donnée (actifs uniquement)
     */
    List<Scenario> findByExecutionTimeAndIsActiveTrue(LocalTime executionTime);

    /**
     * Trouve les scénarios par nom (recherche insensible à la casse)
     */
    List<Scenario> findByNameContainingIgnoreCase(String name);

    /**
     * Trouve un scénario par nom exact (actif uniquement)
     */
    Optional<Scenario> findByNameAndIsActiveTrue(String name);

    /**
     * Compte le nombre de scénarios actifs
     */
    long countByIsActiveTrue();

    /**
     * Trouve les scénarios avec durée limitée (pour monitoring)
     */
    @Query("SELECT s FROM Scenario s WHERE s.durationMinutes IS NOT NULL AND s.isActive = true ORDER BY s.executionTime")
    List<Scenario> findActiveScenariosWithDuration();

    /**
     * Trouve les scénarios qui activent les bulles (pour statistiques)
     */
    List<Scenario> findByBubblesEnabledTrueAndIsActiveTrue();

    /**
     * Trouve les scénarios dans une plage de températures
     */
    @Query("SELECT s FROM Scenario s WHERE s.targetTemperature BETWEEN :minTemp AND :maxTemp AND s.isActive = true ORDER BY s.targetTemperature")
    List<Scenario> findByTemperatureRange(int minTemp, int maxTemp);

    /**
     * Trouve les scénarios qui doivent s'exécuter dans la prochaine heure
     */
    @Query("SELECT s FROM Scenario s WHERE s.isActive = true AND " +
           "(s.executionTime BETWEEN :currentTime AND :nextHour OR " +
           "(s.executionTime >= :currentTime AND :nextHour < s.executionTime))")
    List<Scenario> findScenariosForNextHour(LocalTime currentTime, LocalTime nextHour);

    /**
     * Vérifie s'il existe un conflit d'horaire (même heure d'exécution)
     */
    @Query("SELECT COUNT(s) > 0 FROM Scenario s WHERE s.executionTime = :executionTime AND s.isActive = true AND s.id != :excludeId")
    boolean existsTimeConflict(LocalTime executionTime, Long excludeId);

    /**
     * Supprime tous les scénarios inactifs (nettoyage)
     */
    void deleteByIsActiveFalse();
}