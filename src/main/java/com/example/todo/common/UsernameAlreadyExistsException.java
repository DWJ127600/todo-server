package com.example.todo.common;

/**
 * 注册时用户名已存在。唯一索引是最终保证，这个异常用于给出友好提示。
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("用户名已被占用: " + username);
    }
}
