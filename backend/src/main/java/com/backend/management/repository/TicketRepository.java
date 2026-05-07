package com.backend.management.repository;

import com.backend.management.enums.JegyStatusz;
import com.backend.management.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByLetrehoztaEmailIgnoreCaseOrderByIdDesc(String email);

    List<Ticket> findByStatuszNotOrderByIdDesc(JegyStatusz statusz);

    List<Ticket> findAllByOrderByIdDesc();
}
