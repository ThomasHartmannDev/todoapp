# Agent Prompt — TodoApp (Java + Spring Boot + Thymeleaf)

## Contexto e Objetivo

Você é um agente de desenvolvimento de software autônomo. Sua tarefa é implementar **completamente** uma aplicação web de gerenciamento de Todo Lists usando Java, Spring Boot e Thymeleaf, com pipeline CI/CD no GitHub.

O Projeto eo codigo deve ser todo em inglês.

O desenvolvedor já:
1. Criou a pasta do projeto
2. Executou o Spring Initializr e colocou o projeto base na pasta
3. a raiz do projeto é com.hartmann 

**Você deve fazer todo o resto sozinho, sem intervenção humana.**

---

## Stack e Requisitos

- **Backend:** Java 21, Spring Boot 3.x, Spring Security, Spring Data JPA
- **Frontend:** Thymeleaf, Bootstrap 5 (via CDN)
- **Banco de Dados:** H2 (desenvolvimento), PostgreSQL (produção)
- **Build:** Maven
- **VCS:** Git + GitHub
- **CI/CD:** GitHub Actions

### Funcionalidades obrigatórias:
1. Registro e Login de usuário (com Spring Security)
2. CRUD completo de Todo Lists (criar, listar, editar, deletar)
3. CRUD de itens dentro de cada Todo List
4. Isolamento por usuário (cada usuário vê apenas suas listas)
5. Flow: Login → Dashboard com listas → Entrar em lista existente OU criar nova

---

## Configuração Inicial do Repositório

Antes de qualquer commit de código, execute os seguintes passos:

```bash
# 1. Inicializar git (caso não esteja inicializado)
git init

# 2. Criar .gitignore adequado para Java/Maven/Spring Boot
cat > .gitignore << 'EOF'
HELP.md
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/
build/
!**/src/main/**/build/
!**/src/test/**/build/

### VS Code ###
.vscode/

### Mac ###
.DS_Store

### Application Properties (sensitive) ###
src/main/resources/application-prod.properties
EOF

# 3. Fazer primeiro commit (projeto base do Spring Initializr)
git add .
git commit -m "chore: initial project setup from Spring Initializr

- Java 21
- Spring Boot 3.x
- Maven build system
- Dependencies: Spring Web, Spring Security, Spring Data JPA,
  Thymeleaf, H2 Database, PostgreSQL Driver, Validation

Author: Thomas Hartmann"

# 4. Criar repositório no GitHub via CLI (gh) e fazer push
gh repo create todoapp --public --source=. --remote=origin --push

# 5. Definir branch principal como 'main'
git branch -M main
git push -u origin main
```

---

## Estratégia de Branches e Pull Requests

Cada feature deve ser desenvolvida em uma branch separada e integrada via Pull Request. O merge só deve ocorrer se os testes no GitHub Actions passarem.

**Nomenclatura de branches:**
- `feature/database-config` — configuração do banco
- `feature/user-auth` — autenticação e registro
- `feature/todo-list-crud` — CRUD de listas
- `feature/todo-item-crud` — CRUD de itens
- `feature/ui-polish` — melhorias de UI e UX

**Regra de commit:** Todo commit deve terminar com:
```
Author: Thomas Hartmann
```

---

## Pipeline CI/CD (GitHub Actions)

Crie o arquivo `.github/workflows/ci.yml` **no primeiro commit após o setup**, antes de qualquer feature. Este arquivo deve:

1. Rodar em todo Pull Request para `main`
2. Compilar o projeto com Maven
3. Executar todos os testes
4. Bloquear merge se qualquer teste falhar

```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build-and-test:
    name: Build & Test
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Build with Maven
        run: mvn clean compile --no-transfer-progress

      - name: Run Tests
        run: mvn test --no-transfer-progress

      - name: Package application
        run: mvn package -DskipTests --no-transfer-progress

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports/

      - name: Upload build artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-jar
          path: target/*.jar
```

**Como configurar Branch Protection no GitHub (executar via gh CLI):**
```bash
gh api repos/{owner}/{repo}/branches/main/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"contexts":["Build & Test"]}' \
  --field enforce_admins=false \
  --field required_pull_request_reviews=null \
  --field restrictions=null
```

---

## Sequência de Implementação — Commit por Commit

---

### COMMIT 1 — CI/CD Pipeline Setup

**Branch:** `main` (direto, pois é infraestrutura antes da branch protection)

