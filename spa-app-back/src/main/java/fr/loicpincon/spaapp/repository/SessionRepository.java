package fr.loicpincon.spaapp.repository;

import fr.loicpincon.spaapp.model.Session;
import fr.loicpincon.spaapp.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    /**
     * Trouve toutes les sessions triées par date de début programmée (les plus récentes en premier)
     */
    List<Session> findAllByOrderByScheduledStartTimeDesc();

    /**
     * Trouve les sessions par statut
     */
    List<Session> findByStatusOrderByScheduledStartTimeDesc(SessionStatus status);

    /**
     * Trouve la session actuellement active (il ne devrait y en avoir qu'une seule)
     */
    Optional<Session> findFirstByStatus(SessionStatus status);

    /**
     * Trouve les sessions programmées dans une période donnée
     */
    @Query("SELECT s FROM Session s WHERE s.scheduledStartTime BETWEEN :startTime AND :endTime ORDER BY s.scheduledStartTime")
    List<Session> findSessionsInPeriod(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * Trouve les sessions à démarrer maintenant (dans les 5 prochaines minutes)
     */
    @Query("SELECT s FROM Session s WHERE s.status = 'SCHEDULED' AND s.scheduledStartTime BETWEEN :now AND :fiveMinutesLater ORDER BY s.scheduledStartTime")
    List<Session> findSessionsToStart(@Param("now") LocalDateTime now, @Param("fiveMinutesLater") LocalDateTime fiveMinutesLater);

    /**
     * Trouve les sessions actives qui devraient être terminées
     */
    @Query("SELECT s FROM Session s WHERE s.status = 'ACTIVE' AND s.actualStartTime IS NOT NULL AND " +
           "FUNCTION('TIMESTAMPDIFF', MINUTE, s.actualStartTime, :currentTime) >= s.durationMinutes")
    List<Session> findActiveSessionsToComplete(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Trouve les sessions programmées pour aujourd'hui
     */
    @Query("SELECT s FROM Session s WHERE DATE(s.scheduledStartTime) = DATE(:today) ORDER BY s.scheduledStartTime")
    List<Session> findTodaySessions(@Param("today") LocalDateTime today);

    /**
     * Trouve les sessions programmées pour demain
     */
    @Query("SELECT s FROM Session s WHERE DATE(s.scheduledStartTime) = DATE(:tomorrow) ORDER BY s.scheduledStartTime")
    List<Session> findTomorrowSessions(@Param("tomorrow") LocalDateTime tomorrow);

    /**
     * Compte les sessions par statut
     */
    long countByStatus(SessionStatus status);

    /**
     * Trouve les sessions récemment terminées (pour historique)
     */
    @Query("SELECT s FROM Session s WHERE s.status = 'COMPLETED' AND s.actualEndTime >= :since ORDER BY s.actualEndTime DESC")
    List<Session> findRecentCompletedSessions(@Param("since") LocalDateTime since);

    /**
     * Trouve les sessions par nom (recherche partielle)
     */
    List<Session> findByNameContainingIgnoreCaseOrderByScheduledStartTimeDesc(String name);

    /**
     * Vérifie s'il y a conflit de planning (chevauchement de sessions)
     */
    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.status IN ('SCHEDULED', 'ACTIVE') AND s.id != :excludeId AND " +
           "((s.scheduledStartTime <= :endTime AND FUNCTION('DATE_ADD', s.scheduledStartTime, INTERVAL s.durationMinutes MINUTE) > :startTime))")
    boolean existsScheduleConflict(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   @Param("excludeId") Long excludeId);

    /**
     * Statistiques : durée moyenne des sessions terminées
     */
    @Query("SELECT AVG(s.durationMinutes) FROM Session s WHERE s.status = 'COMPLETED' AND s.actualStartTime IS NOT NULL AND s.actualEndTime IS NOT NULL")
    Optional<Double> findAverageSessionDuration();

    /**
     * Trouve les sessions avec température spécifique (pour statistiques)
     */
    List<Session> findByTargetTemperatureOrderByScheduledStartTimeDesc(Integer targetTemperature);

    /**
     * Supprime les anciennes sessions terminées ou annulées (plus de 6 mois)
     */
    @Query("DELETE FROM Session s WHERE s.status IN ('COMPLETED', 'CANCELLED') AND s.scheduledStartTime < :sixMonthsAgo")
    void deleteOldSessions(@Param("sixMonthsAgo") LocalDateTime sixMonthsAgo);

    /**
     * Trouve les sessions les plus utilisées (par température et durée)
     */
    @Query("SELECT s.targetTemperature, s.durationMinutes, COUNT(s) as usage_count FROM Session s " +
           "WHERE s.status = 'COMPLETED' GROUP BY s.targetTemperature, s.durationMinutes " +
           "ORDER BY COUNT(s) DESC")
    List<Object[]> findMostUsedSessionConfigurations();
}