import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface TeamEmployee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
}

export interface Team {
  id: number;
  name: string;
  owner: TeamEmployee;
  members: TeamEmployee[];
}

@Injectable({ providedIn: 'root' })
export class TeamService {
  private readonly apiUrl = 'http://127.0.0.1:8080/api/teams';

  constructor(private readonly http: HttpClient) {}

  getMyTeam(ownerEmail: string): Observable<Team> {
    return this.http.get<Team>(`${this.apiUrl}/my`, {
      params: { ownerEmail }
    });
  }

  getTeams(email: string): Observable<Team[]> {
    return this.http.get<Team[]>(this.apiUrl, {
      params: { email }
    });
  }

  getMembership(email: string): Observable<Team> {
    return this.http.get<Team>(`${this.apiUrl}/membership`, {
      params: { email }
    });
  }

  createTeam(ownerEmail: string, name: string): Observable<Team> {
    return this.http.post<Team>(`${this.apiUrl}/create`, {
      ownerEmail,
      name
    });
  }

  getAvailableDevelopers(ownerEmail: string): Observable<TeamEmployee[]> {
    return this.http.get<TeamEmployee[]>(`${this.apiUrl}/available-developers`, {
      params: { ownerEmail }
    });
  }

  addMember(ownerEmail: string, developerId: number): Observable<Team> {
    return this.http.post<Team>(`${this.apiUrl}/add-member`, {
      ownerEmail,
      developerId
    });
  }

  joinTeam(email: string, teamId: number): Observable<Team> {
    return this.http.post<Team>(`${this.apiUrl}/join`, {
      email,
      teamId
    });
  }

  leaveTeam(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/leave`, {
      email
    });
  }

  deleteTeam(requesterEmail: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/delete`, {
      requesterEmail
    });
  }
}
