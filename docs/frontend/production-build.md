# Frontend Production Build Guide

## Overview

This document explains how the frontend build integrates with backend deployment and production workflows.

## Critical Understanding

The Inmobiliaria project has a **React frontend embedded within a Spring Boot backend project**. The frontend is:
- Located in `backend/frontend/`
- Built using Vite + TypeScript + React
- Output as static files to `src/main/resources/static/`
- Served by Spring Boot in production

## Build Workflows

### Development Mode (Active Development)

**DO NOT build frontend during development. Use the Vite dev server instead.**

```bash
# Terminal 1 - Backend
mvn spring-boot:run

# Terminal 2 - Frontend Dev Server
cd frontend
npm run dev
```

**Access:** `http://localhost:5173` (Vite dev server with HMR)

### Production Build Workflows

There are **three production build approaches**:

#### Approach 1: Manual Frontend Build + Maven (Current)

**Use case:** Local testing without Docker

```bash
# Step 1: Build frontend
cd frontend
npm run build

# Step 2: Run Spring Boot
cd ..
mvn spring-boot:run
```

**Access:** `http://localhost:8080`

**How it works:**
1. `npm run build` compiles frontend to `src/main/resources/static/`
2. Spring Boot serves these static files from classpath
3. Maven packages static files into the JAR

**Pros:**
- ✅ Simple
- ✅ No Docker required
- ✅ Fast local testing

**Cons:**
- ❌ Manual step (easy to forget)
- ❌ Doesn't match production Docker build

---

#### Approach 2: Manual Frontend Build + Docker Build (Current Production)

**Use case:** Testing production Docker image locally

```bash
# Step 1: Build frontend FIRST
cd frontend
npm run build
cd ..

# Step 2: Build Docker image (includes pre-built frontend)
docker build -t inmobiliaria-app .

# Step 3: Run container
docker run -p 8080:8080 \
  -e GOOGLE_CLOUD_PROJECT=inmobiliaria-adk \
  -e GOOGLE_CLOUD_LOCATION=us-central1 \
  -e GOOGLE_GENAI_USE_VERTEXAI=true \
  inmobiliaria-app
```

**Access:** `http://localhost:8080`

**How it works:**
1. Frontend must be pre-built manually
2. `docker build` copies `src/main/resources/static/` into image
3. Maven builds JAR with static files included
4. Container runs Spring Boot serving embedded frontend

**⚠️ Current Dockerfile (`Dockerfile`):**
```dockerfile
# Does NOT build frontend - expects it pre-built
COPY src ./src
RUN mvn clean package -DskipTests -B
```

**Pros:**
- ✅ Tests exact production container
- ✅ Matches Cloud Run deployment

**Cons:**
- ❌ Manual frontend build step (error-prone)
- ❌ Two separate build commands
- ❌ CI/CD must remember to build frontend first

**⚠️ Critical:** If you forget to build frontend, the Docker image will have stale or missing frontend files!

---

#### Approach 3: Integrated Docker Build (RECOMMENDED)

**Use case:** Automated CI/CD, single build command

```bash
# Single command builds BOTH frontend and backend
docker build -f Dockerfile.integrated -t inmobiliaria-app .

# Run container
docker run -p 8080:8080 \
  -e GOOGLE_CLOUD_PROJECT=inmobiliaria-adk \
  -e GOOGLE_CLOUD_LOCATION=us-central1 \
  -e GOOGLE_GENAI_USE_VERTEXAI=true \
  inmobiliaria-app
```

**Access:** `http://localhost:8080`

**How it works:**
```dockerfile
# Stage 1: Build Frontend
FROM node:20-alpine AS frontend-builder
COPY frontend/ ./
RUN npm ci && npm run build

# Stage 2: Build Backend
FROM maven:3.9-eclipse-temurin-25 AS backend-builder
COPY --from=frontend-builder /app/src/main/resources/static ./src/main/resources/static
RUN mvn clean package -DskipTests -B

# Stage 3: Runtime
FROM eclipse-temurin:25-jre-jammy
COPY --from=backend-builder /app/target/*.jar app.jar
```

