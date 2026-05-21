package com.backend.management.model;

import com.backend.management.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "customers")
public class Customer extends User {

    @Column(name = "company_name")
    private String companyName;

    public Customer() {
    }

    public Customer(String firstName, String lastName, String email, String password,
                  LocalDate birthDate, String phoneNumber, String address, String city,
                  String postalCode, String houseNumber, Role role, boolean active,
                  String companyName) {
        super(firstName, lastName, email, password, birthDate, phoneNumber, address, city,
                postalCode, houseNumber, role, active);
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
