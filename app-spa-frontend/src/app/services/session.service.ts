import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, switchMap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Session, CreateSessionRequest, UpdateSessionRequest, SessionStatus } from '../models';

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private sessionsSubject = new BehaviorSubject<Session[]>([]);
  public sessions$ = this.sessionsSubject.asObservable();

  private activeSessionSubject = new BehaviorSubject<Session | null>(null);
  public activeSession$ = this.activeSessionSubject.asObservable();

  constructor(private apiService: ApiService) {
    this.loadSessions();
  }

  /**
   * Charge toutes les sessions
   */
  loadSessions(): Observable<Session[]> {
    return this.apiService.get<Session[]>('/sessions')
      .pipe(
        tap(sessions => {
          this.sessionsSubject.next(sessions);
          const activeSession = sessions.find(s => s.status === SessionStatus.ACTIVE);
          this.activeSessionSubject.next(activeSession || null);
        })
      );
  }

  /**
   * Obtient toutes les sessions
   */
  getAllSessions(): Observable<Session[]> {
    return this.sessions$;
  }

  /**
   * Obtient une session par ID
   */
  getSessionById(id: number): Observable<Session> {
    return this.apiService.get<Session>(`/sessions/${id}`);
  }

  /**
   * Crée une nouvelle session
   */
  createSession(sessionData: CreateSessionRequest): Observable<Session> {
    return this.apiService.post<Session>('/sessions', sessionData)
      .pipe(
        tap(() => this.loadSessions().subscribe())
      );
  }

  /**
   * Met à jour une session
   */
  updateSession(id: number, sessionData: UpdateSessionRequest): Observable<Session> {
    return this.apiService.put<Session>(`/sessions/${id}`, sessionData)
      .pipe(
        tap(() => this.loadSessions().subscribe())
      );
  }

  /**
   * Supprime une session
   */
  deleteSession(id: number): Observable<void> {
    return this.apiService.delete<void>(`/sessions/${id}`)
      .pipe(
        tap(() => this.loadSessions().subscribe())
      );
  }

  /**
   * Démarre une session
   */
  startSession(id: number): Observable<Session> {
    return this.apiService.post<Session>(`/sessions/${id}/start`, {})
      .pipe(
        tap(() => this.loadSessions().subscribe())
      );
  }

  /**
   * Arrête une session
   */
  stopSession(id: number): Observable<Session> {
    return this.apiService.post<Session>(`/sessions/${id}/stop`, {})
      .pipe(
        tap(() => this.loadSessions().subscribe())
      );
  }

  /**
   * Met en pause une session
   */
  pauseSession(id: number): Observable<Session> {
    return this.apiService.post<Session>(`/sessions/${id}/pause`, {})
      .pipe(
        tap(() => this.loadSessions().subscribe())
      );
  }

  /**
   * Reprend une session
   */
  resumeSession(id: number): Observable<Session> {
    return this.apiService.post<Session>(`/sessions/${id}/resume`, {})
      .pipe(
        tap(() => this.loadSessions().subscribe())
      );
  }

  /**
   * Annule une session
   */
  cancelSession(id: number): Observable<Session> {
    return this.apiService.post<Session>(`/sessions/${id}/cancel`, {})
      .pipe(
        tap(() => this.loadSessions().subscribe())
      );
  }

  /**
   * Obtient les sessions par statut
   */
  getSessionsByStatus(status: SessionStatus): Observable<Session[]> {
    return this.apiService.get<Session[]>(`/sessions/status/${status}`);
  }

  /**
   * Obtient les sessions programmées pour aujourd'hui
   */
  getTodaySessions(): Observable<Session[]> {
    return this.apiService.get<Session[]>('/sessions/today');
  }

  /**
   * Obtient les sessions à venir
   */
  getUpcomingSessions(): Observable<Session[]> {
    return this.apiService.get<Session[]>('/sessions/upcoming');
  }

  /**
   * Obtient la session active actuelle
   */
  getActiveSession(): Session | null {
    return this.activeSessionSubject.value;
  }

  /**
   * Vérifie s'il y a une session active
   */
  hasActiveSession(): boolean {
    return this.activeSessionSubject.value !== null;
  }

  /**
   * Démarre une session rapide
   */
  startQuickSession(temperature: number, durationMinutes: number): Observable<Session> {
    const quickSessionData: CreateSessionRequest = {
      name: `Session rapide - ${new Date().toLocaleTimeString()}`,
      scheduledStartTime: new Date().toISOString(),
      targetTemperature: temperature,
      durationMinutes: durationMinutes,
      bubblesEnabled: false
    };

    return this.createSession(quickSessionData)
      .pipe(
        switchMap(session => this.startSession(session.id!))
      );
  }
}