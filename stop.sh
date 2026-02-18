#!/bin/bash

# Stop script for Gestion Projet

echo "=== Stopping Gestion Projet Application ==="

# Stop Spring Boot backend
pkill -f "mvn spring-boot:run" 2>/dev/null && echo "✓ Backend stopped"
pkill -f "java.*gestion-projet" 2>/dev/null && echo "✓ Java processes stopped"

# Stop Angular frontend
pkill -f "ng serve" 2>/dev/null && echo "✓ Frontend stopped"

# Stop Docker containers
if [ -f "infrastructure/docker-compose.yml" ]; then
    echo "Stopping Docker containers..."
    docker-compose -f infrastructure/docker-compose.yml down 2>/dev/null && echo "✓ Docker containers stopped"
fi

# Kill any remaining processes on ports
fuser -k 8081/tcp 2>/dev/null
fuser -k 4200/tcp 2>/dev/null
fuser -k 8080/tcp 2>/dev/null
fuser -k 5432/tcp 2>/dev/null

echo ""
echo "=== All services stopped ==="
