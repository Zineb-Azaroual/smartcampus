package com.smartcampus.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher(1_000);

    @Test
    void leMotDePasseNestJamaisStockeEnClair() {
        String stored = hasher.hash("Campus2026!");
        assertFalse(stored.contains("Campus2026!"));
    }

    @Test
    void reconnaitLeBonMotDePasse() {
        String stored = hasher.hash("Campus2026!");
        assertTrue(hasher.matches("Campus2026!", stored));
    }

    @Test
    void refuseUnMauvaisMotDePasse() {
        String stored = hasher.hash("Campus2026!");
        assertFalse(hasher.matches("campus2026!", stored));
    }

    @Test
    void deuxHachagesDuMemeMotDePasseSontDifferents() {
        assertNotEquals(hasher.hash("Campus2026!"), hasher.hash("Campus2026!"));
    }
}
