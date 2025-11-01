# Development Workflow Guide

This guide explains the different development modes for the Inmobiliaria Management System and when to use each one.

## Table of Contents
- [Quick Start](#quick-start)
- [Development Modes](#development-modes)
- [Understanding the Architecture](#understanding-the-architecture)
- [Troubleshooting](#troubleshooting)
- [Scripts Reference](#scripts-reference)

## Quick Start

### For Frontend Development (Recommended)

**Terminal 1 - Backend:**
```bash
cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend/frontend
npm run dev
```

**Access:** Open http://localhost:5173 in your browser

✅ **Benefits:**
- Instant hot reload on file changes (HMR)
- TypeScript errors shown immediately
- Fast feedback loop
- Vite dev server proxies API calls to backend

---

## Development Modes

### Mode 1: Full Development Mode (Recommended for Active Development)

**When to use:**
- Developing React components
- Working on UI/UX
- Implementing forms or pages
- Debugging frontend issues
- Active frontend development

**Setup:**

1. **Start Backend** (Terminal 1):
   ```bash
   cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend
   mvn spring-boot:run
   ```
   - Backend runs on `http://localhost:8080`
   - Serves REST API at `/api/*`

2. **Start Frontend Dev Server** (Terminal 2):
   ```bash
   cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend/frontend
   npm run dev
   ```
   - Frontend dev server runs on `http://localhost:5173`
   - Hot Module Replacement (HMR) enabled
   - Auto-refreshes on file changes
   - Proxies `/api` requests to backend

3. **Open Browser:**
   - Navigate to `http://localhost:5173`
   - DevTools available for debugging
   - React DevTools recommended

**How it works:**
```
Browser (localhost:5173)
    ↓
Vite Dev Server
    ├─→ Serves React app with HMR
    └─→ Proxies /api/* to localhost:8080
            ↓
        Spring Boot Backend
```

**File Watching:**
- Your `vite.config.ts` has polling enabled (300ms interval)
- Changes to `.tsx`, `.ts`, `.css` files trigger instant reload
- No manual restart needed
- Works even when running with `nohup` or in background

---

### Mode 2: Production-like Testing

**When to use:**
- Testing production build
- Verifying static asset generation
- Pre-deployment validation
- Testing before Docker build
- Not recommended for active development (slow feedback)

**There are two sub-modes:**

#### Mode 2A: Local Production Testing (Without Docker)

**Setup:**

1. **Build Frontend:**
   ```bash
   cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend/frontend
   npm run build
   ```
   - Compiles TypeScript
   - Bundles React app
   - Outputs to `../src/main/resources/static/`
   - Minifies and optimizes assets

2. **Run Backend:**
   ```bash
   cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend
   mvn spring-boot:run
   ```
   - Serves static files from `src/main/resources/static/`
   - Access at `http://localhost:8080`

3. **Open Browser:**
   - Navigate to `http://localhost:8080`
   - Frontend served as static files by Spring Boot

**How it works:**
```
Browser (localhost:8080)
    ↓
Spring Boot
    ├─→ Serves static files from /static
    └─→ Serves REST API from /api/*
```

**⚠️ Important:**
- No hot reload
- Must run `npm run build` after every frontend change
- Slower development cycle
- Use only for production testing

#### Mode 2B: Full Production Testing with Docker (Recommended for Pre-Deployment)

**Setup:**

1. **Build Frontend FIRST:**
   ```bash
   cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend/frontend
   npm run build
   cd ..
   ```

2. **Build Docker Image:**
   ```bash
   docker build -t inmobiliaria-test .
   ```
   ⚠️ **Important:** The current Dockerfile does NOT build frontend automatically. You MUST build frontend first (step 1).

3. **Run Docker Container:**
   ```bash
   docker run -p 8080:8080 \
     -e GOOGLE_CLOUD_PROJECT=inmobiliaria-adk \
     -e GOOGLE_CLOUD_LOCATION=us-central1 \
     -e GOOGLE_GENAI_USE_VERTEXAI=true \
     -v ~/inmobiliaria-service-account-key.json:/credentials.json:ro \
     -e GOOGLE_APPLICATION_CREDENTIALS=/credentials.json \
     inmobiliaria-test
   ```

4. **Access Application:**
   - Navigate to `http://localhost:8080`
   - Exact production environment
   - Tests container startup and configuration

**How it works:**
```
Browser (localhost:8080)
    ↓
Docker Container
    ↓
Spring Boot (in container)
    ├─→ Serves static files from /static
    └─→ Serves REST API from /api/*
```

**⚠️ Critical Notes:**
- Frontend must be pre-built before `docker build`
- Docker image includes pre-built frontend from `src/main/resources/static/`
- If you update frontend, rebuild AND rebuild Docker image
- This tests the exact production deployment configuration

**🔧 Alternative: Integrated Frontend Build (Future Enhancement)**

To automate frontend build in Docker, update the Dockerfile to use multi-stage build with Node.js. See `docs/production/Dockerfile` for template with integrated frontend build.

---

### Mode 3: Using Helper Script (Concurrent Execution)

For convenience, you can use the provided script to run both servers:

```bash
cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend
./scripts/dev.sh
```

This script:
- Starts backend in background
- Starts frontend dev server in foreground
- Stops both when you press Ctrl+C
- Shows logs from both processes

---

## Understanding the Architecture

### Frontend Build Output

When you run `npm run build`:
```
frontend/
├── dist/                    # ❌ Not used (Vite default)
└── ../src/main/resources/static/   # ✅ Actual output
    ├── index.html
    └── assets/
        ├── index-[hash].js
        ├── index-[hash].css
        └── vite.svg
```

**Why?** The `vite.config.ts` is configured to output directly to Spring Boot's static resources directory:

```typescript
build: {
  outDir: '../src/main/resources/static',  // Spring Boot serves this
  emptyOutDir: true,
}
```

### API Proxy Configuration

In development mode, the Vite dev server proxies API calls:

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

**Example:**
- Frontend makes request to `/api/inmobiliarias`
- Vite proxies to `http://localhost:8080/api/inmobiliarias`
- Backend processes request
- Response returned to frontend

---

## Troubleshooting

### Problem: Frontend changes don't appear in browser

**Solution depends on which mode you're using:**

#### If using `http://localhost:5173` (Dev Server):
1. Check if Vite dev server is running:
   ```bash
   lsof -i :5173
   ```
2. Restart dev server:
   ```bash
   cd frontend
   pkill -f "vite"
   npm run dev
   ```
3. Hard refresh browser: `Cmd+Shift+R` (Mac) or `Ctrl+Shift+R` (Windows/Linux)

#### If using `http://localhost:8080` (Production Mode):
1. Rebuild frontend:
   ```bash
   cd frontend
   npm run build
   ```
2. Restart Spring Boot:
   ```bash
   # In backend directory
   pkill -f "spring-boot"
   mvn spring-boot:run
   ```

### Problem: Vite dev server not watching files

**Check if polling is enabled:**
```bash
cd frontend
cat vite.config.ts | grep -A 3 "watch:"
```

Should show:
```typescript
watch: {
  usePolling: true,
  interval: 300
}
```

**If missing, the vite.config.ts needs to be updated.**

### Problem: API calls return 404 or CORS errors

**In Dev Mode (localhost:5173):**
- Verify backend is running on port 8080
- Check proxy configuration in `vite.config.ts`
- Ensure API endpoint starts with `/api`

**In Production Mode (localhost:8080):**
- Verify Spring Boot SecurityConfig allows static resources
- Check that backend is serving both `/api` and static files

### Problem: Running with nohup and HMR not working

The current configuration with `usePolling: true` should work even with nohup. However, for best results:

**Recommended approach:**
```bash
# Terminal 1 - Backend with nohup
nohup mvn spring-boot:run > /tmp/backend.log 2>&1 &

# Terminal 2 - Frontend WITHOUT nohup (for better terminal output)
cd frontend
npm run dev
```

**Alternative with nohup for both:**
```bash
# Terminal 1 - Backend
nohup mvn spring-boot:run > /tmp/backend.log 2>&1 &

# Terminal 2 - Frontend
nohup npm run dev > /tmp/frontend.log 2>&1 &

# Monitor logs
tail -f /tmp/frontend.log
```

### Problem: Port already in use

**Frontend (5173):**
```bash
lsof -ti:5173 | xargs kill -9
```

**Backend (8080):**
```bash
lsof -ti:8080 | xargs kill -9
```

---

## Scripts Reference

### Frontend Scripts (run from `frontend/` directory)

```bash
# Development server with HMR
npm run dev

# Production build
npm run build

# Type checking without building
npm run type-check

# Preview production build
npm run preview

# Run E2E tests
npm run test:e2e

# Lint code
npm run lint
```

### Backend Scripts (run from `backend/` directory)

```bash
# Run Spring Boot application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Clean and rebuild
mvn clean install

# Run tests
mvn test

# Package without tests
mvn clean package -DskipTests
```

---

## Best Practices

### ✅ DO

1. **Use Mode 1 (Full Development) for all frontend work**
   - Fastest feedback loop
   - Immediate error detection
   - Best developer experience

2. **Keep both terminals visible**
   - Monitor backend logs for API errors
   - Watch frontend for HMR updates

3. **Run `npm run type-check` before committing**
   - Catches TypeScript errors early
   - Ensures type safety

4. **Build and test in Production Mode before deploying**
   - Verify bundling works correctly
   - Check for production-only issues

### ❌ DON'T

1. **Don't use Production Mode for active development**
   - Slow rebuild cycle
   - No HMR
   - Poor developer experience

2. **Don't commit built files**
   - `src/main/resources/static/` should be in `.gitignore`
   - Only source files in version control

3. **Don't run `npm run build` manually during development**
   - Unnecessary
   - Use dev server instead
   - Only build for production testing

4. **Don't ignore TypeScript errors**
   - Fix them immediately
   - Run `npm run type-check` regularly

---

## Environment Variables

### Development (.env.development)
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_AGENT_WS_URL=ws://localhost:8080/agent/ws
```

### Production (.env.production)
```env
VITE_API_BASE_URL=/api
VITE_AGENT_WS_URL=wss://your-domain.com/agent/ws
```

---

## Quick Decision Tree

**Q: Am I developing frontend features?**
- Yes → Use Mode 1 (Full Development)
- No → Continue reading

**Q: Am I testing the final build?**
- Yes → Use Mode 2 (Production-like)
- No → Continue reading

**Q: Am I deploying to production?**
- Yes → Run `npm run build`, then deploy
- No → Use Mode 1 (Full Development)

---

## Summary

| Mode | Frontend Server | Backend Server | Use Case | HMR | Speed |
|------|----------------|----------------|----------|-----|-------|
| **Full Development** | ✅ localhost:5173 | ✅ localhost:8080 | Daily development | ✅ Yes | ⚡ Fast |
| **Production-like** | ❌ | ✅ localhost:8080 | Testing build | ❌ No | 🐢 Slow |
| **Production** | ❌ | ✅ Cloud Run | Deployment | ❌ No | N/A |

**Default recommendation: Always use Full Development Mode (Mode 1) for frontend work.**

---

## Additional Resources

- [Vite Documentation](https://vitejs.dev/)
- [React Documentation](https://react.dev/)
- [Material-UI Documentation](https://mui.com/)
- [Spring Boot Static Resources](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc.static-content)
