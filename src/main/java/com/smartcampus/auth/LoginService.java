package com.smartcampus.auth;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * SC-11 : se connecter avec son e-mail institutionnel et son mot de passe.
 */
public class LoginService {

    static final String MSG_MISSING_FIELDS = "L'e-mail et le mot de passe sont obligatoires.";
    static final String MSG_BAD_EMAIL = "Le format de l'e-mail est invalide.";
    static final String MSG_BAD_CREDENTIALS = "E-mail ou mot de passe incorrect.";
    static final String MSG_DISABLED = "Ce compte est désactivé. Contactez l'administration.";

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public LoginService(UserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
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
        Optional<User> user = users.findByEmail(key);
        // Meme message pour un e-mail inconnu ou un mauvais mot de passe :
        // on ne revele pas quels comptes existent.
        if (user.isEmpty() || !hasher.matches(password, user.get().passwordHash())) {
            return LoginResult.failure(LoginResult.Status.INVALID_CREDENTIALS, MSG_BAD_CREDENTIALS);
        }
        if (!user.get().active()) {
            return LoginResult.failure(LoginResult.Status.ACCOUNT_DISABLED, MSG_DISABLED);
        }

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
