import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { provideRouter } from '@angular/router';
import { UiSettingsService } from './ui-settings.service';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the router outlet', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });
});

describe('UiSettingsService', () => {
  beforeEach(() => {
    document.cookie.split(';').forEach((cookie) => {
      document.cookie = cookie.replace(/^ +/, '').replace(/=.*/, '=;expires=' + new Date(0).toUTCString() + ';path=/');
    });
  });

  it('stores selected language and theme in cookies', () => {
    const service = TestBed.inject(UiSettingsService);

    service.setLanguage('en');
    service.setTheme('dark');

    expect(service.language()).toBe('en');
    expect(service.theme()).toBe('dark');
    expect(document.body.dataset['theme']).toBe('dark');
  });
});
