#!/bin/bash

# Comprehensive startup script for Gestion Projet
# Starts infrastructure, backend, and frontend

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$PROJECT_ROOT/infrastructure"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=${3:-30}
    local attempt=1

    log_info "Waiting for $service_name to be ready..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1 || curl -s -f "$url/health" > /dev/null 2>&1; then
            log_success "$service_name is ready!"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    log_error "$service_name failed to start after $max_attempts attempts"
    return 1
}

cleanup() {
    log_warning "Cleaning up..."
    docker-compose -f "$INFRA_DIR/docker-compose.yml" down 2>/dev/null || true
    pkill -f "mvn spring-boot:run" 2>/dev/null || true
    pkill -f "ng serve" 2>/dev/null || true
}

trap cleanup EXIT

# Main execution
echo "=========================================="
echo "  Gestion Projet - Startup Script"
echo "=========================================="
echo ""

# Step 1: Start Infrastructure Services
log_info "Step 1: Starting infrastructure services..."
cd "$INFRA_DIR"

# Start PostgreSQL and Keycloak only (not backend/frontend via docker)
docker-compose up -d postgres keycloak-db

# Wait for PostgreSQL
log_info "Waiting for PostgreSQL..."
until docker-compose exec -T postgres pg_isready -U admin -d gestionprojet > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
log_success "PostgreSQL is ready!"

# Wait for Keycloak DB
log_info "Waiting for Keycloak PostgreSQL..."
until docker-compose exec -T keycloak-db pg_isready -U keycloak -d keycloak > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
log_success "Keycloak PostgreSQL is ready!"

# Start Keycloak
docker-compose up -d keycloak

# Wait for Keycloak to be ready
log_info "Waiting for Keycloak..."
sleep 5
attempt=1
max_attempts=30
while [ $attempt -le $max_attempts ]; do
    if curl -s http://localhost:8080/realms/gestion-projet/.well-known/openid-configuration > /dev/null 2>&1; then
        log_success "Keycloak is ready!"
        break
    fi
    echo -n "."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    log_error "Keycloak failed to start. Check logs with: docker-compose -f $INFRA_DIR/docker-compose.yml logs keycloak"
    exit 1
fi

echo ""
log_success "Infrastructure is ready!"
echo "  - PostgreSQL: localhost:5432"
echo "  - Keycloak: http://localhost:8080"
echo ""

# Step 2: Verify/Create Admin User in Keycloak
log_info "Step 2: Verifying Keycloak admin user..."

# Get admin token
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/realms/master/protocol/openid-connect/token \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password" \
    -d "client_id=admin-cli" \
    -d "username=admin" \
    -d "password=admin123" 2>/dev/null | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
    log_warning "Could not get admin token. Keycloak admin credentials may not be set yet."
    log_info "Keycloak admin user: admin / admin123"
else
    # Check if gestion-projet realm exists
    REALM_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/admin/realms/gestion-projet \
        -H "Authorization: Bearer $ADMIN_TOKEN" 2>/dev/null)
    
    if [ "$REALM_EXISTS" = "200" ]; then
        log_success "Realm 'gestion-projet' exists"
        
        # Check for admin user
        ADMIN_USER=$(curl -s http://localhost:8080/admin/realms/gestion-projet/users \
            -H "Authorization: Bearer $ADMIN_TOKEN" \
            -H "Content-Type: application/json" 2>/dev/null | grep -o '"username":"admin"' | head -1)
        
        if [ -n "$ADMIN_USER" ]; then
            log_success "Admin user exists in gestion-projet realm"
        else
            log_info "Creating admin user in gestion-projet realm..."
            # Create admin user
            curl -s -X POST http://localhost:8080/admin/realms/gestion-projet/users \
                -H "Authorization: Bearer $ADMIN_TOKEN" \
                -H "Content-Type: application/json" \
                -d '{
                    "username": "admin",
                    "email": "admin@gestionprojet.com",
                    "firstName": "Admin",
                    "lastName": "User",
                    "enabled": true,
                    "emailVerified": true,
                    "credentials": [{"type": "password", "value": "admin123", "temporary": false}],
                    "realmRoles": ["ADMIN", "USER"]
                }' > /dev/null 2>&1
            log_success "Admin user created!"
        fi
        
        echo ""
        echo "=========================================="
        echo "  Default Users"
        echo "=========================================="
        echo "  Admin: admin@gestionprojet.com / admin123"
        echo "  User:  user1@gestionprojet.com / user123"
        echo "=========================================="
        echo ""
    else
        log_warning "Realm 'gestion-projet' not found. It should be imported automatically."
        log_info "If the realm wasn't imported, restart Keycloak with:"
        log_info "  docker-compose -f $INFRA_DIR/docker-compose.yml restart keycloak"
    fi
