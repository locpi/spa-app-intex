package fr.loicpincon.spaapp.service;

import fr.loicpincon.spaapp.model.SpaState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service pour gérer l'état temps réel du spa (NON PERSISTÉ)
 * Les données proviennent du spa via MQTT et sont stockées en mémoire uniquement
 */
@Slf4j
@Service
public class SpaStateService {

    // État actuel en mémoire (volatile)
    private volatile SpaState currentState;

    // Cache des derniers états pour historique court terme (max 100 entrées)
    private final ConcurrentHashMap<LocalDateTime, SpaState> recentStatesCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 100;

    // Lock pour lectures/écritures thread-safe
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public SpaStateService() {
        // Initialisation avec état par défaut (spa hors ligne)
        this.currentState = SpaState.createOfflineState();
    }

    /**
     * Met à jour l'état actuel du spa (appelé lors de réception MQTT)
     */
    public void updateCurrentState(SpaState newState) {
        lock.writeLock().lock();
        try {
            // Validation des données reçues
            if (newState == null) {
                log.warn("Tentative de mise à jour avec un état null - ignoré");
                return;
            }

            if (!newState.isDataConsistent()) {
                log.warn("Données incohérentes reçues du spa - état ignoré: {}", newState);
                return;
            }

            // Mise à jour de l'horodatage
            newState.setLastUpdateTime(LocalDateTime.now());

            // Sauvegarde de l'ancien état dans le cache
            if (currentState != null && currentState.getLastUpdateTime() != null) {
                addToCache(currentState.getLastUpdateTime(), currentState);
            }

            // Mise à jour de l'état actuel
            SpaState previousState = this.currentState;
            this.currentState = newState;

            // Log des changements importants
            logImportantChanges(previousState, newState);

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Récupère l'état actuel du spa
     */
    public SpaState getCurrentState() {
        lock.readLock().lock();
        try {
            return currentState;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Vérifie si le spa est en ligne
     */
    public boolean isSpaOnline() {
        SpaState state = getCurrentState();
        return state != null && Boolean.TRUE.equals(state.getIsOnline());
    }

    /**
     * Vérifie si le spa est en cours de chauffage
     */
    public boolean isSpaHeating() {
        SpaState state = getCurrentState();
        return state != null && state.isHeatingActive();
    }

    /**
     * Vérifie si une session est active
     */
    public boolean hasActiveSession() {
        SpaState state = getCurrentState();
        return state != null && state.hasActiveSession();
    }

    /**
     * Récupère l'ID de la session active (si présente)
     */
    public Long getActiveSessionId() {
        SpaState state = getCurrentState();
        return state != null ? state.getActiveSessionId() : null;
    }

    /**
     * Récupère la température actuelle
     */
    public Double getCurrentTemperature() {
        SpaState state = getCurrentState();
        return state != null ? state.getCurrentTemperature() : null;
    }

    /**
     * Récupère la température cible
     */
    public Integer getTargetTemperature() {
        SpaState state = getCurrentState();
        return state != null ? state.getTargetTemperature() : null;
    }

    /**
     * Vérifie si la température cible est atteinte
     */
    public boolean isTargetTemperatureReached() {
        SpaState state = getCurrentState();
        return state != null && state.isTemperatureReached();
    }

    /**
     * Vérifie si le spa nécessite une attention (erreur, hors ligne, etc.)
     */
    public boolean requiresAttention() {
        SpaState state = getCurrentState();
        return state != null && state.requiresAttention();
    }

    /**
     * Récupère un résumé textuel du statut
     */
    public String getStatusSummary() {
        SpaState state = getCurrentState();
        return state != null ? state.getStatusSummary() : "État inconnu";
    }

    /**
     * Marque le spa comme hors ligne (en cas de perte de connexion MQTT)
     */
    public void markOffline() {
        updateCurrentState(SpaState.createOfflineState());
    }

    /**
     * Récupère l'historique récent des états (pour graphiques/debugging)
     */
    public ConcurrentHashMap<LocalDateTime, SpaState> getRecentStates() {
        return new ConcurrentHashMap<>(recentStatesCache);
    }

    /**
     * Nettoie le cache des anciens états
     */
    private void addToCache(LocalDateTime timestamp, SpaState state) {
        recentStatesCache.put(timestamp, state);

        // Nettoyage si le cache devient trop volumineux
        if (recentStatesCache.size() > MAX_CACHE_SIZE) {
            // Supprime les 10 plus anciennes entrées
            recentStatesCache.entrySet().stream()
                    .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                    .limit(10)
                    .forEach(entry -> recentStatesCache.remove(entry.getKey()));
        }
    }

    /**
     * Log les changements importants d'état
     */
    private void logImportantChanges(SpaState previous, SpaState current) {
        if (previous == null) return;

        // Changement de statut en ligne/hors ligne
        if (!Objects.equals(previous.getIsOnline(), current.getIsOnline())) {
            log.info("Spa {} : {}",
                    Boolean.TRUE.equals(current.getIsOnline()) ? "connecté" : "déconnecté",
                    current.getStatusSummary());
        }

        // Changement d'état de chauffage
        if (!Objects.equals(previous.getIsHeating(), current.getIsHeating())) {
            log.info("Chauffage {} - Température: {}°C → {}°C",
                    Boolean.TRUE.equals(current.getIsHeating()) ? "démarré" : "arrêté",
                    current.getCurrentTemperature(),
                    current.getTargetTemperature());
        }

        // Température cible atteinte
        if (!previous.isTemperatureReached() && current.isTemperatureReached()) {
            log.info("Température cible atteinte: {}°C", current.getCurrentTemperature());
        }

        // Début/Fin de session
        if (!Objects.equals(previous.getActiveSessionId(), current.getActiveSessionId())) {
            if (current.hasActiveSession()) {
                log.info("Session démarrée: ID {}", current.getActiveSessionId());
            } else if (previous.hasActiveSession()) {
                log.info("Session terminée: ID {}", previous.getActiveSessionId());
            }
        }

        // État d'erreur
        if (!Objects.equals(previous.getIsInErrorState(), current.getIsInErrorState())
            && Boolean.TRUE.equals(current.getIsInErrorState())) {
            log.error("Erreur spa détectée: {}", current.getErrorMessage());
        }
    }
}