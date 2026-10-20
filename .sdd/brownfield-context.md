**Overview of Public Behavior & Constraints**

This report provides an exact mapping of the public contracts, operational constraints, persistence models, test fixtures, and undocumented behaviors of the Case Management application. Understanding these operational realities ensures maximum predictability, minimizes integration friction for dependent systems (such as mobile clients and reporting exports), and preserves data consistency across services.

---

**Endpoints and Contract Specifications**

**`GET /cases`**

* **Purpose**: Retrieves all active (non-deleted) cases ordered chronologically.
* **Request Format**: No query parameters or request body.
* **Response Status**: `200 OK`
* **Response Format**: `application/json` (Array of case objects)
  ```json
  [
    {
      "id": 4,
      "title": "Data protection assessment",
      "ownerEmail": "fatima@example.com",
      "openedOn": "21-08-2026",
      "status": "OPEN"
    },
    {
      "id": 2,
      "title": "Licence renewal Utrecht office",
      "ownerEmail": "joost@example.com",
      "openedOn": "02-02-2026",
      "status": "OPEN"
    },
    {
      "id": 1,
      "title": "Annual report filing",
      "ownerEmail": "sanne@example.com",
      "openedOn": "14-03-2021",
      "status": "OPEN"
    }
  ]
  ```
* **Operational Constraints**:
    * Soft-deleted cases (`deleted == true`) are filtered out.
    * Results are sorted in-memory by parsed `openedOn` date in descending order (newest date first).

---

**`GET /cases/{id}`**

* **Purpose**: Retrieves a single active case by its numeric identifier.
* **Path Parameters**: `id` (`long`, e.g., `/cases/2`)
* **Response Status**:
    * `200 OK` on success.
    * `404 Not Found` if the case ID does not exist or has been soft-deleted.
* **Response Format**: `application/json`
    * Success (`200 OK`):
      ```json
      {
        "id": 2,
        "title": "Licence renewal Utrecht office",
        "ownerEmail": "joost@example.com",
        "openedOn": "02-02-2026",
        "status": "OPEN"
      }
      ```
    * Not Found (`404 Not Found`):
      ```json
      {
        "error": "Case 999 not found"
      }
      ```

---

**`POST /cases`**

* **Purpose**: Creates a new legal case.
* **Request Format**: `application/json`
  ```json
  {
    "title": "Board minutes review",
    "ownerEmail": "joost@example.com",
    "openedOn": "5-10-2026"
  }
  ```
* **Validation & Constraints**:
    * `title`: Mandatory; cannot be `null` or blank. Violation yields `400 Bad Request` with `{"error": "title is required"}`.
    * `ownerEmail`: Mandatory; cannot be `null` or blank. Violation yields `400 Bad Request` with `{"error": "ownerEmail is required"}`.
    * `openedOn`: Mandatory; must follow `d-M-uuuu` strict format (day-month-year). Leading zeros can be omitted in input (e.g., `5-10-2026`), but ISO format (e.g., `2026-10-05`) or invalid calendar dates (e.g., `31-02-2025`) yield `400 Bad Request` with `{"error": "Date must be dd-MM-yyyy: <input>"}`.
    * Malformed JSON payloads produce `400 Bad Request` with `{"error": "Malformed request"}`.
* **State Defaults on Creation**:
    * `status`: Always initialized to `"OPEN"`.
    * `deleted`: Always initialized to `false`.
    * `openedOn`: Normalized to two-digit format `dd-MM-yyyy` upon storage (e.g., `05-10-2026`).
* **Response Status**: `201 Created`
* **Response Headers**: `Location: /cases/{id}`
* **Response Format**: `application/json` (created `CaseResponse` object).

---

**`DELETE /cases/{id}`**

* **Purpose**: Soft-deletes a case by setting `deleted = TRUE`.
* **Path Parameters**: `id` (`long`, e.g., `/cases/2`)
* **Response Status**:
    * `204 No Content` (empty body) on success.
    * `404 Not Found` with `{"error": "Case {id} not found"}` if the case does not exist or is already soft-deleted.

---

**`GET /owners/{email}/cases`**

* **Purpose**: Retrieves all active cases owned by a registered owner email.
* **Path Parameters**: `email` (`String`, e.g., `/owners/joost@example.com/cases`)
* **Response Status**:
    * `200 OK` on success.
    * `404 Not Found` with `{"error": "Owner {email} not found"}` if no record exists for that email in the `owner` table.
* **Response Format**: `application/json` (Array of `CaseResponse` objects, or `[]` if the owner exists but has no active cases).
* **Operational Constraints**:
    * Soft-deleted cases are excluded.
    * Output retains database retrieval order (unlike `/cases`, no date sorting is applied).

---

**Data Representation & Error Contract**

**Entity Data Representation**

* **Case (`legal_case`)**:
    * `id`: `long` (Database Auto-increment `BIGINT`)
    * `title`: `String` (Database `VARCHAR(255)`)
    * `ownerEmail`: `String` (Database `VARCHAR(255)`)
    * `openedOn`: `String` (Database `VARCHAR(10)`, strictly formatted as `dd-MM-yyyy`)
    * `status`: `String` (`OPEN`, `DORMANT`, etc.)
    * `deleted`: `boolean` (`BOOLEAN DEFAULT FALSE`, internal flag, omitted from API responses)
