package com.backend.management.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
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

import com.backend.management.enums.Szerepkor;
import com.backend.management.enums.TavolletStatusz;
import com.backend.management.enums.TavolletTipus;
import com.backend.management.model.Alkalmazott;
import com.backend.management.model.Felhasznalo;
import com.backend.management.model.Tavollet;
import com.backend.management.repository.AlkalmazottRepository;
import com.backend.management.repository.TavolletRepository;

@RestController
@RequestMapping("/api/day-off")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class DayOffController {

    private static final int HOME_OFFICE_LIMIT = 150;

    private final TavolletRepository tavolletRepository;
    private final AlkalmazottRepository alkalmazottRepository;

    public DayOffController(TavolletRepository tavolletRepository,
                            AlkalmazottRepository alkalmazottRepository) {
        this.tavolletRepository = tavolletRepository;
        this.alkalmazottRepository = alkalmazottRepository;
    }

    @GetMapping
    public ResponseEntity<?> listazas(@RequestParam String email) {

        Alkalmazott alkalmazott = alkalmazottRepository.findByEmailIgnoreCase(email).orElse(null);

        if (alkalmazott == null || !alkalmazott.isActive() || alkalmazott.getSzerepkor() == Szerepkor.CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Customer nem hasznalhatja a Day off menut."));
        }

        QuotaResponse quotas = keretekAlkalmazotthoz(alkalmazott, LocalDate.now().getYear());

        List<TavolletResponse> myRequests = sajatKerelmek(alkalmazott, email)
                .stream()
                .map(this::valassaAlakitas)
                .toList();

        List<TavolletResponse> teamRequests = jovahagyhatoKerelmek(alkalmazott, email)
                .stream()
                .map(this::valassaAlakitas)
                .toList();

        return ResponseEntity.ok(new DayOffPageResponse(
                quotas,
                myRequests,
                teamRequests
        ));
    }

    @PostMapping
    public ResponseEntity<?> keres(@RequestParam String email, @RequestBody DayOffRequest keres) {

        Alkalmazott alkalmazott = alkalmazottRepository.findByEmailIgnoreCase(email).orElse(null);

        if (alkalmazott == null || !alkalmazott.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Nem aktiv vagy nem letezo felhasznalo."));
        }

        if (alkalmazott.getSzerepkor() == Szerepkor.CUSTOMER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Customer nem kerhet tavolletet."));
        }

        if (keres == null || keres.tipus() == null || keres.kezdet() == null || keres.vege() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Tipus, kezdet es vege kotelezo."));
        }

        if (keres.vege().isBefore(keres.kezdet())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A vege nem lehet korabban, mint a kezdet."));
        }

        if (keres.kezdet().getYear() != keres.vege().getYear()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Egy kerelem csak egy naptari even belul lehet."));
        }

        int munkanapok = munkanapokSzamolasa(keres.kezdet(), keres.vege());

        if (munkanapok == 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A kivalasztott idoszakban nincs munkanap."));
        }

        int ev = keres.kezdet().getYear();
        int limit;

        if (keres.tipus() == TavolletTipus.HOME_OFFICE) {
            limit = HOME_OFFICE_LIMIT;
        } else {
            limit = evesSzabadsagKeret(alkalmazott, ev);
        }

        int used = felhasznaltNapok(alkalmazott, keres.tipus(), ev);

        if (used + munkanapok > limit) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message",
                    "Nincs eleg keret. Keret: " + limit + ", mar foglalt: " + used + ", igenyelt: " + munkanapok + "."
            ));
        }

        boolean overlap = atfedesVanE(alkalmazott, keres.kezdet(), keres.vege());

        if (overlap) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Erre az idoszakra mar van PENDING vagy APPROVED tavolleted."));
        }

        Tavollet tavollet = new Tavollet(
                alkalmazott,
                null,
                keres.tipus(),
                keres.kezdet(),
                keres.vege()
        );

        tavollet.setMunkanapok(munkanapok);

        Tavollet mentett = tavolletRepository.save(tavollet);

        return ResponseEntity.ok(valassaAlakitas(mentett));
    }

    @PostMapping("/{id}/jovahagyas")
    public ResponseEntity<?> jovahagyas(@PathVariable Long id, @RequestParam String email) {
        return jovahagyasVagyElutasitas(id, email, TavolletStatusz.APPROVED);
    }

    @PostMapping("/{id}/elutasitas")
    public ResponseEntity<?> elutasitas(@PathVariable Long id, @RequestParam String email) {
        return jovahagyasVagyElutasitas(id, email, TavolletStatusz.REJECTED);
    }

    private ResponseEntity<?> jovahagyasVagyElutasitas(Long id, String email, TavolletStatusz statusz) {

        Alkalmazott jovahagyo = alkalmazottRepository.findByEmailIgnoreCase(email).orElse(null);

        if (jovahagyo == null || !jovahagyo.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv felhasznalo hagyhat jova tavolletet."));
        }

        if (jovahagyo.getSzerepkor() != Szerepkor.ADMIN &&
                jovahagyo.getSzerepkor() != Szerepkor.PRODUCT_OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak ADMIN vagy PRODUCT_OWNER hagyhat jova tavolletet."));
        }

        Tavollet tavollet = tavolletRepository.findById(id).orElse(null);

        if (tavollet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen tavollet."));
        }

        if (tavollet.getStatusz() != TavolletStatusz.PENDING) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Csak PENDING kerelem kezelheto."));
        }

        if (jovahagyo.getSzerepkor() != Szerepkor.ADMIN) {

            if (tavollet.getMunkatars().getSzerepkor() != Szerepkor.DEVELOPER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Csak DEVELOPER tavollete hagyhato jova."));
            }

            if (tavollet.getMunkatars().getCsapat() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "A munkatarsnak nincs csapata."));
            }

            if (tavollet.getMunkatars().getCsapat().getTulajdonos() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "A csapatnak nincs tulajdonos-je."));
            }

            if (!tavollet.getMunkatars().getCsapat().getTulajdonos().getId().equals(jovahagyo.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Csak a sajat csapatod developereinek tavolletet hagyhatod jova."));
            }
        }

        tavollet.setStatusz(statusz);
        tavollet.setJovahagyta(jovahagyo);

        Tavollet mentett = tavolletRepository.save(tavollet);

        return ResponseEntity.ok(valassaAlakitas(mentett));
    }

    private QuotaResponse keretekAlkalmazotthoz(Alkalmazott alkalmazott, int ev) {

        int felhasznaltHomeOffice = felhasznaltNapok(alkalmazott, TavolletTipus.HOME_OFFICE, ev);
        int szabadsagKeret = evesSzabadsagKeret(alkalmazott, ev);
        int felhasznaltSzabadsag = felhasznaltNapok(alkalmazott, TavolletTipus.DAY_OFF, ev);

        return new QuotaResponse(
                ev,
                HOME_OFFICE_LIMIT,
                felhasznaltHomeOffice,
                HOME_OFFICE_LIMIT - felhasznaltHomeOffice,
                szabadsagKeret,
                felhasznaltSzabadsag,
                szabadsagKeret - felhasznaltSzabadsag
        );
    }

    private int felhasznaltNapok(Alkalmazott alkalmazott, TavolletTipus tipus, int ev) {

        LocalDate evKezdete = LocalDate.of(ev, 1, 1);
        LocalDate evVege = LocalDate.of(ev, 12, 31);

        List<Tavollet> tavolletek =
                tavolletRepository.findByMunkatarsIdAndTipusAndStatuszInAndKezdetBetween(
                        alkalmazott.getId(),
                        tipus,
                        List.of(TavolletStatusz.PENDING, TavolletStatusz.APPROVED),
                        evKezdete,
                        evVege
                );

        int osszeg = 0;

        for (Tavollet t : tavolletek) {
            osszeg += t.getMunkanapok();
        }

        return osszeg;
    }

    private boolean atfedesVanE(Alkalmazott alkalmazott, LocalDate kezdet, LocalDate vege) {

        List<Tavollet> tavolletek =
                tavolletRepository.findByMunkatarsIdAndStatuszInAndKezdetLessThanEqualAndVegeGreaterThanEqual(
                        alkalmazott.getId(),
                        List.of(TavolletStatusz.PENDING, TavolletStatusz.APPROVED),
                        vege,
                        kezdet
                );

        return !tavolletek.isEmpty();
    }

    private List<Tavollet> sajatKerelmek(Alkalmazott alkalmazott, String email) {

        if (alkalmazott.getSzerepkor() == Szerepkor.ADMIN) {
            return tavolletRepository.findAllByOrderByKezdetDesc();
        }

        return tavolletRepository.findByMunkatarsEmailIgnoreCaseOrderByKezdetDesc(email);
    }

    private List<Tavollet> jovahagyhatoKerelmek(Alkalmazott alkalmazott, String email) {

        if (alkalmazott.getSzerepkor() == Szerepkor.ADMIN) {
            return tavolletRepository.findAllByOrderByKezdetDesc();
        }

        if (alkalmazott.getSzerepkor() == Szerepkor.PRODUCT_OWNER) {

            List<Tavollet> osszes =
                    tavolletRepository.findByMunkatarsCsapatTulajdonosEmailIgnoreCaseOrderByKezdetDesc(email);

            return osszes.stream()
                    .filter(t -> t.getMunkatars().getSzerepkor() == Szerepkor.DEVELOPER)
                    .toList();
        }

        return List.of();
    }

    private int evesSzabadsagKeret(Alkalmazott alkalmazott, int ev) {

        if (alkalmazott.getSzuletesiDatum() == null) {
            return 20;
        }

        int eletkor = Period.between(
                alkalmazott.getSzuletesiDatum(),
                LocalDate.of(ev, 12, 31)
        ).getYears();

        int plusz = 0;

        if (eletkor >= 45) {
            plusz = 10;
        } else if (eletkor >= 43) {
            plusz = 9;
        } else if (eletkor >= 41) {
            plusz = 8;
        } else if (eletkor >= 39) {
            plusz = 7;
        } else if (eletkor >= 37) {
            plusz = 6;
        } else if (eletkor >= 35) {
            plusz = 5;
        } else if (eletkor >= 33) {
            plusz = 4;
        } else if (eletkor >= 31) {
            plusz = 3;
        } else if (eletkor >= 28) {
            plusz = 2;
        } else if (eletkor >= 25) {
            plusz = 1;
        }

        return 20 + plusz;
    }

    private int munkanapokSzamolasa(LocalDate kezdet, LocalDate vege) {

        int napok = 0;
        LocalDate aktualis = kezdet;

        while (!aktualis.isAfter(vege)) {

            if (aktualis.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    aktualis.getDayOfWeek() != DayOfWeek.SUNDAY) {
                napok++;
            }

            aktualis = aktualis.plusDays(1);
        }

        return napok;
    }

    private TavolletResponse valassaAlakitas(Tavollet tavollet) {

        Alkalmazott munkatars = tavollet.getMunkatars();
        Alkalmazott jovahagyta = tavollet.getJovahagyta();

        String jovahagytaNev = null;
        String jovahagytaEmail = null;

        if (jovahagyta != null) {
            jovahagytaNev = teljesNev(jovahagyta);
            jovahagytaEmail = jovahagyta.getEmail();
        }

        return new TavolletResponse(
                tavollet.getId(),
                munkatars.getId(),
                teljesNev(munkatars),
                munkatars.getEmail(),
                munkatars.getSzerepkor(),
                tavollet.getTipus(),
                tavollet.getStatusz(),
                tavollet.getKezdet(),
                tavollet.getVege(),
                tavollet.getMunkanapok(),
                jovahagytaNev,
                jovahagytaEmail
        );
    }

    private String teljesNev(Felhasznalo felhasznalo) {
        return felhasznalo.getKeresztnev() + " " + felhasznalo.getVezeteknev();
    }

    public record DayOffRequest(
            TavolletTipus tipus,
            LocalDate kezdet,
            LocalDate vege
    ) {}

    public record DayOffPageResponse(
            QuotaResponse quotas,
            List<TavolletResponse> myRequests,
            List<TavolletResponse> teamRequests
    ) {}

    public record QuotaResponse(
            int ev,
            int homeOfficeLimit,
            int felhasznaltHomeOffice,
            int homeOfficeRemaining,
            int szabadsagKeret,
            int felhasznaltSzabadsag,
            int dayOffRemaining
    ) {}

    public record TavolletResponse(
            Long id,
            Long munkatarsId,
            String munkatarsNev,
            String munkatarsEmail,
            Szerepkor munkatarsSzerepkor,
            TavolletTipus tipus,
            TavolletStatusz statusz,
            LocalDate kezdet,
            LocalDate vege,
            int munkanapok,
            String jovahagytaNev,
            String jovahagytaEmail
    ) {}
}