package com.hartmann.todoapp.repository;

import com.hartmann.todoapp.entity.TodoList;
import com.hartmann.todoapp.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoListRepository extends JpaRepository<TodoList, Long> {
    List<TodoList> findByOwnerOrderByCreatedAtDesc(User owner);

    Optional<TodoList> findByIdAndOwner(Long id, User owner);
}
