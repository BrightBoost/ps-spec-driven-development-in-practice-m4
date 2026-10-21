package com.example.cases.obligations;

/** Dates are dd-MM-yyyy strings, like every other date in this application (DEC-001). */
public record FilingObligation(long id, long caseId, String dueDate, String effectiveDueDate) {
}
