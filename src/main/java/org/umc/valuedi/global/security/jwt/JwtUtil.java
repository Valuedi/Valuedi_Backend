package org.umc.valuedi.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.umc.valuedi.global.security.principal.CustomUserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Duration accessExpiration;
    private final Duration refreshExpiration;

    public JwtUtil(
            @Value("${jwt.token.secretKey}") String secret,
            @Value("${jwt.token.expiration.access}") Long accessExpiration,
            @Value("${jwt.token.expiration.refresh}") Long refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = Duration.ofMillis(accessExpiration);
        this.refreshExpiration = Duration.ofMillis(refreshExpiration);
    }

    public String createAccessToken(CustomUserDetails user) {
        return createToken(user, accessExpiration, true, "access");
    }

    public String createRefreshToken(CustomUserDetails user) {
        return createToken(user, refreshExpiration, false, "refresh");
    }

    public String getMemberId(String token) {
        return getClaims(token).getPayload().getSubject();
    }
    public String getCategory(String token) {
        return getClaims(token).getPayload().get("category", String.class);
    }

    public long getExpiration(String token) {
        try {
            return getClaims(token).getPayload().getExpiration().getTime();
        } catch(ExpiredJwtException e) {
            return 0;
        }
    }

    public long getRefreshTokenExpiration() {
        return refreshExpiration.toMillis();
    }

    private String createToken(CustomUserDetails user, Duration expiration, boolean includeAuthorities, String category) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(user.getUsername())
                .claim("category", category)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(secretKey);

        // AccessToken일 때만 권한 정보를 넣음
        if (includeAuthorities) {
            String authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            builder.claim("role", authorities);
        }

        return builder.compact();
    }

    private Jws<Claims> getClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clockSkewSeconds(60)
                .build()
                .parseSignedClaims(token);
    }
}
