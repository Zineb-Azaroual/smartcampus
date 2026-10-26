package com.smartcampus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.smartcampus.auth.InMemoryUserRepository;
import com.smartcampus.auth.LoginService;
import com.smartcampus.auth.PasswordHasher;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApiServerTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private ApiServer server;
    private String base;

    @BeforeEach
    void start() throws Exception {
        LoginService loginService = new LoginService(new InMemoryUserRepository(), new PasswordHasher(1_000));
        loginService.register("etudiant@smartcampus.ma", "Campus2026!", "STUDENT");
        server = new ApiServer(0, loginService);
        server.start();
        base = "http://localhost:" + server.port();
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    @Test
    void healthRepondUp() throws Exception {
        HttpResponse<String> response = get("/api/health", null);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("UP"));
    }

    @Test
    void parcoursCompletConnexionPuisDeconnexion() throws Exception {
        HttpResponse<String> login = postForm("/api/login", "email=etudiant%40smartcampus.ma&password=Campus2026%21");
        assertEquals(200, login.statusCode());
        String token = login.body().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        assertEquals(200, get("/api/me", token).statusCode());
        assertEquals(204, post("/api/logout", token).statusCode());
        assertEquals(401, get("/api/me", token).statusCode());
    }

    @Test
    void mauvaisMotDePasseRenvoie401() throws Exception {
        HttpResponse<String> response = postForm("/api/login", "email=etudiant%40smartcampus.ma&password=faux");

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("incorrect"));
    }

    @Test
    void mauvaiseMethodeRenvoie405() throws Exception {
        assertEquals(405, get("/api/login", null).statusCode());
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + path)).GET();
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + path))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postForm(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
