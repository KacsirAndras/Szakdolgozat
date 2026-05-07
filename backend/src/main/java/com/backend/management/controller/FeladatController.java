package com.backend.management.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.management.enums.FeladatStatusz;
import com.backend.management.enums.Prioritas;
import com.backend.management.enums.ProjektStatusz;
import com.backend.management.enums.Szerepkor;
import com.backend.management.model.Alkalmazott;
import com.backend.management.model.Feladat;
import com.backend.management.model.Felhasznalo;
import com.backend.management.model.Projekt;
import com.backend.management.repository.AlkalmazottRepository;
import com.backend.management.repository.FeladatRepository;
import com.backend.management.repository.ProjektRepository;

@RestController
@RequestMapping("/api/feladatok")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class FeladatController {

    private final FeladatRepository feladatRepository;
    private final ProjektRepository projektRepository;
    private final AlkalmazottRepository alkalmazottRepository;

    public FeladatController(FeladatRepository feladatRepository,
                             ProjektRepository projektRepository,
                             AlkalmazottRepository alkalmazottRepository) {
        this.feladatRepository = feladatRepository;
        this.projektRepository = projektRepository;
        this.alkalmazottRepository = alkalmazottRepository;
    }

    @GetMapping
    public ResponseEntity<?> feladatokListazasa(@RequestParam String email) {

        Alkalmazott alkalmazott = alkalmazottRepository.findByEmailIgnoreCase(email).orElse(null);

        if (alkalmazott == null || !alkalmazott.isActive()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen aktiv alkalmazott."));
        }

        List<Feladat> feladatok;

        if (alkalmazott.getSzerepkor() == Szerepkor.ADMIN) {
            feladatok = feladatRepository.findAll();
        } else if (alkalmazott.getSzerepkor() == Szerepkor.PRODUCT_OWNER) {
            feladatok = feladatRepository
                    .findByProjektTermekTulajdonosEmailIgnoreCaseAndProjektStatusz(email, ProjektStatusz.ACCEPTED);
        } else if (alkalmazott.getSzerepkor() == Szerepkor.DEVELOPER) {
            if (alkalmazott.getCsapat() == null) {
                return ResponseEntity.ok(List.of());
            }

            feladatok = feladatRepository
                    .findByProjektCsapatIdAndProjektStatusz(
                            alkalmazott.getCsapat().getId(),
                            ProjektStatusz.ACCEPTED
                    );
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN, PRODUCT_OWNER vagy DEVELOPER hasznalhatja a feladat menut."));
        }

        List<TaskResponse> valasz = feladatok.stream()
                .map(this::valassaAlakitas)
                .toList();

        return ResponseEntity.ok(valasz);
    }

    @PostMapping
    public ResponseEntity<?> feladatLetrehozas(@RequestParam String email, @RequestBody CreateTaskRequest keres) {

        Alkalmazott tulajdonos = alkalmazottRepository.findByEmailIgnoreCase(email).orElse(null);

        if (tulajdonos == null || !tulajdonos.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nincs jogosultsagod feladat letrehozasahoz."));
        }

        if (tulajdonos.getSzerepkor() != Szerepkor.ADMIN &&
                tulajdonos.getSzerepkor() != Szerepkor.PRODUCT_OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN vagy PRODUCT_OWNER hozhat letre taskot."));
        }

        if (keres == null || uresE(keres.cim()) || keres.projektId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Projekt es cim kotelezo."));
        }

        Projekt projekt = projektRepository.findById(keres.projektId()).orElse(null);

        if (projekt == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen projekt."));
        }

        if (tulajdonos.getSzerepkor() != Szerepkor.ADMIN) {
            if (projekt.getTermekTulajdonos() == null ||
                    !projekt.getTermekTulajdonos().getId().equals(tulajdonos.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Csak a sajat projektedhez hozhatsz letre taskot."));
            }
        }

        if (projekt.getStatusz() != ProjektStatusz.ACCEPTED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Csak ACCEPTED projekthez hozhato letre feladat."));
        }

        Feladat feladat = new Feladat(
                projekt,
                keres.cim().trim(),
                keres.leiras(),
                keres.prioritas()
        );

        Feladat mentett = feladatRepository.save(feladat);

        return ResponseEntity.ok(valassaAlakitas(mentett));
    }

    @PostMapping("/{id}/take")
    public ResponseEntity<?> feladatFelvetel(@PathVariable Long id, @RequestParam String email) {

        Alkalmazott DEVELOPER = alkalmazottRepository.findByEmailIgnoreCase(email).orElse(null);

        if (DEVELOPER == null || !DEVELOPER.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER vehet fel taskot."));
        }

        if (DEVELOPER.getSzerepkor() != Szerepkor.ADMIN &&
                DEVELOPER.getSzerepkor() != Szerepkor.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN vagy DEVELOPER vehet fel taskot."));
        }

        Feladat feladat = feladatRepository.findById(id).orElse(null);

        if (feladat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen feladat."));
        }

        if (feladat.getStatusz() != FeladatStatusz.TO_DO) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Csak TO_DO feladat veheto fel."));
        }

        if (feladat.getMunkatars() != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ez a feladat mar valakihez tartozik."));
        }

        if (DEVELOPER.getSzerepkor() != Szerepkor.ADMIN) {
            if (!azonosCsapat(DEVELOPER, feladat.getProjekt())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Csak a sajat csapatod projektjen dolgozhatsz."));
            }
        }

        feladat.setMunkatars(DEVELOPER);
        feladat.setStatusz(FeladatStatusz.IN_PROGRESS);

        Feladat mentett = feladatRepository.save(feladat);

        return ResponseEntity.ok(valassaAlakitas(mentett));
    }

    @PostMapping("/{id}/done")
    public ResponseEntity<?> feladatLezaras(@PathVariable Long id, @RequestParam String email) {

        Alkalmazott DEVELOPER = alkalmazottRepository.findByEmailIgnoreCase(email).orElse(null);

        if (DEVELOPER == null || !DEVELOPER.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy DEVELOPER zarhat taskot."));
        }

        if (DEVELOPER.getSzerepkor() != Szerepkor.ADMIN &&
                DEVELOPER.getSzerepkor() != Szerepkor.DEVELOPER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN vagy DEVELOPER zarhat taskot."));
        }

        Feladat feladat = feladatRepository.findById(id).orElse(null);

        if (feladat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen feladat."));
        }

        if (feladat.getStatusz() != FeladatStatusz.IN_PROGRESS) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Csak IN_PROGRESS feladat zarhato le."));
        }

        if (feladat.getMunkatars() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A feladat nincs senkihez rendelve."));
        }

        if (DEVELOPER.getSzerepkor() != Szerepkor.ADMIN) {
            if (!feladat.getMunkatars().getId().equals(DEVELOPER.getId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Csak a sajat taskodat zarhatod le."));
            }
        }

        feladat.setStatusz(FeladatStatusz.DONE);

        Feladat mentett = feladatRepository.save(feladat);

        return ResponseEntity.ok(valassaAlakitas(mentett));
    }

    private boolean azonosCsapat(Alkalmazott DEVELOPER, Projekt projekt) {

        if (DEVELOPER.getCsapat() == null) {
            return false;
        }

        if (projekt.getCsapat() == null) {
            return false;
        }

        return DEVELOPER.getCsapat().getId().equals(projekt.getCsapat().getId());
    }

    private TaskResponse valassaAlakitas(Feladat feladat) {

        Alkalmazott munkatars = feladat.getMunkatars();
        Projekt projekt = feladat.getProjekt();

        Long munkatarsId = null;
        String munkatarsNev = null;
        String munkatarsEmail = null;

        if (munkatars != null) {
            munkatarsId = munkatars.getId();
            munkatarsNev = teljesNev(munkatars);
            munkatarsEmail = munkatars.getEmail();
        }

        return new TaskResponse(
                feladat.getId(),
                projekt.getId(),
                projekt.getCim(),
                feladat.getCim(),
                feladat.getLeiras(),
                feladat.getStatusz(),
                feladat.getPrioritas(),
                munkatarsId,
                munkatarsNev,
                munkatarsEmail
        );
    }

    private String teljesNev(Felhasznalo felhasznalo) {
        return felhasznalo.getKeresztnev() + " " + felhasznalo.getVezeteknev();
    }

    private boolean uresE(String ertek) {
        return ertek == null || ertek.isBlank();
    }

    public record CreateTaskRequest(
            Long projektId,
            String cim,
            String leiras,
            Prioritas prioritas
    ) {}

    public record TaskResponse(
            Long id,
            Long projektId,
            String projektCim,
            String cim,
            String leiras,
            FeladatStatusz statusz,
            Prioritas prioritas,
            Long munkatarsId,
            String munkatarsNev,
            String munkatarsEmail
    ) {}
}