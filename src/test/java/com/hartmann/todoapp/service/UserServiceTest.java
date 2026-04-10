package com.hartmann.todoapp.service;

import com.hartmann.todoapp.entity.User;
import com.hartmann.todoapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        User savedUser = new User();
        savedUser.setUsername("newuser");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.register("newuser", "new@example.com", "password123");

        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenUsernameAlreadyTaken() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("existing", "e@e.com", "pass"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username already taken");
    }

    @Test
    void shouldThrowWhenEmailAlreadyInUse() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("newuser", "existing@example.com", "pass"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already in use");
    }

    @Test
    void shouldThrowWhenUserNotFoundByUsername() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("ghost"))
            .isInstanceOf(UsernameNotFoundException.class);
    }
}
