package com.backend.management.controller;

import java.util.LinkedHashMap;
import java.util.Map;

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

import com.backend.management.enums.JegyStatusz;
import com.backend.management.enums.JegyTipus;
import com.backend.management.enums.Szerepkor;
import com.backend.management.model.Alkalmazott;
import com.backend.management.model.Felhasznalo;
import com.backend.management.model.Ticket;
import com.backend.management.repository.AlkalmazottRepository;
import com.backend.management.repository.FelhasznaloRepository;
import com.backend.management.repository.TicketRepository;

@RestController
@RequestMapping("/api/jegyek")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class TicketController {

    private final TicketRepository ticketRepository;
    private final FelhasznaloRepository felhasznaloRepository;
    private final AlkalmazottRepository alkalmazottRepository;

    public TicketController(TicketRepository ticketRepository,
                            FelhasznaloRepository felhasznaloRepository,
                            AlkalmazottRepository alkalmazottRepository) {
        this.ticketRepository = ticketRepository;
        this.felhasznaloRepository = felhasznaloRepository;
        this.alkalmazottRepository = alkalmazottRepository;
    }

    @GetMapping
    public ResponseEntity<?> jegyekListazasa(@RequestParam String email) {

        Felhasznalo felhasznalo = felhasznaloRepository.findByEmailIgnoreCase(email).orElse(null);

        if (felhasznalo == null || !felhasznalo.isActive()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen aktiv felhasznalo."));
        }

        if (felhasznalo.getSzerepkor() == Szerepkor.ADMIN) {
            return ResponseEntity.ok(ticketRepository.findAllByOrderByIdDesc()
                    .stream()
                    .map(this::valassaAlakitas)
                    .toList());
        }

        if (felhasznalo.getSzerepkor() == Szerepkor.IT) {
            Map<Long, Ticket> jegyek = new LinkedHashMap<>();

            for (Ticket Ticket : ticketRepository.findByStatuszNotOrderByIdDesc(JegyStatusz.CLOSED)) {
                jegyek.put(Ticket.getId(), Ticket);
            }

            for (Ticket Ticket : ticketRepository.findByLetrehoztaEmailIgnoreCaseOrderByIdDesc(email)) {
                jegyek.putIfAbsent(Ticket.getId(), Ticket);
            }

            return ResponseEntity.ok(jegyek.values()
                    .stream()
                    .map(this::valassaAlakitas)
                    .toList());
        }

        return ResponseEntity.ok(ticketRepository.findByLetrehoztaEmailIgnoreCaseOrderByIdDesc(email)
                .stream()
                .map(this::valassaAlakitas)
                .toList());
    }

    @PostMapping
    public ResponseEntity<?> jegyLetrehozas(@RequestParam String email,
                                          @RequestBody CreateTicketRequest keres) {

        Felhasznalo felhasznalo = felhasznaloRepository.findByEmailIgnoreCase(email).orElse(null);

        if (felhasznalo == null || !felhasznalo.isActive()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen aktiv felhasznalo."));
        }

        if (keres == null || keres.tipus() == null || uresE(keres.problema())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Tipus es problema kotelezo."));
        }

        Ticket Ticket = new Ticket(
                felhasznalo,
                keres.tipus(),
                keres.problema()
        );

        Ticket mentett = ticketRepository.save(Ticket);

        return ResponseEntity.ok(valassaAlakitas(mentett));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> jegyModositas(@PathVariable Long id,
                                          @RequestParam String email,
                                          @RequestBody UpdateTicketRequest keres) {

        Alkalmazott kezelo = alkalmazottRepository.findByEmailIgnoreCase(email).orElse(null);

        if (kezelo == null || !kezelo.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy IT modosithat jegyet."));
        }

        if (kezelo.getSzerepkor() != Szerepkor.ADMIN &&
                kezelo.getSzerepkor() != Szerepkor.IT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy IT modosithat jegyet."));
        }

        if (keres == null || keres.statusz() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Statusz kotelezo."));
        }

        Ticket Ticket = ticketRepository.findById(id).orElse(null);

        if (Ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen Ticket."));
        }

        Ticket.setStatusz(keres.statusz());
        Ticket.setItMunkatars(kezelo);

        Ticket mentett = ticketRepository.save(Ticket);

        if (keres.statusz() == JegyStatusz.CLOSED) {
            kezelo.addLezartJegy();
            alkalmazottRepository.save(kezelo);
        }

        return ResponseEntity.ok(valassaAlakitas(mentett));
    }

    private TicketResponse valassaAlakitas(Ticket Ticket) {

        Felhasznalo letrehozta = Ticket.getLetrehozta();
        Alkalmazott itMunkatars = Ticket.getItMunkatars();

        String itEmail = null;
        String itNev = null;
        Integer lezartJegyek = null;

        if (itMunkatars != null) {
            itEmail = itMunkatars.getEmail();
            itNev = teljesNev(itMunkatars);
            lezartJegyek = itMunkatars.getLezartJegyek();
        }

        return new TicketResponse(
                Ticket.getId(),
                Ticket.getTipus(),
                Ticket.getStatusz(),
                Ticket.getProblema(),
                letrehozta.getEmail(),
                teljesNev(letrehozta),
                letrehozta.getSzerepkor().name(),
                itEmail,
                itNev,
                lezartJegyek
        );
    }

    private String teljesNev(Felhasznalo felhasznalo) {
        return felhasznalo.getKeresztnev() + " " + felhasznalo.getVezeteknev();
    }

    private boolean uresE(String ertek) {
        return ertek == null || ertek.isBlank();
    }

    public record CreateTicketRequest(
            JegyTipus tipus,
            String problema
    ) {}

    public record UpdateTicketRequest(
            JegyStatusz statusz
    ) {}

    public record TicketResponse(
            Long id,
            JegyTipus tipus,
            JegyStatusz statusz,
            String problema,
            String letrehoztaEmail,
            String letrehoztaNev,
            String letrehoztaSzerepkor,
            String itMunkatarsEmail,
            String itMunkatarsNev,
            Integer itMunkatarsLezartJegyek
    ) {}
}