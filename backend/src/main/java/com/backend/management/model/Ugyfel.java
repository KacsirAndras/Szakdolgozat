package com.backend.management.model;

import com.backend.management.enums.Szerepkor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "Ugyfel")
public class Ugyfel extends Felhasznalo {

    @Column(name = "Ceg_nev", nullable = false)
    private String cegNev;

    public Ugyfel() {
    }

    public Ugyfel(String keresztnev, String vezeteknev, String email, String jelszo,
                  LocalDate szuletesiDatum, String telefonszam, String cim, String varos,
                  String iranyitoszam, String hazszam, Szerepkor szerepkor, boolean active,
                  String cegNev) {
        super(keresztnev, vezeteknev, email, jelszo, szuletesiDatum, telefonszam, cim, varos,
                iranyitoszam, hazszam, szerepkor, active);
        this.cegNev = cegNev;
    }

    public String getCegNev() {
        return cegNev;
    }

    public void setCegNev(String cegNev) {
        this.cegNev = cegNev;
    }
}
