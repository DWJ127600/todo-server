package com.example.todo.user.dto;

import java.time.Instant;

/**
 * 登录成功响应。刻意不返回 userId：客户端拿 token 就够，少暴露一个内部标识。
 * expiresAt 是绝对时间，客户端可据此决定何时主动跳登录页，不必解析 JWT。
 */
public record AuthResponse(
        String token,
        String tokenType,
        Instant expiresAt,
        String username
) {
}
