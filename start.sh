#!/bin/bash

# Start the Gestion Projet application

echo "=== Starting Gestion Projet Application ==="

# Kill any existing processes
echo "Cleaning up existing processes..."
fuser -k 8080/tcp 2>/dev/null
fuser -k 4200/tcp 2>/dev/null
sleep 2

# Start backend
echo "Starting Backend (Spring Boot)..."
cd "$(dirname "$0")/backend"
mvn spring-boot:run > /tmp/backend.log 2>&1 &
BACKEND_PID=$!

# Wait for backend to start
echo "Waiting for backend to start..."
sleep 10

# Start frontend
echo "Starting Frontend (Angular)..."
cd "$(dirname "$0")/frontend"
npx ng serve --host 0.0.0.0 --port 4200 > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!

# Wait for frontend to start
echo "Waiting for frontend to start..."
sleep 20

# Check status
echo ""
echo "=== Services Status ==="
if kill -0 $BACKEND_PID 2>/dev/null; then
    echo "✅ Backend: Running (PID: $BACKEND_PID)"
else
    echo "❌ Backend: Failed"
fi

if kill -0 $FRONTEND_PID 2>/dev/null; then
    echo "✅ Frontend: Running (PID: $FRONTEND_PID)"
else
    echo "❌ Frontend: Failed"
fi

echo ""
echo "=== Access URLs ==="
echo "Frontend: http://localhost:4200"
echo "Backend API: http://localhost:8080/api"
echo "Swagger UI: http://localhost:8080/api/swagger-ui.html"
echo ""
echo "To stop: ./stop.sh"
