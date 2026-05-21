package com.backend.management.model;

import java.time.LocalDate;

import com.backend.management.enums.DayOffStatus;
import com.backend.management.enums.DayOffType;

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
@Table(name = "day_offs")
public class DayOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    private Employee approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private DayOffType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DayOffStatus status = DayOffStatus.PENDING;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "workdays")
    private int workdays;

    public DayOff() {
    }

    public DayOff(Employee employee, Employee approvedBy, DayOffType type,
                    LocalDate startDate, LocalDate endDate) {
        this.employee = employee;
        this.approvedBy = approvedBy;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public DayOff(Employee employee, Employee approvedBy, DayOffType type,
                    LocalDate endDate) {
        this(employee, approvedBy, type, LocalDate.now(), endDate);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Employee approvedBy) {
        this.approvedBy = approvedBy;
    }

    public DayOffType getType() {
        return type;
    }

    public void setType(DayOffType type) {
        this.type = type;
    }

    public DayOffStatus getStatus() {
        return status;
    }

    public void setStatus(DayOffStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getWorkdays() {
        return workdays;
    }

    public void setWorkdays(int workdays) {
        this.workdays = workdays;
    }
}