```bash
mkdir -p .github/workflows
```

Crie `.github/workflows/ci.yml` com o conteúdo acima.

```bash
git add .github/
git commit -m "ci: add GitHub Actions CI/CD pipeline

- Build and test on every push and PR to main
- Uses JDK 21 (Temurin)
- Maven with dependency caching
- Uploads test results and JAR as artifacts

Author: Thomas Hartmann"

git push origin main
```

Depois configure a branch protection via `gh api` conforme mostrado acima.

---

### COMMIT 2 — Database & JPA Configuration

**Branch:** `feature/database-config`

```bash
git checkout -b feature/database-config
```

**Arquivos a criar/modificar:**

**`src/main/resources/application.properties`**
```properties
# Application
spring.application.name=todoapp
server.port=8080

# H2 Database (development)
spring.datasource.url=jdbc:h2:mem:tododb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# Thymeleaf
spring.thymeleaf.cache=false
```

**`src/main/java/com/example/todoapp/entity/User.java`**
```java
package com.example.todoapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoList> todoLists = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<TodoList> getTodoLists() { return todoLists; }
    public void setTodoLists(List<TodoList> todoLists) { this.todoLists = todoLists; }
}
```

**`src/main/java/com/example/todoapp/entity/TodoList.java`**
```java
package com.example.todoapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "todo_lists")
public class TodoList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 1, max = 100)
    @Column(nullable = false)
    private String title;

    @Size(max = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "todoList", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TodoItem> items = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public List<TodoItem> getItems() { return items; }
    public void setItems(List<TodoItem> items) { this.items = items; }
}
```

**`src/main/java/com/example/todoapp/entity/TodoItem.java`**
```java
package com.example.todoapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_items")
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 1, max = 500)
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    private TodoList todoList;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public TodoList getTodoList() { return todoList; }
    public void setTodoList(TodoList todoList) { this.todoList = todoList; }
}
```

**`src/main/java/com/example/todoapp/repository/UserRepository.java`**
```java
package com.example.todoapp.repository;

import com.example.todoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

**`src/main/java/com/example/todoapp/repository/TodoListRepository.java`**
```java
package com.example.todoapp.repository;

import com.example.todoapp.entity.TodoList;
import com.example.todoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TodoListRepository extends JpaRepository<TodoList, Long> {
    List<TodoList> findByOwnerOrderByCreatedAtDesc(User owner);
    Optional<TodoList> findByIdAndOwner(Long id, User owner);
}
```

**`src/main/java/com/example/todoapp/repository/TodoItemRepository.java`**
```java
package com.example.todoapp.repository;

import com.example.todoapp.entity.TodoItem;
import com.example.todoapp.entity.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
    List<TodoItem> findByTodoListOrderByCreatedAtAsc(TodoList todoList);
    Optional<TodoItem> findByIdAndTodoList(Long id, TodoList todoList);
}
```

**Teste: `src/test/java/com/example/todoapp/repository/UserRepositoryTest.java`**
```java
package com.example.todoapp.repository;

import com.example.todoapp.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByUsername() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashedpassword");
        userRepository.save(user);

        var found = userRepository.findByUsername("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnTrueWhenUsernameExists() {
        User user = new User();
        user.setUsername("existinguser");
        user.setEmail("existing@example.com");
        user.setPassword("hashedpassword");
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("existinguser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }
}
```

```bash
git add .
git commit -m "feat: add JPA entities and repositories

- User, TodoList, TodoItem entities with proper relationships
- UserRepository, TodoListRepository, TodoItemRepository
- H2 in-memory database for development
- Repository unit tests with @DataJpaTest

Author: Thomas Hartmann"

git push origin feature/database-config
```

**Abrir Pull Request:**
```bash
gh pr create \
  --title "feat: database configuration and JPA entities" \
  --body "## Changes
- Add User, TodoList, TodoItem JPA entities
- Add Spring Data repositories
- Configure H2 in-memory database
- Add repository unit tests

## Test Coverage
- UserRepository: save, findByUsername, existsByUsername" \
  --base main \
  --head feature/database-config
```

**Aguardar CI passar, depois fazer merge:**
```bash
gh pr merge --squash --delete-branch
git checkout main
git pull origin main
```

---

### COMMIT 3 — Spring Security & Authentication

**Branch:** `feature/user-auth`

```bash
git checkout -b feature/user-auth
```

**`src/main/java/com/example/todoapp/service/UserService.java`**
```java
package com.example.todoapp.service;

import com.example.todoapp.entity.User;
import com.example.todoapp.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .roles("USER")
            .build();
    }

    @Transactional
    public User register(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
```

**`src/main/java/com/example/todoapp/config/SecurityConfig.java`**
```java
package com.example.todoapp.config;

import com.example.todoapp.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserService userService) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .headers(headers -> headers.frameOptions(f -> f.sameOrigin())) // H2 console
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

