package com.xtu.system.config.security;

public class AuthenticatedUser {

    private final Long userId;
    private final String username;

    public AuthenticatedUser(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
