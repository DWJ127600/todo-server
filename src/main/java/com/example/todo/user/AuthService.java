package com.example.todo.user;

import com.example.todo.common.InvalidCredentialsException;
import com.example.todo.common.JwtService;
import com.example.todo.common.UsernameAlreadyExistsException;
import com.example.todo.user.dto.AuthResponse;
import com.example.todo.user.dto.LoginRequest;
import com.example.todo.user.dto.RegisterRequest;
import com.example.todo.user.dto.UserResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = request.username().trim();

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        User user = new User(username, passwordEncoder.encode(request.password()));
        try {
            // saveAndFlush 让唯一索引冲突在事务内立刻暴露，而不是拖到提交时
            return UserResponse.from(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            // 并发注册同名用户时的兜底：existsByUsername 与 insert 之间存在竞态
            throw new UsernameAlreadyExistsException(username);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String username = request.username().trim();

        // 注意：用户不存在时直接返回，未做等耗时处理，存在时序侧信道。
        // 对本地 Todo 项目可接受；真实产品应统一执行一次哈希比较。
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        Instant now = Instant.now();
        String token = jwtService.issue(user.getId(), user.getUsername(), now);
        return new AuthResponse(token, "Bearer", jwtService.expiresAt(now), user.getUsername());
    }
}
