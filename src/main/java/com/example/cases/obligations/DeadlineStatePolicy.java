package com.example.cases.obligations;

import java.time.LocalDate;

/** Same rules as the Obligations Tracker (AC-N02): at risk from three working days, overdue after. */
public class DeadlineStatePolicy {

    static final int AT_RISK_WORKING_DAYS = 3;

    private final WorkingDayCalendar calendar;

    public DeadlineStatePolicy(WorkingDayCalendar calendar) {
        this.calendar = calendar;
    }

    public LocalDate effectiveDueDate(LocalDate dueDate) {
        return calendar.onOrNextWorkingDay(dueDate);
    }

    public DeadlineState stateFor(LocalDate effectiveDueDate, LocalDate today) {
        if (effectiveDueDate.isBefore(today)) {
            return DeadlineState.OVERDUE;
        }
        int workingDaysLeft = calendar.workingDaysBetween(today, effectiveDueDate);
        return workingDaysLeft <= AT_RISK_WORKING_DAYS ? DeadlineState.AT_RISK : DeadlineState.ON_TRACK;
    }
}
