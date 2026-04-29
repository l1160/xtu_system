package com.xtu.system.config.security;

import com.xtu.system.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String USER_ID_CLAIM = "uid";

    private final SecretKey secretKey;
    private final long expirationSeconds;

    public JwtTokenProvider(
        @Value("${security.jwt.secret}") String secret,
        @Value("${security.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
            .subject(username)
            .claim(USER_ID_CLAIM, userId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(secretKey)
            .compact();
    }

    public AuthenticatedUser getAuthenticatedUser(String token) {
        Claims claims = parseClaims(token).getPayload();
        Object userIdClaim = claims.get(USER_ID_CLAIM);
        if (userIdClaim == null) {
            throw new BusinessException(401, "登录令牌无效");
        }

        Long userId;
        if (userIdClaim instanceof Number number) {
            userId = number.longValue();
        } else {
            try {
                userId = Long.parseLong(String.valueOf(userIdClaim));
            } catch (NumberFormatException exception) {
                throw new BusinessException(401, "登录令牌无效");
            }
        }

        return new AuthenticatedUser(userId, claims.getSubject());
    }

    public String resolveToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new BusinessException(401, "未登录或登录状态已失效");
        }

        String bearerPrefix = "Bearer ";
        if (!authorizationHeader.regionMatches(true, 0, bearerPrefix, 0, bearerPrefix.length())) {
            throw new BusinessException(401, "登录令牌无效");
        }

        String token = authorizationHeader.substring(bearerPrefix.length()).trim();
        if (token.isEmpty()) {
            throw new BusinessException(401, "登录令牌无效");
        }
        return token;
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private Jws<Claims> parseClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(401, "登录令牌无效或已过期");
        }
    }
}
