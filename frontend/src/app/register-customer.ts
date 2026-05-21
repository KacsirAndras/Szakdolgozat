import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService, CustomerRegistrationRequest } from './auth.service';
import { Language, Theme, UiSettingsService } from './ui-settings.service';

const TEXT = {
  hu: {
    header: '\u00dcgyf\u00e9l regisztr\u00e1ci\u00f3',
    firstName: 'Keresztn\u00e9v',
    lastName: 'Vezet\u00e9kn\u00e9v',
    birthDate: 'Sz\u00fclet\u00e9si d\u00e1tum',
    email: 'Email c\u00edm',
    phone: 'Telefon',
    address: 'C\u00edm',
    city: 'V\u00e1ros',
    postalCode: 'Ir\u00e1ny\u00edt\u00f3sz\u00e1m',
    houseNumber: 'H\u00e1zsz\u00e1m',
    company: 'C\u00e9g n\u00e9v (nem k\u00f6telez\u0151)',
    password: 'Jelsz\u00f3',
    passwordAgain: 'Jelsz\u00f3 \u00fajra',
    submit: '\u00dcgyf\u00e9l regisztr\u00e1l\u00e1sa',
    activate: 'Fi\u00f3k aktiv\u00e1l\u00e1sa',
    activationCode: 'Aktiv\u00e1l\u00f3 k\u00f3d',
    activationCodePlaceholder: '6 jegy\u0171 k\u00f3d',
    activationSent: 'A regisztr\u00e1ci\u00f3 sirequest. \u00cdrd be az emailben kapott aktiv\u00e1l\u00f3 k\u00f3dot.',
    activationSuccess: 'A fi\u00f3k sirequesten aktiv\u00e1lva. Most m\u00e1r be tudsz jelentkezni.',
    activationError: 'Nem siker\u00fclt aktiv\u00e1lni a fi\u00f3kot.',
    saving: 'Ment\u00e9s...',
    back: 'Vissza a loginhoz',
    success: 'Az \u00fcgyf\u00e9l sirequesten regisztr\u00e1lt CUSTOMER szerepk\u00f6rrel.',
    error: 'Nem siker\u00fclt regisztr\u00e1lni az \u00fcgyfelet.',
    required: 'Minden mez\u0151t ki kell t\u00f6lteni.',
    invalidEmail: 'Adj meg egy \u00e9rv\u00e9nyes email c\u00edmet.',
    phoneNumberOnly: 'A telefonsz\u00e1m 8-15 sz\u00e1mjegy legyen, opcion\u00e1lis + el\u0151taggal.',
    postalCodeNumberOnly: 'Az ir\u00e1ny\u00edt\u00f3sz\u00e1m pontosan 4 sz\u00e1mjegy legyen.',
    houseNumberOnly: 'A h\u00e1zsz\u00e1m csak sz\u00e1m lehet.',
    mismatch: 'A k\u00e9t jelsz\u00f3 nem egyezik.',
    underage: '18 \u00e9v alatti \u00fcgyf\u00e9l nem regisztr\u00e1lhat.',
    companyCapitalization: 'A c\u00e9g n\u00e9v minden szava nagybet\u0171vel kezd\u0151dj\u00f6n.',
    theme: 'T\u00e9ma',
    language: 'Nyelv'
  },
  en: {
    header: 'Customer Registration',
    firstName: 'First Name',
    lastName: 'Last Name',
    birthDate: 'Date of Birth',
    email: 'Email Address',
    phone: 'Phone',
    address: 'Address',
    city: 'City',
    postalCode: 'Postal code',
    houseNumber: 'House number',
    company: 'Company name (optional)',
    password: 'Password',
    passwordAgain: 'Password again',
    submit: 'Register Customer',
    activate: 'Activate account',
    activationCode: 'Activation code',
    activationCodePlaceholder: '6 digit code',
    activationSent: 'Registration succeeded. Enter the activation code sent by email.',
    activationSuccess: 'The account was activated. You can log in now.',
    activationError: 'Could not activate the account.',
    saving: 'Saving...',
    back: 'Back to login',
    success: 'The customer was registered with CUSTOMER role.',
    error: 'Could not register the customer.',
    required: 'All fields are required.',
    invalidEmail: 'Enter a valid email address.',
    phoneNumberOnly: 'Phone number must be 8-15 digits, with an optional + prefix.',
    postalCodeNumberOnly: 'Postal code must be exactly 4 digits.',
    houseNumberOnly: 'House number must contain numbers only.',
    mismatch: 'The two passwords do not match.',
    underage: 'Customers under 18 cannot register.',
    companyCapitalization: 'Every word in the company name must start with a capital letter.',
    theme: 'Theme',
    language: 'Language'
  }
};

