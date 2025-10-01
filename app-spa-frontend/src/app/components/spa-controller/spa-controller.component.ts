import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import {
  AppStateService,
  SpaStateService,
  SessionService,
  ScenarioService,
  SpaConfigurationService
} from '../../services';

import {
  SpaState,
  Session,
  Scenario,
  SpaConfiguration,
  CreateSessionRequest,
  CreateScenarioRequest,
  SessionStatus
} from '../../models';

@Component({
  selector: 'app-spa-controller',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './spa-controller.component.html',
  styleUrls: ['./spa-controller.component.css']
})
export class SpaControllerComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  // État réactif de l'application
  appState = signal<any>(null);
  spaState = signal<SpaState | null>(null);
  sessions = signal<Session[]>([]);
  scenarios = signal<Scenario[]>([]);
  configuration = signal<SpaConfiguration | null>(null);

  // États dérivés
  isOnline = computed(() => this.spaState()?.isOnline === true);
  hasError = computed(() => this.spaState()?.isInErrorState === true);
  isHeating = computed(() => this.spaState()?.isHeating === true);
  temperature = computed(() => this.spaState()?.currentTemperature || 0);
  currentTemperature = computed(() => this.spaState()?.currentTemperature || 0);
  targetTemperature = computed(() => this.spaState()?.targetTemperature || 0);
  isBubblesOn = computed(() => this.spaState()?.bubblesActive === true);
  isFilterOn = computed(() => this.spaState()?.filterActive === true);

  // Mode automation (pour compatibilité avec le template)
  automationMode = signal(false);

  // Variables pour les formulaires
  newScenario: CreateScenarioRequest = {
    name: '',
    executionTime: '',
    targetTemperature: 37,
    bubblesEnabled: false,
    isRecurring: true
  };

  newSession: CreateSessionRequest = {
    name: '',
    scheduledStartTime: '',
    targetTemperature: 37,
    durationMinutes: 60,
    bubblesEnabled: false
  };

  // Propriétés pour compatibilité avec le template existant
  get bubblesOn() { return this.newSession.bubblesEnabled; }
  set bubblesOn(value: boolean) { this.newSession.bubblesEnabled = value; }

  constructor(
    private appStateService: AppStateService,
    private spaStateService: SpaStateService,
    private sessionService: SessionService,
    private scenarioService: ScenarioService,
    private configurationService: SpaConfigurationService
  ) {
    this.initializeNewSession();
  }

  ngOnInit() {
    // Initialise l'application
    this.appStateService.initializeApp();

    // S'abonne aux changements d'état
    this.appStateService.appState$
      .pipe(takeUntil(this.destroy$))
      .subscribe(state => {
        this.appState.set(state);
        this.spaState.set(state.spaState);
        this.sessions.set(state.sessions);
        this.scenarios.set(state.scenarios);
        this.configuration.set(state.configuration);
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Contrôles du spa
  async setTargetTemperature(temperature: number) {
    if (!this.isValidTemperature(temperature)) {
      this.showError('Température invalide');
      return;
    }

    try {
      await this.spaStateService.setTargetTemperature(temperature).toPromise();
      this.showSuccess('Température cible définie');
    } catch (error) {
      this.showError('Erreur lors de la définition de la température');
    }
  }

  async toggleHeating() {
    try {
      if (this.isHeating()) {
        await this.spaStateService.stopHeating().toPromise();
        this.showSuccess('Chauffage arrêté');
      } else {
        await this.spaStateService.startHeating().toPromise();
        this.showSuccess('Chauffage démarré');
      }
    } catch (error) {
      this.showError('Erreur lors du contrôle du chauffage');
    }
  }

  async toggleBubbles() {
    try {
      await this.spaStateService.toggleBubbles().toPromise();
      this.showSuccess(this.isBubblesOn() ? 'Bulles désactivées' : 'Bulles activées');
    } catch (error) {
      this.showError('Erreur lors du contrôle des bulles');
    }
  }

  async toggleFilter() {
    try {
      await this.spaStateService.toggleFilter().toPromise();
      this.showSuccess(this.isFilterOn() ? 'Filtre désactivé' : 'Filtre activé');
    } catch (error) {
      this.showError('Erreur lors du contrôle du filtre');
    }
  }

  // Modes rapides
  async activateQuickMode(mode: 'relax' | 'eco' | 'heat') {
    const config = this.configuration();
    if (!config) return;

    let temperature: number;
    let startHeating = false;

    switch (mode) {
      case 'relax':
        temperature = config.defaultRelaxTemperature;
        startHeating = true;
        break;
      case 'eco':
        temperature = config.defaultEcoTemperature;
        startHeating = false;
        break;
      case 'heat':
        temperature = config.defaultHeatTemperature;
        startHeating = true;
        break;
      default:
        return;
    }

    try {
      await this.setTargetTemperature(temperature);
      if (startHeating) {
        await this.spaStateService.startHeating().toPromise();
      }
      this.showSuccess(`Mode ${mode} activé`);
    } catch (error) {
      this.showError(`Erreur lors de l'activation du mode ${mode}`);
    }
  }

  // Gestion des scénarios
  async addScenario() {
    if (!this.newScenario.name || !this.newScenario.executionTime) {
      this.showError('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (!this.isValidTemperature(this.newScenario.targetTemperature)) {
      this.showError('Température invalide');
      return;
    }

    try {
      await this.scenarioService.createScenario(this.newScenario).toPromise();
      this.showSuccess('Scénario créé avec succès');
      this.resetNewScenario();
    } catch (error) {
      this.showError('Erreur lors de la création du scénario');
    }
  }

  async toggleScenario(scenarioId: number | undefined) {
    if (!scenarioId) return;

    try {
      await this.scenarioService.toggleScenario(scenarioId).toPromise();
      this.showSuccess('Statut du scénario modifié');
    } catch (error) {
      this.showError('Erreur lors de la modification du scénario');
    }
  }

  async deleteScenario(scenarioId: number | undefined) {
    if (!scenarioId) return;

    try {
      await this.scenarioService.deleteScenario(scenarioId).toPromise();
      this.showSuccess('Scénario supprimé');
    } catch (error) {
      this.showError('Erreur lors de la suppression du scénario');
    }
  }

  async executeScenario(scenarioId: number | undefined) {
    if (!scenarioId) return;

    try {
      await this.scenarioService.executeScenario(scenarioId).toPromise();
      this.showSuccess('Scénario exécuté');
    } catch (error) {
      this.showError('Erreur lors de l\'exécution du scénario');
    }
  }

  // Gestion des sessions
  async addSession() {
    if (!this.newSession.name || !this.newSession.scheduledStartTime) {
      this.showError('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (!this.isValidTemperature(this.newSession.targetTemperature)) {
      this.showError('Température invalide');
      return;
    }

    if (!this.isValidDuration(this.newSession.durationMinutes)) {
      this.showError('Durée invalide');
      return;
    }

    try {
      await this.sessionService.createSession(this.newSession).toPromise();
      this.showSuccess('Session créée avec succès');
      this.resetNewSession();
    } catch (error) {
      this.showError('Erreur lors de la création de la session');
    }
  }

  async startSession(sessionId: number | undefined) {
    if (!sessionId) return;

    try {
      await this.sessionService.startSession(sessionId).toPromise();
      this.showSuccess('Session démarrée');
    } catch (error) {
      this.showError('Erreur lors du démarrage de la session');
    }
  }

  async stopSession(sessionId: number | undefined) {
    if (!sessionId) return;

    try {
      await this.sessionService.stopSession(sessionId).toPromise();
      this.showSuccess('Session arrêtée');
    } catch (error) {
      this.showError('Erreur lors de l\'arrêt de la session');
    }
  }

  async cancelSession(sessionId: number | undefined) {
    if (!sessionId) return;

    try {
      await this.sessionService.cancelSession(sessionId).toPromise();
      this.showSuccess('Session annulée');
    } catch (error) {
      this.showError('Erreur lors de l\'annulation de la session');
    }
  }

  async deleteSession(sessionId: number | undefined) {
    if (!sessionId) return;

    try {
      await this.sessionService.deleteSession(sessionId).toPromise();
      this.showSuccess('Session supprimée');
    } catch (error) {
      this.showError('Erreur lors de la suppression de la session');
    }
  }

  // Session rapide
  async startQuickSession(temperature: number, durationMinutes: number) {
    if (!this.isValidTemperature(temperature) || !this.isValidDuration(durationMinutes)) {
      this.showError('Paramètres invalides');
      return;
    }

    try {
      await this.sessionService.startQuickSession(temperature, durationMinutes).toPromise();
      this.showSuccess('Session rapide démarrée');
    } catch (error) {
      this.showError('Erreur lors du démarrage de la session rapide');
    }
  }

  // Utilitaires
  private isValidTemperature(temperature: number): boolean {
    const config = this.configuration();
    if (!config) return false;
    return temperature >= config.minTemperature && temperature <= config.maxTemperature;
  }

  private isValidDuration(durationMinutes: number): boolean {
    const config = this.configuration();
    if (!config) return false;
    return durationMinutes > 0 && durationMinutes <= config.maxSessionDurationMinutes;
  }

  private resetNewScenario() {
    this.newScenario = {
      name: '',
      executionTime: '',
      targetTemperature: 37,
      bubblesEnabled: false,
      isRecurring: true
    };
  }

  private resetNewSession() {
    this.initializeNewSession();
  }

  private initializeNewSession() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(19, 0, 0, 0);

    this.newSession = {
      name: '',
      scheduledStartTime: tomorrow.toISOString(),
      targetTemperature: 37,
      durationMinutes: 60,
      bubblesEnabled: false
    };
  }

  // Helpers pour le template
  getSessionStatusText(status: string): string {
    switch (status) {
      case SessionStatus.SCHEDULED: return 'Programmée';
      case SessionStatus.ACTIVE: return 'En cours';
      case SessionStatus.COMPLETED: return 'Terminée';
      case SessionStatus.CANCELLED: return 'Annulée';
      default: return status;
    }
  }

  getSessionStatusClass(status: string): string {
    switch (status) {
      case SessionStatus.SCHEDULED: return 'status-scheduled';
      case SessionStatus.ACTIVE: return 'status-active';
      case SessionStatus.COMPLETED: return 'status-completed';
      case SessionStatus.CANCELLED: return 'status-cancelled';
      default: return '';
    }
  }

  getSpaStatusText(): string {
    if (!this.isOnline()) return 'Hors ligne';
    if (this.hasError()) return 'Erreur';
    if (this.appState()?.activeSession) return 'Session en cours';
    if (this.isHeating()) return 'Chauffage en cours';
    return 'En veille';
  }

  getSpaStatusClass(): string {
    if (!this.isOnline()) return 'status-offline';
    if (this.hasError()) return 'status-error';
    if (this.appState()?.activeSession) return 'status-session';
    if (this.isHeating()) return 'status-heating';
    return 'status-idle';
  }

  // État des notifications
  notification = signal<{message: string, type: 'success' | 'error'} | null>(null);

  // Notifications
  private showSuccess(message: string) {
    this.notification.set({message, type: 'success'});
    setTimeout(() => this.notification.set(null), 3000);
    console.log('✅ Success:', message);
  }

  private showError(message: string) {
    this.notification.set({message, type: 'error'});
    setTimeout(() => this.notification.set(null), 5000);
    console.error('❌ Error:', message);
  }

  // Refresh manuel
  refreshData() {
    this.appStateService.refreshAll();
    this.showSuccess('Données actualisées');
  }

  // Méthodes pour compatibilité avec le template existant
  onTemperatureChange(value: number) {
    this.setTargetTemperature(value);
  }

  toggleAutomation() {
    this.automationMode.set(!this.automationMode());
    this.showSuccess(`Mode automatique ${this.automationMode() ? 'activé' : 'désactivé'}`);
  }
}