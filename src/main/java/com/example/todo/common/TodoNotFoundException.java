package com.example.todo.common;

/**
 * 资源不存在或不属于当前用户时抛出。
 * 两种情况合并成同一个异常，避免通过 404/403 的差异探测他人数据是否存在。
 */
public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(Long id) {
        super("Todo 不存在: " + id);
    }
}
