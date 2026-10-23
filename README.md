# Case management

Case management API for the legal team.

```bash
./mvnw test
./mvnw spring-boot:run
```

Endpoints: `GET /cases`, `GET /cases/{id}`, `POST /cases`, `DELETE /cases/{id}`, `GET /owners/{email}/cases`.

Case dates are dd-MM-yyyy. Errors are returned as {"error": "..."}.

## About this repository

This is the existing ("brownfield") application used in module 4 of the Pluralsight course *Spec-Driven Development with AI in Practice*. In the clips we add filing obligations to cases without breaking the behavior that is already there. The greenfield app from the other modules is the [Obligations Tracker](https://github.com/BrightBoost/ps-spec-driven-development-in-practice).

## Prerequisites

- JDK 21 or newer
- Git
- An AI coding assistant (for example GitHub Copilot in VS Code)
- Always use the Maven wrapper (`./mvnw`), never a separately installed Maven

## How to follow along

Each clip has a branch named `recording-m4c<clip>`. Check it out to see the state shown in that clip.

```bash
git clone https://github.com/BrightBoost/ps-spec-driven-development-in-practice-m4.git
cd ps-spec-driven-development-in-practice-m4
git checkout recording-m4c1
./mvnw test
```

Commit or stash your own changes before switching branches. To see what a clip changed:

```bash
git diff recording-m4c1 recording-m4c2
```

| Clip | Branch | What it contains |
|---|---|---|
| M4C1 | `recording-m4c1` | Brownfield context captured before any change (`.sdd/brownfield-context.md`): endpoints, formats, and the 23-test baseline |
| M4C2 | `recording-m4c2` | Brownfield spec, tasks and DEC-001 (new endpoints keep the existing error shape and dd-MM-yyyy dates) |
| M4C3 | `recording-m4c3` | Final version: filing obligations added (B-001 to B-003) with the existing behavior preserved |

`main` is the same as `recording-m4c3`.

## Where things are

- `.sdd/brownfield-context.md`: what the app does today, with test evidence. Captured before the change.
- `.sdd/spec.md`: the spec for the change, including the behavior that must be preserved
- `.sdd/decisions.md`: approved decisions (DEC-001) and compatibility evidence
- `.sdd/tasks.md`: tasks. `B-001` to `B-003` are verified. `INV-001` (does the nightly job mark cases with obligations `DORMANT`?) is an open investigation.

## Working rules

- The 23 baseline tests must keep passing unchanged. Never change an expected value in an existing test.
- New endpoints follow the existing conventions (DEC-001), not the RFC 9457 and ISO-date conventions of the Obligations Tracker.
- Do not make unrelated changes, such as dependency upgrades, as part of a task.
- Run the task's verification command and then `./mvnw test`.
