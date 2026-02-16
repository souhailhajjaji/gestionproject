#!/bin/bash

# Start the Gestion Projet application

echo "=== Starting Gestion Projet Infrastructure ==="

cd "$(dirname "$0")/infrastructure"

# Start databases
echo "Starting PostgreSQL databases..."
docker-compose up -d postgres keycloak-db

echo "Waiting for databases to be ready..."
sleep 10

# Check database status
docker-compose ps

echo ""
echo "=== Databases Started ==="
echo "PostgreSQL (App): localhost:5432"
echo "PostgreSQL (Keycloak): localhost:5433"
echo ""
echo "Next steps:"
echo "1. Start the backend: cd ../backend && mvn spring-boot:run"
echo "2. Start the frontend: cd ../frontend && npm install && npm start"
echo ""
echo "Or use Docker Compose to start everything:"
echo "  docker-compose up -d"
