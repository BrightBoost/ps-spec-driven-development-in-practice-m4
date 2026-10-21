CREATE TABLE owner (
    email VARCHAR(255) PRIMARY KEY,
    name  VARCHAR(255) NOT NULL
);

CREATE TABLE legal_case (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    opened_on   VARCHAR(10)  NOT NULL,  -- dd-MM-yyyy
    status      VARCHAR(20)  NOT NULL,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE filing_obligation (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id            BIGINT      NOT NULL REFERENCES legal_case (id),
    due_date           VARCHAR(10) NOT NULL,  -- dd-MM-yyyy
    effective_due_date VARCHAR(10) NOT NULL   -- dd-MM-yyyy
);
