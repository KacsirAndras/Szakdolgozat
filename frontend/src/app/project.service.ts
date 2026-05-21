import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type ProjectStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'COMPLETED';

export interface Project {
  id: number;
  title: string;
  description: string;
  status: ProjectStatus;
  budget: number;
  startDate: string;
  deadline: string;
  creatorEmail: string;
  creatorName: string;
  productOwnerEmail: string | null;
  productOwnerName: string | null;
  teamName: string | null;
}

export interface ProjectRequest {
  title: string;
  description: string;
  budget: number;
  deadline: string;
}

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly apiUrl = 'http://127.0.0.1:8080/api/projects';

  constructor(private readonly http: HttpClient) {}

  listProjects(email: string): Observable<Project[]> {
    return this.http.get<Project[]>(this.apiUrl, {
      params: { email }
    });
  }

  listPendingProjects(email: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/pending`, {
      params: { email }
    });
  }

  createProject(email: string, request: ProjectRequest, language: string): Observable<Project> {
    return this.http.post<Project>(this.apiUrl, request, {
      params: { email, language }
    });
  }

  acceptProject(email: string, id: number, language: string): Observable<Project> {
    return this.http.post<Project>(`${this.apiUrl}/${id}/accept`, {}, {
      params: { email, language }
    });
  }

  rejectProject(email: string, id: number): Observable<Project> {
    return this.http.post<Project>(`${this.apiUrl}/${id}/reject`, {}, {
      params: { email }
    });
  }

  completeProject(email: string, id: number): Observable<Project> {
    return this.http.post<Project>(`${this.apiUrl}/${id}/complete`, {}, {
      params: { email }
    });
  }
}
