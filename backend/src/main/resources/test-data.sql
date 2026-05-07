-- Teszt felhasznalok a springbootmanagement adatbazishoz.
-- Minden jelszo: test123
-- Ha korabban volt TEAM_OWNER adatod, ez PRODUCT_OWNER-re javitja.
UPDATE Felhasznalo
SET szerepkor = 'PRODUCT_OWNER'
WHERE szerepkor = 'TEAM_OWNER';

INSERT INTO Felhasznalo (
    id,
    keresztnev,
    vezeteknev,
    email,
    jelszo,
    szuletesi_datum,
    telefonszam,
    cim,
    varos,
    iranyitoszam,
    hazszam,
    szerepkor,
    aktiv
) VALUES
    (1001, 'Admin', 'Teszt', 'admin@test.hu', 'test123', '1990-01-01', '+36201111111', 'Fo utca', 'Budapest', '1111', '1', 'ADMIN', true),
    (1002, 'Dev', 'Egy', 'developer1@test.hu', 'test123', '1995-02-02', '+36202222221', 'Kod utca', 'Budapest', '1112', '2', 'DEVELOPER', true),
    (1003, 'Dev', 'Ketto', 'developer2@test.hu', 'test123', '1996-03-03', '+36202222222', 'Kod utca', 'Budapest', '1113', '3', 'DEVELOPER', true),
    (1004, 'HR', 'Teszt', 'hr@test.hu', 'test123', '1991-04-04', '+36203333333', 'Ember utca', 'Budapest', '1114', '4', 'HR', true),
    (1005, 'IT', 'Teszt', 'it@test.hu', 'test123', '1992-05-05', '+36204444444', 'Support utca', 'Budapest', '1115', '5', 'IT', true),
    (1006, 'Product', 'Owner', 'product.owner@test.hu', 'test123', '1989-06-06', '+36205555555', 'Termek utca', 'Budapest', '1116', '6', 'PRODUCT_OWNER', true)
ON DUPLICATE KEY UPDATE
    keresztnev = VALUES(keresztnev),
    vezeteknev = VALUES(vezeteknev),
    email = VALUES(email),
    jelszo = VALUES(jelszo),
    szuletesi_datum = VALUES(szuletesi_datum),
    telefonszam = VALUES(telefonszam),
    cim = VALUES(cim),
    varos = VALUES(varos),
    iranyitoszam = VALUES(iranyitoszam),
    hazszam = VALUES(hazszam),
    szerepkor = VALUES(szerepkor),
    aktiv = VALUES(aktiv);

INSERT INTO Alkalmazott (
    id,
    adoazonosito,
    szemelyi_szam,
    fizetes,
    csapat_id
) VALUES
    (1001, '8000000001', '900101-1001', 900000, null),
    (1002, '8000000002', '950202-1002', 650000, null),
    (1003, '8000000003', '960303-1003', 680000, null),
    (1004, '8000000004', '910404-1004', 600000, null),
    (1005, '8000000005', '920505-1005', 620000, null),
    (1006, '8000000006', '890606-1006', 850000, null)
ON DUPLICATE KEY UPDATE
    adoazonosito = VALUES(adoazonosito),
    szemelyi_szam = VALUES(szemelyi_szam),
    fizetes = VALUES(fizetes),
    csapat_id = VALUES(csapat_id);
