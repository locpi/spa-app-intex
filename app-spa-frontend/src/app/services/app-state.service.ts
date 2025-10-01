import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, combineLatest } from 'rxjs';
import { map } from 'rxjs/operators';
import { SpaStateService } from './spa-state.service';
import { SessionService } from './session.service';
import { ScenarioService } from './scenario.service';
import { SpaConfigurationService } from './spa-configuration.service';
import { SpaState, Session, Scenario, SpaConfiguration } from '../models';

export interface AppState {
  spaState: SpaState | null;
  activeSession: Session | null;
  sessions: Session[];
  scenarios: Scenario[];
  activeScenarios: Scenario[];
  configuration: SpaConfiguration | null;
  isLoading: boolean;
  error: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AppStateService {
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private errorSubject = new BehaviorSubject<string | null>(null);

  // État global de l'application
  public appState$: Observable<AppState>;

  constructor(
    private spaStateService: SpaStateService,
    private sessionService: SessionService,
    private scenarioService: ScenarioService,
    private configurationService: SpaConfigurationService
  ) {
    // Combine tous les observables en un seul état global
    this.appState$ = combineLatest([
      this.spaStateService.currentState$,
      this.sessionService.activeSession$,
      this.sessionService.sessions$,
      this.scenarioService.scenarios$,
      this.scenarioService.activeScenarios$,
      this.configurationService.configuration$,
      this.loadingSubject.asObservable(),
      this.errorSubject.asObservable()
    ]).pipe(
      map(([spaState, activeSession, sessions, scenarios, activeScenarios, configuration, isLoading, error]) => ({
        spaState,
        activeSession,
        sessions,
        scenarios,
        activeScenarios,
        configuration,
        isLoading,
        error
      }))
    );
  }

  /**
   * Initialise l'état de l'application
   */
  initializeApp(): void {
    this.setLoading(true);
    this.clearError();

    // Charge toutes les données initiales
    Promise.all([
      this.spaStateService.getCurrentState().toPromise(),
      this.sessionService.loadSessions().toPromise(),
      this.scenarioService.loadScenarios().toPromise(),
      this.configurationService.loadConfiguration().toPromise()
    ]).then(() => {
      this.setLoading(false);
    }).catch(error => {
      this.setError('Erreur lors du chargement des données: ' + error);
      this.setLoading(false);
    });
  }

  /**
   * Recharge toutes les données
   */
  refreshAll(): void {
    this.setLoading(true);
    this.clearError();

    Promise.all([
      this.spaStateService.getCurrentState().toPromise(),
      this.sessionService.loadSessions().toPromise(),
      this.scenarioService.loadScenarios().toPromise(),
      this.configurationService.loadConfiguration().toPromise()
    ]).then(() => {
      this.setLoading(false);
    }).catch(error => {
      this.setError('Erreur lors du rafraîchissement: ' + error);
      this.setLoading(false);
    });
  }

  /**
   * Met à jour l'état de chargement
   */
  setLoading(loading: boolean): void {
    this.loadingSubject.next(loading);
  }

  /**
   * Met à jour l'erreur
   */
  setError(error: string | null): void {
    this.errorSubject.next(error);
  }

  /**
   * Efface l'erreur
   */
  clearError(): void {
    this.errorSubject.next(null);
  }

  /**
   * Obtient l'état de chargement actuel
   */
  isLoading(): Observable<boolean> {
    return this.loadingSubject.asObservable();
  }

  /**
   * Obtient l'erreur actuelle
   */
  getError(): Observable<string | null> {
    return this.errorSubject.asObservable();
  }

  /**
   * Vérifie si le spa est disponible
   */
  isSpaAvailable(): Observable<boolean> {
    return this.appState$.pipe(
      map(state =>
        state.spaState?.isOnline === true &&
        state.spaState?.isInErrorState !== true
      )
    );
  }

  /**
   * Vérifie si une session est en cours
   */
  hasActiveSession(): Observable<boolean> {
    return this.appState$.pipe(
      map(state => state.activeSession !== null)
    );
  }

  /**
   * Obtient le statut général du spa
   */
  getSpaStatus(): Observable<string> {
    return this.appState$.pipe(
      map(state => {
        if (!state.spaState) return 'Inconnu';
        if (!state.spaState.isOnline) return 'Hors ligne';
        if (state.spaState.isInErrorState) return 'Erreur';
        if (state.activeSession) return 'Session en cours';
        if (state.spaState.isHeating) return 'Chauffage en cours';
        return 'En veille';
      })
    );
  }

  /**
   * Obtient la température actuelle
   */
  getCurrentTemperature(): Observable<number | null> {
    return this.appState$.pipe(
      map(state => state.spaState?.currentTemperature || null)
    );
  }

  /**
   * Obtient la température cible
   */
  getTargetTemperature(): Observable<number | null> {
    return this.appState$.pipe(
      map(state => state.spaState?.targetTemperature || null)
    );
  }

  /**
   * Obtient le temps restant de la session active
   */
  getActiveSessionRemainingTime(): Observable<number | null> {
    return this.appState$.pipe(
      map(state => state.spaState?.sessionRemainingMinutes || null)
    );
  }

  /**
   * Vérifie si les bulles sont actives
   */
  areBubblesActive(): Observable<boolean> {
    return this.appState$.pipe(
      map(state => state.spaState?.bubblesActive === true)
    );
  }

  /**
   * Vérifie si le filtre est actif
   */
  isFilterActive(): Observable<boolean> {
    return this.appState$.pipe(
      map(state => state.spaState?.filterActive === true)
    );
  }

  /**
   * Obtient les scénarios programmés pour maintenant
   */
  getCurrentScenarios(): Observable<Scenario[]> {
    const now = new Date();
    const currentTime = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;

    return this.appState$.pipe(
      map(state =>
        state.activeScenarios.filter(scenario =>
          scenario.executionTime === currentTime
        )
      )
    );
  }

  /**
   * Obtient les sessions à venir aujourd'hui
   */
  getTodayUpcomingSessions(): Observable<Session[]> {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    return this.appState$.pipe(
      map(state =>
        state.sessions.filter(session => {
          const sessionDate = new Date(session.scheduledStartTime);
          return sessionDate >= today && sessionDate < tomorrow &&
                 (session.status === 'SCHEDULED' || session.status === 'ACTIVE');
        })
      )
    );
  }
}