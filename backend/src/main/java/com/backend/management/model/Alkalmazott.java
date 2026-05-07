package com.backend.management.model;

import com.backend.management.enums.Szerepkor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "Alkalmazott")
public class Alkalmazott extends Felhasznalo {

    @Column(name = "Adoazonosito")
    private String adoazonosito;

    @Column(name = "Szemelyi_igazolvany_szam")
    private String szemelyiIgazolvanySzam;

    @Column(name = "Taj_szam")
    private String tajSzam;

    @Column(name = "Fizetes")
    private Long fizetes;

    @Column(name = "Felvetel_datum")
    private LocalDate felvetelDatum = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "Csapat_id")
    private Csapat csapat;

    @Column(name = "Teljesitett_feladatok")
    private int teljesitettFeladatok;

    @Column(name = "Lezart_jegyek")
    private int lezartJegyek;

    public Alkalmazott() {
    }

    public Alkalmazott(String keresztnev, String vezeteknev, String email, String jelszo,
                       LocalDate szuletesiDatum, String telefonszam, String cim, String varos,
                       String iranyitoszam, String hazszam, Szerepkor szerepkor, boolean active,
                       String adoazonosito, String szemelyiIgazolvanySzam, String tajSzam, Long fizetes) {
        super(keresztnev, vezeteknev, email, jelszo, szuletesiDatum, telefonszam, cim,
                varos, iranyitoszam, hazszam, szerepkor, active);
        this.adoazonosito = adoazonosito;
        this.szemelyiIgazolvanySzam = szemelyiIgazolvanySzam;
        this.tajSzam = tajSzam;
        this.fizetes = fizetes;
    }

    public String getAdoazonosito() {
        return adoazonosito;
    }

    public void setAdoazonosito(String adoazonosito) {
        this.adoazonosito = adoazonosito;
    }

    public String getSzemelyiIgazolvanySzam() {
        return szemelyiIgazolvanySzam;
    }

    public void setSzemelyiIgazolvanySzam(String szemelyiIgazolvanySzam) {
        this.szemelyiIgazolvanySzam = szemelyiIgazolvanySzam;
    }

    public String getTajSzam() {
        return tajSzam;
    }

    public void setTajSzam(String tajSzam) {
        this.tajSzam = tajSzam;
    }

    public Long getFizetes() {
        return fizetes;
    }

    public void setFizetes(Long fizetes) {
        this.fizetes = fizetes;
    }

    public LocalDate getFelvetelDatum() {
        return felvetelDatum;
    }

    public void setFelvetelDatum(LocalDate felvetelDatum) {
        this.felvetelDatum = felvetelDatum;
    }

    public Csapat getCsapat() {
        return csapat;
    }

    public void setCsapat(Csapat csapat) {
        this.csapat = csapat;
    }

    public int getTeljesitettFeladatok() {
        return teljesitettFeladatok;
    }

    public void setTeljesitettFeladatok(int teljesitettFeladatok) {
        this.teljesitettFeladatok = teljesitettFeladatok;
    }

    public void addTeljesitettFeladatok(long darab) {
        this.teljesitettFeladatok += Math.toIntExact(darab);
    }

    public int getLezartJegyek() {
        return lezartJegyek;
    }

    public void setLezartJegyek(int lezartJegyek) {
        this.lezartJegyek = lezartJegyek;
    }

    public void addLezartJegy() {
        this.lezartJegyek++;
    }
}
