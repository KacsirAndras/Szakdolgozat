package com.backend.management.repository;

import com.backend.management.enums.TicketStatus;
import com.backend.management.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedByEmailIgnoreCaseOrderByIdDesc(String email);

    List<Ticket> findByStatusNotOrderByIdDesc(TicketStatus status);

    List<Ticket> findAllByOrderByIdDesc();
}
