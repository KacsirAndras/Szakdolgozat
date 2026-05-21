package com.backend.management.model;

import com.backend.management.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee extends User {

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "identity_card_number")
    private String identityCardNumber;

    @Column(name = "social_security_number")
    private String socialSecurityNumber;

    @Column(name = "salary")
    private Long salary;

    @Column(name = "hire_date")
    private LocalDate hireDate = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "completed_tasks")
    private int completedTasks;

    @Column(name = "closed_tickets")
    private int closedTickets;

    public Employee() {
    }

    public Employee(String firstName, String lastName, String email, String password,
                       LocalDate birthDate, String phoneNumber, String address, String city,
                       String postalCode, String houseNumber, Role role, boolean active,
                       String taxId, String identityCardNumber, String socialSecurityNumber, Long salary) {
        super(firstName, lastName, email, password, birthDate, phoneNumber, address,
                city, postalCode, houseNumber, role, active);
        this.taxId = taxId;
        this.identityCardNumber = identityCardNumber;
        this.socialSecurityNumber = socialSecurityNumber;
        this.salary = salary;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getIdentityCardNumber() {
        return identityCardNumber;
    }

    public void setIdentityCardNumber(String identityCardNumber) {
        this.identityCardNumber = identityCardNumber;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public void addCompletedTasks(long count) {
        this.completedTasks += Math.toIntExact(count);
    }

    public int getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(int closedTickets) {
        this.closedTickets = closedTickets;
    }

    public void addClosedTicket() {
        this.closedTickets++;
    }
}
