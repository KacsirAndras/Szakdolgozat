package com.backend.management.model;

import com.backend.management.enums.FeladatStatusz;
import com.backend.management.enums.Prioritas;
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
@Table(name = "Feladat")
public class Feladat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Projekt_id", nullable = false)
    private Projekt projekt;

    @ManyToOne
    @JoinColumn(name = "Munkatars_id")
    private Alkalmazott munkatars;

    @Column(name = "Cim")
    private String cim;

    @Column(name = "Leiras")
    private String leiras;

    @Enumerated(EnumType.STRING)
    @Column(name = "Statusz")
    private FeladatStatusz statusz = FeladatStatusz.TO_DO;

    @Enumerated(EnumType.STRING)
    @Column(name = "Prioritas")
    private Prioritas prioritas = Prioritas.MEDIUM;

    public Feladat() {
    }

    public Feladat(Projekt projekt, String cim, String leiras, Prioritas prioritas) {
        this.projekt = projekt;
        this.cim = cim;
        this.leiras = leiras;
        this.prioritas = prioritas == null ? Prioritas.MEDIUM : prioritas;
        this.statusz = FeladatStatusz.TO_DO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Projekt getProjekt() {
        return projekt;
    }

    public void setProjekt(Projekt projekt) {
        this.projekt = projekt;
    }

    public Alkalmazott getMunkatars() {
        return munkatars;
    }

    public void setMunkatars(Alkalmazott munkatars) {
        this.munkatars = munkatars;
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

    public FeladatStatusz getStatusz() {
        return statusz;
    }

    public void setStatusz(FeladatStatusz statusz) {
        this.statusz = statusz;
    }

    public Prioritas getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(Prioritas prioritas) {
        this.prioritas = prioritas;
    }
}
