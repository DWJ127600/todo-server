package com.example.todo.todo.dto;

import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * 局部更新请求（PATCH 语义）。
 * <p>
 * JSON 里"字段缺失"和"字段显式为 null"反序列化后都是 null，无法区分，
 * 因此统一约定：<b>null 表示本次不修改</b>。
 * 需要清空截止时间时，显式传 {@code "clearDueDate": true}。
 * 若同时传了 dueDate 和 clearDueDate=true，以 clearDueDate 为准。
 */
public record UpdateTodoRequest(

        @Size(min = 1, max = 100, message = "标题长度需在 1 到 100 个字符之间")
        String title,

        @Size(max = 500, message = "描述不能超过 500 个字符")
        String description,

        Boolean completed,

        Instant dueDate,

        Boolean clearDueDate
) {

    public boolean shouldClearDueDate() {
        return Boolean.TRUE.equals(clearDueDate);
    }
}
