package com.example.cases.cases;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class CaseRepository {

    private static final RowMapper<CaseRecord> MAPPER = (rs, row) -> new CaseRecord(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("owner_email"),
            rs.getString("opened_on"),
            rs.getString("status"),
            rs.getBoolean("deleted"));

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insert;

    public CaseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insert = new SimpleJdbcInsert(jdbc).withTableName("legal_case").usingGeneratedKeyColumns("id");
    }

    public List<CaseRecord> findAll() {
        return jdbc.query("SELECT * FROM legal_case", MAPPER);
    }

    public Optional<CaseRecord> findById(long id) {
        return jdbc.query("SELECT * FROM legal_case WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    public List<CaseRecord> findByOwnerEmail(String ownerEmail) {
        return jdbc.query("SELECT * FROM legal_case WHERE owner_email = ?", MAPPER, ownerEmail);
    }

    public long insert(String title, String ownerEmail, String openedOn) {
        Number id = insert.executeAndReturnKey(Map.of(
                "title", title,
                "owner_email", ownerEmail,
                "opened_on", openedOn,
                "status", "OPEN",
                "deleted", false));
        return id.longValue();
    }

    public void markDeleted(long id) {
        jdbc.update("UPDATE legal_case SET deleted = TRUE WHERE id = ?", id);
    }

    public void updateStatus(long id, String status) {
        jdbc.update("UPDATE legal_case SET status = ? WHERE id = ?", status, id);
    }
}
