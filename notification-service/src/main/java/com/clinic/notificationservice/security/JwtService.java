package com.clinic.notificationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Parses JWT tokens issued by the Clinic Portal service.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public UserDetailsImpl parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Number profileIdRaw = claims.get("profileId", Number.class);
        Boolean active = claims.get("active", Boolean.class);

        return new UserDetailsImpl(
                claims.get("userId", Long.class),
                claims.getSubject(),
                Role.valueOf(claims.get("role", String.class)),
                profileIdRaw != null ? profileIdRaw.longValue() : null,
                active != null ? active : true
        );
    }
}
