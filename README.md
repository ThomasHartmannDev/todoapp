# TodoApp

A full-stack Todo List web application built with Java 21 and Spring Boot 4, featuring user authentication, per-user data isolation, and a GitHub Actions CI/CD pipeline that gates every merge to `main`.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [DevOps](#devops)
  - [CI/CD Pipeline](#cicd-pipeline)
  - [Branch Strategy](#branch-strategy)
  - [Branch Protection Rules](#branch-protection-rules)
  - [Artifact Management](#artifact-management)
- [Database](#database)
- [Security](#security)
- [Running Tests](#running-tests)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security 7 |
| Persistence | Spring Data JPA + Hibernate |
| View | Thymeleaf 3 + Bootstrap 5 |
| DB (dev) | H2 (in-memory) |
| DB (prod) | PostgreSQL |
| Build | Maven |
| CI/CD | GitHub Actions |
| VCS | Git + GitHub |

---

## Features

- User registration and login (BCrypt password hashing)
- Each user sees only their own lists (data isolation)
- Full CRUD for Todo Lists
- Full CRUD for Todo Items within each list
- Progress bar per list (completed / total items)
- Inline checkbox toggle without JavaScript
- Custom 404 / 500 error pages

---

## Project Structure

```
src/
├── main/
│   ├── java/com/hartmann/todoapp/
│   │   ├── config/          # SecurityConfig (Spring Security 7)
│   │   ├── controller/      # AuthController, DashboardController,
│   │   │                    # TodoListController, GlobalExceptionHandler
│   │   ├── dto/             # RegistrationForm (Bean Validation)
│   │   ├── entity/          # User, TodoList, TodoItem (JPA entities)
│   │   ├── repository/      # Spring Data JPA repositories
│   │   └── service/         # UserService, TodoListService, TodoItemService
│   └── resources/
│       ├── application.properties
│       └── templates/       # Thymeleaf templates
│           ├── auth/        # login.html, register.html
│           ├── lists/       # view.html, form.html, edit.html
│           └── error/       # 404.html, 500.html
└── test/
    └── java/com/hartmann/todoapp/
        ├── controller/      # AuthControllerTest (MockMvc)
        ├── integration/     # TodoAppIntegrationTest (@SpringBootTest)
        ├── repository/      # UserRepositoryTest
        └── service/         # UserServiceTest, TodoListServiceTest, TodoItemServiceTest
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the included `./mvnw` wrapper)

### Run locally

```bash
./mvnw spring-boot:run
```

The application starts at `http://localhost:8080`.
The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:tododb`).

### Build a JAR

```bash
./mvnw package -DskipTests
java -jar target/todoapp-0.0.1-SNAPSHOT.jar
```

---

## DevOps

### CI/CD Pipeline

The pipeline is defined in [`.github/workflows/ci.yml`](.github/workflows/ci.yml) and runs on **every push to `main`** and every **Pull Request targeting `main`**.

```
Push / PR to main
       │
       ▼
┌─────────────────────────────────────────┐
│  1. Checkout code                        │
│  2. Set up JDK 21 (Temurin, Maven cache) │
│  3. mvn clean compile                    │  ← Fails fast on compile errors
│  4. mvn test                             │  ← All unit + integration tests
│  5. mvn package                          │  ← Produces the runnable JAR
│  6. Upload test results (always)         │  ← Surefire XML reports
│  7. Upload JAR artifact                  │  ← Ready for deployment
└─────────────────────────────────────────┘
```

**Key design decisions:**

- `compile` runs as a separate step before `test` so compile errors surface faster with a clearer message.
- Test results are uploaded with `if: always()` so they are available for inspection even when tests fail.
- The JAR artifact is stored after a successful package step, ready for manual deployment or as input to a future CD stage.
- Maven dependency cache (`cache: maven`) avoids re-downloading dependencies on every run, significantly reducing build times.

### Branch Strategy

All work follows a **feature-branch workflow**. No code is pushed directly to `main` after the initial CI setup.

| Branch pattern | Purpose |
|---|---|
| `main` | Production-ready code only. Protected. |
| `feature/*` | New features, merged via PR after CI passes |
| `fix/*` | Bug fixes, merged via PR after CI passes |
| `docs/*` | Documentation updates |

**Commit convention:** [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `ci:`, `chore:`, `docs:`).

**Merge strategy:** Squash merge — keeps `main` history linear and readable.

**PR flow:**

```
git checkout -b feature/my-feature
# ... develop and commit ...
git push origin feature/my-feature
gh pr create --base main --head feature/my-feature
# Wait for CI to pass
gh pr merge --squash --delete-branch
git checkout main && git pull origin main
```

### Branch Protection Rules

`main` is protected with the following rules (configured via the GitHub API):

| Rule | Value |
|---|---|
| Require status checks to pass | `Build & Test` must pass |
| Require branches to be up to date | Strict mode enabled |
| Allow force pushes | Disabled |
| Allow deletions | Disabled |
| Enforce on administrators | Disabled (admins can bypass if needed) |

To inspect or update the rules via the GitHub CLI:

```bash
# View current protection
gh api repos/ThomasHartmannDev/todoapp/branches/main/protection

# Re-apply standard rules
gh api repos/ThomasHartmannDev/todoapp/branches/main/protection \
  --method PUT \
  --field "required_status_checks[strict]=true" \
  --field "required_status_checks[contexts][]=Build & Test" \
  --field "enforce_admins=false" \
  --field "restrictions=null" \
  --field "required_pull_request_reviews=null" \
  --field "allow_force_pushes=false"
```

### Artifact Management

Each successful CI run produces two downloadable artifacts:

| Artifact | Content | Retention |
|---|---|---|
| `test-results` | Surefire XML reports (`target/surefire-reports/`) | 90 days (GitHub default) |
| `app-jar` | Runnable Spring Boot JAR (`target/*.jar`) | 90 days (GitHub default) |

Download artifacts via the CLI:

```bash
# List recent runs
gh run list --limit 5

# Download all artifacts from a specific run
gh run download <run-id>

# Download a specific artifact
gh run download <run-id> --name app-jar
```

---

## Database

### Development (H2 in-memory)

Configured in `application.properties`. The schema is created automatically on startup (`ddl-auto=create-drop`) and wiped on shutdown — no migration tooling needed for local development.

| Property | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:tododb` |
| Username | `sa` |
| Password | *(empty)* |
| Console | `http://localhost:8080/h2-console` |

### Production (PostgreSQL)

Create `src/main/resources/application-prod.properties` (excluded from git via `.gitignore`):

```properties
spring.datasource.url=jdbc:postgresql://<host>:<port>/<dbname>
spring.datasource.username=<user>
spring.datasource.password=<password>
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=false
```

Activate the profile at startup:

```bash
java -jar target/todoapp-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## Security

- Passwords are hashed with **BCrypt** (strength factor: 10).
- All routes require authentication except `/`, `/login`, `/register`, and static assets.
- CSRF protection is enabled globally; only the H2 console endpoint is excluded (development only).
- User data is isolated at the service layer — every query for lists or items filters by the authenticated user, making it impossible for one user to access another's data even by guessing IDs.

---

## Running Tests

```bash
# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=UserServiceTest

# Skip tests during build
./mvnw package -DskipTests
```

### Test coverage overview

| Layer | Test type | Key scenarios |
|---|---|---|
| Repository | `@SpringBootTest` + `@Transactional` | Save, findByUsername, existsByUsername |
| Service — User | Mockito unit test | Register, duplicate username/email, user not found |
| Service — TodoList | Mockito unit test | Create, findByIdAndUser (not found), findAllByUser |
| Service — TodoItem | Mockito unit test | Add item, toggle completion, item not found |
| Controller — Auth | `@SpringBootTest` + MockMvc | GET /login and /register return correct views |
| Integration | `@SpringBootTest` + MockMvc | Unauthenticated redirect, dashboard access, create list |
