import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { SpaConfiguration, UpdateConfigurationRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class SpaConfigurationService {
  private configurationSubject = new BehaviorSubject<SpaConfiguration | null>(null);
  public configuration$ = this.configurationSubject.asObservable();

  constructor(private apiService: ApiService) {
    this.loadConfiguration();
  }

  /**
   * Charge la configuration actuelle
   */
  loadConfiguration(): Observable<SpaConfiguration> {
    return this.apiService.get<SpaConfiguration>('/configuration')
      .pipe(
        tap(config => this.configurationSubject.next(config))
      );
  }

  /**
   * Obtient la configuration actuelle
   */
  getConfiguration(): Observable<SpaConfiguration | null> {
    return this.configuration$;
  }

  /**
   * Met à jour la configuration
   */
  updateConfiguration(configData: UpdateConfigurationRequest): Observable<SpaConfiguration> {
    return this.apiService.put<SpaConfiguration>('/configuration', configData)
      .pipe(
        tap(config => this.configurationSubject.next(config))
      );
  }

  /**
   * Réinitialise la configuration aux valeurs par défaut
   */
  resetToDefaults(): Observable<SpaConfiguration> {
    return this.apiService.post<SpaConfiguration>('/configuration/reset', {})
      .pipe(
        tap(config => this.configurationSubject.next(config))
      );
  }

  /**
   * Teste la connexion MQTT
   */
  testMqttConnection(): Observable<any> {
    return this.apiService.post('/configuration/test-mqtt', {});
  }

  /**
   * Obtient la configuration actuelle sans faire d'appel API
   */
  getCurrentConfiguration(): SpaConfiguration | null {
    return this.configurationSubject.value;
  }

  /**
   * Vérifie si la température est valide selon la configuration
   */
  isTemperatureValid(temperature: number): boolean {
    const config = this.getCurrentConfiguration();
    if (!config) return false;
    return temperature >= config.minTemperature && temperature <= config.maxTemperature;
  }

  /**
   * Vérifie si la durée est valide selon la configuration
   */
  isDurationValid(durationMinutes: number): boolean {
    const config = this.getCurrentConfiguration();
    if (!config) return false;
    return durationMinutes > 0 && durationMinutes <= config.maxSessionDurationMinutes;
  }

  /**
   * Obtient la température par défaut pour le mode détente
   */
  getRelaxTemperature(): number {
    const config = this.getCurrentConfiguration();
    return config?.defaultRelaxTemperature || 37;
  }

  /**
   * Obtient la température par défaut pour le mode éco
   */
  getEcoTemperature(): number {
    const config = this.getCurrentConfiguration();
    return config?.defaultEcoTemperature || 28;
  }

  /**
   * Obtient la température par défaut pour le mode chauffage
   */
  getHeatTemperature(): number {
    const config = this.getCurrentConfiguration();
    return config?.defaultHeatTemperature || 40;
  }

  /**
   * Obtient la durée par défaut des sessions
   */
  getDefaultSessionDuration(): number {
    const config = this.getCurrentConfiguration();
    return config?.defaultSessionDurationMinutes || 60;
  }

  /**
   * Vérifie si l'arrêt automatique est activé
   */
  isAutoShutdownEnabled(): boolean {
    const config = this.getCurrentConfiguration();
    return config?.autoShutdownEnabled === true;
  }

  /**
   * Vérifie si les notifications sont activées
   */
  areNotificationsEnabled(): boolean {
    const config = this.getCurrentConfiguration();
    return config?.notificationsEnabled === true;
  }

  /**
   * Vérifie si MQTT est configuré
   */
  isMqttConfigured(): boolean {
    const config = this.getCurrentConfiguration();
    return !!(config?.mqttBrokerUrl && config.mqttBrokerUrl.trim());
  }
}