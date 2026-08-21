package com.settle.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.settle.common.ApiException;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${settle.jwt-secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            bytes = (secret + "0123456789abcdef0123456789abcdef").getBytes(StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(java.util.Arrays.copyOf(bytes, 32));
    }

    public String accessToken(UUID userId) {
        return token(userId, "access", 12);
    }

    public String refreshToken(UUID userId) {
        return token(userId, "refresh", 24 * 30);
    }

    public UUID parseAccess(String token) {
        Claims claims = parse(token);
        if (!"access".equals(claims.get("typ"))) {
            throw ApiException.unauthorized();
        }
        return UUID.fromString(claims.getSubject());
    }

    public UUID parseRefresh(String token) {
        Claims claims = parse(token);
        if (!"refresh".equals(claims.get("typ"))) {
            throw ApiException.unauthorized();
        }
        return UUID.fromString(claims.getSubject());
    }

    private String token(UUID userId, String typ, int hours) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("typ", typ)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(hours, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            throw ApiException.unauthorized();
        }
    }
}
