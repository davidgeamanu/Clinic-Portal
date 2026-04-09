package com.clinic.portal.security;

import com.clinic.portal.model.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final Duration tokenExpiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenExpiration = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(UserDetailsImpl userDetails) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .claim("role", userDetails.getRole().name())
                .claim("profileId", userDetails.getProfileId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenExpiration)))
                .signWith(signingKey)
                .compact();
    }

    public UserDetailsImpl parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Number profileIdRaw = claims.get("profileId", Number.class);

        return new UserDetailsImpl(
                claims.get("userId", Long.class),
                claims.getSubject(),
                "",
                Role.valueOf(claims.get("role", String.class)),
                profileIdRaw != null ? profileIdRaw.longValue() : null
        );
    }

    public long getExpirationSeconds() {
        return tokenExpiration.toSeconds();
    }
}