package com.hartmann.todoapp.integration;

import com.hartmann.todoapp.entity.User;
import com.hartmann.todoapp.repository.TodoListRepository;
import com.hartmann.todoapp.repository.UserRepository;
import com.hartmann.todoapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class TodoAppIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private TodoListRepository todoListRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        userService.register("integrationuser", "int@test.com", "password123");
    }

    @Test
    void loginPageShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/login"))
               .andExpect(status().isOk());
    }

    @Test
    void dashboardShouldRedirectUnauthenticated() throws Exception {
        mockMvc.perform(get("/dashboard"))
               .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "integrationuser", roles = "USER")
    void dashboardShouldBeAccessibleWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/dashboard"))
               .andExpect(status().isOk())
               .andExpect(view().name("dashboard"));
    }

    @Test
    @WithMockUser(username = "integrationuser", roles = "USER")
    void shouldCreateAndViewTodoList() throws Exception {
        mockMvc.perform(post("/dashboard/new")
               .param("title", "Integration Test List")
               .param("description", "A test list")
               .with(csrf()))
               .andExpect(status().is3xxRedirection());

        User user = userRepository.findByUsername("integrationuser").orElseThrow();
        var lists = todoListRepository.findByOwnerOrderByCreatedAtDesc(user);
        assertThat(lists).hasSize(1);
        assertThat(lists.get(0).getTitle()).isEqualTo("Integration Test List");
    }

    @Test
    void registerPageShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/register"))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/register"));
    }
}