**Pros:**
- ✅ Single build command
- ✅ Automated - can't forget frontend build
- ✅ Better CI/CD integration
- ✅ Clear build dependencies
- ✅ Multi-stage build optimization

**Cons:**
- ❌ Slightly longer build time
- ❌ Requires Node.js in Docker build

**To use:** Replace `Dockerfile` with `Dockerfile.integrated`

---

## File Paths & Output

### Frontend Build Configuration (`frontend/vite.config.ts`)

```typescript
export default defineConfig({
  build: {
    outDir: '../src/main/resources/static',  // ← Output location
    emptyOutDir: true,  // Clean before build
  }
});
```

### Output Structure

After `npm run build`:
```
backend/
├── src/main/resources/static/    ← Frontend output here
│   ├── index.html
│   └── assets/
│       ├── index-[hash].js
│       ├── index-[hash].css
│       └── *.svg, *.png, etc.
└── frontend/
    └── src/                       ← Source files (not deployed)
```

### Maven Packaging

When Maven runs `mvn package`:
```
target/
└── gestion-0.0.1-SNAPSHOT.jar
    ├── BOOT-INF/
    │   └── classes/
    │       └── static/            ← Frontend included in JAR
    │           ├── index.html
    │           └── assets/
    └── ... (Java classes, libs, etc.)
```

---

## CI/CD Integration

### Current GitHub Actions Workflow

**Problem:** Frontend build is NOT integrated

```yaml
# Current deploy-cloud-run.yml
- name: Build Docker image
  run: docker build -t $IMAGE_TAG .
```

This expects frontend to be pre-built and committed (❌ BAD PRACTICE).

### Recommended GitHub Actions Workflow

**Option A: Build frontend before Docker**

```yaml
- name: Build Frontend
  working-directory: ./frontend
  run: |
    npm ci
    npm run build

- name: Build Docker Image
  run: docker build -t $IMAGE_TAG .
```

**Option B: Use integrated Dockerfile**

```yaml
- name: Build Docker Image with Integrated Frontend
  run: docker build -f Dockerfile.integrated -t $IMAGE_TAG .
```

**Recommended:** Option B (single build step, clearer dependencies)

---

## Production Deployment Checklist

### Before Deployment

- [ ] **DO NOT** commit `src/main/resources/static/` to Git
  - Add to `.gitignore`: `src/main/resources/static/`
  - Static files should be build artifacts, not source code

- [ ] Choose build approach:
  - [ ] Manual frontend build + Docker (current)
  - [ ] Integrated Dockerfile (recommended)

- [ ] Test locally:
  ```bash
  # Build and test
  cd frontend && npm run build && cd ..
  docker build -t test-image .
  docker run -p 8080:8080 test-image

  # Verify frontend loads at http://localhost:8080
  ```

### Deployment Commands

#### Using Current Dockerfile (Manual Frontend Build)

```bash
# Local
cd frontend && npm run build && cd ..
docker build -t inmobiliaria-app .

# CI/CD must include:
# 1. npm run build
# 2. docker build
```

#### Using Integrated Dockerfile (Recommended)

```bash
# Local
docker build -f Dockerfile.integrated -t inmobiliaria-app .

# CI/CD: Single build command
docker build -f Dockerfile.integrated -t $IMAGE_TAG .
```

---

## Troubleshooting

### Issue: Frontend not loading in production

**Symptoms:**
- Browser shows 404 for JavaScript files
- Blank page or old version of frontend
- Spring Boot serves API but not static files

**Diagnosis:**
```bash
# Check if static files are in JAR
jar tf target/gestion-*.jar | grep static

# Expected output:
# BOOT-INF/classes/static/index.html
# BOOT-INF/classes/static/assets/index-xyz.js
# ...
```

**Solutions:**

1. **Forgot to build frontend:**
   ```bash
   cd frontend
   npm run build
   cd ..
   mvn clean package
   ```

