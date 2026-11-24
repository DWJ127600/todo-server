package com.example.todo.todo.dto;

import com.example.todo.todo.Todo;

import java.time.Instant;

/**
 * 出参 DTO。刻意不直接返回 JPA 实体：避免懒加载序列化、字段泄露和实体变更影响 API 契约。
 */
public record TodoResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        Instant dueDate,
        Instant createdAt,
        Instant updatedAt
) {

    public static TodoResponse from(Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getDueDate(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }
}
