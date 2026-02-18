# AGENTS.md - Coding Guidelines for Gestion Projet

Project Management System with Spring Boot 4 (Java 25) + Angular 21 + PostgreSQL + Keycloak

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

# Test (all)
mvn test

# Test (single class)
mvn test -Dtest=UserServiceTest

# Test (single method)
mvn test -Dtest=UserServiceTest#shouldCreateUser

# Coverage (>80% required)
mvn verify  # Runs tests with JaCoCo coverage check

# Lint / Quality
mvn checkstyle:check  # If configured
```

### Frontend (Angular CLI)
```bash
cd frontend

# Install dependencies
npm install

# Development server
npm start           # ng serve --host 0.0.0.0
npm run dev         # Same with port 4200

# Build
npm run build
npm run build -- --configuration production

# Test (all)
npm test

# Test (single file)
npm test -- --include='**/user.service.spec.ts'

# Test (headless for CI)
npm run test -- --browsers=ChromeHeadless --watch=false --code-coverage

# Lint
npm run lint
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
│   └── enums/      # Enumerations
├── repository/      # Spring Data repositories
└── service/         # Business logic
```

### Naming Conventions
- **Classes**: PascalCase (`UserService`, `ProjectController`)
- **Methods**: camelCase (`getUserById()`, `createProject()`)
- **Variables**: camelCase (`userRepository`, `projectId`)
- **Constants**: UPPER_SNAKE_CASE (`WHITE_LIST`, `DEFAULT_PAGE_SIZE`)
- **Packages**: lowercase, reverse domain (`com.gestionprojet.service`)

### Annotations Order
```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User { }
```

### Imports Organization
1. Java standard libraries
2. Third-party libraries (Spring, Lombok, etc.)
3. Project internal imports

Use **wildcard imports** only for static imports.

### Key Technologies
- **Java 25** with **Spring Boot 4.0.0**
- **Lombok** for boilerplate reduction
- **MapStruct** for DTO mapping
- **JPA/Hibernate** with PostgreSQL
- **Flyway** for database migrations
- **Keycloak** for authentication (OAuth2/JWT)
- **TestContainers** for integration tests

### Error Handling
- Use `@ControllerAdvice` with `@ExceptionHandler`
- Custom exceptions extend `RuntimeException`
- Return meaningful error messages in French

### Documentation
- All public classes and methods MUST have Javadoc
- Use `@param`, `@return`, `@throws` tags
- Document business logic purpose, not implementation details

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
- **Variables**: camelCase
- **Selectors**: prefix with `app-` (e.g., `app-user-list`)

### TypeScript Strictness
```json
{
  "strict": true,
  "noImplicitOverride": true,
  "noImplicitReturns": true,
  "noFallthroughCasesInSwitch": true
}
```

### Angular Patterns
- Use **standalone components** (no NgModules)
- Use `inject()` for dependency injection
- Prefer `Observable` with async pipe over subscriptions
- Use RxJS operators: `map`, `catchError`, `switchMap`

### Imports Organization
```typescript
// 1. Angular imports
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

// 2. Third-party imports
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

// 3. Internal imports (ordered by path depth)
import { User } from '../../shared/models/user';
import { ApiService } from '../service/api.service';
```

### Error Handling
- Centralize in `ApiService.handleError()`
- Use `catchError` operator in service methods
- Display user-friendly messages in French

### Styling
- Use **SCSS** (configured in angular.json)
- Bootstrap 5 for UI components
- Component-specific styles in `.component.scss`

---

## General Guidelines

### Git Workflow
- Main branch: `main`
- Create feature branches: `feature/description`
- Commit messages in present tense: "Add user authentication"

### Testing Requirements
- Backend: >80% code coverage (enforced by JaCoCo)
- Frontend: Code coverage reporting with Karma
- Run tests before committing

### Security
- Never commit secrets or credentials
- Use environment variables for configuration
- Validate all user inputs
- Use Keycloak for authentication/authorization

### API Design
- RESTful endpoints under `/api`
- Use proper HTTP methods (GET, POST, PUT, PATCH, DELETE)
- Return appropriate HTTP status codes
- Swagger UI available at `/api/swagger-ui.html`

---

## Environment Setup

### Required
- Java 25
- Maven 3.9+
- Node.js 22
- Docker & Docker Compose

### Local Development URLs
- Frontend: http://localhost:4200
- Backend API: http://localhost:8082/api
- Swagger UI: http://localhost:8082/api/swagger-ui.html
- Keycloak: http://localhost:8080
