package com.smartcampus.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginServiceTest {

    private static final String EMAIL = "etudiant@smartcampus.ma";
    private static final String PASSWORD = "Campus2026!";

    private InMemoryUserRepository repository;
    private PasswordHasher hasher;
    private MutableClock clock;
    private LoginService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        hasher = new PasswordHasher(1_000);
        clock = new MutableClock(Instant.parse("2026-10-26T10:00:00Z"));
        service = new LoginService(repository, hasher, clock);
        service.register(EMAIL, PASSWORD, "STUDENT");
    }

    // Critere 1 : compte actif et identifiants corrects -> acces a l'application

    @Test
    void connexionReussieAvecBonsIdentifiants() {
        LoginResult result = service.login(EMAIL, PASSWORD);

        assertTrue(result.isSuccess());
        assertNotNull(result.token());
        assertEquals(Optional.of(EMAIL), service.currentUser(result.token()));
    }

    @Test
    void lEmailNeDependPasDeLaCasseNiDesEspaces() {
        assertTrue(service.login("  Etudiant@SmartCampus.MA ", PASSWORD).isSuccess());
    }

    // Critere 2 : mot de passe incorrect -> message comprehensible

    @Test
    void motDePasseIncorrectAfficheUnMessageClair() {
        LoginResult result = service.login(EMAIL, "mauvais");

        assertEquals(LoginResult.Status.INVALID_CREDENTIALS, result.status());
        assertEquals("E-mail ou mot de passe incorrect.", result.message());
    }

    @Test
    void emailInconnuDonneLeMemeMessageQueMauvaisMotDePasse() {
        LoginResult unknown = service.login("inconnu@smartcampus.ma", PASSWORD);
        LoginResult wrong = service.login(EMAIL, "mauvais");

        assertEquals(wrong.message(), unknown.message());
    }

    @Test
    void champsVidesSontRefuses() {
        assertEquals(LoginResult.Status.INVALID_INPUT, service.login("", PASSWORD).status());
        assertEquals(LoginResult.Status.INVALID_INPUT, service.login(EMAIL, " ").status());
        assertEquals(LoginResult.Status.INVALID_INPUT, service.login(null, null).status());
    }

    @Test
    void emailMalFormeEstRefuse() {
        LoginResult result = service.login("pas-un-email", PASSWORD);

        assertEquals(LoginResult.Status.INVALID_INPUT, result.status());
        assertEquals("Le format de l'e-mail est invalide.", result.message());
    }

    @Test
    void compteDesactiveNePeutPasSeConnecter() {
        repository.save(new User("ancien@smartcampus.ma", hasher.hash(PASSWORD), "STUDENT", false));

        LoginResult result = service.login("ancien@smartcampus.ma", PASSWORD);

        assertEquals(LoginResult.Status.ACCOUNT_DISABLED, result.status());
    }

    // Critere 3 : trois echecs consecutifs -> evenement enregistre (SC-13)

    @Test
    void troisEchecsVerrouillentLeCompteEtEnregistrentLEvenement() {
        service.login(EMAIL, "a");
        service.login(EMAIL, "b");
        assertTrue(service.getSecurityEvents().isEmpty());

        LoginResult third = service.login(EMAIL, "c");

        assertEquals(LoginResult.Status.ACCOUNT_LOCKED, third.status());
        assertEquals(1, service.getSecurityEvents().size());
        assertTrue(service.getSecurityEvents().get(0).contains(EMAIL));
    }

    @Test
    void compteVerrouilleRefuseMemeLeBonMotDePasse() {
        lockAccount();

        assertEquals(LoginResult.Status.ACCOUNT_LOCKED, service.login(EMAIL, PASSWORD).status());
    }

    @Test
    void leVerrouillageExpireApres15Minutes() {
        lockAccount();
        clock.advance(LoginService.LOCK_DURATION.plusSeconds(1));

        assertTrue(service.login(EMAIL, PASSWORD).isSuccess());
    }

    @Test
    void uneConnexionReussieRemetLeCompteurAZero() {
        service.login(EMAIL, "a");
        service.login(EMAIL, "b");
        assertTrue(service.login(EMAIL, PASSWORD).isSuccess());

        service.login(EMAIL, "c");
        service.login(EMAIL, "d");

        assertTrue(service.login(EMAIL, PASSWORD).isSuccess());
        assertTrue(service.getSecurityEvents().isEmpty());
    }

    // Deconnexion

    @Test
    void laDeconnexionInvalideLeJeton() {
        String token = service.login(EMAIL, PASSWORD).token();

        assertTrue(service.logout(token));
        assertEquals(Optional.empty(), service.currentUser(token));
        assertFalse(service.logout(token));
    }

    private void lockAccount() {
        for (int i = 0; i < LoginService.MAX_FAILURES; i++) {
            service.login(EMAIL, "mauvais");
        }
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
