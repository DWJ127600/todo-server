package com.example.todo.todo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 只暴露带 userId 的查询方法。
 * 刻意不提供 findById(id)：那会返回别人的数据，属于越权。
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {

    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    Page<Todo> findAllByUserId(Long userId, Pageable pageable);

    Page<Todo> findAllByUserIdAndCompleted(Long userId, boolean completed, Pageable pageable);

    Page<Todo> findAllByUserIdAndTitleContainingIgnoreCase(Long userId, String keyword, Pageable pageable);

    Page<Todo> findAllByUserIdAndCompletedAndTitleContainingIgnoreCase(
            Long userId, boolean completed, String keyword, Pageable pageable);
}
