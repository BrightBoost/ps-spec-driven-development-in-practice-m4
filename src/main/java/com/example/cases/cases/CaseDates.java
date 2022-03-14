package com.example.cases.cases;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class CaseDates {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private CaseDates() {
    }

    public static LocalDate parse(String value) {
        try {
            return LocalDate.parse(value, FORMAT);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Date must be dd-MM-yyyy: " + value);
        }
    }

    public static String format(LocalDate date) {
        return date.format(FORMAT);
    }
}
