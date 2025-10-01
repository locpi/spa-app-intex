export enum SessionStatus {
  SCHEDULED = 'SCHEDULED',
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface Session {
  id?: number;
  name: string;
  scheduledStartTime: string; // ISO string
  targetTemperature: number;
  durationMinutes: number;
  bubblesEnabled: boolean;
  status: SessionStatus;
  actualStartTime?: string; // ISO string
  actualEndTime?: string; // ISO string
  notes?: string;
  createdAt?: string; // ISO string
  updatedAt?: string; // ISO string
}

export interface CreateSessionRequest {
  name: string;
  scheduledStartTime: string;
  targetTemperature: number;
  durationMinutes: number;
  bubblesEnabled: boolean;
  notes?: string;
}

export interface UpdateSessionRequest {
  name?: string;
  scheduledStartTime?: string;
  targetTemperature?: number;
  durationMinutes?: number;
  bubblesEnabled?: boolean;
  notes?: string;
}