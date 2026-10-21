# Decisions: case-management

## DEC-001 (case management): New obligation endpoints use the existing error shape and dd-MM-yyyy dates

- Date: 2026-10-19
- Options: (a) RFC 9457 Problem Details and ISO dates, as in the Obligations Tracker; (b) the existing conventions of this API
- Decision: (b)
- Rationale: Clients should not handle two error formats or two date formats in one API
- Follow-up: Migrate the whole API to RFC 9457 and ISO dates as a separate, versioned change
- Approved by: the lawyer, as product owner, and the case-management maintainers

### Compatibility evidence (2026-10-21)

- `./mvnw test`: the 23 baseline tests pass unchanged; 12 new tests pass
- `curl` before and after: `GET /cases/2` fields and `dd-MM-yyyy` dates unchanged; `GET /cases/3` (soft-deleted) still 404; `POST /cases` with an ISO date still 400 `{"error": ...}`
- `git diff --stat`: no changes to existing case or owner code. An unrelated test-library upgrade in `pom.xml` was reverted; it can be proposed as a separate task.
