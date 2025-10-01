export interface Scenario {
  id?: number;
  name: string;
  executionTime: string; // Format HH:mm
  targetTemperature: number;
  maintainTemperature?: number;
  bubblesEnabled: boolean;
  durationMinutes?: number;
  isActive: boolean;
  isRecurring: boolean;
  description?: string;
  createdAt?: string; // ISO string
  updatedAt?: string; // ISO string
}

export interface CreateScenarioRequest {
  name: string;
  executionTime: string; // Format HH:mm
  targetTemperature: number;
  maintainTemperature?: number;
  bubblesEnabled: boolean;
  durationMinutes?: number;
  isRecurring?: boolean;
  description?: string;
}

export interface UpdateScenarioRequest {
  name?: string;
  executionTime?: string; // Format HH:mm
  targetTemperature?: number;
  maintainTemperature?: number;
  bubblesEnabled?: boolean;
  durationMinutes?: number;
  isActive?: boolean;
  isRecurring?: boolean;
  description?: string;
}