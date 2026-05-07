package com.backend.management.controller;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.management.enums.Szerepkor;
import com.backend.management.model.Felhasznalo;
import com.backend.management.model.Ugyfel;
import com.backend.management.repository.FelhasznaloRepository;
import com.backend.management.repository.UgyfelRepository;
import com.backend.management.service.EmailService;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/ugyfelek")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class UgyfelController {

    private static final SecureRandom VELETLEN = new SecureRandom();

    private final UgyfelRepository ugyfelRepository;
    private final FelhasznaloRepository felhasznaloRepository;
    private final EmailService emailService;

    public UgyfelController(UgyfelRepository ugyfelRepository,
                            FelhasznaloRepository felhasznaloRepository,
                            EmailService emailService) {
        this.ugyfelRepository = ugyfelRepository;
        this.felhasznaloRepository = felhasznaloRepository;
        this.emailService = emailService;
    }

    @PostMapping("/regisztracio")
    public ResponseEntity<?> regisztracio(@RequestBody UgyfelRegistrationRequest keres) {

        if (keres == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Minden mezot ki kell tolteni."));
        }

        if (uresE(keres.keresztnev()) ||
                uresE(keres.vezeteknev()) ||
                keres.szuletesiDatum() == null ||
                uresE(keres.email()) ||
                uresE(keres.telefonszam()) ||
                uresE(keres.cim()) ||
                uresE(keres.cegNev()) ||
                uresE(keres.jelszo()) ||
                uresE(keres.jelszoMegEgyszer())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Minden mezot ki kell tolteni."));
        }

        int eletkor = Period.between(keres.szuletesiDatum(), LocalDate.now()).getYears();

        if (eletkor < 18) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "18 ev alatti ugyfel nem regisztralhat."));
        }

        Felhasznalo letezoFelhasznalo = felhasznaloRepository.findByEmailIgnoreCase(keres.email()).orElse(null);

        if (letezoFelhasznalo != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ezzel az email cimmel mar van felhasznalo."));
        }

        if (!keres.jelszo().equals(keres.jelszoMegEgyszer())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A ket jelszo nem egyezik."));
        }

        Ugyfel ugyfel = new Ugyfel(
                keres.keresztnev(),
                keres.vezeteknev(),
                keres.email(),
                keres.jelszo(),
                keres.szuletesiDatum(),
                keres.telefonszam(),
                keres.cim(),
                null,
                null,
                null,
                Szerepkor.CUSTOMER,
                false,
                keres.cegNev()
        );

        String aktivaloKod = kodLetrehozas();

        ugyfel.setAktivaloKod(aktivaloKod);
        ugyfel.setAktivaloKodLejar(LocalDateTime.now().plusMinutes(30));

        Ugyfel mentett = ugyfelRepository.save(ugyfel);

        boolean emailElkuldve = emailService.aktivaloKodKuldes(mentett.getEmail(), aktivaloKod);

        String uzenet;

        if (emailElkuldve) {
            uzenet = "A regisztracio sikeres. Az aktivalo kodot elkuldtuk emailben.";
        } else {
            uzenet = "A regisztracio sikeres, de az email kuldes nem sikerult. A kod a backend konzolban lathato.";
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", mentett.getId(),
                "email", mentett.getEmail(),
                "szerepkor", mentett.getSzerepkor(),
                "active", mentett.isActive(),
                "message", uzenet,
                "activationToken", aktivaloKod
        ));
    }

    @PostMapping("/aktivalas")
    public ResponseEntity<?> aktivalas(@RequestBody ActivationRequest keres) {

        if (keres == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email cim es aktivalo kod szukseges."));
        }

        if (uresE(keres.email()) || uresE(keres.kod())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email cim es aktivalo kod szukseges."));
        }

        Felhasznalo felhasznalo = felhasznaloRepository.findByAktivaloKod(keres.kod()).orElse(null);

        if (felhasznalo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Ervenytelen aktivalo kod."));
        }

        if (!felhasznalo.getEmail().equalsIgnoreCase(keres.email())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Ervenytelen aktivalo kod."));
        }

        if (felhasznalo.getAktivaloKodLejar() == null) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("message", "Az aktivalo kod lejart."));
        }

        if (felhasznalo.getAktivaloKodLejar().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("message", "Az aktivalo kod lejart."));
        }

        felhasznalo.setActive(true);
        felhasznalo.setAktivaloKod(null);
        felhasznalo.setAktivaloKodLejar(null);

        felhasznaloRepository.save(felhasznalo);

        return ResponseEntity.ok(Map.of("message", "A fiok sikeresen aktivalva."));
    }

    private boolean uresE(String ertek) {
        return ertek == null || ertek.isBlank();
    }

    private String kodLetrehozas() {
        return String.valueOf(100000 + VELETLEN.nextInt(900000));
    }

    public record UgyfelRegistrationRequest(
            String keresztnev,
            String vezeteknev,
            LocalDate szuletesiDatum,
            String email,
            String telefonszam,
            String cim,
            String cegNev,
            String jelszo,
            String jelszoMegEgyszer
    ) {}

    public record ActivationRequest(
            String email,
            @JsonProperty("token") String kod
    ) {}
}
