package com.smartcampus.auth;

public record LoginResult(Status status, String token, String message) {

    public enum Status {
        SUCCESS,
        INVALID_INPUT,
        INVALID_CREDENTIALS,
        ACCOUNT_DISABLED,
        ACCOUNT_LOCKED
    }

    public static LoginResult success(String token) {
        return new LoginResult(Status.SUCCESS, token, "Connexion réussie.");
    }

    public static LoginResult failure(Status status, String message) {
        return new LoginResult(status, null, message);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
