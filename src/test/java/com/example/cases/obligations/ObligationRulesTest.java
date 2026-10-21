package com.example.cases.obligations;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ObligationRulesTest {

    private final DeadlineStatePolicy policy = new DeadlineStatePolicy(new WorkingDayCalendar());
    private final LocalDate monday = LocalDate.of(2026, 10, 5);

    @Test
    void atRiskAtThreeWorkingDays() {
        assertThat(policy.stateFor(LocalDate.of(2026, 10, 8), monday)).isEqualTo(DeadlineState.AT_RISK);
    }

    @Test
    void onTrackAtFourWorkingDays() {
        assertThat(policy.stateFor(LocalDate.of(2026, 10, 9), monday)).isEqualTo(DeadlineState.ON_TRACK);
    }

    @Test
    void overdueAfterTheEffectiveDueDate() {
        assertThat(policy.stateFor(LocalDate.of(2026, 10, 2), monday)).isEqualTo(DeadlineState.OVERDUE);
    }

    @Test
    void weekendDueDateRollsForward() {
        assertThat(policy.effectiveDueDate(LocalDate.of(2026, 10, 10))).isEqualTo(LocalDate.of(2026, 10, 12));
    }
}
