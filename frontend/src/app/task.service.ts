import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type TaskStatus = 'TO_DO' | 'IN_PROGRESS' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Task {
  id: number;
  projectId: number;
  projectTitle: string;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  employeeId: number | null;
  employeeName: string | null;
  employeeEmail: string | null;
}

export interface TaskRequest {
  projectId: number | null;
  title: string;
  description: string;
  priority: TaskPriority;
}

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly apiUrl = 'http://127.0.0.1:8080/api/tasks';

  constructor(private readonly http: HttpClient) {}

  listTasks(email: string): Observable<Task[]> {
    return this.http.get<Task[]>(this.apiUrl, {
      params: { email }
    });
  }

  createTask(email: string, request: TaskRequest): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, request, {
      params: { email }
    });
  }

  takeTask(email: string, id: number): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/${id}/take`, {}, {
      params: { email }
    });
  }

  finishTask(email: string, id: number): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/${id}/done`, {}, {
      params: { email }
    });
  }
}
