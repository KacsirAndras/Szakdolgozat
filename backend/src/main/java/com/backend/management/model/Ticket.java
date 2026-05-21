package com.backend.management.model;

import com.backend.management.enums.TicketStatus;
import com.backend.management.enums.TicketType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "it_employee_id")
    private Employee itEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TicketType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TicketStatus status = TicketStatus.CLOSED;

    @Column(name = "problem", columnDefinition = "TEXT")
    private String problem;

    public Ticket() {
    }

    public Ticket(User createdBy, TicketType type, String problem) {
        this.createdBy = createdBy;
        this.type = type;
        this.problem = problem;
        this.status = TicketStatus.CLOSED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Employee getItEmployee() {
        return itEmployee;
    }

    public void setItEmployee(Employee itEmployee) {
        this.itEmployee = itEmployee;
    }

    public TicketType getType() {
        return type;
    }

    public void setType(TicketType type) {
        this.type = type;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }
}
