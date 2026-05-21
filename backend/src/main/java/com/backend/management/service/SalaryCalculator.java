package com.backend.management.service;

import java.time.LocalDate;

public class SalaryCalculator {

    public static long adjustedGrossSalary(Long grossSalary, LocalDate hireDate, LocalDate currentDate) {

        long gross = 0;

        if (grossSalary != null) {
            gross = grossSalary;
        }

        if (hireDate != null && currentDate != null) {

            LocalDate fiveYearsLater = hireDate.plusYears(5);

            if (!fiveYearsLater.isAfter(currentDate)) {
                gross = Math.round(gross * 1.10);
            }
        }

        return gross;
    }

    public static long netSalary(Long grossSalary, LocalDate hireDate, LocalDate currentDate) {

        long gross = adjustedGrossSalary(grossSalary, hireDate, currentDate);

        long net = Math.round(gross * 0.665);

        return net;
    }
}
