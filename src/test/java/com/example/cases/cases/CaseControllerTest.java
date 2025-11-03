package com.example.cases.cases;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.example.cases.Http;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class CaseControllerTest {

    @Value("${local.server.port}")
    int port;

    Http http;

    @BeforeEach
    void setUp() {
        http = new Http(port);
    }

    @Test
    void getExistingCaseReturnsItsFields() {
        String body = http.get("/cases/2").body();

        assertThat(JsonPath.<String>read(body, "$.title")).isEqualTo("Licence renewal Utrecht office");
        assertThat(JsonPath.<String>read(body, "$.ownerEmail")).isEqualTo("joost@example.com");
        assertThat(JsonPath.<String>read(body, "$.openedOn")).isEqualTo("02-02-2026");
        assertThat(JsonPath.<String>read(body, "$.status")).isEqualTo("OPEN");
    }

    @Test
    void unknownCaseReturnsLegacyError() {
        HttpResponse<String> response = http.get("/cases/999");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(JsonPath.<String>read(response.body(), "$.error")).isEqualTo("Case 999 not found");
    }

    @Test
    void deletedCaseReturnsNotFound() {
        assertThat(http.get("/cases/3").statusCode()).isEqualTo(404);
    }

    @Test
    void createCaseReturns201WithNormalizedDate() {
        HttpResponse<String> response = http.post("/cases", """
                {"title": "Board minutes review", "ownerEmail": "joost@example.com", "openedOn": "5-10-2026"}
                """);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(JsonPath.<String>read(response.body(), "$.openedOn")).isEqualTo("05-10-2026");
    }

    @Test
    void createWithIsoDateIsRejectedWithLegacyError() {
        HttpResponse<String> response = http.post("/cases", """
                {"title": "Board minutes review", "ownerEmail": "joost@example.com", "openedOn": "2026-10-05"}
                """);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(JsonPath.<String>read(response.body(), "$.error")).startsWith("Date must be dd-MM-yyyy");
    }

    @Test
    void createWithBlankTitleIsRejected() {
        HttpResponse<String> response = http.post("/cases", """
                {"title": "", "ownerEmail": "joost@example.com", "openedOn": "05-10-2026"}
                """);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(JsonPath.<String>read(response.body(), "$.error")).isEqualTo("title is required");
    }

    @Test
    void malformedJsonIsRejectedWithLegacyError() {
        HttpResponse<String> response = http.post("/cases", "{not json");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(JsonPath.<String>read(response.body(), "$.error")).isEqualTo("Malformed request");
    }

    @Test
    void errorsAreNotProblemDetails() {
        HttpResponse<String> response = http.get("/cases/999");

        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                type -> assertThat(type).startsWith("application/json"));
    }

    @Test
    void deleteSoftDeletesTheCase() {
        String created = http.post("/cases", """
                {"title": "Temporary case", "ownerEmail": "fatima@example.com", "openedOn": "01-10-2026"}
                """).body();
        int id = JsonPath.read(created, "$.id");

        assertThat(http.delete("/cases/" + id).statusCode()).isEqualTo(204);
        assertThat(http.get("/cases/" + id).statusCode()).isEqualTo(404);
    }
}
