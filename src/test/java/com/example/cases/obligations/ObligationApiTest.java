package com.example.cases.obligations;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.example.cases.Http;
import com.jayway.jsonpath.JsonPath;

/** Today: Monday 05-10-2026. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.today=2026-10-05")
@DirtiesContext
class ObligationApiTest {

    @Value("${local.server.port}")
    int port;

    Http http;

    @BeforeEach
    void setUp() {
        http = new Http(port);
    }

    private HttpResponse<String> createObligation(long caseId, String dueDate) {
        return http.post("/cases/" + caseId + "/obligations", """
                {"dueDate": "%s"}
                """.formatted(dueDate));
    }

    @Test
    void acN01_createsObligationWithEffectiveDueDate() {
        HttpResponse<String> response = createObligation(2, "10-10-2026");

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(JsonPath.<String>read(response.body(), "$.effectiveDueDate")).isEqualTo("12-10-2026");
    }

    @Test
    void acN02_atRiskAtThreeWorkingDaysEscalatesToTheCaseOwner() {
        String body = createObligation(2, "08-10-2026").body();

        assertThat(JsonPath.<String>read(body, "$.deadlineState")).isEqualTo("AT_RISK");
        assertThat(JsonPath.<String>read(body, "$.escalationState")).isEqualTo("ESCALATED");
        assertThat(JsonPath.<List<String>>read(body, "$.escalationRecipients")).containsExactly("joost@example.com");
    }

    @Test
    void acN02_onTrackAtFourWorkingDays() {
        String body = createObligation(2, "09-10-2026").body();

        assertThat(JsonPath.<String>read(body, "$.deadlineState")).isEqualTo("ON_TRACK");
    }

    @Test
    void acN03_dueSoonExcludesObligationsOfSoftDeletedCases() {
        String createdCase = http.post("/cases", """
                {"title": "Short-lived case", "ownerEmail": "fatima@example.com", "openedOn": "01-10-2026"}
                """).body();
        int caseId = JsonPath.read(createdCase, "$.id");
        int obligationId = JsonPath.read(createObligation(caseId, "06-10-2026").body(), "$.id");
        assertThat(JsonPath.<List<Integer>>read(http.get("/obligations/due-soon").body(), "$[*].id")).contains(obligationId);

        http.delete("/cases/" + caseId);

        assertThat(JsonPath.<List<Integer>>read(http.get("/obligations/due-soon").body(), "$[*].id"))
                .doesNotContain(obligationId);
    }

    @Test
    void acN04_unknownCaseReturnsLegacyError() {
        HttpResponse<String> response = createObligation(999, "08-10-2026");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(JsonPath.<String>read(response.body(), "$.error")).isEqualTo("Case 999 not found");
    }

    @Test
    void acN04_softDeletedCaseReturnsNotFound() {
        assertThat(createObligation(3, "08-10-2026").statusCode()).isEqualTo(404);
    }

    @Test
    void acN05_isoDueDateIsRejectedWithLegacyError() {
        HttpResponse<String> response = createObligation(2, "2026-10-08");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(JsonPath.<String>read(response.body(), "$.error")).startsWith("Date must be dd-MM-yyyy");
    }

    @Test
    void obligationDatesUseDayMonthYear() {
        String body = createObligation(4, "7-10-2026").body();

        assertThat(JsonPath.<String>read(body, "$.dueDate")).isEqualTo("07-10-2026");
        assertThat(JsonPath.<String>read(body, "$.effectiveDueDate")).matches("\\d{2}-\\d{2}-\\d{4}");
    }
}
