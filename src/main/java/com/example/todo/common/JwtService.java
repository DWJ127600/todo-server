package com.example.todo.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * JWT 签发与解析。
 * 只负责 token 本身，不参与认证流程的组装。
 */
@Service
public class JwtService {

    private static final String CLAIM_USERNAME = "username";

    private final SecretKey signingKey;
    private final Duration ttl;

    public JwtService(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.ttl = Duration.ofMinutes(properties.expireMinutes());
    }

    public String issue(Long userId, String username, Instant issuedAt) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_USERNAME, username)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(ttl)))
                .signWith(signingKey)
                .compact();
    }

    public Instant expiresAt(Instant issuedAt) {
        return issuedAt.plus(ttl);
    }

    /**
     * 解析并校验签名/有效期。任何异常都收敛为 Optional.empty()，
     * 由调用方决定"无身份"还是"直接拒绝"。
     */
    public Optional<JwtPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new JwtPrincipal(
                    Long.valueOf(claims.getSubject()),
                    claims.get(CLAIM_USERNAME, String.class)));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public record JwtPrincipal(Long userId, String username) {
    }
}
