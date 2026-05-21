import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface LoginResponse {
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'HR' | 'IT' | 'PRODUCT_OWNER' | 'CUSTOMER' | 'DEVELOPER';
  active: boolean;
}

export interface ForgotPasswordResponse {
  message: string;
  email: string;
}

export interface CustomerRegistrationRequest {
  firstName: string;
  lastName: string;
  birthDate: string;
  email: string;
  phoneNumber: string;
  address: string;
  city: string;
  postalCode: string;
  houseNumber: string;
  companyName: string;
  password: string;
  passwordAgain: string;
}

export interface CustomerRegistrationResponse {
  id: number;
  email: string;
  role: string;
  active: boolean;
  message: string;
}

export interface ProfileUpdateResponse {
  email: string;
  firstName: string;
  lastName: string;
  message: string;
}

export interface ProfileUpdateRequest {
  firstName: string;
  lastName: string;
  email: string;
  birthDate: string;
  phoneNumber: string;
  address: string;
  city: string;
  postalCode: string;
  houseNumber: string;
  companyName: string;
}

export interface ProfileResponse {
  firstName: string;
  lastName: string;
  email: string;
  birthDate: string | null;
  phoneNumber: string | null;
  postalCode: string | null;
  city: string | null;
  address: string | null;
  houseNumber: string | null;
  role: 'ADMIN' | 'HR' | 'IT' | 'PRODUCT_OWNER' | 'CUSTOMER' | 'DEVELOPER';
  companyName: string | null;
  taxId: string | null;
  identityCardNumber: string | null;
  socialSecurityNumber: string | null;
  salary: number | null;
  netSalary: number | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://127.0.0.1:8080/api/auth';
  private readonly customerApiUrl = 'http://127.0.0.1:8080/api/customers';

  constructor(private readonly http: HttpClient) {}

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, password });
  }

  forgotPassword(email: string): Observable<ForgotPasswordResponse> {
    return this.http.post<ForgotPasswordResponse>(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string, newPasswordAgain: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/reset-password`, {
      token,
      newPassword,
      newPasswordAgain
    });
  }

  updateProfile(currentEmail: string, request: ProfileUpdateRequest): Observable<ProfileUpdateResponse> {
    return this.http.post<ProfileUpdateResponse>(`${this.apiUrl}/update-profile`, {
      currentEmail,
      ...request
    });
  }

  loadProfile(email: string): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.apiUrl}/profile`, {
      params: { email }
    });
  }

  changePassword(
    email: string,
    oldPassword: string,
    newPassword: string,
    newPasswordAgain: string
  ): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/change-password`, {
      email,
      oldPassword,
      newPassword,
      newPasswordAgain
    });
  }

  adminChangePassword(
    requesterEmail: string,
    targetEmail: string,
    newPassword: string,
    newPasswordAgain: string
  ): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/admin-change-password`, {
      requesterEmail,
      targetEmail,
      newPassword,
      newPasswordAgain
    });
  }

  registerCustomer(request: CustomerRegistrationRequest, language: string): Observable<CustomerRegistrationResponse> {
    return this.http.post<CustomerRegistrationResponse>(
      `${this.customerApiUrl}/register`,
      request,
      { params: { language } }
    );
  }

  activateCustomer(email: string, token: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.customerApiUrl}/activate`, { email, token });
  }
}
