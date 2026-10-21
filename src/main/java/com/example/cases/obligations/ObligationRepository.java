package com.example.cases.obligations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class ObligationRepository {

    private static final RowMapper<FilingObligation> MAPPER = (rs, row) -> new FilingObligation(
            rs.getLong("id"),
            rs.getLong("case_id"),
            rs.getString("due_date"),
            rs.getString("effective_due_date"));

    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insert;

    public ObligationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insert = new SimpleJdbcInsert(jdbc).withTableName("filing_obligation").usingGeneratedKeyColumns("id");
    }

    public FilingObligation add(long caseId, String dueDate, String effectiveDueDate) {
        long id = insert.executeAndReturnKey(Map.of(
                "case_id", caseId,
                "due_date", dueDate,
                "effective_due_date", effectiveDueDate)).longValue();
        return new FilingObligation(id, caseId, dueDate, effectiveDueDate);
    }

    public Optional<FilingObligation> findById(long id) {
        return jdbc.query("SELECT * FROM filing_obligation WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    /** Returns obligations for every case, including soft-deleted ones. Filter through CaseService. */
    public List<FilingObligation> findAll() {
        return jdbc.query("SELECT * FROM filing_obligation", MAPPER);
    }
}
