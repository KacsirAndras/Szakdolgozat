import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
import { Language, Theme, UiSettingsService } from './ui-settings.service';

type Step = 'email' | 'reset' | 'done';

const TEXT = {
  hu: {
    title: 'Elfelejtett jelszó',
    email: 'Email cím',
    emailPlaceholder: 'pelda@email.hu',
    sendToken: 'Token küldése',
    sending: 'Küldés...',
    token: 'Token',
    tokenPlaceholder: '6 jegyű kód',
    password: 'Új jelszó',
    passwordAgain: 'Új jelszó még egyszer',
    save: 'Jelszó módosítása',
    saving: 'Mentés...',
    back: 'Vissza a loginhoz',
    tokenCreated: 'címre elküldtük a tokent.',
    tokenError: 'Nem sikerült tokent kérni.',
    passwordMismatch: 'A két jelszó nem egyezik.',
    resetError: 'Nem sikerült megváltoztatni a jelszót.',
    theme: 'Téma',
    language: 'Nyelv'
  },
  en: {
    title: 'Forgot password',
    email: 'Email address',
    emailPlaceholder: 'example@email.com',
    sendToken: 'Send token',
    sending: 'Sending...',
    token: 'Token code',
    tokenPlaceholder: '6 digit code',
    password: 'New password',
    passwordAgain: 'New password again',
    save: 'Change password',
    saving: 'Saving...',
    back: 'Back to login',
    tokenCreated: 'received a reset token.',
    tokenError: 'Could not request a token.',
    passwordMismatch: 'The two passwords do not match.',
    resetError: 'Could not change the password.',
    theme: 'Theme',
    language: 'Language'
  }
};

@Component({
  selector: 'app-forgot-password',
  imports: [FormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {
  email = '';
  token = '';
  newPassword = '';
  newPasswordAgain = '';
  step = signal<Step>('email');
  message = signal('');
  error = signal('');
  loading = signal(false);

  constructor(
    private readonly authService: AuthService,
    readonly ui: UiSettingsService
  ) {}

  get t() {
    return TEXT[this.ui.language()];
  }

  requestToken(): void {
    this.loading.set(true);
    this.error.set('');
    this.message.set('');
    this.authService.forgotPassword(this.email.trim()).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.step.set('reset');
        this.message.set(`${response.email} ${this.t.tokenCreated}`);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(error.error?.message || this.t.tokenError);
      }
    });
  }

  resetPassword(): void {
    if (this.newPassword !== this.newPasswordAgain) {
      this.error.set(this.t.passwordMismatch);
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.message.set('');

    this.authService.resetPassword(this.token.trim(), this.newPassword, this.newPasswordAgain).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.step.set('done');
        this.message.set(response.message);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(error.error?.message || this.t.resetError);
      }
    });
  }

  setLanguage(language: Language): void {
    this.ui.setLanguage(language);
    this.error.set('');
  }

  setTheme(theme: Theme): void {
    this.ui.setTheme(theme);
  }
}
