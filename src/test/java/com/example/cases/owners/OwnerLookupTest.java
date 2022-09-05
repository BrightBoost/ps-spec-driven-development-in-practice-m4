package com.example.cases.owners;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.cases.Http;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OwnerLookupTest {

    @Value("${local.server.port}")
    int port;

    Http http;

    @BeforeEach
    void setUp() {
        http = new Http(port);
    }

    @Test
    void findsCasesByOwnerEmail() {
        List<Integer> ids = JsonPath.read(http.get("/owners/joost@example.com/cases").body(), "$[*].id");

        assertThat(ids).containsExactly(2);
    }

    @Test
    void ownerCasesExcludeDeletedCases() {
        List<Integer> ids = JsonPath.read(http.get("/owners/sanne@example.com/cases").body(), "$[*].id");

        assertThat(ids).containsExactly(1);
    }

    @Test
    void unknownOwnerReturnsLegacyError() {
        HttpResponse<String> response = http.get("/owners/nobody@example.com/cases");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(JsonPath.<String>read(response.body(), "$.error")).isEqualTo("Owner nobody@example.com not found");
    }
}
