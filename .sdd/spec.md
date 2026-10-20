# Filing obligations for cases

Status: approved for planning

The deadline rules are the ones specified for the Obligations Tracker: working days in Europe/Amsterdam, roll-forward of weekend and demo-holiday due dates, at risk from three working days before the effective due date, overdue after it, and a due-soon report. This spec adds them to case management without changing existing behavior.

## Existing behavior to preserve

### Observed API Contracts

- **`GET /cases`**:
  - SHALL return `200 OK` with `application/json` array of all active cases.
  - SHALL exclude soft-deleted cases (`deleted == true`).
  - SHALL sort cases descending by `openedOn` date (newest first).
- **`GET /cases/{id}`**:
  - SHALL return `200 OK` with the case object for an active case.
  - SHALL return `404 Not Found` with `{"error": "Case {id} not found"}` if the case does not exist or is soft-deleted.
- **`POST /cases`**:
  - SHALL accept JSON payload `{"title": "...", "ownerEmail": "...", "openedOn": "..."}`.
  - SHALL validate that `title` is non-null and not blank, returning `400 Bad Request` with `{"error": "title is required"}` on violation.
  - SHALL validate that `ownerEmail` is non-null and not blank, returning `400 Bad Request` with `{"error": "ownerEmail is required"}` on violation.
  - SHALL validate that `openedOn` strictly matches `d-M-uuuu` date format, normalizing stored value to `dd-MM-yyyy`, and returning `400 Bad Request` with `{"error": "Date must be dd-MM-yyyy: <input>"}` on invalid format or invalid calendar date.
  - SHALL return `400 Bad Request` with `{"error": "Malformed request"}` for malformed JSON bodies.
  - SHALL default `status` to `"OPEN"` and `deleted` to `false`.
  - SHALL return `201 Created` with `Location: /cases/{id}` header and the created case payload.
- **`DELETE /cases/{id}`**:
  - SHALL soft-delete the case by updating `deleted = TRUE`.
  - SHALL return `204 No Content` on success.
  - SHALL return `404 Not Found` with `{"error": "Case {id} not found"}` if the case does not exist or has already been soft-deleted.
- **`GET /owners/{email}/cases`**:
  - SHALL return `200 OK` with an array of active cases for registered owner email (or `[]` if none).
  - SHALL return `404 Not Found` with `{"error": "Owner {email} not found"}` if the owner email is not registered in the `owner` table.
  - SHALL exclude soft-deleted cases.
  - SHALL preserve database retrieval order (does not sort by date).

### Data Representation & Error Handling

- Error responses SHALL consistently return `application/json` with shape `{"error": "..."}` (RFC 7807 / RFC 9457 Problem Details are avoided per DEC-001).
- Date values SHALL remain formatted and represented as `dd-MM-yyyy`.
- The owner email SHALL remain the lookup key for case owners.
- Persistence SHALL use Spring JDBC against H2 (`legal_case` and `owner` tables), with soft deletion implemented via the `deleted` column.

### Background Jobs & Scheduling

- `NightlyCaseStatusJob` SHALL run at `02:00:00` daily in `Europe/Amsterdam` time zone (`0 0 2 * * *`).
- The job SHALL transition any `OPEN` case opened strictly before `LocalDate.now(clock).minusYears(2)` to `DORMANT`.
- The clock SHALL support deterministic date overrides via `app.today=YYYY-MM-DD`.

## Observed Contradictions & Undocumented Behaviors

The following contradictions and implicit behaviors were identified in the brownfield codebase (`.sdd/brownfield-context.md`). They are explicitly flagged here to prevent accidental breakage and ensure maximum predictability:

