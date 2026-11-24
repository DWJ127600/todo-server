package com.example.todo.common;

/**
 * 用户名或密码错误。不区分"用户不存在"和"密码错误"，
 * 避免被用来枚举已注册用户名。
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("用户名或密码错误");
    }
}
