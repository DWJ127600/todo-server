package com.example.todo.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求只校验非空。刻意不校验长度和格式：
 * 登录阶段做格式校验只会泄露"这个用户名/密码规则"，且会把错误类型暴露给攻击者。
 */
public record LoginRequest(

        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "密码不能为空")
        String password
) {
}
