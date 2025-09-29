package fr.loicpincon.spaapp.repository;

import fr.loicpincon.spaapp.model.SpaConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpaConfigurationRepository extends JpaRepository<SpaConfiguration, Long> {

    /**
     * Récupère la configuration unique du système (Singleton pattern)
     * Il ne devrait y avoir qu'une seule configuration dans la base
     */
    @Query("SELECT c FROM SpaConfiguration c ORDER BY c.id ASC")
    Optional<SpaConfiguration> findFirst();

    /**
     * Compte le nombre de configurations (devrait être 1)
     */
    @Override
    long count();

    /**
     * Trouve la configuration par nom du spa (si renseigné)
     */
    Optional<SpaConfiguration> findBySpaName(String spaName);

    /**
     * Vérifie si MQTT est configuré
     */
    @Query("SELECT COUNT(c) > 0 FROM SpaConfiguration c WHERE c.mqttBrokerUrl IS NOT NULL AND c.mqttBrokerUrl != ''")
    boolean isMqttConfigured();

    /**
     * Trouve les configurations avec auto-shutdown activé
     */
    @Query("SELECT c FROM SpaConfiguration c WHERE c.autoShutdownEnabled = true")
    Optional<SpaConfiguration> findConfigurationWithAutoShutdown();

    /**
     * Méthode utilitaire pour obtenir ou créer la configuration par défaut
     * Cette méthode sera utilisée dans le service pour implémenter le pattern Singleton
     */
    default SpaConfiguration getOrCreateDefaultConfiguration() {
        return findFirst().orElse(createDefaultConfiguration());
    }

    /**
     * Crée une configuration par défaut
     */
    static SpaConfiguration createDefaultConfiguration() {
        return SpaConfiguration.builder()
                .minTemperature(20)
                .maxTemperature(42)
                .defaultRelaxTemperature(37)
                .defaultEcoTemperature(28)
                .defaultHeatTemperature(40)
                .defaultSessionDurationMinutes(60)
                .maxSessionDurationMinutes(480)
                .autoShutdownEnabled(true)
                .autoShutdownDurationMinutes(240)
                .mqttCommandTopic("spa/command")
                .mqttStatusTopic("spa/status")
                .mqttTemperatureTopic("spa/temperature")
                .notificationsEnabled(true)
                .sessionStartNotification(true)
                .sessionEndNotification(true)
                .temperatureReachedNotification(true)
                .spaName("Mon Spa")
                .build();
    }
}