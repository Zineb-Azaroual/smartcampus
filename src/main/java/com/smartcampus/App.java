package com.smartcampus;

import com.smartcampus.auth.InMemoryUserRepository;
import com.smartcampus.auth.LoginService;
import com.smartcampus.auth.PasswordHasher;

public class App {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(env("PORT", "8080"));

        LoginService loginService = new LoginService(new InMemoryUserRepository(), new PasswordHasher());
        // Compte de demonstration, en attendant la base PostgreSQL du TP Docker
        loginService.register(env("SC_DEMO_EMAIL", "etudiant@smartcampus.ma"),
                              env("SC_DEMO_PASSWORD", "Campus2026!"), "STUDENT");

        new ApiServer(port, loginService).start();
        System.out.println("SmartCampus démarré sur le port " + port);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
