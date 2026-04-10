package com.hartmann.todoapp.service;

import com.hartmann.todoapp.entity.TodoItem;
import com.hartmann.todoapp.entity.TodoList;
import com.hartmann.todoapp.entity.User;
import com.hartmann.todoapp.repository.TodoItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodoItemService {

    private final TodoItemRepository todoItemRepository;
    private final TodoListService todoListService;

    public TodoItemService(TodoItemRepository todoItemRepository, TodoListService todoListService) {
        this.todoItemRepository = todoItemRepository;
        this.todoListService = todoListService;
    }

    @Transactional
    public TodoItem addItem(Long listId, String content, User owner) {
        TodoList list = todoListService.findByIdAndUser(listId, owner);
        TodoItem item = new TodoItem();
        item.setContent(content.trim());
        item.setTodoList(list);
        return todoItemRepository.save(item);
    }

    @Transactional
    public void toggleItem(Long itemId, Long listId, User owner) {
        TodoList list = todoListService.findByIdAndUser(listId, owner);
        TodoItem item = todoItemRepository.findByIdAndTodoList(itemId, list)
            .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        item.setCompleted(!item.isCompleted());
        todoItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long itemId, Long listId, User owner) {
        TodoList list = todoListService.findByIdAndUser(listId, owner);
        TodoItem item = todoItemRepository.findByIdAndTodoList(itemId, list)
            .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        todoItemRepository.delete(item);
    }
}
