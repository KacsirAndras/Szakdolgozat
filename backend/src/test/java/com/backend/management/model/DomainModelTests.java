package com.backend.management.model;

import com.backend.management.enums.FeladatStatusz;
import com.backend.management.enums.JegyStatusz;
import com.backend.management.enums.JegyTipus;
import com.backend.management.enums.Prioritas;
import com.backend.management.enums.ProjektStatusz;
import com.backend.management.enums.Szerepkor;
import com.backend.management.enums.TavolletStatusz;
import com.backend.management.enums.TavolletTipus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelTests {

    @Test
    void projectStartsAsPendingWithGivenDatesAndCost() {
        Felhasznalo customer = new Felhasznalo("Ugyfel", "Elek", "customer@example.com", "pw",
                null, null, null, null, null, null, Szerepkor.CUSTOMER, true);

        Projekt project = new Projekt(customer, "Portal", "Demo", 5_000_000L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(project.getStatusz()).isEqualTo(ProjektStatusz.PENDING);
        assertThat(project.getLetrehozo()).isEqualTo(customer);
        assertThat(project.getProjektKoltseg()).isEqualTo(5_000_000L);
        assertThat(project.getBefejezve()).isNull();
    }

    @Test
    void taskDefaultsToTodoAndMediumPriorityWhenPriorityIsMissing() {
        Projekt project = new Projekt();

        Feladat feladat = new Feladat(project, "Teszt feladat", "Leiras", null);

        assertThat(feladat.getProjekt()).isEqualTo(project);
        assertThat(feladat.getStatusz()).isEqualTo(FeladatStatusz.TO_DO);
        assertThat(feladat.getPrioritas()).isEqualTo(Prioritas.MEDIUM);
    }

    @Test
    void ticketStartsClosedForCreatedSupportRequest() {
        Felhasznalo creator = new Felhasznalo("Dev", "Dora", "dev@example.com", "pw",
                null, null, null, null, null, null, Szerepkor.DEVELOPER, true);

        Ticket Ticket = new Ticket(creator, JegyTipus.HELP, "Nem mukodik");

        assertThat(Ticket.getLetrehozta()).isEqualTo(creator);
        assertThat(Ticket.getTipus()).isEqualTo(JegyTipus.HELP);
        assertThat(Ticket.getStatusz()).isEqualTo(JegyStatusz.CLOSED);
    }

    @Test
    void dayOffStartsPendingAndKeepsWorkingDays() {
        Alkalmazott alkalmazott = new Alkalmazott();
        Tavollet dayOff = new Tavollet(alkalmazott, null, TavolletTipus.DAY_OFF,
                LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 8));

        dayOff.setMunkanapok(5);

        assertThat(dayOff.getMunkatars()).isEqualTo(alkalmazott);
        assertThat(dayOff.getStatusz()).isEqualTo(TavolletStatusz.PENDING);
        assertThat(dayOff.getMunkanapok()).isEqualTo(5);
    }

    @Test
    void employeeCountersCanBeIncremented() {
        Alkalmazott alkalmazott = new Alkalmazott();

        alkalmazott.addTeljesitettFeladatok(3);
        alkalmazott.addLezartJegy();

        assertThat(alkalmazott.getTeljesitettFeladatok()).isEqualTo(3);
        assertThat(alkalmazott.getLezartJegyek()).isEqualTo(1);
    }
}
