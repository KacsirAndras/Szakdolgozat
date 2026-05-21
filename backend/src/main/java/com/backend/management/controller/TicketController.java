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

import com.backend.management.enums.TicketStatus;
import com.backend.management.enums.TicketType;
import com.backend.management.enums.Role;
import com.backend.management.model.Employee;
import com.backend.management.model.User;
import com.backend.management.model.Ticket;
import com.backend.management.repository.EmployeeRepository;
import com.backend.management.repository.UserRepository;
import com.backend.management.repository.TicketRepository;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = {"http://127.0.0.1:4200", "http://localhost:4200"})
public class TicketController {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public TicketController(TicketRepository ticketRepository,
                            UserRepository userRepository,
                            EmployeeRepository employeeRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public ResponseEntity<?> listTickets(@RequestParam String email) {

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen aktiv user."));
        }

        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(ticketRepository.findAllByOrderByIdDesc()
                    .stream()
                    .map(this::toResponse)
                    .toList());
        }

        if (user.getRole() == Role.IT) {
            Map<Long, Ticket> tickets = new LinkedHashMap<>();

            for (Ticket ticket : ticketRepository.findByStatusNotOrderByIdDesc(TicketStatus.CLOSED)) {
                tickets.put(ticket.getId(), ticket);
            }

            for (Ticket ticket : ticketRepository.findByCreatedByEmailIgnoreCaseOrderByIdDesc(email)) {
                tickets.putIfAbsent(ticket.getId(), ticket);
            }

            return ResponseEntity.ok(tickets.values()
                    .stream()
                    .map(this::toResponse)
                    .toList());
        }

        return ResponseEntity.ok(ticketRepository.findByCreatedByEmailIgnoreCaseOrderByIdDesc(email)
                .stream()
                .map(this::toResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestParam String email,
                                          @RequestBody CreateTicketRequest request) {

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen aktiv user."));
        }

        if (request == null || request.type() == null || isBlank(request.problem())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Type and problem are required."));
        }

        Ticket ticket = new Ticket(
                user,
                request.type(),
                request.problem()
        );

        Ticket saved = ticketRepository.save(ticket);

        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable Long id,
                                          @RequestParam String email,
                                          @RequestBody UpdateTicketRequest request) {

        Employee handler = employeeRepository.findByEmailIgnoreCase(email).orElse(null);

        if (handler == null || !handler.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy IT modosithat jegyet."));
        }

        if (handler.getRole() != Role.ADMIN &&
                handler.getRole() != Role.IT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Csak aktiv ADMIN vagy IT modosithat jegyet."));
        }

        if (request == null || request.status() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Status is required."));
        }

        Ticket ticket = ticketRepository.findById(id).orElse(null);

        if (ticket == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nincs ilyen Ticket."));
        }

        ticket.setStatus(request.status());
        ticket.setItEmployee(handler);

        Ticket saved = ticketRepository.save(ticket);

        if (request.status() == TicketStatus.CLOSED) {
            handler.addClosedTicket();
            employeeRepository.save(handler);
        }

        return ResponseEntity.ok(toResponse(saved));
    }

    private TicketResponse toResponse(Ticket ticket) {

        User createdBy = ticket.getCreatedBy();
        Employee itEmployee = ticket.getItEmployee();

        String itEmail = null;
        String itName = null;
        Integer closedTickets = null;

        if (itEmployee != null) {
            itEmail = itEmployee.getEmail();
            itName = fullName(itEmployee);
            closedTickets = itEmployee.getClosedTickets();
        }

        return new TicketResponse(
                ticket.getId(),
                ticket.getType(),
                ticket.getStatus(),
                ticket.getProblem(),
                createdBy.getEmail(),
                fullName(createdBy),
                createdBy.getRole().name(),
                itEmail,
                itName,
                closedTickets
        );
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record CreateTicketRequest(
            TicketType type,
            String problem
    ) {}

    public record UpdateTicketRequest(
            TicketStatus status
    ) {}

    public record TicketResponse(
            Long id,
            TicketType type,
            TicketStatus status,
            String problem,
            String createdByEmail,
            String createdByName,
            String createdByRole,
            String itEmployeeEmail,
            String itEmployeeName,
            Integer itEmployeeClosedTickets
    ) {}
}
