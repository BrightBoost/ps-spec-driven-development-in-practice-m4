package com.example.cases.cases;

/** A case. Dates are stored and returned as dd-MM-yyyy strings; clients depend on that format. */
public record CaseRecord(
        long id,
        String title,
        String ownerEmail,
        String openedOn,
        String status,
        boolean deleted) {
}
