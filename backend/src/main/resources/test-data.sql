-- Teszt userk a springbootmanagement adatbazishoz.
-- Minden password: test123
-- Ha korabban volt TEAM_OWNER adatod, ez PRODUCT_OWNER-re javitja.
UPDATE users
SET role = 'PRODUCT_OWNER'
WHERE role = 'TEAM_OWNER';

INSERT INTO users (
    id,
    first_name,
    last_name,
    email,
    password,
    birth_date,
    phone_number,
    address,
    city,
    postal_code,
    house_number,
    role,
    active
) VALUES
    (1001, 'Admin', 'Teszt', 'admin@test.hu', 'test123', '1990-01-01', '+36201111111', 'Fo utca', 'Budapest', '1111', '1', 'ADMIN', true),
    (1002, 'Dev', 'Egy', 'developer1@test.hu', 'test123', '1995-02-02', '+36202222221', 'Kod utca', 'Budapest', '1112', '2', 'DEVELOPER', true),
    (1003, 'Dev', 'Ketto', 'developer2@test.hu', 'test123', '1996-03-03', '+36202222222', 'Kod utca', 'Budapest', '1113', '3', 'DEVELOPER', true),
    (1004, 'HR', 'Teszt', 'hr@test.hu', 'test123', '1991-04-04', '+36203333333', 'Ember utca', 'Budapest', '1114', '4', 'HR', true),
    (1005, 'IT', 'Teszt', 'it@test.hu', 'test123', '1992-05-05', '+36204444444', 'Support utca', 'Budapest', '1115', '5', 'IT', true),
    (1006, 'Product', 'Owner', 'product.owner@test.hu', 'test123', '1989-06-06', '+36205555555', 'Termek utca', 'Budapest', '1116', '6', 'PRODUCT_OWNER', true),
    (1010, 'Demo', 'Customer', 'customer@test.hu', 'test123', '1987-07-07', '+36206666666', 'Megrendelo utca', 'Gyor', '9021', '7', 'CUSTOMER', true),
    (1011, 'Partner', 'Ceg', 'partner@test.hu', 'test123', '1986-08-08', '+36207777777', 'Piac ter', 'Pecs', '7621', '8', 'CUSTOMER', true)
ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    email = VALUES(email),
    password = VALUES(password),
    birth_date = VALUES(birth_date),
    phone_number = VALUES(phone_number),
    address = VALUES(address),
    city = VALUES(city),
    postal_code = VALUES(postal_code),
    house_number = VALUES(house_number),
    role = VALUES(role),
    active = VALUES(active);

INSERT INTO employees (
    id,
    tax_id,
    identity_card_number,
    social_security_number,
    salary,
    hire_date,
    team_id
) VALUES
    (1001, '8000000001', '900101-1001', '111111111', 900000, '2019-01-01', null),
    (1002, '8000000002', '950202-1002', '222222222', 650000, '2024-01-01', null),
    (1003, '8000000003', '960303-1003', '333333333', 680000, '2024-02-01', null),
    (1004, '8000000004', '910404-1004', '444444444', 600000, '2023-01-01', null),
    (1005, '8000000005', '920505-1005', '555555555', 620000, '2022-01-01', null),
    (1006, '8000000006', '890606-1006', '666666666', 850000, '2020-01-01', null)
ON DUPLICATE KEY UPDATE
    tax_id = VALUES(tax_id),
    identity_card_number = VALUES(identity_card_number),
    social_security_number = VALUES(social_security_number),
    salary = VALUES(salary),
    hire_date = VALUES(hire_date),
    team_id = VALUES(team_id);

INSERT INTO customers (
    id,
    company_name
) VALUES
    (1010, 'Demo Customer Kft.'),
    (1011, 'Partner Rendszer Zrt.')
ON DUPLICATE KEY UPDATE
    company_name = VALUES(company_name);

INSERT INTO teams (
    id,
    name,
    owner_id
) VALUES
    (2001, 'Teszt fejlesztoi team', 1006)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    owner_id = VALUES(owner_id);

UPDATE employees
SET team_id = 2001
WHERE id IN (1002, 1003);

INSERT INTO projects (
    id,
    creator_id,
    product_owner_id,
    team_id,
    title,
    description,
    status,
    budget,
    start_date,
    deadline,
    completed_at
) VALUES
    (3001, 1010, 1006, 2001, 'Aktiv webaruhaz fejlesztes', 'Elfogadott teszt project fejlesztoi taskokkal.', 'ACCEPTED', 8500000, '2026-01-15', '2026-09-30', null),
    (3002, 1010, null, null, 'Uj CRM igeny', 'Elfogadasra varo customer project.', 'PENDING', 6000000, '2026-02-01', '2026-10-15', null),
    (3003, 1011, 1006, 2001, 'Lezart riport modul', 'Befejezett project statisztikai bevetelekhez.', 'COMPLETED', 7200000, '2025-03-01', '2025-11-30', '2025-12-05'),
    (3004, 1011, null, null, 'Elutasitott prototype', 'Elutasitott demo project.', 'REJECTED', 4500000, '2026-03-01', '2026-08-31', null)
ON DUPLICATE KEY UPDATE
    creator_id = VALUES(creator_id),
    product_owner_id = VALUES(product_owner_id),
    team_id = VALUES(team_id),
    title = VALUES(title),
    description = VALUES(description),
    status = VALUES(status),
    budget = VALUES(budget),
    start_date = VALUES(start_date),
    deadline = VALUES(deadline),
    completed_at = VALUES(completed_at);

INSERT INTO tasks (
    id,
    project_id,
    employee_id,
    title,
    description,
    status,
    priority
) VALUES
    (4001, 3001, 1002, 'Kosar folyamat', 'Kosar oldal es rendelesi folyamat kialakitasa.', 'IN_PROGRESS', 'HIGH'),
    (4002, 3001, 1003, 'Admin termekkezeles', 'Termek CRUD es jogosultsagok.', 'TO_DO', 'MEDIUM'),
    (4003, 3003, 1002, 'Riport export', 'PDF export kesz modul lezart taska.', 'DONE', 'LOW')
ON DUPLICATE KEY UPDATE
    project_id = VALUES(project_id),
    employee_id = VALUES(employee_id),
    title = VALUES(title),
    description = VALUES(description),
    status = VALUES(status),
    priority = VALUES(priority);
