package com.example.todo.todo.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页出参。不直接返回 Spring 的 PageImpl：
 * 它的 JSON 结构随版本变动，且字段冗余，前端不好消费。
 */
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
