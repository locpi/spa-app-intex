import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, interval } from 'rxjs';
import { switchMap, catchError, tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { SpaState, SpaCommand } from '../models';

@Injectable({
  providedIn: 'root'
})
export class SpaStateService {
  private currentStateSubject = new BehaviorSubject<SpaState | null>(null);
  public currentState$ = this.currentStateSubject.asObservable();

  private isPollingSubject = new BehaviorSubject<boolean>(false);
  public isPolling$ = this.isPollingSubject.asObservable();

  constructor(private apiService: ApiService) {
    this.startPolling();
  }

  /**
   * Obtient l'état actuel du spa
   */
  getCurrentState(): Observable<SpaState> {
    return this.apiService.get<SpaState>('/spa/state/current')
      .pipe(
        tap(state => this.currentStateSubject.next(state))
      );
  }

  /**
   * Obtient l'historique des états récents
   */
  getRecentStates(): Observable<SpaState[]> {
    return this.apiService.get<SpaState[]>('/spa/state/recent');
  }

  /**
   * Met à jour l'état du spa
   */
  updateState(state: Partial<SpaState>): Observable<SpaState> {
    return this.apiService.put<SpaState>('/spa/state', state)
      .pipe(
        tap(updatedState => this.currentStateSubject.next(updatedState))
      );
  }

  /**
   * Envoie une commande au spa
   */
  sendCommand(command: SpaCommand): Observable<any> {
    return this.apiService.post('/spa/state/command', command);
  }

  /**
   * Active le chauffage
   */
  startHeating(): Observable<any> {
    return this.sendCommand({ action: 'START_HEATING' });
  }

  /**
   * Désactive le chauffage
   */
  stopHeating(): Observable<any> {
    return this.sendCommand({ action: 'STOP_HEATING' });
  }

  /**
   * Définit la température cible
   */
  setTargetTemperature(temperature: number): Observable<any> {
    return this.sendCommand({
      action: 'SET_TEMPERATURE',
      value: temperature
    });
  }

  /**
   * Active/désactive les bulles
   */
  toggleBubbles(): Observable<any> {
    return this.sendCommand({ action: 'TOGGLE_BUBBLES' });
  }

  /**
   * Active/désactive le filtre
   */
  toggleFilter(): Observable<any> {
    return this.sendCommand({ action: 'TOGGLE_FILTER' });
  }

  /**
   * Active/désactive les lumières
   */
  toggleLights(): Observable<any> {
    return this.sendCommand({ action: 'TOGGLE_LIGHTS' });
  }

  /**
   * Démarre le polling automatique de l'état
   */
  startPolling(intervalMs: number = 5000): void {
    if (this.isPollingSubject.value) {
      return;
    }

    this.isPollingSubject.next(true);

    interval(intervalMs)
      .pipe(
        switchMap(() => this.getCurrentState()),
        catchError(error => {
          console.error('Erreur lors du polling:', error);
          return [];
        })
      )
      .subscribe();
  }

  /**
   * Arrête le polling automatique
   */
  stopPolling(): void {
    this.isPollingSubject.next(false);
  }

  /**
   * Obtient l'état actuel sans faire d'appel API
   */
  getCurrentStateValue(): SpaState | null {
    return this.currentStateSubject.value;
  }

  /**
   * Vérifie si le spa est en ligne
   */
  isOnline(): boolean {
    const state = this.getCurrentStateValue();
    return state?.isOnline === true;
  }

  /**
   * Vérifie si le spa est en erreur
   */
  hasError(): boolean {
    const state = this.getCurrentStateValue();
    return state?.isInErrorState === true;
  }

  /**
   * Vérifie si le chauffage est actif
   */
  isHeating(): boolean {
    const state = this.getCurrentStateValue();
    return state?.isHeating === true;
  }
}