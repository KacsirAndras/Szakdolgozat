package com.backend.management.controller;

import com.backend.management.enums.FeladatStatusz;
import com.backend.management.enums.ProjektStatusz;
import com.backend.management.enums.Szerepkor;
import com.backend.management.model.Alkalmazott;
import com.backend.management.repository.AlkalmazottRepository;
import com.backend.management.repository.CsapatRepository;
import com.backend.management.repository.FeladatRepository;
import com.backend.management.repository.ProjektRepository;
import com.backend.management.service.SalaryCalculator;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hr")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class HumanResourcesController {
    private final AlkalmazottRepository alkalmazottRepository;
    private final FeladatRepository feladatRepository;
    private final ProjektRepository projektRepository;
    private final CsapatRepository csapatRepository;

    public HumanResourcesController(AlkalmazottRepository alkalmazottRepository,
                                    FeladatRepository feladatRepository,
                                    ProjektRepository projektRepository,
                                    CsapatRepository csapatRepository) {
        this.alkalmazottRepository = alkalmazottRepository;
        this.feladatRepository = feladatRepository;
        this.projektRepository = projektRepository;
        this.csapatRepository = csapatRepository;
    }

    @GetMapping("/employees")
    public ResponseEntity<?> alkalmazottakListazasa(@RequestParam String requesterEmail) {
        if (!hasznalhatjaHumanResourcesE(requesterEmail)) {
            return tiltott();
        }

        List<EmployeeResponse> alkalmazottak = alkalmazottRepository.findAll()
                .stream()
                .map(this::valassaAlakitas)
                .toList();

        return ResponseEntity.ok(alkalmazottak);
    }

    @PostMapping("/employees")
    public ResponseEntity<?> alkalmazottLetrehozas(@RequestParam String requesterEmail,
                                            @RequestBody EmployeeRequest keres) {
        if (!hasznalhatjaHumanResourcesE(requesterEmail)) {
            return tiltott();
        }

        if (uresE(keres.email()) || uresE(keres.jelszo()) || uresE(keres.keresztnev()) ||
                uresE(keres.vezeteknev()) || keres.szerepkor() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Keresztnev, vezeteknev, email, jelszo es szerepkor kotelezo."));
        }

        if (alkalmazottRepository.findByEmailIgnoreCase(keres.email()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ezzel az email cimmel mar van alkalmazott."));
        }

        String ellenorzesiHiba = alkalmazottKeresEllenorzes(keres);
        if (ellenorzesiHiba != null) {
            return ResponseEntity.badRequest().body(Map.of("message", ellenorzesiHiba));
        }

        Alkalmazott alkalmazott = new Alkalmazott(
                keres.keresztnev(),
                keres.vezeteknev(),
                keres.email(),
                keres.jelszo(),
                keres.szuletesiDatum(),
                keres.telefonszam(),
                keres.cim(),
                keres.varos(),
                keres.iranyitoszam(),
                keres.hazszam(),
                keres.szerepkor(),
                keres.active(),
                keres.adoazonosito(),
                keres.szemelyiIgazolvanySzam(),
                keres.tajSzam(),
                keres.fizetes()
        );
        alkalmazott.setFelvetelDatum(LocalDate.now());

        return ResponseEntity.ok(valassaAlakitas(alkalmazottRepository.save(alkalmazott)));
    }

    @PutMapping("/employees/{id}")
    @Transactional
    public ResponseEntity<?> alkalmazottModositas(@RequestParam String requesterEmail,
                                            @PathVariable Long id,
                                            @RequestBody EmployeeRequest keres) {
        if (!hasznalhatjaHumanResourcesE(requesterEmail)) {
            return tiltott();
        }

        return alkalmazottRepository.findById(id)
                .<ResponseEntity<?>>map(alkalmazott -> {
                    Szerepkor regiSzerepkor = alkalmazott.getSzerepkor();
                    if (uresE(keres.email()) || uresE(keres.keresztnev()) ||
                            uresE(keres.vezeteknev()) || keres.szerepkor() == null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Keresztnev, vezeteknev, email es szerepkor kotelezo."));
                    }

                    var azonosEmailuAlkalmazott = alkalmazottRepository.findByEmailIgnoreCase(keres.email());
                    if (azonosEmailuAlkalmazott.isPresent() && !azonosEmailuAlkalmazott.get().getId().equals(id)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Ezzel az email cimmel mar van alkalmazott."));
                    }

                    String ellenorzesiHiba = alkalmazottKeresEllenorzes(keres);
                    if (ellenorzesiHiba != null) {
                        return ResponseEntity.badRequest().body(Map.of("message", ellenorzesiHiba));
                    }

                    alkalmazott.setKeresztnev(keres.keresztnev());
                    alkalmazott.setVezeteknev(keres.vezeteknev());
                    alkalmazott.setEmail(keres.email());
                    if (!uresE(keres.jelszo())) {
                        alkalmazott.setJelszo(keres.jelszo());
                    }
                    alkalmazott.setSzuletesiDatum(keres.szuletesiDatum());
                    alkalmazott.setTelefonszam(keres.telefonszam());
                    alkalmazott.setCim(keres.cim());
                    alkalmazott.setVaros(keres.varos());
                    alkalmazott.setIranyitoszam(keres.iranyitoszam());
                    alkalmazott.setHazszam(keres.hazszam());
                    alkalmazott.setSzerepkor(keres.szerepkor());
                    alkalmazott.setActive(keres.active());
                    alkalmazott.setAdoazonosito(keres.adoazonosito());
                    alkalmazott.setSzemelyiIgazolvanySzam(keres.szemelyiIgazolvanySzam());
                    alkalmazott.setTajSzam(keres.tajSzam());
                    alkalmazott.setFizetes(keres.fizetes());

                    if (!keres.active() || regiSzerepkor != keres.szerepkor()) {
                        alkalmazottSzerepkorbolEltavolitva(alkalmazott, regiSzerepkor);
                    }

                    return ResponseEntity.ok(valassaAlakitas(alkalmazottRepository.save(alkalmazott)));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Nincs ilyen alkalmazott.")));
    }

    @PostMapping("/employees/{id}/deactivate")
    @Transactional
    public ResponseEntity<?> alkalmazottInaktivalas(@RequestParam String requesterEmail, @PathVariable Long id) {
        if (!hasznalhatjaHumanResourcesE(requesterEmail)) {
            return tiltott();
        }

        return alkalmazottRepository.findById(id)
                .<ResponseEntity<?>>map(alkalmazott -> {
                    alkalmazott.setActive(false);
                    alkalmazottSzerepkorbolEltavolitva(alkalmazott, alkalmazott.getSzerepkor());
                    return ResponseEntity.ok(valassaAlakitas(alkalmazottRepository.save(alkalmazott)));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Nincs ilyen alkalmazott.")));
    }

    private boolean hasznalhatjaHumanResourcesE(String email) {
        if (uresE(email)) {
            return false;
        }

        return alkalmazottRepository.findByEmailIgnoreCase(email)
                .filter(Alkalmazott::isActive)
                .filter(alkalmazott -> alkalmazott.getSzerepkor() == Szerepkor.ADMIN ||
                        alkalmazott.getSzerepkor() == Szerepkor.HR)
                .isPresent();
    }

    private ResponseEntity<?> tiltott() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Csak aktiv ADMIN es HR hasznalhatja a Human resources menut."));
    }

    private EmployeeResponse valassaAlakitas(Alkalmazott alkalmazott) {
        Long csapatId = alkalmazott.getCsapat() == null ? null : alkalmazott.getCsapat().getId();
        String csapatNev = alkalmazott.getCsapat() == null ? null : alkalmazott.getCsapat().getNev();

        return new EmployeeResponse(
                alkalmazott.getId(),
                alkalmazott.getKeresztnev(),
                alkalmazott.getVezeteknev(),
                alkalmazott.getEmail(),
                alkalmazott.getSzuletesiDatum(),
                alkalmazott.getTelefonszam(),
                alkalmazott.getCim(),
                alkalmazott.getVaros(),
                alkalmazott.getIranyitoszam(),
                alkalmazott.getHazszam(),
                alkalmazott.getSzerepkor(),
                alkalmazott.isActive(),
                alkalmazott.getAdoazonosito(),
                alkalmazott.getSzemelyiIgazolvanySzam(),
                alkalmazott.getTajSzam(),
                alkalmazott.getFizetes(),
                korrigaltBruttoFizetes(alkalmazott),
                nettoFizetes(alkalmazott),
                alkalmazott.getFelvetelDatum(),
                csapatId,
                csapatNev,
                alkalmazott.getTeljesitettFeladatok(),
                alkalmazott.getLezartJegyek()
        );
    }

    private void alkalmazottSzerepkorbolEltavolitva(Alkalmazott alkalmazott, Szerepkor eltavolitottSzerepkor) {
        if (eltavolitottSzerepkor == Szerepkor.DEVELOPER) {
            feladatRepository.findByMunkatarsIdAndStatuszNot(alkalmazott.getId(), FeladatStatusz.DONE)
                    .forEach(feladat -> {
                        feladat.setMunkatars(null);
                        feladat.setStatusz(FeladatStatusz.TO_DO);
                        feladatRepository.save(feladat);
                    });
            alkalmazott.setCsapat(null);
        }

        if (eltavolitottSzerepkor == Szerepkor.PRODUCT_OWNER) {
            projektRepository.findByTermekTulajdonosIdAndStatusz(alkalmazott.getId(), ProjektStatusz.ACCEPTED)
                    .forEach(projekt -> {
                        feladatRepository.deleteByProjektId(projekt.getId());
                        projekt.setTermekTulajdonos(null);
                        projekt.setCsapat(null);
                        projekt.setStatusz(ProjektStatusz.PENDING);
                        projektRepository.save(projekt);
                    });

            csapatRepository.findByTulajdonosId(alkalmazott.getId()).ifPresent(csapat -> {
                csapat.getTagok().forEach(tag -> {
                    tag.setCsapat(null);
                    alkalmazottRepository.save(tag);
                });
                csapatRepository.delete(csapat);
            });
        }
    }

    private String alkalmazottKeresEllenorzes(EmployeeRequest keres) {
        if (keres.szerepkor() == Szerepkor.ADMIN) {
            return "Human resources feluleten nem adhato ADMIN szerepkor.";
        }

        if (keres.fizetes() != null && keres.fizetes() < 0) {
            return "A brutto fizetes nem lehet negativ.";
        }

        return null;
    }

    private long korrigaltBruttoFizetes(Alkalmazott alkalmazott) {
        return SalaryCalculator.korrigaltBruttoFizetes(alkalmazott.getFizetes(), alkalmazott.getFelvetelDatum(), LocalDate.now());
    }

    private long nettoFizetes(Alkalmazott alkalmazott) {
        return SalaryCalculator.nettoFizetes(alkalmazott.getFizetes(), alkalmazott.getFelvetelDatum(), LocalDate.now());
    }

    private boolean uresE(String ertek) {
        return ertek == null || ertek.isBlank();
    }

    public record EmployeeRequest(
            String keresztnev,
            String vezeteknev,
            String email,
            String jelszo,
            LocalDate szuletesiDatum,
            String telefonszam,
            String cim,
            String varos,
            String iranyitoszam,
            String hazszam,
            Szerepkor szerepkor,
            boolean active,
            String adoazonosito,
            String szemelyiIgazolvanySzam,
            String tajSzam,
            Long fizetes
    ) {}

    public record EmployeeResponse(
            Long id,
            String keresztnev,
            String vezeteknev,
            String email,
            LocalDate szuletesiDatum,
            String telefonszam,
            String cim,
            String varos,
            String iranyitoszam,
            String hazszam,
            Szerepkor szerepkor,
            boolean active,
            String adoazonosito,
            String szemelyiIgazolvanySzam,
            String tajSzam,
            Long fizetes,
            long korrigaltBruttoFizetes,
            long nettoFizetes,
            LocalDate felvetelDatum,
            Long csapatId,
            String csapatNev,
            int teljesitettFeladatok,
            int lezartJegyek
    ) {}
}
