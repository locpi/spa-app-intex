package fr.loicpincon.spaapp.service;

import fr.loicpincon.spaapp.model.Session;
import fr.loicpincon.spaapp.model.SessionStatus;
import fr.loicpincon.spaapp.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SpaStateService spaStateService;

    public List<Session> getAllSessions() {
        return sessionRepository.findAllByOrderByScheduledStartTimeDesc();
    }

    public List<Session> getSessionsByStatus(SessionStatus status) {
        return sessionRepository.findByStatusOrderByScheduledStartTimeDesc(status);
    }

    public Optional<Session> getSessionById(Long id) {
        return sessionRepository.findById(id);
    }

    public Optional<Session> getActiveSession() {
        return sessionRepository.findFirstByStatus(SessionStatus.ACTIVE);
    }

    public Session createSession(Session session) {
        validateSession(session);

        if (hasScheduleConflict(session.getScheduledStartTime(),
                               session.getScheduledStartTime().plusMinutes(session.getDurationMinutes()),
                               session.getId())) {
            throw new IllegalArgumentException("Conflit d'horaire avec une autre session");
        }

        session.setStatus(SessionStatus.SCHEDULED);
        session.setCreatedAt(LocalDateTime.now());

        Session saved = sessionRepository.save(session);
        log.info("Session créée: '{}' programmée pour {}",
                 saved.getName(), saved.getScheduledStartTime());

        return saved;
    }

    public Session updateSession(Long id, Session sessionUpdate) {
        return sessionRepository.findById(id)
            .map(existing -> {
                if (existing.getStatus() == SessionStatus.ACTIVE) {
                    throw new IllegalArgumentException("Impossible de modifier une session active");
                }
                if (existing.getStatus() == SessionStatus.COMPLETED) {
                    throw new IllegalArgumentException("Impossible de modifier une session terminée");
                }

                validateSession(sessionUpdate);

                LocalDateTime newEndTime = sessionUpdate.getScheduledStartTime()
                    .plusMinutes(sessionUpdate.getDurationMinutes());

                if (!existing.getScheduledStartTime().equals(sessionUpdate.getScheduledStartTime()) ||
                    !existing.getDurationMinutes().equals(sessionUpdate.getDurationMinutes())) {

                    if (hasScheduleConflict(sessionUpdate.getScheduledStartTime(), newEndTime, id)) {
                        throw new IllegalArgumentException("Conflit d'horaire avec une autre session");
                    }
                }

                existing.setName(sessionUpdate.getName());
                existing.setScheduledStartTime(sessionUpdate.getScheduledStartTime());
                existing.setTargetTemperature(sessionUpdate.getTargetTemperature());
                existing.setDurationMinutes(sessionUpdate.getDurationMinutes());
                existing.setBubblesEnabled(sessionUpdate.getBubblesEnabled());

                Session saved = sessionRepository.save(existing);
                log.info("Session mise à jour: '{}'", saved.getName());

                return saved;
            })
            .orElseThrow(() -> new IllegalArgumentException("Session non trouvée avec l'ID: " + id));
    }

    public void deleteSession(Long id) {
        sessionRepository.findById(id)
            .ifPresentOrElse(
                session -> {
                    if (session.getStatus() == SessionStatus.ACTIVE) {
                        throw new IllegalArgumentException("Impossible de supprimer une session active");
                    }
                    sessionRepository.deleteById(id);
                    log.info("Session supprimée: '{}'", session.getName());
                },
                () -> {
                    throw new IllegalArgumentException("Session non trouvée avec l'ID: " + id);
                }
            );
    }

    public Session startSession(Long id) {
        return sessionRepository.findById(id)
            .map(session -> {
                if (session.getStatus() != SessionStatus.SCHEDULED) {
                    throw new IllegalArgumentException("Seules les sessions programmées peuvent être démarrées");
                }

                if (getActiveSession().isPresent()) {
                    throw new IllegalArgumentException("Une session est déjà active");
                }

                session.setStatus(SessionStatus.ACTIVE);
                session.setActualStartTime(LocalDateTime.now());

                Session saved = sessionRepository.save(session);

                // Mettre à jour l'état du spa pour refléter la session active
                spaStateService.getCurrentState().setActiveSessionId(saved.getId());
                spaStateService.getCurrentState().setSessionStartTime(saved.getActualStartTime());
                spaStateService.getCurrentState().setSessionRemainingMinutes(saved.getDurationMinutes());

                log.info("Session démarrée: '{}' (ID: {})", saved.getName(), saved.getId());

                return saved;
            })
            .orElseThrow(() -> new IllegalArgumentException("Session non trouvée avec l'ID: " + id));
    }

    public Session completeSession(Long id) {
        return sessionRepository.findById(id)
            .map(session -> {
                if (session.getStatus() != SessionStatus.ACTIVE) {
                    throw new IllegalArgumentException("Seules les sessions actives peuvent être terminées");
                }

                session.setStatus(SessionStatus.COMPLETED);
                session.setActualEndTime(LocalDateTime.now());

                Session saved = sessionRepository.save(session);

                // Nettoyer l'état du spa
                spaStateService.getCurrentState().setActiveSessionId(null);
                spaStateService.getCurrentState().setSessionStartTime(null);
                spaStateService.getCurrentState().setSessionRemainingMinutes(null);

                log.info("Session terminée: '{}' (durée réelle: {} min)",
                         saved.getName(),
                         java.time.Duration.between(saved.getActualStartTime(), saved.getActualEndTime()).toMinutes());

                return saved;
            })
            .orElseThrow(() -> new IllegalArgumentException("Session non trouvée avec l'ID: " + id));
    }

    public Session cancelSession(Long id) {
        return sessionRepository.findById(id)
            .map(session -> {
                if (session.getStatus() == SessionStatus.COMPLETED) {
                    throw new IllegalArgumentException("Impossible d'annuler une session terminée");
                }

                SessionStatus previousStatus = session.getStatus();
                session.setStatus(SessionStatus.CANCELLED);

                if (previousStatus == SessionStatus.ACTIVE) {
                    session.setActualEndTime(LocalDateTime.now());

                    // Nettoyer l'état du spa
                    spaStateService.getCurrentState().setActiveSessionId(null);
                    spaStateService.getCurrentState().setSessionStartTime(null);
                    spaStateService.getCurrentState().setSessionRemainingMinutes(null);
                }

                Session saved = sessionRepository.save(session);
                log.info("Session annulée: '{}'", saved.getName());

                return saved;
            })
            .orElseThrow(() -> new IllegalArgumentException("Session non trouvée avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Session> getSessionsToStart() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesLater = now.plusMinutes(5);
        return sessionRepository.findSessionsToStart(now, fiveMinutesLater);
    }

    @Transactional(readOnly = true)
    public List<Session> getActiveSessionsToComplete() {
        return sessionRepository.findActiveSessionsToComplete(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Session> getTodaySessions() {
        return sessionRepository.findTodaySessions(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Session> getTomorrowSessions() {
        return sessionRepository.findTomorrowSessions(LocalDateTime.now().plusDays(1));
    }

    @Transactional(readOnly = true)
    public List<Session> getRecentCompletedSessions(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return sessionRepository.findRecentCompletedSessions(since);
    }

    @Transactional(readOnly = true)
    public List<Session> searchSessions(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllSessions();
        }
        return sessionRepository.findByNameContainingIgnoreCaseOrderByScheduledStartTimeDesc(searchTerm.trim());
    }

    public long countSessionsByStatus(SessionStatus status) {
        return sessionRepository.countByStatus(status);
    }

    public Optional<Double> getAverageSessionDuration() {
        return sessionRepository.findAverageSessionDuration();
    }

    public List<Session> getSessionsByTemperature(Integer targetTemperature) {
        return sessionRepository.findByTargetTemperatureOrderByScheduledStartTimeDesc(targetTemperature);
    }

    public List<Object[]> getMostUsedConfigurations() {
        return sessionRepository.findMostUsedSessionConfigurations();
    }

    @Transactional
    public void cleanupOldSessions() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        sessionRepository.deleteOldSessions(sixMonthsAgo);
        log.info("Nettoyage des anciennes sessions (> 6 mois) effectué");
    }

    private void validateSession(Session session) {
        if (session.getName() == null || session.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la session est obligatoire");
        }

        if (session.getScheduledStartTime() == null) {
            throw new IllegalArgumentException("L'heure de début est obligatoire");
        }

        if (session.getScheduledStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("L'heure de début ne peut pas être dans le passé");
        }

        if (session.getTargetTemperature() == null ||
            session.getTargetTemperature() < 20 ||
            session.getTargetTemperature() > 42) {
            throw new IllegalArgumentException("La température cible doit être entre 20°C et 42°C");
        }

        if (session.getDurationMinutes() == null ||
            session.getDurationMinutes() < 15 ||
            session.getDurationMinutes() > 480) {
            throw new IllegalArgumentException("La durée doit être entre 15 et 480 minutes");
        }
    }

    private boolean hasScheduleConflict(LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        return sessionRepository.existsScheduleConflict(startTime, endTime, excludeId != null ? excludeId : -1L);
    }
}