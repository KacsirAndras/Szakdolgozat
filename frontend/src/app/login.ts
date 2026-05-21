import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
import { Language, Theme, UiSettingsService } from './ui-settings.service';

const TEXT = {
  hu: {
    title: 'manage.me',
    subtitle: 'Bejelentkezes',
    email: 'Email',
    showPassword: 'Show password',
    password: 'Password',
    remember: 'Emlekezz ram',
    login: 'Belepes',
    cancel: 'Megse',
    forgot: 'Elfelejtett jelszo?',
    register: 'Ugyfel regisztracio',
    invalid: 'Hibas email vagy jelszo.',
    theme: 'Tema',
    language: 'Nyelv',
    black: 'Fekete',
    white: 'Feher',
    loading: 'Belepes...',
    languageChanged: 'A nyelv magyarra valtva.',
    themeChanged: 'A tema modositva.'
  },
  en: {
    title: 'manage.me',
    subtitle: 'Sign in',
    email: 'Email',
    password: 'Password',
    showPassword: 'Show password',
    remember: 'Remember me',
    login: 'Sign in',
    cancel: 'Cancel',
    forgot: 'Forgot password?',
    register: 'Customer registration',
    invalid: 'Invalid email or password.',
    theme: 'Theme',
    language: 'Language',
    black: 'Black',
    white: 'White',
    loading: 'Signing in...',
    languageChanged: 'Language changed to English.',
    themeChanged: 'Theme changed.'
  }
};

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login implements OnInit {
  email = '';
  password = '';
  showPassword = false;
  remember = true;
  error = signal('');
  message = signal('');
  loading = signal(false);

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    readonly ui: UiSettingsService
  ) {}

  get t() {
    return TEXT[this.ui.language()];
  }

  ngOnInit(): void {
    this.email = this.ui.readCookie('rememberedEmail') || '';
  }

  login(): void {
    if (!this.email || !this.password) {
      this.message.set('');
      this.error.set(this.t.invalid);
      return;
    }

    this.loading.set(true);
    this.message.set('');
    this.error.set('');

    this.authService.login(this.email.trim(), this.password).subscribe({
      next: (user) => {
        this.ui.writeCookie('loggedInUserEmail', user.email, 1);
        this.ui.writeCookie('loggedInUserFirstName', user.firstName, 1);
        this.ui.writeCookie('loggedInUserLastName', user.lastName, 1);
        this.ui.writeCookie('loggedInUserActive', String(user.active), 1);
        this.ui.writeCookie('loggedInUserRole', user.role, 1);

        if (this.remember) {
          this.ui.writeCookie('rememberedEmail', user.email, 30);
        } else {
          this.ui.deleteCookie('rememberedEmail');
        }

        this.loading.set(false);
        this.router.navigate(['/management']);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(error.error?.message || this.t.invalid);
      }
    });
  }

  setLanguage(language: Language): void {
    this.ui.setLanguage(language);
    this.error.set('');
    this.message.set(this.t.languageChanged);
  }

  setTheme(theme: Theme): void {
    this.ui.setTheme(theme);
    this.error.set('');
    this.message.set(this.t.themeChanged);
  }
}