**`src/main/java/com/example/todoapp/dto/RegistrationForm.java`**
```java
package com.example.todoapp.dto;

import jakarta.validation.constraints.*;

public class RegistrationForm {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
```

**`src/main/java/com/example/todoapp/controller/AuthController.java`**
```java
package com.example.todoapp.controller;

import com.example.todoapp.dto.RegistrationForm;
import com.example.todoapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid username or password.");
        if (logout != null) model.addAttribute("message", "You have been logged out.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("form") RegistrationForm form,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (!form.passwordsMatch()) {
            result.rejectValue("confirmPassword", "error.form", "Passwords do not match.");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(form.getUsername(), form.getEmail(), form.getPassword());
            redirectAttributes.addFlashAttribute("success", "Account created! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            result.reject("error.form", e.getMessage());
            return "auth/register";
        }
    }
}
```

**Templates Thymeleaf:**

**`src/main/resources/templates/auth/login.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — TodoApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container d-flex justify-content-center align-items-center min-vh-100">
    <div class="card shadow-sm" style="width: 100%; max-width: 400px;">
        <div class="card-body p-4">
            <h1 class="h4 text-center mb-4"><i class="bi bi-check2-square me-2 text-primary"></i>TodoApp</h1>

            <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
            <div th:if="${message}" class="alert alert-success" th:text="${message}"></div>
            <div th:if="${success}" class="alert alert-success" th:text="${success}"></div>

            <form th:action="@{/login}" method="post">
                <div class="mb-3">
                    <label for="username" class="form-label">Username</label>
                    <input type="text" class="form-control" id="username" name="username" required autofocus>
                </div>
                <div class="mb-3">
                    <label for="password" class="form-label">Password</label>
                    <input type="password" class="form-control" id="password" name="password" required>
                </div>
                <button type="submit" class="btn btn-primary w-100">Sign In</button>
            </form>

            <hr>
            <p class="text-center mb-0 text-muted small">
                Don't have an account? <a th:href="@{/register}">Register here</a>
            </p>
        </div>
    </div>
</div>
</body>
</html>
```

**`src/main/resources/templates/auth/register.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register — TodoApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container d-flex justify-content-center align-items-center min-vh-100">
    <div class="card shadow-sm" style="width: 100%; max-width: 420px;">
        <div class="card-body p-4">
            <h1 class="h4 text-center mb-4"><i class="bi bi-check2-square me-2 text-primary"></i>Create Account</h1>

            <div th:if="${#fields.hasGlobalErrors()}" class="alert alert-danger">
                <span th:each="err : ${#fields.globalErrors()}" th:text="${err}"></span>
            </div>

            <form th:action="@{/register}" th:object="${form}" method="post" novalidate>
                <div class="mb-3">
                    <label for="username" class="form-label">Username</label>
                    <input type="text" class="form-control" th:class="${#fields.hasErrors('username')} ? 'form-control is-invalid' : 'form-control'"
                           id="username" th:field="*{username}" required>
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('username')}" th:errors="*{username}"></div>
                </div>
                <div class="mb-3">
                    <label for="email" class="form-label">Email</label>
                    <input type="email" class="form-control" th:class="${#fields.hasErrors('email')} ? 'form-control is-invalid' : 'form-control'"
                           id="email" th:field="*{email}" required>
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('email')}" th:errors="*{email}"></div>
                </div>
                <div class="mb-3">
                    <label for="password" class="form-label">Password</label>
                    <input type="password" class="form-control" th:class="${#fields.hasErrors('password')} ? 'form-control is-invalid' : 'form-control'"
                           id="password" th:field="*{password}" required>
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('password')}" th:errors="*{password}"></div>
                </div>
                <div class="mb-3">
                    <label for="confirmPassword" class="form-label">Confirm Password</label>
                    <input type="password" class="form-control" th:class="${#fields.hasErrors('confirmPassword')} ? 'form-control is-invalid' : 'form-control'"
                           id="confirmPassword" th:field="*{confirmPassword}" required>
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('confirmPassword')}" th:errors="*{confirmPassword}"></div>
                </div>
                <button type="submit" class="btn btn-primary w-100">Create Account</button>
            </form>

            <hr>
            <p class="text-center mb-0 text-muted small">
                Already have an account? <a th:href="@{/login}">Sign in</a>
            </p>
        </div>
    </div>
</div>
</body>
</html>
```

