package com.example.cases.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class CaseDatesTest {

    @Test
    void parsesDayMonthYear() {
        assertThat(CaseDates.parse("14-03-2021")).isEqualTo(LocalDate.of(2021, 3, 14));
    }

    @Test
    void formatsWithLeadingZeros() {
        assertThat(CaseDates.format(LocalDate.of(2025, 3, 5))).isEqualTo("05-03-2025");
    }

    @Test
    void rejectsIsoDates() {
        assertThatThrownBy(() -> CaseDates.parse("2025-03-05")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsImpossibleDates() {
        assertThatThrownBy(() -> CaseDates.parse("31-02-2025")).isInstanceOf(IllegalArgumentException.class);
    }
}
