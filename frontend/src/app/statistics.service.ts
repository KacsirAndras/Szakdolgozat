import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface RoleSlice {
  role: string;
  count: number;
  color: string;
}

export interface MonthlyFinance {
  month: number;
  label: string;
  projectIncome: number;
  salaryCost: number;
  profit: number;
  balanceAfter: number;
}

export interface StatisticsPage {
  startDate: string;
  startBalance: number;
  year: number;
  balanceBeforeYear: number;
  roleSlices: RoleSlice[];
  months: MonthlyFinance[];
}

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {
  private readonly apiUrl = 'http://localhost:8080/api/statistics';

  constructor(private readonly http: HttpClient) {}

  load(email: string, year: number): Observable<StatisticsPage> {
    const params = new HttpParams()
      .set('email', email)
      .set('year', year);

    return this.http.get<StatisticsPage>(this.apiUrl, { params });
  }
}
