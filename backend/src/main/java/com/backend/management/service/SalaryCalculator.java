package com.backend.management.service;

import java.time.LocalDate;

public class SalaryCalculator {

    public static long korrigaltBruttoFizetes(Long bruttoFizetes, LocalDate felvetelDatum, LocalDate maiNap) {

        long brutto = 0;

        if (bruttoFizetes != null) {
            brutto = bruttoFizetes;
        }

        if (felvetelDatum != null && maiNap != null) {

            LocalDate otEvvelKesobb = felvetelDatum.plusYears(5);

            if (!otEvvelKesobb.isAfter(maiNap)) {
                brutto = Math.round(brutto * 1.10);
            }
        }

        return brutto;
    }

    public static long nettoFizetes(Long bruttoFizetes, LocalDate felvetelDatum, LocalDate maiNap) {

        long brutto = korrigaltBruttoFizetes(bruttoFizetes, felvetelDatum, maiNap);

        long netto = Math.round(brutto * 0.665);

        return netto;
    }
}