* **Owner (`owner`)**:
    * `email`: `String` (`VARCHAR(255)`, Primary Key)
    * `name`: `String` (`VARCHAR(255)`)

**Error Handling Contract**

* All application-level HTTP errors avoid RFC 7807 Problem Details and return a unified single-field JSON payload:
  ```json
  {
    "error": "Descriptive error message"
  }
  ```
* Media type is consistently `application/json`.

---

**Persistence Assumptions & Architectural Mechanics**

1. **Storage Mechanism**:
    * Uses Spring Boot JDBC (`JdbcTemplate` and `SimpleJdbcInsert`) directly against an in-memory or embedded H2 database.
    * Auto-initialization executes `schema.sql` and `data.sql` at startup via `spring.sql.init.mode=always`.
2. **Schema Constraints**:
    * `owner`: `email` is primary key, `name` is `NOT NULL`.
    * `legal_case`: `id` is primary key (`BIGINT AUTO_INCREMENT`), all other fields are `NOT NULL`.
    * **Foreign Keys**: There is **no foreign key constraint** between `legal_case.owner_email` and `owner.email` defined in `schema.sql`.
3. **Soft Deletion Mechanics**:
    * Deleting records never executes `DELETE FROM legal_case`; it executes `UPDATE legal_case SET deleted = TRUE WHERE id = ?`.
    * Filtering of soft-deleted records is performed in the Java service layer (`c -> !c.deleted()`) rather than via database `WHERE deleted = FALSE` clauses.

---

**Background Jobs & Scheduling**

**`NightlyCaseStatusJob`**

* **Trigger**: Scheduled cron expression `0 0 2 * * *` (executed at 02:00:00 every night) in the `Europe/Amsterdam` time zone.
* **Functional Logic**:
    * Calculates `cutoff = LocalDate.now(clock).minusYears(2)`.
    * Scans all cases via `repository.findAll()`.
    * For every record where `status == "OPEN"` and `openedOn` is strictly before `cutoff`, executes `UPDATE legal_case SET status = 'DORMANT' WHERE id = ?`.
* **Clock Configuration**:
    * Backed by `ClockConfig`. Defaults to system clock in `Europe/Amsterdam`.
    * Allows date overrides via the `app.today` property (`app.today=YYYY-MM-DD` at `12:00:00 PM Amsterdam time`), maximizing determinism during automated testing.

---

**Test Fixtures & Seed State**

* **Seed Data (`data.sql`)**:
    * Owners:
        1. `sanne@example.com` (`Sanne`)
        2. `joost@example.com` (`Joost`)
        3. `fatima@example.com` (`Fatima`)
    * Legal Cases:
        1. ID 1: `"Annual report filing"`, `sanne@example.com`, `14-03-2021`, `OPEN`, `deleted=false` (Candidate for dormancy).
        2. ID 2: `"Licence renewal Utrecht office"`, `joost@example.com`, `02-02-2026`, `OPEN`, `deleted=false`.
        3. ID 3: `"Supplier audit (opened in error)"`, `sanne@example.com`, `10-01-2026`, `OPEN`, `deleted=true` (Pre-seeded soft-deleted fixture).
        4. ID 4: `"Data protection assessment"`, `fatima@example.com`, `21-08-2026`, `OPEN`, `deleted=false`.

* **Test Suite Verifications**:
    * `CaseControllerTest`: Validates `GET`, `POST`, `DELETE`, date normalization (`5-10-2026` -> `05-10-2026`), reject formats, and `{"error": "..."}` responses.
    * `CaseListingTest`: Verifies exclusion of case 3 and descending order by date (`[4, 2, 1]`).
    * `NightlyCaseStatusJobTest`: Uses `app.today=2026-10-07` to verify case 1 becomes `DORMANT` while case 2 remains `OPEN`.
    * `OwnerLookupTest`: Tests owner case retrieval and 404 behavior for unknown owners.
    * `CaseDatesTest`: Unit-tests parsing and formatting routines.

---

**Undocumented & Implicit Behaviors**

1. **Unchecked Owner Existence on Case Creation**:
    * While `GET /owners/{email}/cases` checks for owner existence and returns a `404` if the owner is missing, `POST /cases` **does not check** whether `ownerEmail` exists in the `owner` table. Cases with unregistered owner emails can be created successfully without error.
2. **Inconsistent Sorting Across Endpoints**:
    * `GET /cases` sorts results chronologically descending by `openedOn`.
    * `GET /owners/{email}/cases` does not sort cases by date, returning them in arbitrary/insertion database order.
3. **Non-Idempotent Deletes**:
    * Calling `DELETE /cases/{id}` on an already-deleted case returns `404 Not Found` (because `getCase(id)` checks `!c.deleted()`) rather than returning `204 No Content`.
4. **Nightly Job Modifies Soft-Deleted Records**:
    * `NightlyCaseStatusJob` iterates over `repository.findAll()` without checking `c.deleted()`. Soft-deleted cases matching the criteria are updated to `DORMANT` in the database.
5. **Full Table Scan In-Memory Processing**:
    * Filtering active records and sorting occurs in application memory on full table lists rather than database-level queries (`SELECT * FROM legal_case`).
6. **Strict Day-Month-Year Parsing**:
    * Parsing uses `ResolverStyle.STRICT` with pattern `d-M-uuuu`. Year representations and month/day boundaries are strictly validated; invalid dates fail immediately with 400. All stored dates and outbound representations enforce zero-padded `dd-MM-yyyy`.