package com.example.cases;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Small HTTP helper for API tests. */
public class Http {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String baseUrl;

    public Http(int port) {
        this.baseUrl = "http://localhost:" + port;
    }

    public HttpResponse<String> get(String path) {
        return send(HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build());
    }

    public HttpResponse<String> delete(String path) {
        return send(HttpRequest.newBuilder(URI.create(baseUrl + path)).DELETE().build());
    }

    public HttpResponse<String> post(String path, String json) {
        return send(HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build());
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
