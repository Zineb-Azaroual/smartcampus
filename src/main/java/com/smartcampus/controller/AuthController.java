package com.smartcampus.controller;

import com.smartcampus.auth.LoginService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smartcampus.auth.LoginResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String email,
            @RequestParam String password) {

        LoginResult result = loginService.login(email, password);

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                    java.util.Map.of("token", result.token())
            );
        }

        int status = switch (result.status()) {
            case INVALID_INPUT -> 400;
            case INVALID_CREDENTIALS -> 401;
            case ACCOUNT_DISABLED -> 403;
            case ACCOUNT_LOCKED -> 423;
            case SUCCESS -> 200;
        };

        return ResponseEntity.status(status).body(
                java.util.Map.of("error", result.message())
        );
    }
    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String token = null;

        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring("Bearer ".length()).trim();
        }

        Optional<String> user = loginService.currentUser(token);

        if (user.isPresent()) {
            return ResponseEntity.ok(
                    Map.of("email", user.get())
            );
        }
        return ResponseEntity.status(401).body(
                Map.of("error", "Non authentifié.")
        );
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String token = null;

        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring("Bearer ".length()).trim();
        }

        if (loginService.logout(token)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(401).body(
                Map.of("error", "Non authentifié.")
        );
    }
}