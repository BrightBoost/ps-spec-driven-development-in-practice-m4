package com.example.cases.owners;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.cases.cases.CaseController.CaseResponse;
import com.example.cases.cases.CaseService;
import com.example.cases.common.NotFoundException;

/** Owners are identified by their email address; it is the key in the owner table. */
@RestController
public class OwnerController {

    private final JdbcTemplate jdbc;
    private final CaseService caseService;

    public OwnerController(JdbcTemplate jdbc, CaseService caseService) {
        this.jdbc = jdbc;
        this.caseService = caseService;
    }

    @GetMapping("/owners/{email}/cases")
    public List<CaseResponse> casesForOwner(@PathVariable String email) {
        Integer owners = jdbc.queryForObject("SELECT COUNT(*) FROM owner WHERE email = ?", Integer.class, email);
        if (owners == null || owners == 0) {
            throw new NotFoundException("Owner " + email + " not found");
        }
        return caseService.casesForOwner(email).stream()
                .map(CaseResponse::from)
                .toList();
    }
}
