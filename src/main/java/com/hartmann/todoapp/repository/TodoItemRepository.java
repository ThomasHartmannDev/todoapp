package com.hartmann.todoapp.repository;

import com.hartmann.todoapp.entity.TodoItem;
import com.hartmann.todoapp.entity.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
    List<TodoItem> findByTodoListOrderByCreatedAtAsc(TodoList todoList);
    Optional<TodoItem> findByIdAndTodoList(Long id, TodoList todoList);
}
