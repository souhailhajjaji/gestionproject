# AGENTS.md - Coding Guidelines for Gestion Projet

Project Management System with Spring Boot 3.4 + Angular 21 + PostgreSQL + Keycloak

---

## Build / Test / Lint Commands

### Backend (Maven)
```bash
cd backend

# Build
mvn clean compile
mvn package -DskipTests

# Run
mvn spring-boot:run

# Test
mvn test                          # All tests
mvn test -Dtest=UserServiceTest   # Single class
mvn test -Dtest=UserServiceTest#createUser  # Single method

# Lint & Format
mvn checkstyle:check              # Check style violations
mvn spotless:apply                # Auto-format code

# Coverage (enforced >80%)
mvn verify
```

### Frontend (Angular CLI)
```bash
cd frontend

# Install & Run
npm install
npm start              # ng serve --host 0.0.0.0 (port 4200)

# Test
npm test                              # All tests
npm test -- --include='**/user.service.spec.ts'  # Single file
npm test -- --browsers=ChromeHeadless --watch=false --code-coverage

# Lint & Build
npm run lint
npm run build
```

---

## Backend Code Style (Java 21 / Spring Boot 3.4)

### Project Structure
```
backend/src/main/java/com/gestionprojet/
├── config/           # Security, CORS, OpenAPI
├── controller/       # REST controllers
├── dto/              # Data Transfer Objects
├── exception/        # Custom exceptions & handlers
├── model/            # JPA entities + enums
├── repository/       # Spring Data repositories
└── service/          # Business logic
```

### Naming Conventions
- **Classes**: PascalCase (`UserService`, `ProjectController`)
- **Methods/Variables**: camelCase (`getUserById()`, `userRepository`)
- **Constants**: UPPER_SNAKE_CASE (`DEFAULT_PAGE_SIZE`)
- **Packages**: lowercase reverse domain

### Annotations Order
```java
@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
@Tag(name = "Users") @SecurityRequirement(name = "bearerAuth")
public class UserController { }
```

### Dependency Injection
Use Lombok `@RequiredArgsConstructor` with `private final`:
```java
@Service @RequiredArgsConstructor @Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
}
```

### Imports Order
1. Java standard libraries
2. Third-party (Spring, Lombok)
3. Project internal imports
4. Static imports (use wildcards)

### Error Handling
- Use `@ControllerAdvice` with `@ExceptionHandler`
- Custom exceptions extend `RuntimeException`
- Error messages in French

### Key Technologies
- Java 21, Spring Boot 3.4.2, Lombok, JPA/Hibernate + PostgreSQL
- Flyway migrations, Keycloak (OAuth2/JWT), TestContainers, JaCoCo (>80%)

---

## Frontend Code Style (Angular 21 / TypeScript)

### Project Structure
```
frontend/src/app/
├── core/           # Singleton services, guards, interceptors
├── features/       # Feature modules (users, projects, tasks)
└── shared/         # Shared components, models, utilities
```

### Naming Conventions
- **Components**: kebab-case (`user-list.component.ts`)
- **Services**: `feature.service.ts`
- **Models**: PascalCase (`User`, `Project`)
- **Selectors**: prefix with `app-`

### TypeScript & Angular
- Explicit types, avoid `any`, interfaces for objects, `readonly` for immutable
- Standalone components, use `inject()` for DI, RxJS: `map`, `catchError`, `switchMap`

### Error Handling
- Backend: `@ControllerAdvice`, custom `RuntimeException`, French messages
- Frontend: Centralize in `ApiService.handleError()`, French user messages

### Styling
- SCSS, Bootstrap 5

---

## General Guidelines

### Git
- Main: `main`, Features: `feature/description`
- Commit messages: present tense ("Add user authentication")

### Security
- Never commit secrets
- Use environment variables
- Validate all inputs
- Keycloak for authz

### API Design
- RESTful under `/api`
- Proper HTTP methods
- Swagger: `/api/swagger-ui.html`

### Environment
- Java 21, Maven 3.9+, Node.js 22, Docker
- Frontend: http://localhost:4200
- Backend: http://localhost:8082/api
- Keycloak: http://localhost:8080
