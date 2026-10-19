# Decisions: case-management

## DEC-001 (case management): New obligation endpoints use the existing error shape and dd-MM-yyyy dates

- Date: 2026-10-19
- Options: (a) RFC 9457 Problem Details and ISO dates, as in the Obligations Tracker; (b) the existing conventions of this API
- Decision: (b)
- Rationale: Clients should not handle two error formats or two date formats in one API
- Follow-up: Migrate the whole API to RFC 9457 and ISO dates as a separate, versioned change
- Approved by: the lawyer, as product owner, and the case-management maintainers
