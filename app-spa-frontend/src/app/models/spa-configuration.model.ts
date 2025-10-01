export interface SpaConfiguration {
  id?: number;

  // Limites de température
  minTemperature: number;
  maxTemperature: number;

  // Températures par défaut pour les modes rapides
  defaultRelaxTemperature: number;
  defaultEcoTemperature: number;
  defaultHeatTemperature: number;

  // Durées par défaut
  defaultSessionDurationMinutes: number;
  maxSessionDurationMinutes: number;

  // Paramètres de sécurité
  autoShutdownEnabled: boolean;
  autoShutdownDurationMinutes: number;

  // Paramètres MQTT
  mqttBrokerUrl?: string;
  mqttClientId?: string;
  mqttUsername?: string;
  mqttPassword?: string;
  mqttCommandTopic: string;
  mqttStatusTopic: string;
  mqttTemperatureTopic: string;

  // Notifications
  notificationsEnabled: boolean;
  sessionStartNotification: boolean;
  sessionEndNotification: boolean;
  temperatureReachedNotification: boolean;
  errorNotification: boolean;
  maintenanceReminderEnabled: boolean;

  // Métadonnées
  spaName?: string;
  spaModel?: string;
  installationDate?: string; // ISO string

  createdAt?: string; // ISO string
  updatedAt?: string; // ISO string
}

export interface UpdateConfigurationRequest {
  minTemperature?: number;
  maxTemperature?: number;
  defaultRelaxTemperature?: number;
  defaultEcoTemperature?: number;
  defaultHeatTemperature?: number;
  defaultSessionDurationMinutes?: number;
  maxSessionDurationMinutes?: number;
  autoShutdownEnabled?: boolean;
  autoShutdownDurationMinutes?: number;
  mqttBrokerUrl?: string;
  mqttClientId?: string;
  mqttUsername?: string;
  mqttPassword?: string;
  mqttCommandTopic?: string;
  mqttStatusTopic?: string;
  mqttTemperatureTopic?: string;
  notificationsEnabled?: boolean;
  sessionStartNotification?: boolean;
  sessionEndNotification?: boolean;
  temperatureReachedNotification?: boolean;
  errorNotification?: boolean;
  maintenanceReminderEnabled?: boolean;
  spaName?: string;
  spaModel?: string;
  installationDate?: string;
}