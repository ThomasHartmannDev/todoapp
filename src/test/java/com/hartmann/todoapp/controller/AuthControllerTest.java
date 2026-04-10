package com.hartmann.todoapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPageShouldReturn200() throws Exception {
        mockMvc.perform(get("/login"))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/login"));
    }

    @Test
    void registerPageShouldReturn200() throws Exception {
        mockMvc.perform(get("/register"))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/register"));
    }
}
