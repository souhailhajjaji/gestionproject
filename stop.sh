#!/bin/bash

# Stop the Gestion Projet application

echo "=== Stopping Gestion Projet Application ==="

# Kill processes on ports
echo "Stopping services..."
fuser -k 8080/tcp 2>/dev/null && echo "✅ Backend stopped"
fuser -k 4200/tcp 2>/dev/null && echo "✅ Frontend stopped"

# Also kill any remaining ng or java processes for this project
pkill -f "ng serve" 2>/dev/null
pkill -f "GestionProjetApplication" 2>/dev/null

echo ""
echo "=== All services stopped ==="
