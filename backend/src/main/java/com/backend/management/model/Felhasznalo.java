package com.backend.management.model;

import com.backend.management.enums.Szerepkor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "Felhasznalo")
@Inheritance(strategy = InheritanceType.JOINED)
public class Felhasznalo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Keresztnev")
    private String keresztnev;

    @Column(name = "Vezeteknev")
    private String vezeteknev;

    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    @Column(name = "Jelszo", nullable = false)
    private String jelszo;

    @Column(name = "Szuletesi_datum")
    private LocalDate szuletesiDatum;

    @Column(name = "Telefonszam")
    private String telefonszam;

    @Column(name = "Cim")
    private String cim;

    @Column(name = "Varos")
    private String varos;

    @Column(name = "Iranyitoszam")
    private String iranyitoszam;

    @Column(name = "Hazszam")
    private String hazszam;

    @Enumerated(EnumType.STRING)
    @Column(name = "Szerepkor")
    private Szerepkor szerepkor;

    @Column(name = "Aktiv", nullable = false)
    private boolean active = false;

    @Column(name = "Visszaallito_kod")
    private String visszaallitoKod;

    @Column(name = "Visszaallito_kod_lejar")
    private LocalDateTime visszaallitoKodLejar;

    @Column(name = "Aktivalo_kod")
    private String aktivaloKod;

    @Column(name = "Aktivalo_kod_lejar")
    private LocalDateTime aktivaloKodLejar;

    public Felhasznalo() {
    }

    public Felhasznalo(String keresztnev, String vezeteknev, String email, String jelszo,
                       LocalDate szuletesiDatum, String telefonszam, String cim, String varos,
                       String iranyitoszam, String hazszam, Szerepkor szerepkor, boolean active) {
        this.keresztnev = keresztnev;
        this.vezeteknev = vezeteknev;
        this.email = email;
        this.jelszo = jelszo;
        this.szuletesiDatum = szuletesiDatum;
        this.telefonszam = telefonszam;
        this.cim = cim;
        this.varos = varos;
        this.iranyitoszam = iranyitoszam;
        this.hazszam = hazszam;
        this.szerepkor = szerepkor;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeresztnev() {
        return keresztnev;
    }

    public void setKeresztnev(String keresztnev) {
        this.keresztnev = keresztnev;
    }

    public String getVezeteknev() {
        return vezeteknev;
    }

    public void setVezeteknev(String vezeteknev) {
        this.vezeteknev = vezeteknev;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJelszo() {
        return jelszo;
    }

    public void setJelszo(String jelszo) {
        this.jelszo = jelszo;
    }

    public LocalDate getSzuletesiDatum() {
        return szuletesiDatum;
    }

    public void setSzuletesiDatum(LocalDate szuletesiDatum) {
        this.szuletesiDatum = szuletesiDatum;
    }

    public String getTelefonszam() {
        return telefonszam;
    }

    public void setTelefonszam(String telefonszam) {
        this.telefonszam = telefonszam;
    }

    public String getCim() {
        return cim;
    }

    public void setCim(String cim) {
        this.cim = cim;
    }

    public String getVaros() {
        return varos;
    }

    public void setVaros(String varos) {
        this.varos = varos;
    }

    public String getIranyitoszam() {
        return iranyitoszam;
    }

    public void setIranyitoszam(String iranyitoszam) {
        this.iranyitoszam = iranyitoszam;
    }

    public String getHazszam() {
        return hazszam;
    }

    public void setHazszam(String hazszam) {
        this.hazszam = hazszam;
    }

    public Szerepkor getSzerepkor() {
        return szerepkor;
    }

    public void setSzerepkor(Szerepkor szerepkor) {
        this.szerepkor = szerepkor;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getVisszaallitoKod() {
        return visszaallitoKod;
    }

    public void setVisszaallitoKod(String visszaallitoKod) {
        this.visszaallitoKod = visszaallitoKod;
    }

    public LocalDateTime getVisszaallitoKodLejar() {
        return visszaallitoKodLejar;
    }

    public void setVisszaallitoKodLejar(LocalDateTime visszaallitoKodLejar) {
        this.visszaallitoKodLejar = visszaallitoKodLejar;
    }

    public String getAktivaloKod() {
        return aktivaloKod;
    }

    public void setAktivaloKod(String aktivaloKod) {
        this.aktivaloKod = aktivaloKod;
    }

    public LocalDateTime getAktivaloKodLejar() {
        return aktivaloKodLejar;
    }

    public void setAktivaloKodLejar(LocalDateTime aktivaloKodLejar) {
        this.aktivaloKodLejar = aktivaloKodLejar;
    }
}
