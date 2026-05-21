import { expect, test, type Page } from '@playwright/test';

const customer = {
  firstName: 'Teszt',
  lastName: 'Ugyfel',
  email: 'playwright.customer@test.hu',
  password: 'test123',
  newPassword: 'test456',
  birthDate: '1996-02-05',
  phoneNumber: '+36201234567',
  updatedPhoneNumber: '+36207654321',
  postalCode: '3525',
  city: 'Miskolc',
  address: 'Szinyei Merse utca',
  houseNumber: '10',
  companyName: 'Geoform Kft'
};

type ProjectItem = {
  id: number;
  title: string;
  description: string;
  status: 'PENDING';
  budget: number;
  startDate: string;
  deadline: string;
  creatorEmail: string;
  creatorName: string;
  productOwnerEmail: null;
  productOwnerName: null;
  teamName: null;
};

type TicketItem = {
  id: number;
  type: 'BUG' | 'FEATURE' | 'HELP';
  status: 'CLOSED';
  problem: string;
  createdByEmail: string;
  createdByName: string;
  createdByRole: 'CUSTOMER';
  itEmployeeEmail: null;
  itEmployeeName: null;
  itEmployeeClosedTickets: null;
};

test('ugyfel teljes felhasznaloi folyamata', async ({ page }) => {
  await mockBackend(page);

  await page.goto('/login');

  await page.getByRole('button', { name: 'Ugyfel regisztracio' }).click();
  await expect(page.getByRole('heading', { name: 'Ügyfél regisztráció' })).toBeVisible();

  await page.locator('#firstName').fill(customer.firstName);
  await page.locator('#lastName').fill(customer.lastName);
  await page.locator('#email').fill(customer.email);
  await page.locator('#password').fill(customer.password);
  await page.locator('#passwordAgain').fill(customer.password);
  await page.locator('#birthDate').fill(customer.birthDate);
  await page.locator('#phoneNumber').fill(customer.phoneNumber);
  await page.locator('#postalCode').fill(customer.postalCode);
  await page.locator('#city').fill(customer.city);
  await page.locator('#address').fill(customer.address);
  await page.locator('#houseNumber').fill(customer.houseNumber);
  await page.locator('#companyName').fill(customer.companyName);

  await page.getByRole('button', { name: 'Ügyfél regisztrálása' }).click();
  await expect(page.getByText('Registration succeeded.')).toBeVisible();

  await page.locator('#activationToken').fill('123456');
  await page.getByRole('button', { name: 'Fiók aktiválása' }).click();
  await expect(page.getByText('A fiok aktivalva.')).toBeVisible();

  await page.getByRole('link', { name: 'Vissza a loginhoz' }).click();
  await page.getByRole('button', { name: 'Elfelejtett jelszo?' }).click();

  await page.locator('#email').fill(customer.email);
  await page.getByRole('button', { name: 'Token küldése' }).click();
  await expect(page.getByText(`${customer.email} címre elküldtük a tokent.`)).toBeVisible();

  await page.locator('#token').fill('654321');
  await page.locator('#newPassword').fill(customer.newPassword);
  await page.locator('#newPasswordAgain').fill(customer.newPassword);
  await page.getByRole('button', { name: 'Jelszó módosítása' }).click();
  await expect(page.getByText('Password changed')).toBeVisible();

  await page.getByRole('link', { name: 'Vissza a loginhoz' }).click();
  await page.locator('#email').fill(customer.email);
  await page.locator('#password').fill(customer.newPassword);
  await page.getByRole('button', { name: 'Belepes' }).click();

  await expect(page).toHaveURL(/\/management$/);
  await expect(page.getByRole('heading', { name: 'Profil' })).toBeVisible();
  await expect(page.getByText(`${customer.firstName} ${customer.lastName}`)).toBeVisible();

  await page.getByRole('button', { name: 'Módosít' }).click();
  await page.locator('#editPhone').fill(customer.updatedPhoneNumber);
  await page.getByRole('button', { name: 'Profil mentese' }).click();
  await expect(page.getByText('Profil modositva')).toBeVisible();
  await expect(page.getByText(customer.updatedPhoneNumber)).toBeVisible();

  await page.getByRole('button', { name: 'Password change' }).click();
  await page.locator('#oldPassword').fill(customer.newPassword);
  await page.locator('#newPassword').fill('test789');
  await page.locator('#newPasswordAgain').fill('test789');
  await page.getByRole('button', { name: 'Save password' }).click();
  await expect(page.getByText('Password changed successfully')).toBeVisible();

  await page.getByRole('button', { name: 'IT tamogatas' }).click();
  await expect(page.getByRole('heading', { name: 'IT support' })).toBeVisible();
  await page.getByRole('button', { name: 'Új jegy' }).click();
  await page.locator('#ticketType').selectOption('HELP');
  await page.locator('#ticketProblema').fill('Nem sikerul megnyitni a profil oldalt.');
  await page.getByRole('button', { name: 'Jegy létrehozása' }).click();
  await expect(page.getByText('A jegy letrejott CLOSED statusban.')).toBeVisible();
  await expect(page.getByText('Nem sikerul megnyitni a profil oldalt.')).toBeVisible();

  await page.getByRole('button', { name: 'Projektek' }).click();
  await expect(page.getByRole('heading', { name: 'Projektek' })).toBeVisible();
  await page.getByRole('button', { name: 'Új projekt' }).click();
  await page.locator('#projectTitle').fill('Playwright teszt projekt');
  await page.locator('#projectLeiras').fill('Automatizalt feluleti tesztbol letrehozott projekt.');
  await page.locator('#projectBudget').fill('6000000');
  await page.locator('#projectHatarido').fill('2026-12-31');
  await page.getByRole('button', { name: 'Projekt létrehozása' }).click();
  await expect(page.getByText('A project PENDING statusban letrejott.')).toBeVisible();
  await expect(page.getByText('Playwright teszt projekt')).toBeVisible();
  await expect(page.getByText('PENDING')).toBeVisible();

  await page.getByRole('button', { name: 'Kijelentkezes' }).click();
  await expect(page).toHaveURL(/\/login$/);
});

