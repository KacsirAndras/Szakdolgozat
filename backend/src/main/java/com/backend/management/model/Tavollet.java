package com.backend.management.model;

import java.time.LocalDate;

import com.backend.management.enums.TavolletStatusz;
import com.backend.management.enums.TavolletTipus;

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
@Table(name = "Tavollet")
public class Tavollet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Munkatars_id")
    private Alkalmazott munkatars;

    @ManyToOne
    @JoinColumn(name = "Jovahagyta")
    private Alkalmazott jovahagyta;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipus")
    private TavolletTipus tipus;

    @Enumerated(EnumType.STRING)
    @Column(name = "Statusz")
    private TavolletStatusz statusz = TavolletStatusz.PENDING;

    @Column(name = "Kezdet")
    private LocalDate kezdet;

    @Column(name = "Vege")
    private LocalDate vege;

    @Column(name = "Munkanapok")
    private int munkanapok;

    public Tavollet() {
    }

    public Tavollet(Alkalmazott munkatars, Alkalmazott jovahagyta, TavolletTipus tipus,
                    LocalDate kezdet, LocalDate vege) {
        this.munkatars = munkatars;
        this.jovahagyta = jovahagyta;
        this.tipus = tipus;
        this.kezdet = kezdet;
        this.vege = vege;
    }

    public Tavollet(Alkalmazott munkatars, Alkalmazott jovahagyta, TavolletTipus tipus,
                    LocalDate vege) {
        this(munkatars, jovahagyta, tipus, LocalDate.now(), vege);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Alkalmazott getMunkatars() {
        return munkatars;
    }

    public void setMunkatars(Alkalmazott munkatars) {
        this.munkatars = munkatars;
    }

    public Alkalmazott getJovahagyta() {
        return jovahagyta;
    }

    public void setJovahagyta(Alkalmazott jovahagyta) {
        this.jovahagyta = jovahagyta;
    }

    public TavolletTipus getTipus() {
        return tipus;
    }

    public void setTipus(TavolletTipus tipus) {
        this.tipus = tipus;
    }

    public TavolletStatusz getStatusz() {
        return statusz;
    }

    public void setStatusz(TavolletStatusz statusz) {
        this.statusz = statusz;
    }

    public LocalDate getKezdet() {
        return kezdet;
    }

    public void setKezdet(LocalDate kezdet) {
        this.kezdet = kezdet;
    }

    public LocalDate getVege() {
        return vege;
    }

    public void setVege(LocalDate vege) {
        this.vege = vege;
    }

    public int getMunkanapok() {
        return munkanapok;
    }

    public void setMunkanapok(int munkanapok) {
        this.munkanapok = munkanapok;
    }
}
