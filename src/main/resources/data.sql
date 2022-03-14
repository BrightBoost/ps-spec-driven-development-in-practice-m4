INSERT INTO owner (email, name) VALUES ('sanne@example.com', 'Sanne');
INSERT INTO owner (email, name) VALUES ('joost@example.com', 'Joost');
INSERT INTO owner (email, name) VALUES ('fatima@example.com', 'Fatima');

INSERT INTO legal_case (title, owner_email, opened_on, status, deleted) VALUES ('Annual report filing', 'sanne@example.com', '14-03-2021', 'OPEN', FALSE);
INSERT INTO legal_case (title, owner_email, opened_on, status, deleted) VALUES ('Licence renewal Utrecht office', 'joost@example.com', '02-02-2026', 'OPEN', FALSE);
INSERT INTO legal_case (title, owner_email, opened_on, status, deleted) VALUES ('Supplier audit (opened in error)', 'sanne@example.com', '10-01-2026', 'OPEN', TRUE);
INSERT INTO legal_case (title, owner_email, opened_on, status, deleted) VALUES ('Data protection assessment', 'fatima@example.com', '21-08-2026', 'OPEN', FALSE);
