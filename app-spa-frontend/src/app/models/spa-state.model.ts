export interface SpaState {
  // État actuel de la température
  currentTemperature?: number;
  targetTemperature?: number;
  isHeating?: boolean;

  // État des équipements
  bubblesActive?: boolean;
  filterActive?: boolean;
  lightsActive?: boolean;

  // État général du système
  isOnline?: boolean;
  isInErrorState?: boolean;
  errorMessage?: string;

  // Session en cours
  activeSessionId?: number;
  sessionStartTime?: string; // ISO string
  sessionRemainingMinutes?: number;

  // Informations système
  firmwareVersion?: string;
  powerConsumption?: number;
  lastUpdateTime?: string; // ISO string
  wifiSignalStrength?: number;
}

export interface SpaCommand {
  action: 'SET_TEMPERATURE' | 'START_HEATING' | 'STOP_HEATING' | 'TOGGLE_BUBBLES' | 'TOGGLE_FILTER' | 'TOGGLE_LIGHTS';
  value?: number;
  sessionId?: number;
}