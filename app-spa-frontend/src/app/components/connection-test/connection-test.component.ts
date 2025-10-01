import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HealthCheckService, HealthCheckResult } from '../../services/health-check.service';

@Component({
  selector: 'app-connection-test',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="connection-test">
      <h2>Test des connexions Backend</h2>

      <div class="test-section">
        <h3>Santé générale</h3>
        <button (click)="testBackendHealth()" [disabled]="testing">
          {{ testing ? 'Test en cours...' : 'Tester la santé du backend' }}
        </button>

        @if (healthResult) {
          <div class="health-result" [class]="'status-' + healthResult.status">
            <h4>Résultat: {{ healthResult.status }}</h4>
            <p>{{ healthResult.message }}</p>
            <ul>
              <li>API: {{ healthResult.services.api ? '✅' : '❌' }}</li>
              <li>Base de données: {{ healthResult.services.database ? '✅' : '❌' }}</li>
              <li>MQTT: {{ healthResult.services.mqtt ? '✅' : '❌' }}</li>
            </ul>
            <small>Testé le: {{ healthResult.timestamp | date:'medium' }}</small>
          </div>
        }
      </div>

      <div class="test-section">
        <h3>APIs individuelles</h3>
        <button (click)="testAllApis()" [disabled]="testing">
          {{ testing ? 'Test en cours...' : 'Tester toutes les APIs' }}
        </button>

        @if (apiResults) {
          <div class="api-results">
            <div class="api-result">
              <span>État du Spa:</span>
              <span class="status">{{ apiResults.spaState ? '✅' : '❌' }}</span>
            </div>
            <div class="api-result">
              <span>Sessions:</span>
              <span class="status">{{ apiResults.sessions ? '✅' : '❌' }}</span>
            </div>
            <div class="api-result">
              <span>Scénarios:</span>
              <span class="status">{{ apiResults.scenarios ? '✅' : '❌' }}</span>
            </div>
            <div class="api-result">
              <span>Configuration:</span>
              <span class="status">{{ apiResults.configuration ? '✅' : '❌' }}</span>
            </div>
          </div>
        }
      </div>

      <div class="test-section">
        <h3>Ping simple</h3>
        <button (click)="pingBackend()" [disabled]="testing">
          {{ testing ? 'Test en cours...' : 'Ping Backend' }}
        </button>

        @if (pingResult !== null) {
          <div class="ping-result" [class]="pingResult ? 'success' : 'error'">
            {{ pingResult ? 'Backend accessible ✅' : 'Backend inaccessible ❌' }}
          </div>
        }
      </div>

      @if (error) {
        <div class="error-message">
          <h4>Erreur:</h4>
          <pre>{{ error }}</pre>
        </div>
      }
    </div>
  `,
  styles: [`
    .connection-test {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
    }

    .test-section {
      margin: 20px 0;
      padding: 15px;
      border: 1px solid #ddd;
      border-radius: 8px;
    }

    .test-section h3 {
      margin-top: 0;
      color: #333;
    }

    button {
      padding: 10px 20px;
      background: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      margin: 10px 0;
    }

    button:disabled {
      background: #6c757d;
      cursor: not-allowed;
    }

    button:hover:not(:disabled) {
      background: #0056b3;
    }

    .health-result {
      margin: 15px 0;
      padding: 15px;
      border-radius: 8px;
    }

    .status-healthy {
      background: #d4edda;
      border: 1px solid #c3e6cb;
      color: #155724;
    }

    .status-error {
      background: #f8d7da;
      border: 1px solid #f5c6cb;
      color: #721c24;
    }

    .api-results {
      margin: 15px 0;
    }

    .api-result {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid #eee;
    }

    .ping-result {
      margin: 15px 0;
      padding: 10px;
      border-radius: 4px;
      font-weight: bold;
    }

    .ping-result.success {
      background: #d4edda;
      color: #155724;
    }

    .ping-result.error {
      background: #f8d7da;
      color: #721c24;
    }

    .error-message {
      margin: 20px 0;
      padding: 15px;
      background: #f8d7da;
      border: 1px solid #f5c6cb;
      border-radius: 8px;
      color: #721c24;
    }

    .error-message pre {
      white-space: pre-wrap;
      word-break: break-all;
    }
  `]
})
export class ConnectionTestComponent implements OnInit {
  testing = false;
  healthResult: HealthCheckResult | null = null;
  apiResults: any = null;
  pingResult: boolean | null = null;
  error: string | null = null;

  constructor(private healthCheckService: HealthCheckService) {}

  ngOnInit() {
    // Test automatique au démarrage
    this.testBackendHealth();
  }

  async testBackendHealth() {
    this.testing = true;
    this.error = null;

    try {
      this.healthResult = await this.healthCheckService.checkBackendHealth().toPromise() || null;
    } catch (error) {
      this.error = 'Erreur lors du test de santé: ' + error;
      console.error('Health check error:', error);
    } finally {
      this.testing = false;
    }
  }

  async testAllApis() {
    this.testing = true;
    this.error = null;

    try {
      this.apiResults = await this.healthCheckService.checkAllApis().toPromise();
    } catch (error) {
      this.error = 'Erreur lors du test des APIs: ' + error;
      console.error('API test error:', error);
    } finally {
      this.testing = false;
    }
  }

  async pingBackend() {
    this.testing = true;
    this.error = null;

    try {
      this.pingResult = await this.healthCheckService.pingBackend().toPromise() || false;
    } catch (error) {
      this.error = 'Erreur lors du ping: ' + error;
      this.pingResult = false;
      console.error('Ping error:', error);
    } finally {
      this.testing = false;
    }
  }
}