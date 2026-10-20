# Filing obligations for cases

Status: approved for planning

The deadline rules are the ones specified for the Obligations Tracker: working days in Europe/Amsterdam, roll-forward of weekend and demo-holiday due dates, at risk from three working days before the effective due date, overdue after it, and a due-soon report. This spec adds them to case management without changing existing behavior.

## Existing behavior to preserve

- Case queries SHALL exclude soft-deleted cases
- Error responses SHALL keep the shape `{"error": "..."}`
- The owner email SHALL remain the lookup key for case owners
- Existing date values SHALL remain represented as `dd-MM-yyyy`

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
| Soft-deleted cases excluded | `CaseListingTest.deletedCasesAreNotListed`, `CaseControllerTest.deletedCaseReturnsNotFound`, `OwnerLookupTest.ownerCasesExcludeDeletedCases` |
| Legacy error shape | `CaseControllerTest.unknownCaseReturnsLegacyError`, `errorsAreNotProblemDetails`; captured `curl` in `.sdd/brownfield-context.md` |
| Owner email as key | `OwnerLookupTest.findsCasesByOwnerEmail` |
| `dd-MM-yyyy` dates | `CaseDatesTest`, `CaseListingTest.listedDatesUseDayMonthYear`; new: `ObligationApiTest.obligationDatesUseDayMonthYear` |

## Out of scope

- Migrating the API to RFC 9457 or ISO dates (follow-up, DEC-001)
- Everything out of scope for the Obligations Tracker (notifications, recurring obligations, configurable calendars)
