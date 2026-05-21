import { Routes } from '@angular/router';
import { ForgotPassword } from './forgot-password';
import { Login } from './login';
import { Management } from './management';
import { RegisterCustomer } from './register-customer';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'forgot-password', component: ForgotPassword },
  { path: 'register', component: RegisterCustomer },
  { path: 'management', component: Management },
  { path: '**', redirectTo: 'login' }
];
