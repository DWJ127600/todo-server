package com.example.todo.todo;

import com.example.todo.common.TodoNotFoundException;
import com.example.todo.todo.dto.CreateTodoRequest;
import com.example.todo.todo.dto.PageResponse;
import com.example.todo.todo.dto.TodoResponse;
import com.example.todo.todo.dto.UpdateTodoRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务层。所有方法都必须显式接收 userId，Service 内部不做"当前用户"的推断，
 * 这样单测可以直接传入任意用户来验证越权隔离。
 */
@Service
@Transactional(readOnly = true)
public class TodoService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public PageResponse<TodoResponse> list(Long userId, Boolean completed, String keyword, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, size));
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<Todo> result;
        if (completed == null && normalizedKeyword == null) {
            result = todoRepository.findAllByUserId(userId, pageable);
        } else if (completed == null) {
            result = todoRepository.findAllByUserIdAndTitleContainingIgnoreCase(userId, normalizedKeyword, pageable);
        } else if (normalizedKeyword == null) {
            result = todoRepository.findAllByUserIdAndCompleted(userId, completed, pageable);
        } else {
            result = todoRepository.findAllByUserIdAndCompletedAndTitleContainingIgnoreCase(
                    userId, completed, normalizedKeyword, pageable);
        }

        return PageResponse.of(result.map(TodoResponse::from));
    }

    public TodoResponse get(Long userId, Long id) {
        return TodoResponse.from(findOwned(userId, id));
    }

    @Transactional
    public TodoResponse create(Long userId, CreateTodoRequest request) {
        Todo todo = new Todo(userId, request.title().trim());
        todo.setDescription(request.description());
        todo.setDueDate(request.dueDate());
        return TodoResponse.from(todoRepository.save(todo));
    }

    @Transactional
    public TodoResponse update(Long userId, Long id, UpdateTodoRequest request) {
        Todo todo = findOwned(userId, id);

        if (request.title() != null) {
            todo.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            todo.setDescription(request.description());
        }
        if (request.completed() != null) {
            todo.setCompleted(request.completed());
        }
        if (request.shouldClearDueDate()) {
            todo.setDueDate(null);
        } else if (request.dueDate() != null) {
            todo.setDueDate(request.dueDate());
        }

        // saveAndFlush 让 @PreUpdate 立刻执行，返回体里的 updatedAt 才是新值
        return TodoResponse.from(todoRepository.saveAndFlush(todo));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        todoRepository.delete(findOwned(userId, id));
    }

    private Todo findOwned(Long userId, Long id) {
        return todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }
}
