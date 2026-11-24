package com.example.todo.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

/**
 * JWT 配置。secret 走环境变量 JWT_SECRET，未配置时用 application.yml 里的开发默认值。
 * HS256 要求密钥至少 32 字节，这里在绑定阶段就校验，避免运行到签发时才报 WeakKeyException。
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expireMinutes) {

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.jwt.secret 至少需要 32 字节（256 bit）");
        }
        if (expireMinutes <= 0) {
            throw new IllegalArgumentException("app.jwt.expire-minutes 必须大于 0");
        }
    }
}
