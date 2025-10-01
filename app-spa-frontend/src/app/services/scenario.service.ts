import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, switchMap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Scenario, CreateScenarioRequest, UpdateScenarioRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ScenarioService {
  private scenariosSubject = new BehaviorSubject<Scenario[]>([]);
  public scenarios$ = this.scenariosSubject.asObservable();

  private activeScenariosSubject = new BehaviorSubject<Scenario[]>([]);
  public activeScenarios$ = this.activeScenariosSubject.asObservable();

  constructor(private apiService: ApiService) {
    this.loadScenarios();
  }

  /**
   * Charge tous les scénarios
   */
  loadScenarios(): Observable<Scenario[]> {
    return this.apiService.get<Scenario[]>('/scenarios')
      .pipe(
        tap(scenarios => {
          this.scenariosSubject.next(scenarios);
          const activeScenarios = scenarios.filter(s => s.isActive);
          this.activeScenariosSubject.next(activeScenarios);
        })
      );
  }

  /**
   * Obtient tous les scénarios
   */
  getAllScenarios(): Observable<Scenario[]> {
    return this.scenarios$;
  }

  /**
   * Obtient les scénarios actifs
   */
  getActiveScenarios(): Observable<Scenario[]> {
    return this.activeScenarios$;
  }

  /**
   * Obtient un scénario par ID
   */
  getScenarioById(id: number): Observable<Scenario> {
    return this.apiService.get<Scenario>(`/scenarios/${id}`);
  }

  /**
   * Crée un nouveau scénario
   */
  createScenario(scenarioData: CreateScenarioRequest): Observable<Scenario> {
    return this.apiService.post<Scenario>('/scenarios', scenarioData)
      .pipe(
        tap(() => this.loadScenarios().subscribe())
      );
  }

  /**
   * Met à jour un scénario
   */
  updateScenario(id: number, scenarioData: UpdateScenarioRequest): Observable<Scenario> {
    return this.apiService.put<Scenario>(`/scenarios/${id}`, scenarioData)
      .pipe(
        tap(() => this.loadScenarios().subscribe())
      );
  }

  /**
   * Supprime un scénario
   */
  deleteScenario(id: number): Observable<void> {
    return this.apiService.delete<void>(`/scenarios/${id}`)
      .pipe(
        tap(() => this.loadScenarios().subscribe())
      );
  }

  /**
   * Active/désactive un scénario
   */
  toggleScenario(id: number): Observable<Scenario> {
    return this.apiService.post<Scenario>(`/scenarios/${id}/toggle`, {})
      .pipe(
        tap(() => this.loadScenarios().subscribe())
      );
  }

  /**
   * Exécute un scénario manuellement
   */
  executeScenario(id: number): Observable<any> {
    return this.apiService.post(`/scenarios/${id}/execute`, {});
  }

  /**
   * Obtient les scénarios à exécuter à une heure donnée
   */
  getScenariosToExecute(time: string): Observable<Scenario[]> {
    return this.apiService.get<Scenario[]>(`/scenarios/execute-at/${time}`);
  }

  /**
   * Obtient les scénarios pour la prochaine heure
   */
  getUpcomingScenarios(): Observable<Scenario[]> {
    return this.apiService.get<Scenario[]>('/scenarios/upcoming');
  }

  /**
   * Recherche des scénarios par nom
   */
  searchScenarios(query: string): Observable<Scenario[]> {
    return this.apiService.get<Scenario[]>(`/scenarios/search?q=${encodeURIComponent(query)}`);
  }

  /**
   * Obtient les scénarios par plage de température
   */
  getScenariosByTemperatureRange(minTemp: number, maxTemp: number): Observable<Scenario[]> {
    return this.apiService.get<Scenario[]>(`/scenarios/temperature-range?min=${minTemp}&max=${maxTemp}`);
  }

  /**
   * Obtient les scénarios avec bulles activées
   */
  getScenariosWithBubbles(): Observable<Scenario[]> {
    return this.apiService.get<Scenario[]>('/scenarios/with-bubbles');
  }

  /**
   * Clone un scénario existant
   */
  cloneScenario(id: number, newName: string): Observable<Scenario> {
    return this.getScenarioById(id)
      .pipe(
        switchMap(scenario => {
          const clonedScenario: CreateScenarioRequest = {
            name: newName,
            executionTime: scenario.executionTime,
            targetTemperature: scenario.targetTemperature,
            maintainTemperature: scenario.maintainTemperature,
            bubblesEnabled: scenario.bubblesEnabled,
            durationMinutes: scenario.durationMinutes,
            isRecurring: scenario.isRecurring,
            description: scenario.description
          };
          return this.createScenario(clonedScenario);
        })
      );
  }

  /**
   * Crée un scénario rapide
   */
  createQuickScenario(name: string, time: string, temperature: number): Observable<Scenario> {
    const quickScenarioData: CreateScenarioRequest = {
      name: name,
      executionTime: time,
      targetTemperature: temperature,
      bubblesEnabled: false,
      isRecurring: true
    };

    return this.createScenario(quickScenarioData);
  }
}