**Teste: `src/test/java/com/example/todoapp/service/UserServiceTest.java`**
```java
package com.example.todoapp.service;

import com.example.todoapp.entity.User;
import com.example.todoapp.repository.UserRepository;
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
```

**Teste de integração: `src/test/java/com/example/todoapp/controller/AuthControllerTest.java`**
```java
package com.example.todoapp.controller;

import com.example.todoapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;

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
```

```bash
git add .
git commit -m "feat: implement user registration and authentication

- Spring Security configuration with BCrypt password encoding
- UserService implementing UserDetailsService
- AuthController for login and registration
- RegistrationForm DTO with Bean Validation
- Login and Register Thymeleaf templates with Bootstrap 5
- Unit tests for UserService
- WebMvc integration tests for AuthController

Author: Thomas Hartmann"

git push origin feature/user-auth

gh pr create \
  --title "feat: user authentication and registration" \
  --body "## Changes
- Spring Security configuration
- BCrypt password encoding
- Registration and login flow
- Thymeleaf templates (Bootstrap 5)

## Test Coverage
- UserService: registration, duplicate validation, error handling
- AuthController: GET endpoints return correct views" \
  --base main \
  --head feature/user-auth
```

**Aguardar CI, fazer merge:**
```bash
gh pr merge --squash --delete-branch
git checkout main && git pull origin main
```

---

### COMMIT 4 — Todo List CRUD

**Branch:** `feature/todo-list-crud`

```bash
git checkout -b feature/todo-list-crud
```

**`src/main/java/com/example/todoapp/service/TodoListService.java`**
```java
package com.example.todoapp.service;

import com.example.todoapp.entity.TodoList;
import com.example.todoapp.entity.User;
import com.example.todoapp.repository.TodoListRepository;
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
```

**`src/main/java/com/example/todoapp/controller/DashboardController.java`**
```java
package com.example.todoapp.controller;

import com.example.todoapp.entity.User;
import com.example.todoapp.service.TodoListService;
import com.example.todoapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final TodoListService todoListService;
    private final UserService userService;

    public DashboardController(TodoListService todoListService, UserService userService) {
        this.todoListService = todoListService;
        this.userService = userService;
    }

    private User getCurrentUser(UserDetails principal) {
        return userService.findByUsername(principal.getUsername());
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("todoLists", todoListService.findAllByUser(user));
        model.addAttribute("username", user.getUsername());
        return "dashboard";
    }

    @GetMapping("/new")
    public String newListForm() {
        return "lists/form";
    }

    @PostMapping("/new")
    public String createList(@RequestParam String title,
                              @RequestParam(required = false) String description,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Title cannot be empty.");
            return "redirect:/dashboard/new";
        }
        var created = todoListService.create(title.trim(), description, user);
        return "redirect:/lists/" + created.getId();
    }

    @GetMapping("/edit/{id}")
    public String editListForm(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails principal,
                                Model model) {
        User user = getCurrentUser(principal);
        model.addAttribute("list", todoListService.findByIdAndUser(id, user));
        return "lists/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateList(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam(required = false) String description,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        todoListService.update(id, title.trim(), description, user);
        redirectAttributes.addFlashAttribute("success", "List updated successfully.");
        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteList(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        todoListService.delete(id, user);
        redirectAttributes.addFlashAttribute("success", "List deleted.");
        return "redirect:/dashboard";
    }
}
```

