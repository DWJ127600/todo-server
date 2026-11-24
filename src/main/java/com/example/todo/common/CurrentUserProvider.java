package com.example.todo.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户。身份由 JwtAuthenticationFilter 写入 SecurityContext，
 * principal 是 userId（Long）。
 * <p>
 * 阶段 2 这里是固定返回值；现在换成从上下文读取，
 * Controller / Service 的调用方式完全没有变化 —— 这就是当初留这层的原因。
 */
@Component
public class CurrentUserProvider {

    public Long userId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            // 走到这里说明某个接口在没有认证的情况下进入了业务层，属于配置错误而非用户输入错误
            throw new IllegalStateException("当前请求没有认证信息，请检查 SecurityFilterChain 配置");
        }

        return userId;
    }
}
