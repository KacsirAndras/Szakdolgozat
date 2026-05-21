import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type DayOffType = 'HOME_OFFICE' | 'DAY_OFF';
export type DayOffStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface DayOffQuota {
  year: number;
  homeOfficeLimit: number;
  usedHomeOffice: number;
  homeOfficeRemaining: number;
  dayOffLimit: number;
  usedDayOff: number;
  dayOffRemaining: number;
}

export interface DayOffRequestItem {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeEmail: string;
  employeeRole: string;
  type: DayOffType;
  status: DayOffStatus;
  startDate: string;
  endDate: string;
  workdays: number;
  approvedByName: string | null;
  approvedByEmail: string | null;
}

export interface DayOffPage {
  quotas: DayOffQuota;
  myRequests: DayOffRequestItem[];
  teamRequests: DayOffRequestItem[];
}

export interface DayOffCreateRequest {
  type: DayOffType;
  startDate: string;
  endDate: string;
}

@Injectable({ providedIn: 'root' })
export class DayOffService {
  private readonly apiUrl = 'http://127.0.0.1:8080/api/day-off';

  constructor(private readonly http: HttpClient) {}

  load(email: string): Observable<DayOffPage> {
    return this.http.get<DayOffPage>(this.apiUrl, {
      params: { email }
    });
  }

  request(email: string, request: DayOffCreateRequest): Observable<DayOffRequestItem> {
    return this.http.post<DayOffRequestItem>(this.apiUrl, request, {
      params: { email }
    });
  }

  approve(email: string, id: number): Observable<DayOffRequestItem> {
    return this.http.post<DayOffRequestItem>(`${this.apiUrl}/${id}/approve`, {}, {
      params: { email }
    });
  }

  reject(email: string, id: number): Observable<DayOffRequestItem> {
    return this.http.post<DayOffRequestItem>(`${this.apiUrl}/${id}/reject`, {}, {
      params: { email }
    });
  }
}
