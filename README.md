# Gestion Projet - Project Management System

A full-stack project management application built with Spring Boot 4, Angular 21, and Keycloak for authentication.

## 🏗️ Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Angular   │────▶│ Spring Boot │────▶│ PostgreSQL  │
│   Frontend  │     │    Backend  │     │  Database   │
└─────────────┘     └─────────────┘     └─────────────┘
       │                   │
       ▼                   ▼
┌─────────────┐     ┌─────────────┐
│  Keycloak   │     │   RustFS    │
│     Auth    │     │   Storage   │
└─────────────┘     └─────────────┘
```

## 📋 Features

### User Management (Keycloak)
- Add / Modify / Delete users
- Fields: Last name, First name, Birth date, Email, Phone, Identity document (RustFS)
- Roles: ADMIN, USER

### Project Management
- Create projects
- Assign a project manager
- Fields: Name, Description, Start date, End date

### Task Management
- Add tasks to a project
- Assign tasks to users
- Update status
- Fields: Title, Description, Status (TODO, IN_PROGRESS, DONE), Priority (LOW, MEDIUM, HIGH)

### Dashboard
- List of all projects
- Project details with tasks by status
- Filter tasks by user and status

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Backend | Java 25, Spring Boot 4.0.0, Spring Data JPA |
| Frontend | Angular 21, Bootstrap 5 |
| Database | PostgreSQL 16 |
| Authentication | Keycloak 26 |
| File Storage | RustFS |
| Container | Docker, Kubernetes |
| CI/CD | GitLab CI |
| Quality | SonarQube (0 bugs, >80% coverage) |

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 25
- Node.js 22
- Maven 3.9+

### Local Development

1. **Clone the repository**
```bash
git clone https://gitlab.com/your-org/gestionprojet.git
cd gestionprojet
```

2. **Start infrastructure services**
```bash
cd infrastructure
docker-compose up -d postgres keycloak-db rustfs
```

3. **Wait for services to be ready**
```bash
# Check PostgreSQL
docker-compose ps

# Check Keycloak logs
docker-compose logs -f keycloak
```

4. **Start the backend**
```bash
cd backend
mvn spring-boot:run
```

5. **Start the frontend**
```bash
cd frontend
npm install
npm start
```

6. **Access the application**
- Frontend: http://localhost:4200
- Backend API: http://localhost:8082/api
- Swagger UI: http://localhost:8082/api/swagger-ui.html
- Keycloak: http://localhost:8080

### Docker Compose (All-in-one)

```bash
cd infrastructure
docker-compose up -d
```

All services will be available:
- Frontend: http://localhost:4200
- Backend: http://localhost:8082
- Keycloak: http://localhost:8080
- PostgreSQL: localhost:5432
- RustFS: http://localhost:8081

## 🔐 Default Credentials

| User | Email | Password | Role |
|------|-------|----------|------|
| Admin | admin@gestionprojet.com | admin123 | ADMIN |
| User | user1@gestionprojet.com | user123 | USER |

## 📁 Project Structure

```
gestionprojet/
├── backend/                    # Spring Boot Application
│   ├── src/main/java/com/gestionprojet/
│   │   ├── config/            # Security, CORS, OpenAPI config
│   │   ├── controller/        # REST controllers
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── exception/         # Exception handling
│   │   ├── model/             # JPA entities
│   │   ├── repository/        # Spring Data repositories
│   │   └── service/           # Business logic
│   ├── src/main/resources/
│   │   └── db/migration/      # Flyway migrations
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/                   # Angular Application
│   ├── src/app/
│   │   ├── core/              # Guards, interceptors, services
│   │   ├── features/          # Feature modules
│   │   ├── shared/            # Shared components & models
│   │   └── app.config.ts
│   ├── package.json
│   └── Dockerfile
│
├── infrastructure/
│   ├── docker-compose.yml     # Local development
│   ├── kubernetes/            # K8s manifests
│   └── keycloak/              # Keycloak realm export
│
├── .gitlab-ci.yml             # CI/CD pipeline
└── README.md
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
mvn verify  # With coverage check
```

### Frontend Tests
```bash
cd frontend
npm test
npm run test -- --code-coverage
```

### SonarQube Analysis
```bash
cd backend
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=gestion-projet \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token
```

## 📦 Kubernetes Deployment

```bash
kubectl apply -f infrastructure/kubernetes/namespace.yaml
kubectl apply -f infrastructure/kubernetes/
kubectl rollout status deployment/backend
kubectl rollout status deployment/frontend
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL URL | `jdbc:postgresql://localhost:5432/gestionprojet` |
| `SPRING_DATASOURCE_USERNAME` | Database user | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `admin123` |
| `KEYCLOAK_AUTH_SERVER_URL` | Keycloak URL | `http://localhost:8080` |
| `RUSTFS_BASE_URL` | RustFS URL | `http://localhost:8081` |

## 📝 API Endpoints

### Users
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `POST /api/users/{id}/document` - Upload identity document

### Projects
- `GET /api/projects` - List all projects
- `GET /api/projects/{id}` - Get project by ID
- `POST /api/projects` - Create project
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

### Tasks
- `GET /api/tasks` - List all tasks
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks` - Create task
- `PUT /api/tasks/{id}` - Update task
- `PATCH /api/tasks/{id}/status` - Update task status
- `DELETE /api/tasks/{id}` - Delete task
- `GET /api/tasks/filter` - Filter tasks

## 🎯 Quality Gates

- **SonarQube**: 0 bugs, 0 vulnerabilities
- **Test Coverage**: >80%
- **Code Style**: Spring Boot conventions

## 📄 License

MIT License - see LICENSE file for details

## 👥 Team

Gestion Projet Team
