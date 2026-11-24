package com.example.todo.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * 创建请求。标题必填，其余可选。
 * dueDate 使用 ISO-8601 字符串，例如 "2026-11-10T09:00:00Z"。
 */
public record CreateTodoRequest(

        @NotBlank(message = "标题不能为空")
        @Size(max = 100, message = "标题不能超过 100 个字符")
        String title,

        @Size(max = 500, message = "描述不能超过 500 个字符")
        String description,

        Instant dueDate
) {
}
