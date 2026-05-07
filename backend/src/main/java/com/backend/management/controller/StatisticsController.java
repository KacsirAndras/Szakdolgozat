package com.backend.management.controller;

import com.backend.management.enums.ProjektStatusz;
import com.backend.management.enums.Szerepkor;
import com.backend.management.model.Alkalmazott;
import com.backend.management.model.Felhasznalo;
import com.backend.management.model.Projekt;
import com.backend.management.repository.AlkalmazottRepository;
import com.backend.management.repository.FelhasznaloRepository;
import com.backend.management.repository.ProjektRepository;
import com.backend.management.service.SalaryCalculator;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class StatisticsController {

    private static final LocalDate KEZDO_DATUM = LocalDate.of(2020, 1, 1);
    private static final long KEZDO_EGYENLEG = 20_000_000L;

    private final FelhasznaloRepository felhasznaloRepository;
    private final AlkalmazottRepository alkalmazottRepository;
    private final ProjektRepository projektRepository;

    public StatisticsController(FelhasznaloRepository felhasznaloRepository,
                                AlkalmazottRepository alkalmazottRepository,
                                ProjektRepository projektRepository) {
        this.felhasznaloRepository = felhasznaloRepository;
        this.alkalmazottRepository = alkalmazottRepository;
        this.projektRepository = projektRepository;
    }

    @GetMapping
    public ResponseEntity<?> statisztikaLekerdezes(@RequestParam String email,
                                           @RequestParam(defaultValue = "2025") int year) {

        if (!aktivAdminE(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN lathatja a statisztikat."));
        }

        int valasztottEv = year;

        if (valasztottEv < 2020) {
            valasztottEv = 2020;
        }

        if (valasztottEv > 2100) {
            valasztottEv = 2100;
        }

        List<Felhasznalo> felhasznalok = felhasznaloRepository.findAll();
        List<Alkalmazott> alkalmazottak = alkalmazottRepository.findAll();
        List<Projekt> projektek = projektRepository.findAll();

        long haviBerKoltseg = 0;

        for (Alkalmazott alkalmazott : alkalmazottak) {
            if (alkalmazott.isActive()) {
                haviBerKoltseg += SalaryCalculator.nettoFizetes(
                        alkalmazott.getFizetes(),
                        alkalmazott.getFelvetelDatum(),
                        LocalDate.now()
                );
            }
        }

        List<RoleSlice> szerepkorSzeletek = new ArrayList<>();

        for (Szerepkor szerepkor : Szerepkor.values()) {
            long darab = 0;

            for (Felhasznalo felhasznalo : felhasznalok) {
                if (felhasznalo.getSzerepkor() == szerepkor) {
                    darab++;
                }
            }

            szerepkorSzeletek.add(new RoleSlice(
                    szerepkor.name(),
                    darab,
                    szinSzerepkorhoz(szerepkor)
            ));
        }

        YearMonth elsoHonap = YearMonth.of(valasztottEv, 1);
        long evElottiEgyenleg = egyenlegSzamitasHonapig(elsoHonap, haviBerKoltseg, projektek);

        long egyenleg = evElottiEgyenleg;
        List<MonthlyFinance> honapok = new ArrayList<>();

        for (Month honap : Month.values()) {

            YearMonth aktualisHonap = YearMonth.of(valasztottEv, honap);

            long projektBevetel = befejezettProjektBevetel(projektek, aktualisHonap);
            long nyereseg = projektBevetel - haviBerKoltseg;

            egyenleg = egyenleg + nyereseg;

            honapok.add(new MonthlyFinance(
                    honap.getValue(),
                    magyarHonap(honap),
                    projektBevetel,
                    haviBerKoltseg,
                    nyereseg,
                    egyenleg
            ));
        }

        StatisticsResponse valasz = new StatisticsResponse(
                KEZDO_DATUM,
                KEZDO_EGYENLEG,
                valasztottEv,
                evElottiEgyenleg,
                szerepkorSzeletek,
                honapok
        );

        return ResponseEntity.ok(valasz);
    }

    private long egyenlegSzamitasHonapig(YearMonth celHonap,
                                       long haviBerKoltseg,
                                       List<Projekt> projektek) {

        long egyenleg = KEZDO_EGYENLEG;

        YearMonth aktualis = YearMonth.from(KEZDO_DATUM);

        while (aktualis.isBefore(celHonap)) {

            long bevetel = befejezettProjektBevetel(projektek, aktualis);
            long nyereseg = bevetel - haviBerKoltseg;

            egyenleg = egyenleg + nyereseg;

            aktualis = aktualis.plusMonths(1);
        }

        return egyenleg;
    }

    private long befejezettProjektBevetel(List<Projekt> projektek, YearMonth honap) {

        long osszeg = 0;

        for (Projekt project : projektek) {

            if (project.getStatusz() == ProjektStatusz.COMPLETED &&
                    project.getBefejezve() != null) {

                YearMonth projektHonap = YearMonth.from(project.getBefejezve());

                if (projektHonap.equals(honap)) {
                    osszeg += project.getProjektKoltseg();
                }
            }
        }

        return osszeg;
    }

    private boolean aktivAdminE(String email) {

        if (email == null || email.isBlank()) {
            return false;
        }

        Felhasznalo felhasznalo = felhasznaloRepository.findByEmailIgnoreCase(email).orElse(null);

        if (felhasznalo == null) {
            return false;
        }

        if (!felhasznalo.isActive()) {
            return false;
        }

        return felhasznalo.getSzerepkor() == Szerepkor.ADMIN;
    }

    private String szinSzerepkorhoz(Szerepkor szerepkor) {

        if (szerepkor == Szerepkor.CUSTOMER) {
            return "#0969da";
        }

        if (szerepkor == Szerepkor.ADMIN) {
            return "#a40e26";
        }

        if (szerepkor == Szerepkor.IT) {
            return "#1a7f37";
        }

        if (szerepkor == Szerepkor.HR) {
            return "#bf8700";
        }

        if (szerepkor == Szerepkor.PRODUCT_OWNER) {
            return "#8250df";
        }

        if (szerepkor == Szerepkor.DEVELOPER) {
            return "#57606a";
        }

        return "#000000";
    }

    private String magyarHonap(Month honap) {

        if (honap == Month.JANUARY) {
            return "Januar";
        } else if (honap == Month.FEBRUARY) {
            return "Februar";
        } else if (honap == Month.MARCH) {
            return "Marcius";
        } else if (honap == Month.APRIL) {
            return "Aprilis";
        } else if (honap == Month.MAY) {
            return "Majus";
        } else if (honap == Month.JUNE) {
            return "Junius";
        } else if (honap == Month.JULY) {
            return "Julius";
        } else if (honap == Month.AUGUST) {
            return "Augusztus";
        } else if (honap == Month.SEPTEMBER) {
            return "Szeptember";
        } else if (honap == Month.OCTOBER) {
            return "Oktober";
        } else if (honap == Month.NOVEMBER) {
            return "November";
        } else if (honap == Month.DECEMBER) {
            return "December";
        }

        return "";
    }

    public record StatisticsResponse(
            LocalDate startDate,
            long startBalance,
            int year,
            long evElottiEgyenleg,
            List<RoleSlice> szerepkorSzeletek,
            List<MonthlyFinance> honapok
    ) {}

    public record RoleSlice(
            String szerepkor,
            long darab,
            String color
    ) {}

    public record MonthlyFinance(
            int honap,
            String label,
            long projektBevetel,
            long salaryCost,
            long nyereseg,
            long balanceAfter
    ) {}
}