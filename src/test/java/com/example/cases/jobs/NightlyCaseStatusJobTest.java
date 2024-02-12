package com.example.cases.jobs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.example.cases.cases.CaseRepository;

@SpringBootTest(properties = "app.today=2026-10-07")
@DirtiesContext
class NightlyCaseStatusJobTest {

    @Autowired
    NightlyCaseStatusJob job;

    @Autowired
    CaseRepository repository;

    @Test
    void marksOpenCasesOlderThanTwoYearsDormant() {
        job.markDormantCases();

        assertThat(repository.findById(1)).hasValueSatisfying(c -> assertThat(c.status()).isEqualTo("DORMANT"));
    }

    @Test
    void leavesRecentCasesOpen() {
        job.markDormantCases();

        assertThat(repository.findById(2)).hasValueSatisfying(c -> assertThat(c.status()).isEqualTo("OPEN"));
    }
}
