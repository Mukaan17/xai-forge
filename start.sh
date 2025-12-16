# @Date:   2025-09-04 16:11:39
# @Last Modified by:   Mukhil Sundararaj
# @Last Modified time: 2025-10-24 18:34:50
#!/bin/bash

# XAI Application Startup Script
echo "Starting XAI Application..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "Node.js is not installed. Please install Node.js 16 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

# Create uploads directory if it doesn't exist
mkdir -p backend/uploads
mkdir -p backend/uploads/models

echo "Starting backend..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!

# Wait for backend to start
echo "Waiting for backend to start..."
sleep 30

echo "Starting frontend..."
cd ../frontend
npm start &
FRONTEND_PID=$!

echo "Application started!"
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop both services"

# Function to cleanup on exit
cleanup() {
    echo "Stopping services..."
    kill $BACKEND_PID 2>/dev/null
    kill $FRONTEND_PID 2>/dev/null
    exit 0
}

# Set trap to cleanup on script exit
trap cleanup SIGINT SIGTERM

# Wait for both processes
wait
