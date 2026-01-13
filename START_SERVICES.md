# Starting the XAI Forge Project

## Prerequisites

1. **Java 17+** - Required for backend
2. **Node.js 18+** - Required for frontend
3. **PostgreSQL** (optional, can use Docker) - Database
4. **Redis** (optional, can use Docker) - Caching

## Quick Start

### Option 1: Using Docker Compose (Recommended)

```bash
# Start all services (PostgreSQL, Redis, Backend, Frontend)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Option 2: Manual Start

#### 1. Start Infrastructure Services

```bash
# Start PostgreSQL and Redis
docker-compose up -d postgres redis

# Or use local installations if available
```

#### 2. Start Backend

```bash
cd backend
mvn clean install -DskipTests
cd xai-api

# Set JWT secret (required)
export JWT_SECRET=$(openssl rand -base64 64)

# Start backend
mvn spring-boot:run
```

Backend will be available at: http://localhost:8080

#### 3. Start Frontend

```bash
cd frontend
npm install --legacy-peer-deps
npm run dev
```

Frontend will be available at: http://localhost:5173 (Vite default) or http://localhost:3000

## Environment Variables

### Backend

- `JWT_SECRET` - Required! Generate with: `openssl rand -base64 64`
- `DB_URL` - Database connection URL (default: jdbc:postgresql://localhost:5432/xaiforge)
- `DB_USERNAME` - Database username (default: xaiforge)
- `DB_PASSWORD` - Database password (default: changeme)
- `SPRING_REDIS_HOST` - Redis host (default: localhost)
- `SPRING_REDIS_PORT` - Redis port (default: 6379)

### Frontend

- `VITE_API_URL` - Backend API URL (default: http://localhost:8080/api)

## Health Checks

- Backend Health: http://localhost:8080/actuator/health
- Backend API Docs: http://localhost:8080/swagger-ui.html (if enabled)
- Frontend: http://localhost:5173

## Troubleshooting

### Backend won't start
1. Check if PostgreSQL is running: `docker ps | grep postgres`
2. Check if Redis is running: `docker ps | grep redis`
3. Check logs: `tail -f /tmp/backend.log` or `docker-compose logs backend`
4. Verify JWT_SECRET is set

### Frontend won't start
1. Check Node.js version: `node --version` (should be 18+)
2. Clear node_modules and reinstall: `rm -rf node_modules package-lock.json && npm install --legacy-peer-deps`
3. Check logs: `tail -f /tmp/frontend.log`

### Database connection errors
1. Ensure PostgreSQL is running
2. Check connection string in application.properties
3. Verify database exists: `psql -U xaiforge -d xaiforge`

### Redis connection errors
1. Ensure Redis is running: `redis-cli ping` (should return PONG)
2. Check Redis host/port in application.properties
3. Backend will work without Redis but caching won't function

## Current Status

- ✅ Backend compilation: SUCCESS
- ✅ Frontend dependencies: Installed
- ⚠️  Services: Starting...

## Logs Location

- Backend: `/tmp/backend.log`
- Frontend: `/tmp/frontend.log`
- Docker: `docker-compose logs`

