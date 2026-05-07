package com.backend.management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Csapat")
public class Csapat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Nev")
    private String nev;

    @OneToOne
    @JoinColumn(name = "Tulajdonos_id", nullable = false, unique = true)
    private Alkalmazott tulajdonos;

    @OneToMany(mappedBy = "csapat")
    private List<Alkalmazott> tagok = new ArrayList<>();

    public Csapat() {
    }

    public Csapat(String nev, Alkalmazott tulajdonos) {
        this.nev = nev;
        this.tulajdonos = tulajdonos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNev() {
        return nev;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    public Alkalmazott getTulajdonos() {
        return tulajdonos;
    }

    public void setTulajdonos(Alkalmazott tulajdonos) {
        this.tulajdonos = tulajdonos;
    }

    public List<Alkalmazott> getTagok() {
        return tagok;
    }

    public void setTagok(List<Alkalmazott> tagok) {
        this.tagok = tagok;
    }
}
