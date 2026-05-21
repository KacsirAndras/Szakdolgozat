import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type TicketType = 'BUG' | 'FEATURE' | 'HELP';
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';

export interface Ticket {
  id: number;
  type: TicketType;
  status: TicketStatus;
  problem: string;
  createdByEmail: string;
  createdByName: string;
  createdByRole: string;
  itEmployeeEmail: string | null;
  itEmployeeName: string | null;
  itEmployeeClosedTickets: number | null;
}

export interface TicketRequest {
  type: TicketType;
  problem: string;
}

@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly apiUrl = 'http://127.0.0.1:8080/api/tickets';

  constructor(private readonly http: HttpClient) {}

  listTickets(email: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(this.apiUrl, {
      params: { email }
    });
  }

  createTicket(email: string, request: TicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(this.apiUrl, request, {
      params: { email }
    });
  }

  updateTicket(email: string, id: number, status: TicketStatus): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.apiUrl}/${id}`, { status }, {
      params: { email }
    });
  }
}
