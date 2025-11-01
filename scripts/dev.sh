#!/bin/bash

# Development Helper Script
# Runs both backend (Spring Boot) and frontend (Vite) dev servers concurrently

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Inmobiliaria Development Environment${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if frontend directory exists
if [ ! -d "$FRONTEND_DIR" ]; then
    echo -e "${RED}Error: Frontend directory not found at $FRONTEND_DIR${NC}"
    exit 1
fi

# Check if node_modules exists
if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
    echo -e "${YELLOW}Frontend dependencies not installed. Installing...${NC}"
    cd "$FRONTEND_DIR"
    npm install
    cd "$PROJECT_ROOT"
fi

# Function to cleanup background processes
cleanup() {
    echo ""
    echo -e "${YELLOW}Shutting down servers...${NC}"

    # Kill backend if running
    if [ ! -z "$BACKEND_PID" ] && ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo -e "${YELLOW}Stopping backend (PID: $BACKEND_PID)...${NC}"
        kill $BACKEND_PID 2>/dev/null || true
    fi

    # Kill frontend if running
    if [ ! -z "$FRONTEND_PID" ] && ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo -e "${YELLOW}Stopping frontend (PID: $FRONTEND_PID)...${NC}"
        kill $FRONTEND_PID 2>/dev/null || true
    fi

    # Additional cleanup for any remaining processes
    pkill -f "spring-boot:run" 2>/dev/null || true
    pkill -f "vite" 2>/dev/null || true

    echo -e "${GREEN}Cleanup complete${NC}"
    exit 0
}

# Trap Ctrl+C and other exit signals
trap cleanup SIGINT SIGTERM EXIT

# Check if ports are already in use
check_port() {
    local port=$1
    local service=$2
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${RED}Error: Port $port is already in use (needed for $service)${NC}"
        echo -e "${YELLOW}Try killing the process: lsof -ti:$port | xargs kill -9${NC}"
        exit 1
    fi
}

echo -e "${BLUE}Checking ports...${NC}"
check_port 8080 "Backend (Spring Boot)"
check_port 5173 "Frontend (Vite)"
echo -e "${GREEN}✓ Ports available${NC}"
echo ""

# Start backend
echo -e "${BLUE}Starting Spring Boot backend...${NC}"
cd "$PROJECT_ROOT"
mvn spring-boot:run > /tmp/inmobiliaria-backend.log 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}✓ Backend started (PID: $BACKEND_PID)${NC}"
echo -e "${YELLOW}  Logs: /tmp/inmobiliaria-backend.log${NC}"
echo -e "${YELLOW}  URL: http://localhost:8080${NC}"
echo ""

# Wait a bit for backend to start
echo -e "${BLUE}Waiting for backend to initialize...${NC}"
sleep 5

# Start frontend
echo -e "${BLUE}Starting Vite frontend dev server...${NC}"
cd "$FRONTEND_DIR"
npm run dev > /tmp/inmobiliaria-frontend.log 2>&1 &
FRONTEND_PID=$!
echo -e "${GREEN}✓ Frontend started (PID: $FRONTEND_PID)${NC}"
echo -e "${YELLOW}  Logs: /tmp/inmobiliaria-frontend.log${NC}"
echo -e "${YELLOW}  URL: http://localhost:5173${NC}"
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Development servers are running!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}Access the application at:${NC}"
echo -e "  ${GREEN}Frontend (Dev):${NC} http://localhost:5173"
echo -e "  ${YELLOW}Backend API:${NC}    http://localhost:8080/api"
echo ""
echo -e "${BLUE}Logs:${NC}"
echo -e "  ${YELLOW}Backend:${NC}  tail -f /tmp/inmobiliaria-backend.log"
echo -e "  ${YELLOW}Frontend:${NC} tail -f /tmp/inmobiliaria-frontend.log"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop all servers${NC}"
echo ""

# Monitor both processes
while true; do
    # Check if backend is still running
    if ! ps -p $BACKEND_PID > /dev/null 2>&1; then
        echo -e "${RED}Backend process died unexpectedly${NC}"
        echo -e "${YELLOW}Check logs: tail /tmp/inmobiliaria-backend.log${NC}"
        cleanup
    fi

    # Check if frontend is still running
    if ! ps -p $FRONTEND_PID > /dev/null 2>&1; then
        echo -e "${RED}Frontend process died unexpectedly${NC}"
        echo -e "${YELLOW}Check logs: tail /tmp/inmobiliaria-frontend.log${NC}"
        cleanup
    fi

    sleep 2
done