fi

# Step 3: Start Backend
log_info "Step 3: Starting Backend (Spring Boot)..."
cd "$BACKEND_DIR"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    log_error "Maven is not installed. Please install Maven 3.9+"
    exit 1
fi

# Kill any existing backend process
pkill -f "mvn spring-boot:run" 2>/dev/null || true
sleep 2

# Start backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev > /tmp/backend.log 2>&1 &
BACKEND_PID=$!

# Wait for backend
log_info "Waiting for backend to start..."
sleep 10
attempt=1
max_attempts=30
while [ $attempt -le $max_attempts ]; do
    if curl -s http://localhost:8081/api/actuator/health > /dev/null 2>&1; then
        log_success "Backend is ready!"
        break
    fi
    echo -n "."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    log_error "Backend failed to start. Check logs: /tmp/backend.log"
    log_info "Trying to start anyway..."
fi

# Step 4: Start Frontend
log_info "Step 4: Starting Frontend (Angular)..."
cd "$FRONTEND_DIR"

# Check if Node.js is available
if ! command -v node &> /dev/null; then
    log_error "Node.js is not installed. Please install Node.js 22"
    exit 1
fi

# Kill any existing frontend process
pkill -f "ng serve" 2>/dev/null || true
sleep 2

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    log_info "Installing frontend dependencies..."
    npm install --legacy-peer-deps
fi

# Start frontend
npx ng serve --host 0.0.0.0 --port 4200 --disable-host-check > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!

# Wait for frontend
log_info "Waiting for frontend to start..."
sleep 15
attempt=1
max_attempts=30
while [ $attempt -le $max_attempts ]; do
    if curl -s http://localhost:4200 > /dev/null 2>&1; then
        log_success "Frontend is ready!"
        break
    fi
    echo -n "."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    log_error "Frontend failed to start. Check logs: /tmp/frontend.log"
fi

echo ""
echo "=========================================="
echo "  Gestion Projet is Running!"
echo "=========================================="
echo ""
echo -e "${GREEN}Frontend:${NC}     http://localhost:4200"
echo -e "${GREEN}Backend API:${NC}  http://localhost:8081/api"
echo -e "${GREEN}Swagger UI:${NC}   http://localhost:8081/api/swagger-ui.html"
echo -e "${GREEN}Keycloak:${NC}     http://localhost:8080"
echo ""
echo "Default Credentials:"
echo "  Admin: admin@gestionprojet.com / admin123 (roles: ADMIN, USER)"
echo "  User:  user1@gestionprojet.com / user123 (role: USER)"
echo ""
echo -e "${YELLOW}Keycloak Admin:${NC} http://localhost:8080/admin (admin / admin123)"
echo ""
echo "Logs:"
echo "  Backend:  tail -f /tmp/backend.log"
echo "  Frontend: tail -f /tmp/frontend.log"
echo ""
echo "To stop: ./stop.sh"
echo ""

# Keep script running to maintain trap
echo "Press Ctrl+C to stop all services..."
while true; do
    sleep 1
done
