package com.hartmann.todoapp.service;

import com.hartmann.todoapp.entity.TodoList;
import com.hartmann.todoapp.entity.User;
import com.hartmann.todoapp.repository.TodoListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TodoListService {

    private final TodoListRepository todoListRepository;

    public TodoListService(TodoListRepository todoListRepository) {
        this.todoListRepository = todoListRepository;
    }

    public List<TodoList> findAllByUser(User user) {
        return todoListRepository.findByOwnerOrderByCreatedAtDesc(user);
    }

    public TodoList findByIdAndUser(Long id, User user) {
        return todoListRepository.findByIdAndOwner(id, user)
            .orElseThrow(() -> new IllegalArgumentException("Todo list not found or access denied"));
    }

    @Transactional
    public TodoList create(String title, String description, User owner) {
        TodoList list = new TodoList();
        list.setTitle(title);
        list.setDescription(description);
        list.setOwner(owner);
        return todoListRepository.save(list);
    }

    @Transactional
    public TodoList update(Long id, String title, String description, User owner) {
        TodoList list = findByIdAndUser(id, owner);
        list.setTitle(title);
        list.setDescription(description);
        return todoListRepository.save(list);
    }

    @Transactional
    public void delete(Long id, User owner) {
        TodoList list = findByIdAndUser(id, owner);
        todoListRepository.delete(list);
    }
}
