package com.example.todo.todo;

import com.example.todo.common.CurrentUserProvider;
import com.example.todo.todo.dto.CreateTodoRequest;
import com.example.todo.todo.dto.PageResponse;
import com.example.todo.todo.dto.TodoResponse;
import com.example.todo.todo.dto.UpdateTodoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 只做参数接收与响应封装，业务逻辑全部在 Service。
 */
@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

    private final TodoService todoService;
    private final CurrentUserProvider currentUserProvider;

    public TodoController(TodoService todoService, CurrentUserProvider currentUserProvider) {
        this.todoService = todoService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public PageResponse<TodoResponse> list(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return todoService.list(currentUserProvider.userId(), completed, keyword, page, size);
    }

    @GetMapping("/{id}")
    public TodoResponse get(@PathVariable Long id) {
        return todoService.get(currentUserProvider.userId(), id);
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse created = todoService.create(currentUserProvider.userId(), request);
        return ResponseEntity
                .created(URI.create("/api/v1/todos/" + created.id()))
                .body(created);
    }

    @PatchMapping("/{id}")
    public TodoResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTodoRequest request) {
        return todoService.update(currentUserProvider.userId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        todoService.delete(currentUserProvider.userId(), id);
    }
}
