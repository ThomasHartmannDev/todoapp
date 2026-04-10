package com.hartmann.todoapp.service;

import com.hartmann.todoapp.entity.TodoList;
import com.hartmann.todoapp.entity.User;
import com.hartmann.todoapp.repository.TodoListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoListServiceTest {

    @Mock private TodoListRepository todoListRepository;
    private TodoListService todoListService;

    private User testUser;

    @BeforeEach
    void setUp() {
        todoListService = new TodoListService(todoListRepository);
        testUser = new User();
        testUser.setUsername("testuser");
    }

    @Test
    void shouldCreateTodoList() {
        TodoList saved = new TodoList();
        saved.setTitle("My List");
        when(todoListRepository.save(any(TodoList.class))).thenReturn(saved);

        TodoList result = todoListService.create("My List", "desc", testUser);

        assertThat(result.getTitle()).isEqualTo("My List");
        verify(todoListRepository).save(any(TodoList.class));
    }

    @Test
    void shouldThrowWhenListNotFound() {
        when(todoListRepository.findByIdAndOwner(99L, testUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoListService.findByIdAndUser(99L, testUser))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnListsForUser() {
        TodoList list = new TodoList();
        list.setTitle("Work");
        when(todoListRepository.findByOwnerOrderByCreatedAtDesc(testUser)).thenReturn(List.of(list));

        var result = todoListService.findAllByUser(testUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Work");
    }
}
