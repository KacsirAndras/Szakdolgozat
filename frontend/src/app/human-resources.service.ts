import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type EmployeeRole = 'ADMIN' | 'HR' | 'IT' | 'PRODUCT_OWNER' | 'CUSTOMER' | 'DEVELOPER';

export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  birthDate: string | null;
  phoneNumber: string;
  address: string;
  city: string;
  postalCode: string;
  houseNumber: string;
  role: EmployeeRole;
  active: boolean;
  taxId: string;
  identityCardNumber: string;
  socialSecurityNumber: string;
  salary: number | null;
  adjustedGrossSalary: number;
  netSalary: number;
  hireDate: string | null;
  teamId: number | null;
  teamName: string | null;
  completedTasks: number;
  closedTickets: number;
}

export type EmployeeRequest = Omit<Employee, 'id' | 'adjustedGrossSalary' | 'netSalary' | 'hireDate' | 'teamId' | 'teamName' | 'completedTasks' | 'closedTickets'>;

@Injectable({ providedIn: 'root' })
export class HumanResourcesService {
  private readonly apiUrl = 'http://127.0.0.1:8080/api/hr';

  constructor(private readonly http: HttpClient) {}

  listEmployees(requesterEmail: string): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiUrl}/employees`, {
      params: { requesterEmail }
    });
  }

  createEmployee(requesterEmail: string, request: EmployeeRequest, language: string): Observable<Employee> {
    return this.http.post<Employee>(`${this.apiUrl}/employees`, request, {
      params: { requesterEmail, language }
    });
  }

  updateEmployee(requesterEmail: string, id: number, request: EmployeeRequest, language: string): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/employees/${id}`, request, {
      params: { requesterEmail, language }
    });
  }

  deactivateEmployee(requesterEmail: string, id: number): Observable<Employee> {
    return this.http.post<Employee>(`${this.apiUrl}/employees/${id}/deactivate`, {}, {
      params: { requesterEmail }
    });
  }
}
