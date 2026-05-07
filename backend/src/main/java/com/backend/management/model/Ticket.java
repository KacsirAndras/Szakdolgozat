package com.backend.management.model;

import com.backend.management.enums.JegyStatusz;
import com.backend.management.enums.JegyTipus;
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
@Table(name = "jegyek")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Letrehozta", nullable = false)
    private Felhasznalo letrehozta;

    @ManyToOne
    @JoinColumn(name = "It_munkatars_id")
    private Alkalmazott itMunkatars;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipus")
    private JegyTipus tipus;

    @Enumerated(EnumType.STRING)
    @Column(name = "Statusz")
    private JegyStatusz statusz = JegyStatusz.CLOSED;

    @Column(name = "Problema", columnDefinition = "TEXT")
    private String problema;

    public Ticket() {
    }

    public Ticket(Felhasznalo letrehozta, JegyTipus tipus, String problema) {
        this.letrehozta = letrehozta;
        this.tipus = tipus;
        this.problema = problema;
        this.statusz = JegyStatusz.CLOSED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Felhasznalo getLetrehozta() {
        return letrehozta;
    }

    public void setLetrehozta(Felhasznalo letrehozta) {
        this.letrehozta = letrehozta;
    }

    public Alkalmazott getItMunkatars() {
        return itMunkatars;
    }

    public void setItMunkatars(Alkalmazott itMunkatars) {
        this.itMunkatars = itMunkatars;
    }

    public JegyTipus getTipus() {
        return tipus;
    }

    public void setTipus(JegyTipus tipus) {
        this.tipus = tipus;
    }

    public JegyStatusz getStatusz() {
        return statusz;
    }

    public void setStatusz(JegyStatusz statusz) {
        this.statusz = statusz;
    }

    public String getProblema() {
        return problema;
    }

    public void setProblema(String problema) {
        this.problema = problema;
    }
}