@Component({
  selector: 'app-register-customer',
  imports: [FormsModule, RouterLink],
  templateUrl: './register-customer.html',
  styleUrl: './register-customer.css'
})
export class RegisterCustomer {
  form: CustomerRegistrationRequest = {
    firstName: '',
    lastName: '',
    birthDate: '',
    email: '',
    phoneNumber: '',
    address: '',
    city: '',
    postalCode: '',
    houseNumber: '',
    companyName: '',
    password: '',
    passwordAgain: ''
  };
  loading = signal(false);
  message = signal('');
  error = signal('');
  registeredEmail = signal('');
  activationToken = '';
  activated = signal(false);

  constructor(
    private readonly authService: AuthService,
    readonly ui: UiSettingsService
  ) {}

  get t() {
    return TEXT[this.ui.language()];
  }

  register(): void {
    this.formatCompanyName();

    const validationError = this.validateForm();

    if (validationError) {
      this.error.set(validationError);
      return;
    }

    this.loading.set(true);
    this.message.set('');
    this.error.set('');

    this.authService.registerCustomer(this.form, this.ui.language()).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.registeredEmail.set(response.email);
        this.activated.set(false);
        this.message.set(response.message || this.t.activationSent);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(error.error?.message || this.t.error);
      }
    });
  }

  private validateForm(): string {
    const requiredFields = [
      this.form.firstName,
      this.form.lastName,
      this.form.birthDate,
      this.form.email,
      this.form.phoneNumber,
      this.form.address,
      this.form.city,
      this.form.postalCode,
      this.form.houseNumber,
      this.form.password,
      this.form.passwordAgain
    ];

    if (requiredFields.some((value) => !String(value).trim())) {
      return this.t.required;
    }

    if (!this.isValidEmail(this.form.email)) {
      return this.t.invalidEmail;
    }

    if (!this.isValidPhoneNumber(this.form.phoneNumber)) {
      return this.t.phoneNumberOnly;
    }

    if (!this.isValidPostalCode(this.form.postalCode)) {
      return this.t.postalCodeNumberOnly;
    }

    if (!this.isOnlyDigits(this.form.houseNumber)) {
      return this.t.houseNumberOnly;
    }

    if (this.form.password !== this.form.passwordAgain) {
      return this.t.mismatch;
    }

    if (this.isUnderage(this.form.birthDate)) {
      return this.t.underage;
    }

    if (this.form.companyName.trim() && !this.isCompanyNameCapitalized(this.form.companyName)) {
      return this.t.companyCapitalization;
    }

    return '';
  }

  activate(): void {
    if (!this.registeredEmail() || !this.activationToken.trim()) {
      this.error.set(this.t.activationError);
      return;
    }

    this.loading.set(true);
    this.message.set('');
    this.error.set('');

    this.authService.activateCustomer(this.registeredEmail(), this.activationToken.trim()).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.activated.set(true);
        this.message.set(response.message || this.t.activationSuccess);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(error.error?.message || this.t.activationError);
      }
    });
  }

  setLanguage(language: Language): void {
    this.ui.setLanguage(language);
    this.error.set('');
    this.message.set('');
  }

  setTheme(theme: Theme): void {
    this.ui.setTheme(theme);
  }

  formatCompanyName(): void {
    this.form.companyName = this.toTitleCase(this.form.companyName);
  }

  private toTitleCase(value: string): string {
    return value
      .trim()
      .replace(/\s+/g, ' ')
      .split(' ')
      .map((word) => word.charAt(0).toLocaleUpperCase('hu-HU') + word.slice(1))
      .join(' ');
  }

  private isCompanyNameCapitalized(value: string): boolean {
    return value
      .trim()
      .split(/\s+/)
      .every((word) => !word || word.charAt(0) === word.charAt(0).toLocaleUpperCase('hu-HU'));
  }

  private isOnlyDigits(value: string): boolean {
    return /^\d+$/.test(value.trim());
  }

  private isValidPhoneNumber(value: string): boolean {
    return /^\+?\d{8,15}$/.test(value.trim());
  }

  private isValidPostalCode(value: string): boolean {
    return /^\d{4}$/.test(value.trim());
  }

  private isValidEmail(value: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());
  }

  private isUnderage(birthDate: string): boolean {
    if (!birthDate) {
      return true;
    }

    const today = new Date();
    const birthday = new Date(birthDate);
    let age = today.getFullYear() - birthday.getFullYear();
    const monthDiff = today.getMonth() - birthday.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthday.getDate())) {
      age -= 1;
    }

    return age < 18;
  }
}