**`src/main/resources/templates/dashboard.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard — TodoApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
        <a class="navbar-brand" th:href="@{/dashboard}">
            <i class="bi bi-check2-square me-2"></i>TodoApp
        </a>
        <div class="navbar-nav ms-auto">
            <span class="navbar-text me-3 text-white-50" th:text="'Hello, ' + ${username}"></span>
            <form th:action="@{/logout}" method="post" class="d-inline">
                <button class="btn btn-outline-light btn-sm" type="submit">
                    <i class="bi bi-box-arrow-right me-1"></i>Logout
                </button>
            </form>
        </div>
    </div>
</nav>

<div class="container py-4">
    <div th:if="${success}" class="alert alert-success alert-dismissible fade show">
        <span th:text="${success}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <div th:if="${error}" class="alert alert-danger alert-dismissible fade show">
        <span th:text="${error}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="h4 mb-0">My Todo Lists</h2>
        <a th:href="@{/dashboard/new}" class="btn btn-primary">
            <i class="bi bi-plus-lg me-1"></i>New List
        </a>
    </div>

    <div th:if="${todoLists.empty}" class="text-center py-5 text-muted">
        <i class="bi bi-clipboard2 display-4 d-block mb-3"></i>
        <p>You have no lists yet. Create your first one!</p>
        <a th:href="@{/dashboard/new}" class="btn btn-primary">Create a List</a>
    </div>

    <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4" th:unless="${todoLists.empty}">
        <div class="col" th:each="list : ${todoLists}">
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h5 class="card-title" th:text="${list.title}"></h5>
                    <p class="card-text text-muted small" th:text="${list.description}" th:if="${list.description}"></p>
                    <p class="card-text text-muted small" th:if="${!list.description}">No description</p>
                    <span class="badge bg-secondary" th:text="${list.items.size()} + ' items'"></span>
                </div>
                <div class="card-footer bg-transparent d-flex gap-2">
                    <a th:href="@{/lists/{id}(id=${list.id})}" class="btn btn-primary btn-sm flex-fill">
                        <i class="bi bi-arrow-right-circle me-1"></i>Open
                    </a>
                    <a th:href="@{/dashboard/edit/{id}(id=${list.id})}" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-pencil"></i>
                    </a>
                    <form th:action="@{/dashboard/delete/{id}(id=${list.id})}" method="post" class="d-inline"
                          onsubmit="return confirm('Delete this list and all its items?')">
                        <button class="btn btn-outline-danger btn-sm" type="submit">
                            <i class="bi bi-trash"></i>
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

**`src/main/resources/templates/lists/form.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <meta charset="UTF-8">
    <title>New List — TodoApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-4" style="max-width: 600px;">
    <a th:href="@{/dashboard}" class="btn btn-link ps-0 mb-3">
        <i class="bi bi-arrow-left me-1"></i>Back to Dashboard
    </a>
    <div class="card shadow-sm">
        <div class="card-body p-4">
            <h2 class="h5 mb-4">Create New Todo List</h2>
            <div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
            <form th:action="@{/dashboard/new}" method="post">
                <div class="mb-3">
                    <label for="title" class="form-label">Title <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" id="title" name="title" required autofocus maxlength="100">
                </div>
                <div class="mb-4">
                    <label for="description" class="form-label">Description <span class="text-muted small">(optional)</span></label>
                    <textarea class="form-control" id="description" name="description" rows="3" maxlength="500"></textarea>
                </div>
                <div class="d-flex gap-2">
                    <button type="submit" class="btn btn-primary">Create List</button>
                    <a th:href="@{/dashboard}" class="btn btn-outline-secondary">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
```

**`src/main/resources/templates/lists/edit.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Edit List — TodoApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container py-4" style="max-width: 600px;">
    <a th:href="@{/dashboard}" class="btn btn-link ps-0 mb-3">
        <i class="bi bi-arrow-left me-1"></i>Back to Dashboard
    </a>
    <div class="card shadow-sm">
        <div class="card-body p-4">
            <h2 class="h5 mb-4">Edit Todo List</h2>
            <form th:action="@{/dashboard/edit/{id}(id=${list.id})}" method="post">
                <div class="mb-3">
                    <label for="title" class="form-label">Title <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" id="title" name="title"
                           th:value="${list.title}" required maxlength="100">
                </div>
                <div class="mb-4">
                    <label for="description" class="form-label">Description</label>
                    <textarea class="form-control" id="description" name="description"
                              rows="3" maxlength="500" th:text="${list.description}"></textarea>
                </div>
                <div class="d-flex gap-2">
                    <button type="submit" class="btn btn-primary">Save Changes</button>
                    <a th:href="@{/dashboard}" class="btn btn-outline-secondary">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
```

**Teste: `src/test/java/com/example/todoapp/service/TodoListServiceTest.java`**
```java
package com.example.todoapp.service;

import com.example.todoapp.entity.TodoList;
import com.example.todoapp.entity.User;
import com.example.todoapp.repository.TodoListRepository;
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
```

```bash
git add .
git commit -m "feat: implement Todo List CRUD

