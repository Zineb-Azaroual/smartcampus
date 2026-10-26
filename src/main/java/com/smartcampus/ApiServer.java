package com.smartcampus;

import com.smartcampus.auth.LoginResult;
import com.smartcampus.auth.LoginService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * API HTTP de SmartCampus :
 *   GET  /api/health  etat de l'application (healthcheck Docker)
 *   POST /api/login   email=...&password=...  renvoie un jeton
 *   GET  /api/me      Authorization: Bearer <jeton>
 *   POST /api/logout  Authorization: Bearer <jeton>
 */
public class ApiServer {

    private final HttpServer server;
    private final LoginService loginService;

    public ApiServer(int port, LoginService loginService) throws IOException {
        this.loginService = loginService;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/health", this::health);
        server.createContext("/api/login", this::login);
        server.createContext("/api/me", this::me);
        server.createContext("/api/logout", this::logout);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    private void health(HttpExchange ex) throws IOException {
        if (!allow(ex, "GET")) {
            return;
        }
        send(ex, 200, "{\"status\":\"UP\"}");
    }

    private void login(HttpExchange ex) throws IOException {
        if (!allow(ex, "POST")) {
            return;
        }
        Map<String, String> form = parseForm(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        LoginResult result = loginService.login(form.get("email"), form.get("password"));
        if (result.isSuccess()) {
            send(ex, 200, "{\"token\":\"" + result.token() + "\"}");
        } else {
            send(ex, statusFor(result.status()), "{\"error\":\"" + json(result.message()) + "\"}");
        }
    }

    private void me(HttpExchange ex) throws IOException {
        if (!allow(ex, "GET")) {
            return;
        }
        var user = loginService.currentUser(bearer(ex));
        if (user.isPresent()) {
            send(ex, 200, "{\"email\":\"" + json(user.get()) + "\"}");
        } else {
            send(ex, 401, "{\"error\":\"Non authentifié.\"}");
        }
    }

    private void logout(HttpExchange ex) throws IOException {
        if (!allow(ex, "POST")) {
            return;
        }
        if (loginService.logout(bearer(ex))) {
            ex.sendResponseHeaders(204, -1);
            ex.close();
        } else {
            send(ex, 401, "{\"error\":\"Non authentifié.\"}");
        }
    }

    private static int statusFor(LoginResult.Status status) {
        return switch (status) {
            case INVALID_INPUT -> 400;
            case INVALID_CREDENTIALS -> 401;
            case ACCOUNT_DISABLED -> 403;
            case ACCOUNT_LOCKED -> 423;
            case SUCCESS -> 200;
        };
    }

    private static boolean allow(HttpExchange ex, String method) throws IOException {
        if (method.equals(ex.getRequestMethod())) {
            return true;
        }
        send(ex, 405, "{\"error\":\"Méthode non autorisée.\"}");
        return false;
    }

    private static String bearer(HttpExchange ex) {
        String header = ex.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length()).trim();
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> form = new HashMap<>();
        for (String pair : body.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                form.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
                         URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
            }
        }
        return form;
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }
}
