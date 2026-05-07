package com.backend.management.model;

import com.backend.management.enums.ProjektStatusz;
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

import java.time.LocalDate;

@Entity
@Table(name = "projekt")
public class Projekt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Letrehozo_id", nullable = false)
    private Felhasznalo letrehozo;

    @ManyToOne
    @JoinColumn(name = "Termek_tulajdonos_id")
    private Alkalmazott termekTulajdonos;

    @ManyToOne
    @JoinColumn(name = "Csapat_id")
    private Csapat csapat;

    @Column(name = "Cim")
    private String cim;

    @Column(name = "Leiras")
    private String leiras;

    @Enumerated(EnumType.STRING)
    @Column(name = "Statusz")
    private ProjektStatusz statusz = ProjektStatusz.PENDING;

    @Column(name = "Projekt_koltseg")
    private long projektKoltseg;

    @Column(name = "Kezdete")
    private LocalDate kezdete;

    @Column(name = "Hatarido")
    private LocalDate hatarido;

    @Column(name = "Befejezve")
    private LocalDate befejezve;

    public Projekt() {
    }

    public Projekt(Felhasznalo letrehozo, String cim, String leiras, long projektKoltseg,
                   LocalDate kezdete, LocalDate hatarido) {
        this.letrehozo = letrehozo;
        this.cim = cim;
        this.leiras = leiras;
        this.projektKoltseg = projektKoltseg;
        this.kezdete = kezdete;
        this.hatarido = hatarido;
        this.statusz = ProjektStatusz.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Felhasznalo getLetrehozo() {
        return letrehozo;
    }

    public void setLetrehozo(Felhasznalo letrehozo) {
        this.letrehozo = letrehozo;
    }

    public Alkalmazott getTermekTulajdonos() {
        return termekTulajdonos;
    }

    public void setTermekTulajdonos(Alkalmazott termekTulajdonos) {
        this.termekTulajdonos = termekTulajdonos;
    }

    public Csapat getCsapat() {
        return csapat;
    }

    public void setCsapat(Csapat csapat) {
        this.csapat = csapat;
    }

    public String getCim() {
        return cim;
    }

    public void setCim(String cim) {
        this.cim = cim;
    }

    public String getLeiras() {
        return leiras;
    }

    public void setLeiras(String leiras) {
        this.leiras = leiras;
    }

    public ProjektStatusz getStatusz() {
        return statusz;
    }

    public void setStatusz(ProjektStatusz statusz) {
        this.statusz = statusz;
    }

    public long getProjektKoltseg() {
        return projektKoltseg;
    }

    public void setProjektKoltseg(long projektKoltseg) {
        this.projektKoltseg = projektKoltseg;
    }

    public LocalDate getKezdete() {
        return kezdete;
    }

    public void setKezdete(LocalDate kezdete) {
        this.kezdete = kezdete;
    }

    public LocalDate getHatarido() {
        return hatarido;
    }

    public void setHatarido(LocalDate hatarido) {
        this.hatarido = hatarido;
    }

    public LocalDate getBefejezve() {
        return befejezve;
    }

    public void setBefejezve(LocalDate befejezve) {
        this.befejezve = befejezve;
    }
}
