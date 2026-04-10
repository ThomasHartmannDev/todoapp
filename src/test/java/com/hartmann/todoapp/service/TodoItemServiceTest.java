package com.hartmann.todoapp.service;

import com.hartmann.todoapp.entity.TodoItem;
import com.hartmann.todoapp.entity.TodoList;
import com.hartmann.todoapp.entity.User;
import com.hartmann.todoapp.repository.TodoItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoItemServiceTest {

    @Mock private TodoItemRepository todoItemRepository;
    @Mock private TodoListService todoListService;
    private TodoItemService todoItemService;

    private User testUser;
    private TodoList testList;

    @BeforeEach
    void setUp() {
        todoItemService = new TodoItemService(todoItemRepository, todoListService);
        testUser = new User();
        testUser.setUsername("testuser");
        testList = new TodoList();
        testList.setTitle("My List");
    }

    @Test
    void shouldAddItemToList() {
        when(todoListService.findByIdAndUser(1L, testUser)).thenReturn(testList);
        TodoItem saved = new TodoItem();
        saved.setContent("Buy milk");
        when(todoItemRepository.save(any(TodoItem.class))).thenReturn(saved);

        TodoItem result = todoItemService.addItem(1L, "Buy milk", testUser);

        assertThat(result.getContent()).isEqualTo("Buy milk");
        verify(todoItemRepository).save(any(TodoItem.class));
    }

    @Test
    void shouldToggleItemCompletion() {
        when(todoListService.findByIdAndUser(1L, testUser)).thenReturn(testList);
        TodoItem item = new TodoItem();
        item.setCompleted(false);
        when(todoItemRepository.findByIdAndTodoList(1L, testList)).thenReturn(Optional.of(item));
        when(todoItemRepository.save(any(TodoItem.class))).thenAnswer(inv -> inv.getArgument(0));

        todoItemService.toggleItem(1L, 1L, testUser);

        verify(todoItemRepository).save(argThat(i -> i.isCompleted()));
    }

    @Test
    void shouldThrowWhenItemNotFound() {
        when(todoListService.findByIdAndUser(1L, testUser)).thenReturn(testList);
        when(todoItemRepository.findByIdAndTodoList(99L, testList)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoItemService.toggleItem(99L, 1L, testUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Item not found");
    }
}
