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
mvn test                                      # All tests
mvn test -Dtest=UserServiceTest              # Single class
mvn test -Dtest=UserServiceTest#createUser   # Single method

# Coverage (enforced >80%)
mvn verify
```

### Frontend (Angular CLI)
```bash
cd frontend

# Install & Run
npm install
npm start              # ng serve --host 0.0.0.0
npm run dev            # Port 4200

# Test
npm test               # All tests
npm test -- --include='**/user.service.spec.ts'  # Single file
npm run test -- --browsers=ChromeHeadless --watch=false --code-coverage

# Lint
npm run lint
npm run build
```

---

## Backend Code Style (Java)

### Project Structure
```
backend/src/main/java/com/gestionprojet/
├── config/           # Security, CORS, OpenAPI configs
├── controller/       # REST controllers
├── dto/             # Data Transfer Objects
├── exception/       # Custom exceptions & handlers
├── model/           # JPA entities
│   └── enums/       # Enumerations
├── repository/      # Spring Data repositories
└── service/         # Business logic
```

### Naming Conventions
- **Classes**: PascalCase (`UserService`, `ProjectController`)
- **Methods**: camelCase (`getUserById()`, `createProject()`)
- **Variables**: camelCase (`userRepository`, `projectId`)
- **Constants**: UPPER_SNAKE_CASE (`WHITE_LIST`, `DEFAULT_PAGE_SIZE`)
- **Packages**: lowercase reverse domain (`com.gestionprojet.service`)

### Annotations Order
```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UserController { }
```

### Dependency Injection
Use Lombok `@RequiredArgsConstructor` with `private final` fields:
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
}
```

### Imports Organization
1. Java standard libraries
2. Third-party libraries (Spring, Lombok, etc.)
3. Project internal imports

Use wildcard imports only for static imports.

### Key Technologies
- **Java 21** with **Spring Boot 3.4.2**
- **Lombok** (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`)
- **JPA/Hibernate** with PostgreSQL
- **Flyway** for database migrations
- **Keycloak** for authentication (OAuth2/JWT)
- **TestContainers** for integration tests
- **JaCoCo** for coverage (>80% required)

### Error Handling
- Use `@ControllerAdvice` with `@ExceptionHandler`
- Custom exceptions extend `RuntimeException`
- Return meaningful error messages in French

### Documentation
- All public classes and methods MUST have Javadoc
- Use `@param`, `@return`, `@throws` tags

---

## Frontend Code Style (Angular/TypeScript)

### Project Structure
```
frontend/src/app/
├── core/                    # Singleton services, guards, interceptors
│   ├── guard/
│   ├── interceptor/
│   └── service/
├── features/                # Feature modules
│   ├── users/
│   ├── projects/
│   └── tasks/
└── shared/                  # Shared components, models, utilities
    ├── models/
    └── components/
```

### Naming Conventions
- **Components**: `feature-name.component.ts` (kebab-case)
- **Services**: `feature.service.ts`
- **Models**: PascalCase (`User`, `Project`, `TaskStatus`)
- **Selectors**: prefix with `app-` (e.g., `app-user-list`)

### Angular Patterns
- Use **standalone components** (no NgModules)
- Use `inject()` for dependency injection
- Prefer inline templates for simple components
- Use RxJS operators: `map`, `catchError`, `switchMap`

### Component Example
```typescript
@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `...`,
  styles: []
})
export class UserListComponent implements OnInit {
  private apiService = inject(ApiService);
  keycloakService = inject(KeycloakService);
}
```

### Error Handling
- Centralize in `ApiService.handleError()`
- Display user-friendly messages in French

### Styling
- Use **SCSS** (configured in angular.json)
- Bootstrap 5 for UI components

---

## General Guidelines

### Git Workflow
- Main branch: `main`
- Feature branches: `feature/description`
- Commit messages in present tense: "Add user authentication"

### Security
- Never commit secrets or credentials
- Use environment variables for configuration
- Validate all user inputs
- Use Keycloak for authentication/authorization

### API Design
- RESTful endpoints under `/api`
- Use proper HTTP methods (GET, POST, PUT, PATCH, DELETE)
- Swagger UI available at `/api/swagger-ui.html`

---

## Environment Setup

### Required
- Java 21
- Maven 3.9+
- Node.js 22
- Docker & Docker Compose

### Local Development URLs
- Frontend: http://localhost:4200
- Backend API: http://localhost:8082/api
- Swagger UI: http://localhost:8082/api/swagger-ui.html
- Keycloak: http://localhost:8080
