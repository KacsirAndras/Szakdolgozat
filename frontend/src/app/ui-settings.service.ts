import { Injectable, signal } from '@angular/core';

export type Language = 'hu' | 'en';
export type Theme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class UiSettingsService {
  readonly language = signal<Language>(this.readCookie('language') as Language || 'hu');
  readonly theme = signal<Theme>(this.readCookie('theme') as Theme || 'light');

  constructor() {
    this.applyTheme();
  }

  setLanguage(language: Language): void {
    this.language.set(language);
    this.writeCookie('language', language, 365);
  }

  setTheme(theme: Theme): void {
    this.theme.set(theme);
    this.writeCookie('theme', theme, 365);
    this.applyTheme();
  }

  readCookie(name: string): string {
    const value = document.cookie
      .split('; ')
      .find((row) => row.startsWith(`${name}=`))
      ?.split('=')[1];

    return value ? decodeURIComponent(value) : '';
  }

  writeCookie(name: string, value: string, days: number): void {
    const expires = new Date(Date.now() + days * 86400000).toUTCString();
    document.cookie = `${name}=${encodeURIComponent(value)}; expires=${expires}; path=/; SameSite=Lax`;
  }

  deleteCookie(name: string): void {
    document.cookie = `${name}=; Max-Age=0; path=/; SameSite=Lax`;
  }

  private applyTheme(): void {
    document.body.dataset['theme'] = this.theme();
  }
}
