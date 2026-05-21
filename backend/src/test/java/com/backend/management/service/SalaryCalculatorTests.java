package com.backend.management.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SalaryCalculatorTests {

    @Test
    void netSalaryUsesHungarianBaseDeductionsWithoutDiscounts() {
        long netSalary = SalaryCalculator.netSalary(500_000L, LocalDate.of(2024, 1, 1), LocalDate.of(2026, 1, 1));

        assertThat(netSalary).isEqualTo(332_500L);
    }

    @Test
    void grossSalaryGetsTenPercentRaiseAfterFiveYears() {
        long effectiveGross = SalaryCalculator.adjustedGrossSalary(500_000L, LocalDate.of(2020, 1, 1), LocalDate.of(2025, 1, 1));
        long netSalary = SalaryCalculator.netSalary(500_000L, LocalDate.of(2020, 1, 1), LocalDate.of(2025, 1, 1));

        assertThat(effectiveGross).isEqualTo(550_000L);
        assertThat(netSalary).isEqualTo(365_750L);
    }

    @Test
    void nullGrossSalaryCountsAsZero() {
        assertThat(SalaryCalculator.netSalary(null, LocalDate.of(2020, 1, 1), LocalDate.of(2026, 1, 1))).isZero();
    }
}
