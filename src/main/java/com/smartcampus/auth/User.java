package com.smartcampus.auth;

public record User(String email, String passwordHash, String role, boolean active) {
}
