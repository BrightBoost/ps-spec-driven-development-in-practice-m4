# Brownfield context: case-management

Captured before any change. Baseline: `./mvnw test`, 23 tests, all passing (2026-10-19).

## Endpoints

| Method and path | Behavior |
|---|---|
| `GET /cases` | Active cases, newest `openedOn` first |
| `GET /cases/{id}` | One case; 404 if unknown |
| `POST /cases` | Creates a case; 201; `openedOn` normalized to `dd-MM-yyyy` |
| `DELETE /cases/{id}` | 204 |
| `GET /owners/{email}/cases` | Cases for an owner; 404 if the owner email is unknown |

## Request and response formats

- Case JSON fields: `id`, `title`, `ownerEmail`, `openedOn`, `status`
- Dates are `dd-MM-yyyy` strings, in the database and on the wire. Input may omit leading zeros (`5-3-2025`); output never does. ISO dates are rejected. Evidence: `CaseDatesTest`, `CaseControllerTest.createWithIsoDateIsRejectedWithLegacyError`
- Errors are `{"error": "..."}` with `Content-Type: application/json`, not Problem Details. Evidence: `CaseControllerTest.unknownCaseReturnsLegacyError`, `errorsAreNotProblemDetails`

## Persistence

- H2 via `JdbcTemplate`; schema in `schema.sql`, seed data in `data.sql`
- `owner.email` is the primary key of the owner table and the lookup key for owners. Cases reference owners by `owner_email`. Evidence: `OwnerLookupTest`

## Test fixtures

- Seed cases 1, 2, 4 are active; case 3 is flagged deleted
- `NightlyCaseStatusJobTest` pins today with `app.today`

## Undocumented behavior

- `NightlyCaseStatusJob` runs at 02:00 Europe/Amsterdam and sets OPEN cases older than two years to `DORMANT`. It reads cases through the repository directly. Impact on other features unknown.

## Found in the baseline tests (missed by the agent's reconnaissance)

- **Soft delete.** Cases are never removed. `DELETE /cases/{id}` sets `deleted = TRUE`. The filtering is not in the queries or the repository: every `CaseService` method filters `!c.deleted()` by convention. Code that reads `CaseRepository` directly sees deleted cases. The rule is stated plainly only in a test name: `CaseListingTest.deletedCasesAreNotListed`. Also: `CaseControllerTest.deletedCaseReturnsNotFound`, `OwnerLookupTest.ownerCasesExcludeDeletedCases`
