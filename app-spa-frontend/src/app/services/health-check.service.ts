import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface HealthCheckResult {
  status: 'healthy' | 'error';
  services: {
    api: boolean;
    database: boolean;
    mqtt?: boolean;
  };
  timestamp: string;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class HealthCheckService {

  constructor(private apiService: ApiService) {}

  /**
   * Vérifie la santé de l'API backend
   */
  checkBackendHealth(): Observable<HealthCheckResult> {
    return this.apiService.get<any>('/health')
      .pipe(
        map(response => ({
          status: 'healthy' as const,
          services: {
            api: true,
            database: response.database || false,
            mqtt: response.mqtt || false
          },
          timestamp: new Date().toISOString(),
          message: 'Backend accessible'
        })),
        catchError(error => {
          console.error('Backend health check failed:', error);
          return [{
            status: 'error' as const,
            services: {
              api: false,
              database: false,
              mqtt: false
            },
            timestamp: new Date().toISOString(),
            message: 'Backend inaccessible: ' + error
          }];
        })
      );
  }

  /**
   * Test de connectivité simple
   */
  pingBackend(): Observable<boolean> {
    return this.apiService.get<any>('/spa/state/current')
      .pipe(
        map(() => true),
        catchError(() => [false])
      );
  }

  /**
   * Vérifie toutes les APIs principales
   */
  checkAllApis(): Observable<{
    spaState: boolean;
    sessions: boolean;
    scenarios: boolean;
    configuration: boolean;
  }> {
    return new Observable(observer => {
      const results = {
        spaState: false,
        sessions: false,
        scenarios: false,
        configuration: false
      };

      let completedChecks = 0;
      const totalChecks = 4;

      const checkComplete = () => {
        completedChecks++;
        if (completedChecks === totalChecks) {
          observer.next(results);
          observer.complete();
        }
      };

      // Test SpaState API
      this.apiService.get('/spa/state/current').subscribe({
        next: () => { results.spaState = true; checkComplete(); },
        error: () => checkComplete()
      });

      // Test Sessions API
      this.apiService.get('/sessions').subscribe({
        next: () => { results.sessions = true; checkComplete(); },
        error: () => checkComplete()
      });

      // Test Scenarios API
      this.apiService.get('/scenarios').subscribe({
        next: () => { results.scenarios = true; checkComplete(); },
        error: () => checkComplete()
      });

      // Test Configuration API
      this.apiService.get('/configuration').subscribe({
        next: () => { results.configuration = true; checkComplete(); },
        error: () => checkComplete()
      });
    });
  }
}