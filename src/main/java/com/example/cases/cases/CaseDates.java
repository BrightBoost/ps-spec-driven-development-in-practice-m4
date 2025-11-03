package com.example.cases.cases;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/** Case dates are dd-MM-yyyy strings. Input may omit leading zeros; output never does. */
public final class CaseDates {

    private static final DateTimeFormatter INPUT =
            DateTimeFormatter.ofPattern("d-M-uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter OUTPUT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private CaseDates() {
    }

    public static LocalDate parse(String value) {
        try {
            return LocalDate.parse(value, INPUT);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Date must be dd-MM-yyyy: " + value);
        }
    }

    public static String format(LocalDate date) {
        return date.format(OUTPUT);
    }
}