- TodoListService with create, read, update, delete operations
- DashboardController for list management endpoints
- Dashboard template with card grid layout
- Form templates for creating and editing lists
- Security: users can only access their own lists
- Unit tests for TodoListService

Author: Thomas Hartmann"

git push origin feature/todo-list-crud

gh pr create \
  --title "feat: Todo List CRUD" \
  --body "## Changes
- TodoListService with full CRUD
- DashboardController endpoints
- Thymeleaf templates: dashboard, create form, edit form
- User isolation: each user sees only their lists

## Test Coverage
- TodoListService: create, findByIdAndUser (not found), findAllByUser" \
  --base main \
  --head feature/todo-list-crud
```

```bash
gh pr merge --squash --delete-branch
git checkout main && git pull origin main
```

---

### COMMIT 5 — Todo Item CRUD

**Branch:** `feature/todo-item-crud`

```bash
git checkout -b feature/todo-item-crud
```

**`src/main/java/com/example/todoapp/service/TodoItemService.java`**
```java
package com.example.todoapp.service;

import com.example.todoapp.entity.TodoItem;
import com.example.todoapp.entity.TodoList;
import com.example.todoapp.entity.User;
import com.example.todoapp.repository.TodoItemRepository;
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
```

**`src/main/java/com/example/todoapp/controller/TodoListController.java`**
```java
package com.example.todoapp.controller;

import com.example.todoapp.entity.User;
import com.example.todoapp.service.TodoItemService;
import com.example.todoapp.service.TodoListService;
import com.example.todoapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lists")
public class TodoListController {

    private final TodoListService todoListService;
    private final TodoItemService todoItemService;
    private final UserService userService;

    public TodoListController(TodoListService todoListService,
                               TodoItemService todoItemService,
                               UserService userService) {
        this.todoListService = todoListService;
        this.todoItemService = todoItemService;
        this.userService = userService;
    }

    private User getCurrentUser(UserDetails principal) {
        return userService.findByUsername(principal.getUsername());
    }

    @GetMapping("/{id}")
    public String viewList(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails principal,
                            Model model) {
        User user = getCurrentUser(principal);
        var list = todoListService.findByIdAndUser(id, user);
        model.addAttribute("list", list);
        long completed = list.getItems().stream().filter(i -> i.isCompleted()).count();
        model.addAttribute("completedCount", completed);
        model.addAttribute("totalCount", list.getItems().size());
        return "lists/view";
    }

