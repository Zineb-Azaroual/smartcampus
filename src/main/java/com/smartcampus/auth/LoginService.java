package com.smartcampus.auth;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * SC-11 : se connecter avec son e-mail institutionnel et son mot de passe.
 * SC-13 : apres trois echecs consecutifs, le compte est verrouille et l'evenement est enregistre.
 */
public class LoginService {

    public static final int MAX_FAILURES = 3;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    static final String MSG_MISSING_FIELDS = "L'e-mail et le mot de passe sont obligatoires.";
    static final String MSG_BAD_EMAIL = "Le format de l'e-mail est invalide.";
    static final String MSG_BAD_CREDENTIALS = "E-mail ou mot de passe incorrect.";
    static final String MSG_DISABLED = "Ce compte est désactivé. Contactez l'administration.";
    static final String MSG_LOCKED = "Trop de tentatives échouées. Compte verrouillé pendant 15 minutes.";

    private static final Logger LOG = Logger.getLogger(LoginService.class.getName());
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final Clock clock;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final Map<String, Integer> failures = new ConcurrentHashMap<>();
    private final Map<String, Instant> lockedUntil = new ConcurrentHashMap<>();
    private final List<String> securityEvents = new CopyOnWriteArrayList<>();
    private final SecureRandom random = new SecureRandom();

    public LoginService(UserRepository users, PasswordHasher hasher) {
        this(users, hasher, Clock.systemUTC());
    }

    public LoginService(UserRepository users, PasswordHasher hasher, Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.clock = clock;
    }

    public void register(String email, String password, String role) {
        users.save(new User(normalize(email), hasher.hash(password), role, true));
    }

    public LoginResult login(String email, String password) {
        if (isBlank(email) || isBlank(password)) {
            return LoginResult.failure(LoginResult.Status.INVALID_INPUT, MSG_MISSING_FIELDS);
        }
        if (!EMAIL.matcher(email.trim()).matches()) {
            return LoginResult.failure(LoginResult.Status.INVALID_INPUT, MSG_BAD_EMAIL);
        }

        String key = normalize(email);
        if (isLocked(key)) {
            return LoginResult.failure(LoginResult.Status.ACCOUNT_LOCKED, MSG_LOCKED);
        }

        Optional<User> user = users.findByEmail(key);
        // Meme message pour un e-mail inconnu ou un mauvais mot de passe :
        // on ne revele pas quels comptes existent.
        if (user.isEmpty() || !hasher.matches(password, user.get().passwordHash())) {
            return registerFailure(key);
        }
        if (!user.get().active()) {
            return LoginResult.failure(LoginResult.Status.ACCOUNT_DISABLED, MSG_DISABLED);
        }

        failures.remove(key);
        String token = newToken();
        sessions.put(token, key);
        return LoginResult.success(token);
    }

    public boolean logout(String token) {
        return token != null && sessions.remove(token) != null;
    }

    public Optional<String> currentUser(String token) {
        return token == null ? Optional.empty() : Optional.ofNullable(sessions.get(token));
    }

    public List<String> getSecurityEvents() {
        return List.copyOf(securityEvents);
    }

    private boolean isLocked(String key) {
        Instant until = lockedUntil.get(key);
        if (until == null) {
            return false;
        }
        if (clock.instant().isBefore(until)) {
            return true;
        }
        lockedUntil.remove(key);
        return false;
    }

    private LoginResult registerFailure(String key) {
        int count = failures.merge(key, 1, Integer::sum);
        if (count < MAX_FAILURES) {
            return LoginResult.failure(LoginResult.Status.INVALID_CREDENTIALS, MSG_BAD_CREDENTIALS);
        }
        Instant now = clock.instant();
        failures.remove(key);
        lockedUntil.put(key, now.plus(LOCK_DURATION));
        String event = now + " " + key + " : " + MAX_FAILURES + " échecs consécutifs, compte verrouillé";
        securityEvents.add(event);
        LOG.warning(event);
        return LoginResult.failure(LoginResult.Status.ACCOUNT_LOCKED, MSG_LOCKED);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
