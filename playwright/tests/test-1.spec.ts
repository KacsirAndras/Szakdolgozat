import { test, expect } from '@playwright/test';

test('Customer registration page shows required fields and error messages', async ({ page }) => {
  await page.goto('http://localhost:4200/login');

  await page.getByRole('button', { name: 'EN' }).click();
  await expect(page.getByText('Language changed to English.')).toBeVisible();

  await page.getByRole('button', { name: 'Customer registration' }).click();

  const englishLabels = [
    page.getByText('First Name'),
    page.getByText('Last Name'),
    page.getByText('Email Address'),
    page.getByText('Password', { exact: true }),
    page.getByText('Password again'),
    page.getByText('Date of Birth'),
    page.getByText('Phone'),
    page.getByText('Postal code'),
    page.getByText('City'),
    page.getByText('Address', { exact: true }),
    page.getByText('House number'),
    page.getByText('Company name (optional)')
  ];

  for (const label of englishLabels) {
    await expect(label).toBeVisible();
  }

  await page.getByRole('button', { name: 'Register Customer' }).click();
  await expect(page.getByText('All fields are required.')).toBeVisible();

  await page.getByRole('link', { name: 'Back to login' }).click();

  await page.getByRole('button', { name: 'Customer registration' }).click();
  await page.getByRole('button', { name: 'HU' }).click();

  const hungarianLabels = [
    page.getByText('Email cím'),
    page.getByText('Jelszó', { exact: true }),
    page.getByText('Jelszó újra'),
    page.getByText('Születési dátum'),
    page.getByText('Telefon'),
    page.getByText('Irányítószám'),
    page.getByText('Város'),
    page.getByText('Házszám'),
    page.getByText('Cím', { exact: true }),
    page.getByText('Cég név (nem kötelező)')
  ];

  for (const label of hungarianLabels) {
    await expect(label).toBeVisible();
  }

  await page.getByRole('button', { name: 'Ügyfél regisztrálása' }).click();
  await expect(page.getByText('Minden mezőt ki kell tölteni.')).toBeVisible();

  await page.getByRole('link', { name: 'Vissza a loginhoz' }).click();
  await page.getByRole('button', { name: 'Ugyfel regisztracio' }).click();

  await expect(page.getByRole('textbox', { name: 'Email cím' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Keresztnév' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Vezetéknév' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Jelszó', exact: true })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Jelszó újra' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Születési dátum' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Telefon' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Irányítószám' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Város' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Cím', exact: true })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Házszám' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Cég név (nem kötelező)' })).toBeVisible();

  // Example fill actions are kept as comments for future test detail:
  await page.getByRole('textbox', { name: 'Keresztnév' }).fill('Kacsir');
  await page.getByRole('textbox', { name: 'Vezetéknév' }).fill('András');
  await page.getByRole('textbox', { name: 'Email cím' }).fill('wasd');
  await page.getByRole('textbox', { name: 'Jelszó', exact: true }).fill('123456');
  await page.getByRole('textbox', { name: 'Jelszó újra' }).fill('1234567');
  await page.getByRole('textbox', { name: 'Születési dátum' }).fill('1996-02-05');
  await page.getByRole('textbox', { name: 'Telefon' }).fill('telefon');
  await page.getByRole('textbox', { name: 'Irányítószám' }).fill('szam');
  await page.getByRole('textbox', { name: 'Város' }).fill('Miskolc');
  await page.getByRole('textbox', { name: 'Cím', exact: true }).fill('Szinyei Merse');
  await page.getByRole('textbox', { name: 'Házszám' }).fill('szám');
  await page.getByRole('textbox', { name: 'Cég név (nem kötelező)' }).fill('Geoform');
  await page.getByRole('button', { name: 'Ügyfél regisztrálása' }).click();
  await expect(page.getByText('Adj meg egy érvényes email cí')).toBeVisible();
  await expect(page.getByText('A telefonszám 8-15 számjegy')).toBeVisible();
  await expect(page.getByText('Ezzel az email cimmel mar van')).toBeVisible();
});
