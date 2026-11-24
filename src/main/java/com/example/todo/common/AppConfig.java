package com.example.todo.common;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AppConfig {

    /**
     * 使用 DelegatingPasswordEncoder：新密码默认用 bcrypt 并带上 {bcrypt} 前缀，
     * 同时仍能校验历史数据中的 {noop} 前缀（仅本地种子用户）。
     * 生产环境应改为 new BCryptPasswordEncoder()，彻底禁止 noop。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