async function mockBackend(page: Page): Promise<void> {
  let currentPassword = customer.password;
  let profile = createProfile(customer.phoneNumber);
  const tickets: TicketItem[] = [];
  const projects: ProjectItem[] = [];

  await page.route('**/api/customers/register', async (route) => {
    await route.fulfill({
      status: 201,
      contentType: 'application/json',
      body: JSON.stringify({
        id: 9001,
        email: customer.email,
        role: 'CUSTOMER',
        active: false,
        message: 'Registration succeeded. The activation code was sent by email.'
      })
    });
  });

  await page.route('**/api/customers/activate', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'A fiok aktivalva.' })
    });
  });

  await page.route('**/api/auth/forgot-password', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Kod elkuldve', email: customer.email })
    });
  });

  await page.route('**/api/auth/reset-password', async (route) => {
    const request = route.request().postDataJSON() as { newPassword: string };
    currentPassword = request.newPassword;
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Password changed' })
    });
  });

  await page.route('**/api/auth/login', async (route) => {
    const request = route.request().postDataJSON() as { email: string; password: string };

    if (request.email !== customer.email || request.password !== currentPassword) {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Hibas email vagy password' })
      });
      return;
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        email: customer.email,
        firstName: customer.firstName,
        lastName: customer.lastName,
        role: 'CUSTOMER',
        active: true
      })
    });
  });

  await page.route('**/api/auth/profile?**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(profile)
    });
  });

  await page.route('**/api/auth/update-profile', async (route) => {
    const request = route.request().postDataJSON() as typeof profile;
    profile = { ...profile, ...request, role: 'CUSTOMER' };
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        email: profile.email,
        firstName: profile.firstName,
        lastName: profile.lastName,
        message: 'Profil modositva'
      })
    });
  });

  await page.route('**/api/auth/change-password', async (route) => {
    const request = route.request().postDataJSON() as { newPassword: string };
    currentPassword = request.newPassword;
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Password changed successfully' })
    });
  });

  await page.route('**/api/tickets?**', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(tickets)
      });
      return;
    }

    const request = route.request().postDataJSON() as { type: TicketItem['type']; problem: string };
    tickets.push({
      id: 7001,
      type: request.type,
      status: 'CLOSED',
      problem: request.problem,
      createdByEmail: customer.email,
      createdByName: `${customer.firstName} ${customer.lastName}`,
      createdByRole: 'CUSTOMER',
      itEmployeeEmail: null,
      itEmployeeName: null,
      itEmployeeClosedTickets: null
    });

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(tickets.at(-1))
    });
  });

  await page.route('**/api/projects?**', async (route) => {
    if (route.request().method() === 'GET') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(projects)
      });
      return;
    }

    const request = route.request().postDataJSON() as {
      title: string;
      description: string;
      budget: number;
      deadline: string;
    };

    projects.push({
      id: 8001,
      title: request.title,
      description: request.description,
      status: 'PENDING',
      budget: request.budget,
      startDate: '2026-05-20',
      deadline: request.deadline,
      creatorEmail: customer.email,
      creatorName: `${customer.firstName} ${customer.lastName}`,
      productOwnerEmail: null,
      productOwnerName: null,
      teamName: null
    });

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(projects.at(-1))
    });
  });
}

function createProfile(phoneNumber: string) {
  return {
    firstName: customer.firstName,
    lastName: customer.lastName,
    email: customer.email,
    birthDate: customer.birthDate,
    phoneNumber,
    postalCode: customer.postalCode,
    city: customer.city,
    address: customer.address,
    houseNumber: customer.houseNumber,
    role: 'CUSTOMER',
    companyName: customer.companyName,
    taxId: null,
    identityCardNumber: null,
    socialSecurityNumber: null,
    salary: null,
    netSalary: null
  };
}
