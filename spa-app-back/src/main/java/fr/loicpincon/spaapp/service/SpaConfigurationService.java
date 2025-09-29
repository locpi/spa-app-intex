package fr.loicpincon.spaapp.service;

import fr.loicpincon.spaapp.model.SpaConfiguration;
import fr.loicpincon.spaapp.repository.SpaConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SpaConfigurationService {

    private final SpaConfigurationRepository configurationRepository;

    @Transactional(readOnly = true)
    public SpaConfiguration getConfiguration() {
        Optional<SpaConfiguration> config = configurationRepository.findFirst();
        if (config.isPresent()) {
            return config.get();
        } else {
            // Créer et retourner la configuration par défaut si aucune n'existe
            log.info("Aucune configuration trouvée, création de la configuration par défaut");
            return createDefaultConfiguration();
        }
    }

    public SpaConfiguration updateConfiguration(SpaConfiguration configUpdate) {
        validateConfiguration(configUpdate);

        SpaConfiguration existingConfig = getConfiguration();

        // Mise à jour de tous les champs
        existingConfig.setSpaName(configUpdate.getSpaName());
        existingConfig.setMinTemperature(configUpdate.getMinTemperature());
        existingConfig.setMaxTemperature(configUpdate.getMaxTemperature());
        existingConfig.setDefaultRelaxTemperature(configUpdate.getDefaultRelaxTemperature());
        existingConfig.setDefaultEcoTemperature(configUpdate.getDefaultEcoTemperature());
        existingConfig.setDefaultHeatTemperature(configUpdate.getDefaultHeatTemperature());
        existingConfig.setDefaultSessionDurationMinutes(configUpdate.getDefaultSessionDurationMinutes());
        existingConfig.setMaxSessionDurationMinutes(configUpdate.getMaxSessionDurationMinutes());
        existingConfig.setAutoShutdownEnabled(configUpdate.getAutoShutdownEnabled());
        existingConfig.setAutoShutdownDurationMinutes(configUpdate.getAutoShutdownDurationMinutes());

        // Configuration MQTT
        existingConfig.setMqttBrokerUrl(configUpdate.getMqttBrokerUrl());
        existingConfig.setMqttUsername(configUpdate.getMqttUsername());
        existingConfig.setMqttPassword(configUpdate.getMqttPassword());
        existingConfig.setMqttCommandTopic(configUpdate.getMqttCommandTopic());
        existingConfig.setMqttStatusTopic(configUpdate.getMqttStatusTopic());
        existingConfig.setMqttTemperatureTopic(configUpdate.getMqttTemperatureTopic());

        // Configuration notifications
        existingConfig.setNotificationsEnabled(configUpdate.getNotificationsEnabled());
        existingConfig.setSessionStartNotification(configUpdate.getSessionStartNotification());
        existingConfig.setSessionEndNotification(configUpdate.getSessionEndNotification());
        existingConfig.setTemperatureReachedNotification(configUpdate.getTemperatureReachedNotification());
        existingConfig.setErrorNotification(configUpdate.getErrorNotification());
        existingConfig.setMaintenanceReminderEnabled(configUpdate.getMaintenanceReminderEnabled());

        SpaConfiguration saved = configurationRepository.save(existingConfig);
        log.info("Configuration mise à jour pour le spa: {}", saved.getSpaName());

        return saved;
    }

    public SpaConfiguration createDefaultConfiguration() {
        // Vérifier qu'il n'existe pas déjà une configuration
        if (configurationRepository.count() > 0) {
            log.warn("Tentative de création d'une configuration par défaut alors qu'une configuration existe déjà");
            return getConfiguration();
        }

        SpaConfiguration defaultConfig = SpaConfigurationRepository.createDefaultConfiguration();
        SpaConfiguration saved = configurationRepository.save(defaultConfig);
        log.info("Configuration par défaut créée pour le spa: {}", saved.getSpaName());

        return saved;
    }

    @Transactional(readOnly = true)
    public boolean isMqttConfigured() {
        return configurationRepository.isMqttConfigured();
    }

    @Transactional(readOnly = true)
    public Optional<SpaConfiguration> findBySpaName(String spaName) {
        return configurationRepository.findBySpaName(spaName);
    }

    @Transactional(readOnly = true)
    public boolean isAutoShutdownEnabled() {
        SpaConfiguration config = getConfiguration();
        return Boolean.TRUE.equals(config.getAutoShutdownEnabled());
    }

    @Transactional(readOnly = true)
    public Integer getAutoShutdownDuration() {
        SpaConfiguration config = getConfiguration();
        return config.getAutoShutdownDurationMinutes();
    }

    @Transactional(readOnly = true)
    public boolean areNotificationsEnabled() {
        SpaConfiguration config = getConfiguration();
        return Boolean.TRUE.equals(config.getNotificationsEnabled());
    }

    @Transactional(readOnly = true)
    public String getMqttBrokerUrl() {
        SpaConfiguration config = getConfiguration();
        return config.getMqttBrokerUrl();
    }

    @Transactional(readOnly = true)
    public String getMqttCommandTopic() {
        SpaConfiguration config = getConfiguration();
        return config.getMqttCommandTopic();
    }

    @Transactional(readOnly = true)
    public String getMqttStatusTopic() {
        SpaConfiguration config = getConfiguration();
        return config.getMqttStatusTopic();
    }

    @Transactional(readOnly = true)
    public String getMqttTemperatureTopic() {
        SpaConfiguration config = getConfiguration();
        return config.getMqttTemperatureTopic();
    }

    public SpaConfiguration updateMqttConfiguration(String brokerUrl, String username, String password) {
        SpaConfiguration config = getConfiguration();

        config.setMqttBrokerUrl(brokerUrl);
        config.setMqttUsername(username);
        config.setMqttPassword(password);

        SpaConfiguration saved = configurationRepository.save(config);
        log.info("Configuration MQTT mise à jour - Broker: {}", brokerUrl);

        return saved;
    }

    public SpaConfiguration updateNotificationSettings(boolean enabled,
                                                       boolean sessionStart,
                                                       boolean sessionEnd,
                                                       boolean temperatureReached,
                                                       boolean error) {
        SpaConfiguration config = getConfiguration();

        config.setNotificationsEnabled(enabled);
        config.setSessionStartNotification(sessionStart);
        config.setSessionEndNotification(sessionEnd);
        config.setTemperatureReachedNotification(temperatureReached);
        config.setErrorNotification(error);

        SpaConfiguration saved = configurationRepository.save(config);
        log.info("Paramètres de notification mis à jour - Activé: {}", enabled);

        return saved;
    }

    public SpaConfiguration updateTemperatureDefaults(int relax, int eco, int heat) {
        validateTemperatureRange(relax, "relax");
        validateTemperatureRange(eco, "éco");
        validateTemperatureRange(heat, "chauffage");

        SpaConfiguration config = getConfiguration();

        config.setDefaultRelaxTemperature(relax);
        config.setDefaultEcoTemperature(eco);
        config.setDefaultHeatTemperature(heat);

        SpaConfiguration saved = configurationRepository.save(config);
        log.info("Températures par défaut mises à jour - Relax: {}°C, Éco: {}°C, Chauffage: {}°C",
                 relax, eco, heat);

        return saved;
    }

    public void resetToDefaults() {
        // Supprimer la configuration existante
        configurationRepository.deleteAll();

        // Créer une nouvelle configuration par défaut
        createDefaultConfiguration();
        log.info("Configuration réinitialisée aux valeurs par défaut");
    }

    @Transactional(readOnly = true)
    public List<SpaConfiguration> getAllConfigurations() {
        return configurationRepository.findAll();
    }

    private void validateConfiguration(SpaConfiguration config) {
        if (config.getSpaName() == null || config.getSpaName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du spa est obligatoire");
        }

        if (config.getMinTemperature() == null || config.getMinTemperature() < 10 || config.getMinTemperature() > 30) {
            throw new IllegalArgumentException("La température minimale doit être entre 10°C et 30°C");
        }

        if (config.getMaxTemperature() == null || config.getMaxTemperature() < 35 || config.getMaxTemperature() > 50) {
            throw new IllegalArgumentException("La température maximale doit être entre 35°C et 50°C");
        }

        if (config.getMinTemperature() >= config.getMaxTemperature()) {
            throw new IllegalArgumentException("La température minimale doit être inférieure à la température maximale");
        }

        validateTemperatureRange(config.getDefaultRelaxTemperature(), "relax");
        validateTemperatureRange(config.getDefaultEcoTemperature(), "éco");
        validateTemperatureRange(config.getDefaultHeatTemperature(), "chauffage");

        if (config.getDefaultSessionDurationMinutes() == null ||
            config.getDefaultSessionDurationMinutes() < 15 ||
            config.getDefaultSessionDurationMinutes() > 480) {
            throw new IllegalArgumentException("La durée par défaut des sessions doit être entre 15 et 480 minutes");
        }

        if (config.getMaxSessionDurationMinutes() == null ||
            config.getMaxSessionDurationMinutes() < config.getDefaultSessionDurationMinutes() ||
            config.getMaxSessionDurationMinutes() > 720) {
            throw new IllegalArgumentException("La durée maximale des sessions doit être entre la durée par défaut et 720 minutes");
        }

        if (config.getAutoShutdownDurationMinutes() != null &&
            (config.getAutoShutdownDurationMinutes() < 30 ||
             config.getAutoShutdownDurationMinutes() > 600)) {
            throw new IllegalArgumentException("La durée d'arrêt automatique doit être entre 30 et 600 minutes");
        }

        // Validation MQTT (optionnelle)
        if (config.getMqttBrokerUrl() != null && !config.getMqttBrokerUrl().trim().isEmpty()) {
            if (!config.getMqttBrokerUrl().startsWith("tcp://") &&
                !config.getMqttBrokerUrl().startsWith("ws://") &&
                !config.getMqttBrokerUrl().startsWith("wss://") &&
                !config.getMqttBrokerUrl().startsWith("ssl://")) {
                throw new IllegalArgumentException("L'URL du broker MQTT doit commencer par tcp://, ws://, wss:// ou ssl://");
            }
        }
    }

    private void validateTemperatureRange(Integer temperature, String type) {
        SpaConfiguration config = getConfiguration();
        if (temperature == null ||
            temperature < config.getMinTemperature() ||
            temperature > config.getMaxTemperature()) {
            throw new IllegalArgumentException(
                String.format("La température %s doit être entre %d°C et %d°C",
                              type, config.getMinTemperature(), config.getMaxTemperature()));
        }
    }
}