# Tasks: filing obligations for cases

Baseline: `./mvnw test`, 23 tests passing (see `.sdd/brownfield-context.md`)

## INV-001 Determine whether the nightly case-status job changes cases that have obligations
- Goal: Know whether `DORMANT` affects obligations before relying on case status
- Status: Todo

## B-001 Obligation storage and working-day rules
- Spec links: AC-N01, AC-N02
- Files likely affected: `schema.sql`, `obligations/*`
- Verification: `./mvnw -q -Dtest=ObligationRulesTest test`
- Status: Verified
- Evidence: `./mvnw -q -Dtest=ObligationRulesTest test` passed, 2026-10-21

## B-002 Obligation endpoints with the existing error shape and dates
- Spec links: AC-N01, AC-N04, AC-N05, DEC-001
- Depends on: B-001
- Verification: `./mvnw -q -Dtest=ObligationApiTest test`
- Status: Verified
- Evidence: `./mvnw -q -Dtest=ObligationApiTest test` passed, 2026-10-21

## B-003 Due-soon report through CaseService
- Spec links: AC-N03
- Depends on: B-001
- Verification: `./mvnw -q -Dtest=ObligationApiTest test`, then `./mvnw test` against the baseline
- Status: Verified
- Evidence: `ObligationApiTest.acN03_*` passed; `./mvnw test`: 23 baseline tests unchanged plus 12 new, all passing, 2026-10-21