    @PostMapping("/{id}/items")
    public String addItem(@PathVariable Long id,
                           @RequestParam String content,
                           @AuthenticationPrincipal UserDetails principal,
                           RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Item content cannot be empty.");
            return "redirect:/lists/" + id;
        }
        todoItemService.addItem(id, content, user);
        return "redirect:/lists/" + id;
    }

    @PostMapping("/{listId}/items/{itemId}/toggle")
    public String toggleItem(@PathVariable Long listId,
                              @PathVariable Long itemId,
                              @AuthenticationPrincipal UserDetails principal) {
        User user = getCurrentUser(principal);
        todoItemService.toggleItem(itemId, listId, user);
        return "redirect:/lists/" + listId;
    }

    @PostMapping("/{listId}/items/{itemId}/delete")
    public String deleteItem(@PathVariable Long listId,
                              @PathVariable Long itemId,
                              @AuthenticationPrincipal UserDetails principal,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        todoItemService.deleteItem(itemId, listId, user);
        redirectAttributes.addFlashAttribute("success", "Item deleted.");
        return "redirect:/lists/" + listId;
    }
}
```

**`src/main/resources/templates/lists/view.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${list.title} + ' — TodoApp'">List — TodoApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        .item-completed label { text-decoration: line-through; color: #6c757d; }
        .todo-item { transition: background-color 0.15s; }
        .todo-item:hover { background-color: #f8f9fa; }
    </style>
</head>
<body class="bg-light">
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
        <a class="navbar-brand" th:href="@{/dashboard}">
            <i class="bi bi-check2-square me-2"></i>TodoApp
        </a>
        <div class="navbar-nav ms-auto">
            <form th:action="@{/logout}" method="post" class="d-inline">
                <button class="btn btn-outline-light btn-sm" type="submit">
                    <i class="bi bi-box-arrow-right me-1"></i>Logout
                </button>
            </form>
        </div>
    </div>
</nav>

<div class="container py-4" style="max-width: 700px;">
    <div class="d-flex align-items-center gap-2 mb-4">
        <a th:href="@{/dashboard}" class="btn btn-link ps-0 text-decoration-none">
            <i class="bi bi-arrow-left"></i>
        </a>
        <div class="flex-fill">
            <h2 class="h4 mb-0" th:text="${list.title}"></h2>
            <p class="text-muted small mb-0" th:if="${list.description}" th:text="${list.description}"></p>
        </div>
        <a th:href="@{/dashboard/edit/{id}(id=${list.id})}" class="btn btn-outline-secondary btn-sm">
            <i class="bi bi-pencil me-1"></i>Edit
        </a>
    </div>

    <!-- Progress -->
    <div class="card mb-4 shadow-sm" th:if="${totalCount > 0}">
        <div class="card-body py-2">
            <div class="d-flex justify-content-between align-items-center mb-1">
                <small class="text-muted" th:text="${completedCount} + ' of ' + ${totalCount} + ' completed'"></small>
                <small class="text-muted" th:text="${totalCount > 0} ? ${#numbers.formatInteger(completedCount * 100 / totalCount, 0)} + '%' : '0%'"></small>
            </div>
            <div class="progress" style="height: 6px;">
                <div class="progress-bar bg-success"
                     th:style="'width: ' + ${totalCount > 0 ? completedCount * 100 / totalCount : 0} + '%'"></div>
            </div>
        </div>
    </div>

    <!-- Flash messages -->
    <div th:if="${success}" class="alert alert-success alert-dismissible fade show">
        <span th:text="${success}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <div th:if="${error}" class="alert alert-danger alert-dismissible fade show">
        <span th:text="${error}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <!-- Add item form -->
    <div class="card mb-4 shadow-sm">
        <div class="card-body">
            <form th:action="@{/lists/{id}/items(id=${list.id})}" method="post" class="d-flex gap-2">
                <input type="text" class="form-control" name="content"
                       placeholder="Add a new item..." required maxlength="500" autofocus>
                <button type="submit" class="btn btn-primary px-3">
                    <i class="bi bi-plus-lg"></i>
                </button>
            </form>
        </div>
    </div>

    <!-- Items list -->
    <div class="card shadow-sm" th:unless="${list.items.empty}">
        <ul class="list-group list-group-flush">
            <li th:each="item : ${list.items}"
                th:class="${item.completed} ? 'list-group-item todo-item item-completed d-flex align-items-center gap-2 py-3'
                                            : 'list-group-item todo-item d-flex align-items-center gap-2 py-3'">

                <form th:action="@{/lists/{listId}/items/{itemId}/toggle(listId=${list.id},itemId=${item.id})}"
                      method="post" class="d-inline">
                    <input type="checkbox" class="form-check-input mt-0"
                           th:checked="${item.completed}"
                           onchange="this.form.submit()">
                </form>

                <label class="flex-fill mb-0" style="cursor: pointer;" th:text="${item.content}"></label>

                <form th:action="@{/lists/{listId}/items/{itemId}/delete(listId=${list.id},itemId=${item.id})}"
                      method="post" class="d-inline"
                      onsubmit="return confirm('Delete this item?')">
                    <button type="submit" class="btn btn-link text-danger btn-sm p-0" title="Delete item">
                        <i class="bi bi-x-lg"></i>
                    </button>
                </form>
            </li>
        </ul>
    </div>

    <div th:if="${list.items.empty}" class="text-center py-5 text-muted">
        <i class="bi bi-list-task display-4 d-block mb-2"></i>
        <p>No items yet. Add your first task above!</p>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

**Teste: `src/test/java/com/example/todoapp/service/TodoItemServiceTest.java`**
```java
package com.example.todoapp.service;

import com.example.todoapp.entity.TodoItem;
import com.example.todoapp.entity.TodoList;
import com.example.todoapp.entity.User;
import com.example.todoapp.repository.TodoItemRepository;
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
```

```bash
git add .
git commit -m "feat: implement Todo Item CRUD

- TodoItemService: add, toggle completion, delete items
- TodoListController: view list, add/toggle/delete items
- List view template with progress bar and inline checkbox toggle
- Security: items are only accessible through their parent list owner
- Unit tests for TodoItemService

Author: Thomas Hartmann"

git push origin feature/todo-item-crud

gh pr create \
  --title "feat: Todo Item CRUD" \
  --body "## Changes
- TodoItemService with add, toggle, delete operations
- TodoListController for item management
- Full list view with progress bar
- Inline checkbox toggle without JavaScript
- Item isolation: only accessible by list owner

## Test Coverage
- TodoItemService: add item, toggle completion, item not found" \
  --base main \
  --head feature/todo-item-crud
```

```bash
gh pr merge --squash --delete-branch
git checkout main && git pull origin main
```

---

### COMMIT 6 — Error Handling & Integration Tests

**Branch:** `feature/ui-polish`

```bash
git checkout -b feature/ui-polish
```

**`src/main/java/com/example/todoapp/controller/GlobalExceptionHandler.java`**
```java
package com.example.todoapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(IllegalArgumentException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Something went wrong. Please try again.");
        return "error/500";
    }
}
```

**`src/main/resources/templates/error/404.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Not Found — TodoApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light d-flex align-items-center justify-content-center min-vh-100">
<div class="text-center">
    <i class="bi bi-exclamation-circle text-warning display-1"></i>
    <h1 class="h3 mt-3">Not Found</h1>
    <p class="text-muted" th:text="${errorMessage}">The resource you requested could not be found.</p>
    <a href="/dashboard" class="btn btn-primary">Go to Dashboard</a>
</div>
</body>
</html>
```

**`src/main/resources/templates/error/500.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Error — TodoApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light d-flex align-items-center justify-content-center min-vh-100">
<div class="text-center">
    <i class="bi bi-exclamation-triangle text-danger display-1"></i>
    <h1 class="h3 mt-3">Something went wrong</h1>
    <p class="text-muted" th:text="${errorMessage}"></p>
    <a href="/dashboard" class="btn btn-primary">Go to Dashboard</a>
</div>
</body>
</html>
```

**Teste de integração completo: `src/test/java/com/example/todoapp/integration/TodoAppIntegrationTest.java`**
```java
package com.example.todoapp.integration;

import com.example.todoapp.entity.User;
import com.example.todoapp.repository.TodoListRepository;
import com.example.todoapp.repository.UserRepository;
import com.example.todoapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TodoAppIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private TodoListRepository todoListRepository;

    @BeforeEach
    void setUp() {
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
```

```bash
git add .
git commit -m "feat: add error handling and integration tests

- GlobalExceptionHandler for 404 and 500 errors
- Custom error page templates
- Full integration test suite covering:
  - Unauthenticated access redirects
  - Dashboard access with auth
  - Creating and persisting a Todo List
  - Register/login page accessibility

Author: Thomas Hartmann"

git push origin feature/ui-polish

gh pr create \
  --title "feat: error handling and integration tests" \
  --body "## Changes
- GlobalExceptionHandler with Thymeleaf error pages
- Integration tests with @SpringBootTest and MockMvc
- Tests cover auth, dashboard access, and list creation

## Test Coverage
- Integration: login page, dashboard redirect, authenticated dashboard, create list, register page" \
  --base main \
  --head feature/ui-polish
```

```bash
gh pr merge --squash --delete-branch
git checkout main && git pull origin main
```

---

## Checklist Final do Agente

Antes de considerar o projeto concluído, verifique:

- [ ] `git log --oneline` mostra todos os commits com "Author: Thomas Hartmann"
- [ ] `gh pr list --state merged` mostra todos os PRs já mergeados
- [ ] `gh run list` mostra todos os CI runs com status `success`
- [ ] `mvn test` roda localmente sem falhas
- [ ] `mvn spring-boot:run` sobe a aplicação em `localhost:8080`
- [ ] Fluxo funciona: `/register` → `/login` → `/dashboard` → criar lista → `/lists/{id}` → adicionar itens
- [ ] Branch protection está ativa na `main`

---

## Notas Importantes

1. **Nunca commitar diretamente na `main`** após a branch protection estar ativa. Sempre via PR.
2. **Sempre aguardar o CI** antes de fazer merge. Se falhar, investigar, corrigir na branch, e fazer push novamente.
3. **Commits atômicos:** cada commit deve representar uma mudança coesa e funcional.
4. **Mensagens de commit em inglês**, seguindo Conventional Commits (feat, fix, ci, chore, test, docs).
5. **Assinatura:** todos os commits terminam com `Author: Thomas Hartmann`.
6. **Sem dependências externas não declaradas:** tudo que for necessário deve estar no `pom.xml`.

---

*Projeto: TodoApp | Stack: Java 21 + Spring Boot 3 + Thymeleaf + H2/PostgreSQL*
*Author: Thomas Hartmann*