1. **Owner Validation Contradiction**: `GET /owners/{email}/cases` validates owner existence against the `owner` table and returns `404` for unregistered owners. In contrast, `POST /cases` does not validate `ownerEmail` against the `owner` table, and the schema lacks a foreign key constraint, permitting case creation for unregistered owners.
2. **Sorting Inconsistency**: `GET /cases` sorts cases in-memory descending by `openedOn` date, whereas `GET /owners/{email}/cases` does not sort results, returning them in database insertion order.
3. **Non-Idempotent Deletion**: `DELETE /cases/{id}` returns `404 Not Found` on subsequent calls for an already soft-deleted case, rather than returning `204 No Content`.
4. **Nightly Job Modifies Soft-Deleted Cases**: `NightlyCaseStatusJob` queries `findAll()` without filtering `deleted == false`, mutating the `status` of matching soft-deleted records to `DORMANT`.

## New behavior

- `AC-N01`: WHEN a filing obligation is created for a case, THE SYSTEM SHALL store it with a due date, the case owner's email, and an effective due date
- `AC-N02`: Deadline state and escalation follow the tracker rules for at-risk, overdue, and roll-forward, evaluated in Europe/Amsterdam
- `AC-N03`: WHEN the due-soon report is requested, THE SYSTEM SHALL exclude obligations that belong to soft-deleted cases
- `AC-N04`: IF an obligation is created for a case that does not exist or is soft-deleted, THEN THE SYSTEM SHALL return status 404 with the existing error shape
- `AC-N05`: IF the due date is not a valid `dd-MM-yyyy` date, THEN THE SYSTEM SHALL return status 400 with the existing error shape

API (DEC-001): `POST /cases/{caseId}/obligations`, `GET /cases/{caseId}/obligations/{id}`, `GET /obligations/due-soon`. Obligation dates use `dd-MM-yyyy`.

## Compatibility constraints

- Do not change existing case or owner endpoints, their fields, or their status codes
- Obligation queries go through `CaseService`, never directly through `CaseRepository`, so soft-deleted cases stay invisible
- Use the existing Maven wrapper, `schema.sql`, and test fixtures; no new dependencies

## Regression evidence

| Constraint | Evidence |
|---|---|
| Soft-deleted cases excluded from queries | `CaseListingTest.deletedCasesAreNotListed`, `CaseControllerTest.deletedCaseReturnsNotFound`, `OwnerLookupTest.ownerCasesExcludeDeletedCases` |
| Legacy error shape (`{"error": "..."}`) | `CaseControllerTest.unknownCaseReturnsLegacyError`, `CaseControllerTest.errorsAreNotProblemDetails`, `CaseControllerTest.malformedRequestReturnsBadRequest`; captured `curl` in `.sdd/brownfield-context.md` |
| Owner email as lookup key | `OwnerLookupTest.findsCasesByOwnerEmail`, `OwnerLookupTest.unknownOwnerReturnsNotFound` |
| `dd-MM-yyyy` date formatting & parsing | `CaseDatesTest`, `CaseListingTest.listedDatesUseDayMonthYear`, `CaseControllerTest.createNormalizesDate`; new: `ObligationApiTest.obligationDatesUseDayMonthYear` |
| Case creation validation & 201 Created | `CaseControllerTest.createCaseReturns201AndLocation`, `CaseControllerTest.missingTitleReturnsBadRequest`, `CaseControllerTest.missingOwnerEmailReturnsBadRequest`, `CaseControllerTest.invalidDateReturnsBadRequest` |
| Case listing date ordering (descending) | `CaseListingTest.listsCasesDescendingByDate` |
| Soft delete status code (204 No Content) | `CaseControllerTest.deleteReturns204` |
| Nightly dormancy transitions (2-year cutoff) | `NightlyCaseStatusJobTest.oldOpenCasesBecomeDormant`, `NightlyCaseStatusJobTest.recentCasesStayOpen` |

## Out of scope

- Migrating the API to RFC 9457 or ISO dates (follow-up, DEC-001)
- Everything out of scope for the Obligations Tracker (notifications, recurring obligations, configurable calendars)
- Resolving existing brownfield contradictions (e.g. altering owner validation in `POST /cases` or unifying sorting) unless explicitly prioritized in future decisions
