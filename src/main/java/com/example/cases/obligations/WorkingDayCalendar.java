package com.example.cases.obligations;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Copied from the Obligations Tracker (same rules, AC-N02).
 * Working days in Europe/Amsterdam for the demo years 2026 and 2027.
 * The holiday list is fixed on purpose: user-configurable calendars are out of scope.
 */
public class WorkingDayCalendar {

    static final Set<LocalDate> FIXED_DEMO_HOLIDAYS = Set.of(
            // 2026
            LocalDate.of(2026, 1, 1),   // New Year's Day
            LocalDate.of(2026, 4, 6),   // Easter Monday
            LocalDate.of(2026, 4, 27),  // King's Day
            LocalDate.of(2026, 5, 5),   // Liberation Day
            LocalDate.of(2026, 5, 14),  // Ascension Day
            LocalDate.of(2026, 5, 25),  // Whit Monday
            LocalDate.of(2026, 12, 25), // Christmas Day
            LocalDate.of(2026, 12, 26), // Boxing Day
            // 2027
            LocalDate.of(2027, 1, 1),
            LocalDate.of(2027, 3, 29),
            LocalDate.of(2027, 4, 27),
            LocalDate.of(2027, 5, 5),
            LocalDate.of(2027, 5, 6),
            LocalDate.of(2027, 5, 17),
            LocalDate.of(2027, 12, 25),
            LocalDate.of(2027, 12, 26));

    public boolean isWorkingDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY
                && day != DayOfWeek.SUNDAY
                && !FIXED_DEMO_HOLIDAYS.contains(date);
    }

    /** The date itself if it is a working day, otherwise the next working day (AC-004). */
    public LocalDate onOrNextWorkingDay(LocalDate date) {
        LocalDate candidate = date;
        while (!isWorkingDay(candidate)) {
            candidate = candidate.plusDays(1);
        }
        return candidate;
    }

    /**
     * Number of working days after {@code from}, up to and including {@code to}.
     * Monday to Thursday of the same week is 3. The same day is 0.
     */
    public int workingDaysBetween(LocalDate from, LocalDate to) {
        if (!to.isAfter(from)) {
            return 0;
        }
        int count = 0;
        for (LocalDate day = from.plusDays(1); !day.isAfter(to); day = day.plusDays(1)) {
            if (isWorkingDay(day)) {
                count++;
            }
        }
        return count;
    }
}
