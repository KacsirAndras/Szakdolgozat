package com.backend.management.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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
import com.backend.management.repository.FelhasznaloRepository;
import com.backend.management.service.EmailService;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class AuthController {

    private final FelhasznaloRepository felhasznaloRepository;
    private final EmailService emailService;
    private static final SecureRandom VELETLEN = new SecureRandom();

    public AuthController(FelhasznaloRepository felhasznaloRepository, EmailService emailService) {
        this.felhasznaloRepository = felhasznaloRepository;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> bejelentkezes(@RequestBody LoginRequest keres) {

        Felhasznalo felhasznalo = felhasznaloRepository.findByEmailIgnoreCase(keres.email()).orElse(null);

        if (felhasznalo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Hibas email vagy jelszo"));
        }

        if (!felhasznalo.getJelszo().equals(keres.jelszo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Hibas email vagy jelszo"));
        }

        if (!felhasznalo.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "A felhasznalo nem aktiv"));
        }

        return ResponseEntity.ok(new LoginResponse(
                felhasznalo.getEmail(),
                felhasznalo.getKeresztnev(),
                felhasznalo.getVezeteknev(),
                felhasznalo.getSzerepkor(),
                felhasznalo.isActive()
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> elfelejtettJelszo(@RequestBody ForgotPasswordRequest keres) {

        Felhasznalo felhasznalo = felhasznaloRepository.findByEmailIgnoreCase(keres.email()).orElse(null);

        if (felhasznalo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen felhasznalo"));
        }

        if (!felhasznalo.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nem aktiv felhasznalo"));
        }

        String kod = String.valueOf(100000 + VELETLEN.nextInt(900000));
        felhasznalo.setVisszaallitoKod(kod);
        felhasznalo.setVisszaallitoKodLejar(LocalDateTime.now().plusMinutes(15));
        felhasznaloRepository.save(felhasznalo);

        boolean elkuldve = emailService.jelszoVisszaallitoKodKuldes(felhasznalo.getEmail(), kod);

        if (!elkuldve) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Email kuldes hiba"));
        }

        return ResponseEntity.ok(Map.of("message", "Kod elkuldve"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> jelszoVisszaallitas(@RequestBody ResetPasswordRequest keres) {

        if (!keres.ujJelszo().equals(keres.ujJelszoMegEgyszer())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A ket jelszo nem egyezik"));
        }

        Felhasznalo felhasznalo = felhasznaloRepository.findByVisszaallitoKod(keres.kod()).orElse(null);

        if (felhasznalo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Ervenytelen kod"));
        }

        if (felhasznalo.getVisszaallitoKodLejar().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("message", "kod lejart"));
        }

        felhasznalo.setJelszo(keres.ujJelszo());
        felhasznalo.setVisszaallitoKod(null);
        felhasznaloRepository.save(felhasznalo);

        return ResponseEntity.ok(Map.of("message", "Jelszo modositva"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> jelszoValtoztatas(@RequestBody ChangePasswordRequest keres) {

        Felhasznalo felhasznalo = felhasznaloRepository.findByEmailIgnoreCase(keres.email()).orElse(null);

        if (felhasznalo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen felhasznalo"));
        }

        if (!felhasznalo.getJelszo().equals(keres.regiJelszo())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Rossz regi jelszo"));
        }

        if (!keres.ujJelszo().equals(keres.ujJelszoMegEgyszer())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Nem egyezik az uj jelszo"));
        }

        felhasznalo.setJelszo(keres.ujJelszo());
        felhasznaloRepository.save(felhasznalo);

        return ResponseEntity.ok(Map.of("message", "Jelszo sikeresen modositva"));
    }

    public record LoginRequest(String email, String jelszo) {}
    public record LoginResponse(String email, String keresztnev, String vezeteknev, Szerepkor szerepkor, boolean active) {}
    public record ForgotPasswordRequest(String email) {}
    public record ResetPasswordRequest(
            @JsonProperty("token") String kod,
            @JsonProperty("newPassword") String ujJelszo,
            @JsonProperty("newPasswordAgain") String ujJelszoMegEgyszer
    ) {}
    public record ChangePasswordRequest(
            String email,
            @JsonProperty("oldPassword") String regiJelszo,
            @JsonProperty("newPassword") String ujJelszo,
            @JsonProperty("newPasswordAgain") String ujJelszoMegEgyszer
    ) {}
}
