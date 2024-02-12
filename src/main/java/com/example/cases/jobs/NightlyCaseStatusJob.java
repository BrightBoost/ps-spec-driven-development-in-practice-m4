package com.example.cases.jobs;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.cases.cases.CaseDates;
import com.example.cases.cases.CaseRecord;
import com.example.cases.cases.CaseRepository;

/** Every night, open cases older than two years become DORMANT. */
@Component
public class NightlyCaseStatusJob {

    private final CaseRepository repository;
    private final Clock clock;

    public NightlyCaseStatusJob(CaseRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 2 * * *", zone = "Europe/Amsterdam")
    public void markDormantCases() {
        LocalDate cutoff = LocalDate.now(clock).minusYears(2);
        for (CaseRecord c : repository.findAll()) {
            if ("OPEN".equals(c.status()) && CaseDates.parse(c.openedOn()).isBefore(cutoff)) {
                repository.updateStatus(c.id(), "DORMANT");
            }
        }
    }
}