2. **Docker image has stale frontend:**
   ```bash
   # Rebuild frontend AND Docker image
   cd frontend && npm run build && cd ..
   docker build -t inmobiliaria-app .
   ```

3. **Wrong vite.config.ts output path:**
   ```typescript
   // Verify this in frontend/vite.config.ts
   outDir: '../src/main/resources/static'  // ✅ Correct
   outDir: 'dist'                          // ❌ Wrong
   ```

### Issue: Frontend changes not appearing

**Problem:** Built frontend and redeployed, but changes not visible

**Cause:** Browser caching or stale Docker layer

**Solution:**
```bash
# 1. Hard refresh browser
# Mac: Cmd+Shift+R
# Windows/Linux: Ctrl+Shift+R

# 2. Clear Docker build cache
docker build --no-cache -t inmobiliaria-app .

# 3. Verify build actually ran
docker build -t test . 2>&1 | grep "npm run build"
```

### Issue: Different frontend in dev vs production

**Problem:** App works in dev (localhost:5173) but not in production (localhost:8080)

**Cause:** Different build configurations or environment variables

**Diagnosis:**
```bash
# Compare dev and prod environments
echo "Dev API base: $VITE_API_BASE_URL"

# Check production .env.production
cat frontend/.env.production
```

**Solution:**
Ensure `frontend/.env.production` has correct values:
```env
VITE_API_BASE_URL=/api
```

---

## Best Practices

### ✅ DO

1. **Use integrated Dockerfile for production builds**
   - Single build command
   - Automated frontend build
   - Clear dependencies

2. **Add `src/main/resources/static/` to `.gitignore`**
   - Build artifacts shouldn't be in version control
   - Keeps repo clean

3. **Test production build locally before deploying**
   ```bash
   docker build -f Dockerfile.integrated -t test .
   docker run -p 8080:8080 test
   # Visit http://localhost:8080 and test thoroughly
   ```

4. **Use environment-specific frontend configs**
   - `.env.development` - for Vite dev server
   - `.env.production` - for production builds

### ❌ DON'T

1. **Don't commit built frontend files**
   - `src/main/resources/static/` should be in `.gitignore`
   - CI/CD should build fresh each time

2. **Don't forget to build frontend before Docker build** (if using current Dockerfile)
   - Easy to forget
   - Results in stale or missing frontend
   - Use integrated Dockerfile instead

3. **Don't run `npm run build` during active development**
   - Slow feedback loop
   - No hot reload
   - Use Vite dev server instead (`npm run dev`)

4. **Don't skip production testing**
   - Always test Docker image locally before deploying
   - Verify frontend loads correctly
   - Test API integration

---

## Migration Path

### From Current to Integrated Dockerfile

**Step 1:** Test integrated Dockerfile locally
```bash
docker build -f Dockerfile.integrated -t test-integrated .
docker run -p 8080:8080 test-integrated
# Verify frontend loads at http://localhost:8080
```

**Step 2:** Update CI/CD workflow
```yaml
# In .github/workflows/deploy-cloud-run.yml
- name: Build Docker Image
  run: docker build -f Dockerfile.integrated -t $IMAGE_TAG .
```

**Step 3:** Replace Dockerfile
```bash
mv Dockerfile Dockerfile.old
mv Dockerfile.integrated Dockerfile
```

**Step 4:** Update documentation
- Update `DEPLOYMENT-PRODUCTION.md`
- Update team documentation
- Remove manual frontend build steps

---

## Summary

| Approach | Commands | Use Case | Automation |
|----------|----------|----------|------------|
| **Manual Build** | `npm run build` + `mvn spring-boot:run` | Local testing | ❌ Manual |
| **Manual + Docker** | `npm run build` + `docker build` | Production testing | ❌ Manual |
| **Integrated Docker** | `docker build -f Dockerfile.integrated` | Production CI/CD | ✅ Automated |

**Recommendation:** Migrate to **Integrated Docker** for production deployments.

---

**Last Updated:** 2025-10-31
