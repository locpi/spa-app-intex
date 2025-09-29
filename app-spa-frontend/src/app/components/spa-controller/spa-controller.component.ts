import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Scenario {
  id: string;
  name: string;
  time: string;
  targetTemp: number;
  maintainTemp?: number;
  bubblesOn: boolean;
  duration?: number;
  active: boolean;
  isRecurring?: boolean; // Pour les scénarios récurrents (quotidiens)
}

interface Session {
  id: string;
  name: string;
  date: string; // Format YYYY-MM-DD
  time: string; // Format HH:MM
  targetTemp: number;
  duration: number; // en minutes
  bubblesOn: boolean;
  status: 'scheduled' | 'active' | 'completed' | 'cancelled';
  startedAt?: string;
  completedAt?: string;
}

@Component({
  selector: 'app-spa-controller',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './spa-controller.component.html',
  styleUrls: ['./spa-controller.component.css']
})
export class SpaControllerComponent {
  temperature = signal(24);
  targetTemperature = signal(37);
  isHeating = signal(false);
  isBubblesOn = signal(false);
  isFilterOn = signal(true);

  automationMode = signal(false);

  scenarios = signal<Scenario[]>([
    {
      id: '1',
      name: 'Chauffage du soir',
      time: '23:00',
      targetTemp: 37,
      maintainTemp: 37,
      bubblesOn: false,
      active: true,
      isRecurring: true
    },
    {
      id: '2',
      name: 'Préparation détente',
      time: '19:00',
      targetTemp: 35,
      maintainTemp: 35,
      bubblesOn: true,
      duration: 120,
      active: false,
      isRecurring: true
    },
    {
      id: '3',
      name: 'Economie nocturne',
      time: '01:00',
      targetTemp: 25,
      bubblesOn: false,
      active: true,
      isRecurring: true
    }
  ]);

  sessions = signal<Session[]>([
    {
      id: '1',
      name: 'Session relaxation weekend',
      date: '2025-09-30',
      time: '15:00',
      targetTemp: 38,
      duration: 90,
      bubblesOn: true,
      status: 'scheduled'
    },
    {
      id: '2',
      name: 'Préparation invités',
      date: '2025-10-01',
      time: '18:30',
      targetTemp: 37,
      duration: 60,
      bubblesOn: false,
      status: 'scheduled'
    }
  ]);

  newScenario: Scenario = {
    id: '',
    name: '',
    time: '',
    targetTemp: 37,
    bubblesOn: false,
    active: false,
    isRecurring: true
  };

  newSession: Session = {
    id: '',
    name: '',
    date: new Date(Date.now() + 86400000).toISOString().split('T')[0], // Tomorrow
    time: '19:00',
    targetTemp: 37,
    duration: 60,
    bubblesOn: false,
    status: 'scheduled'
  };

  onTemperatureChange(value: number) {
    this.targetTemperature.set(value);
  }

  toggleHeating() {
    this.isHeating.set(!this.isHeating());
  }

  toggleBubbles() {
    this.isBubblesOn.set(!this.isBubblesOn());
  }

  toggleFilter() {
    this.isFilterOn.set(!this.isFilterOn());
  }

  toggleAutomation() {
    this.automationMode.set(!this.automationMode());
  }

  toggleScenario(scenarioId: string) {
    const scenarios = this.scenarios();
    const updatedScenarios = scenarios.map(scenario =>
      scenario.id === scenarioId
        ? { ...scenario, active: !scenario.active }
        : scenario
    );
    this.scenarios.set(updatedScenarios);
  }

  addScenario() {
    if (this.newScenario.name && this.newScenario.time) {
      const scenarios = this.scenarios();
      const newId = (scenarios.length + 1).toString();
      const scenario: Scenario = {
        ...this.newScenario,
        id: newId
      };
      this.scenarios.set([...scenarios, scenario]);
      this.resetNewScenario();
    }
  }

  deleteScenario(scenarioId: string) {
    const scenarios = this.scenarios();
    const updatedScenarios = scenarios.filter(scenario => scenario.id !== scenarioId);
    this.scenarios.set(updatedScenarios);
  }

  resetNewScenario() {
    this.newScenario = {
      id: '',
      name: '',
      time: '',
      targetTemp: 37,
      bubblesOn: false,
      active: false,
      isRecurring: true
    };
  }

  // Sessions management
  addSession() {
    if (this.newSession.name && this.newSession.date && this.newSession.time) {
      const sessions = this.sessions();
      const newId = (sessions.length + 1).toString();
      const session: Session = {
        ...this.newSession,
        id: newId
      };
      this.sessions.set([...sessions, session]);
      this.resetNewSession();
    }
  }

  deleteSession(sessionId: string) {
    const sessions = this.sessions();
    const updatedSessions = sessions.filter(session => session.id !== sessionId);
    this.sessions.set(updatedSessions);
  }

  startSession(sessionId: string) {
    const sessions = this.sessions();
    const updatedSessions = sessions.map(session =>
      session.id === sessionId
        ? { ...session, status: 'active' as const, startedAt: new Date().toISOString() }
        : session
    );
    this.sessions.set(updatedSessions);

    // Apply session settings
    const session = sessions.find(s => s.id === sessionId);
    if (session) {
      this.targetTemperature.set(session.targetTemp);
      this.isBubblesOn.set(session.bubblesOn);
      this.isHeating.set(true);
    }
  }

  completeSession(sessionId: string) {
    const sessions = this.sessions();
    const updatedSessions = sessions.map(session =>
      session.id === sessionId
        ? { ...session, status: 'completed' as const, completedAt: new Date().toISOString() }
        : session
    );
    this.sessions.set(updatedSessions);
  }

  cancelSession(sessionId: string) {
    const sessions = this.sessions();
    const updatedSessions = sessions.map(session =>
      session.id === sessionId
        ? { ...session, status: 'cancelled' as const }
        : session
    );
    this.sessions.set(updatedSessions);
  }

  resetNewSession() {
    // Set default date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const defaultDate = tomorrow.toISOString().split('T')[0];

    this.newSession = {
      id: '',
      name: '',
      date: defaultDate,
      time: '19:00',
      targetTemp: 37,
      duration: 60,
      bubblesOn: false,
      status: 'scheduled'
    };
  }

  getSessionStatusText(status: string): string {
    switch (status) {
      case 'scheduled': return 'Programmée';
      case 'active': return 'En cours';
      case 'completed': return 'Terminée';
      case 'cancelled': return 'Annulée';
      default: return status;
    }
  }

  getSessionStatusClass(status: string): string {
    switch (status) {
      case 'scheduled': return 'status-scheduled';
      case 'active': return 'status-active';
      case 'completed': return 'status-completed';
      case 'cancelled': return 'status-cancelled';
      default: return '';
    }
  }

  activateQuickMode(mode: 'relax' | 'eco' | 'heat') {
    switch (mode) {
      case 'relax':
        this.targetTemperature.set(37);
        this.isBubblesOn.set(true);
        this.isHeating.set(true);
        break;
      case 'eco':
        this.targetTemperature.set(28);
        this.isBubblesOn.set(false);
        this.isHeating.set(false);
        break;
      case 'heat':
        this.targetTemperature.set(40);
        this.isBubblesOn.set(false);
        this.isHeating.set(true);
        break;
    }
  }
}