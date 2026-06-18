package com.example.cases.cases;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.cases.Http;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CaseListingTest {

    @Value("${local.server.port}")
    int port;

    Http http;

    @BeforeEach
    void setUp() {
        http = new Http(port);
    }

    @Test
    void listsActiveCases() {
        List<Integer> ids = JsonPath.read(http.get("/cases").body(), "$[*].id");

        assertThat(ids).contains(1, 2, 4);
    }

    @Test
    void deletedCasesAreNotListed() {
        List<Integer> ids = JsonPath.read(http.get("/cases").body(), "$[*].id");

        assertThat(ids).doesNotContain(3);
    }

    @Test
    void casesAreListedNewestFirst() {
        List<Integer> ids = JsonPath.read(http.get("/cases").body(), "$[*].id");

        assertThat(ids).containsSubsequence(4, 2, 1);
    }

    @Test
    void listedDatesUseDayMonthYear() {
        List<String> dates = JsonPath.read(http.get("/cases").body(), "$[*].openedOn");

        assertThat(dates).allMatch(date -> date.matches("\\d{2}-\\d{2}-\\d{4}"));
    }
}
