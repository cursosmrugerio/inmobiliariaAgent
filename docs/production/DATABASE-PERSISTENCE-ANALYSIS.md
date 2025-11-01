# Database Persistence Analysis - Production H2 Configuration

## Executive Summary

**⚠️ CRITICAL FINDING: Data in production is NOT persistent across deployments**

Your current production setup uses H2 file-based database with **ephemeral storage** in Google Cloud Run. This means:

- ❌ Data is **LOST** on every redeployment
- ❌ Data is **LOST** when Cloud Run scales down to zero instances
- ❌ Data is **LOST** when Cloud Run restarts containers
- ✅ Data **DOES persist** in `/app/data/proddb.mv.db` during container lifetime
- ✅ Configuration is correct for **temporary testing** (as documented)

## Current Configuration Analysis

### Development Configuration (application.properties)

```properties
# Development uses file-based H2 with local persistence
spring.datasource.url=jdbc:h2:file:./data/testdb;AUTO_SERVER=TRUE
```

**Storage location:** `./data/testdb.mv.db` (relative to working directory)
**Persistence:** ✅ Data persists between application restarts on local machine
**File location:** `/Users/mike/Desarrollo/compyser/inmobiliaria/backend/data/testdb.mv.db`

### Production Configuration (application-prod.properties)

```properties
# Production uses file-based H2 with container-local persistence
spring.datasource.url=jdbc:h2:file:./data/proddb;DB_CLOSE_DELAY=-1
```

**Storage location:** `./data/proddb.mv.db` (relative to `/app` in Docker)
**Persistence:** ❌ Data is ephemeral (lost on container restart)
**File location:** `/app/data/proddb.mv.db` inside Docker container

### Dockerfile Configuration

```dockerfile
# Creates directory for H2 database files
RUN mkdir -p /app/data && chown -R appuser:appuser /app
```

**Directory created:** `/app/data/`
**Owner:** `appuser:appuser`
**Persistence:** ❌ Not mounted to persistent volume

### Cloud Run Configuration

**Active Spring Profile:** `prod`

**Environment Variables Set:**
- `SPRING_PROFILES_ACTIVE=prod`
- `GOOGLE_CLOUD_PROJECT=inmobiliaria-adk`
- `GOOGLE_CLOUD_LOCATION=us-central1`
- `GOOGLE_GENAI_USE_VERTEXAI=true`
- `JWT_SECRET=<secret>` ✅

**Persistent Volumes:** `null` ❌

**Result:** Cloud Run has **NO persistent volumes** mounted. The `/app/data/` directory exists only in container filesystem (ephemeral).

## Persistence Behavior Comparison

| Aspect | Development (Local) | Production (Cloud Run) |
|--------|-------------------|----------------------|
| **Database** | H2 file-based | H2 file-based |
| **File Location** | `./data/testdb.mv.db` | `/app/data/proddb.mv.db` |
| **Storage Type** | Local disk | Container filesystem |
| **Persists on app restart?** | ✅ YES | ❌ NO |
| **Persists on redeployment?** | ✅ YES | ❌ NO |
| **Persists on scale down?** | N/A | ❌ NO |
| **Suitable for production?** | ✅ For dev/testing | ❌ NO - Testing only |

## When Data is Lost in Production

### Scenario 1: GitHub Actions Deployment (MOST COMMON)
```bash
git push origin main
# → GitHub Actions builds new Docker image
# → Cloud Run deploys new container
# → OLD container is destroyed (with all data)
# → NEW container starts with empty database
```
**Result:** ❌ All data is LOST

### Scenario 2: Cloud Run Auto-Scaling Down to Zero
```
No requests for ~15 minutes
→ Cloud Run scales down to 0 instances (min-instances=0)
→ Container is stopped and destroyed
→ New request arrives
→ Cloud Run creates fresh container
```
**Result:** ❌ All data is LOST

### Scenario 3: Cloud Run Container Restart
```
Container crashes or Cloud Run performs maintenance
→ Container is destroyed
→ New container is created
```
**Result:** ❌ All data is LOST

### Scenario 4: Manual Redeployment
```bash
gcloud run services update inmobiliaria-api --region=us-central1
```
**Result:** ❌ All data is LOST

## What DOES Persist in Current Setup

### During Container Lifetime ✅

Data persists as long as the **same container instance** keeps running:

```
User 1: Creates inmobiliaria → Saved to /app/data/proddb.mv.db
User 2: Lists inmobiliarias → Sees User 1's data ✅
User 3: Updates inmobiliaria → Saved to same file ✅
```

**This works because:** All requests go to the same running container instance.

### What Happens to the File

1. **Container starts:**
   ```bash
   /app/data/ directory is empty
   Spring Boot starts H2 database
   Creates: /app/data/proddb.mv.db
   Flyway runs migrations (creates tables)
   Test data loaded by DataInitializer
   ```

2. **Application runs:**
   ```bash
   Data is written to /app/data/proddb.mv.db
   File exists in container filesystem
   Queries read from the same file
   ```

3. **Container stops (for ANY reason):**
   ```bash
   Container filesystem is DESTROYED
   /app/data/proddb.mv.db is DELETED
   Data is LOST forever
   ```

4. **New container starts:**
   ```bash
   /app/data/ directory is empty again
   Cycle repeats from step 1
   ```

## Why This Is the Current Design

Looking at `application-prod.properties` lines 11-24:

```properties
# ⚠️ WARNING: This is a TEMPORARY configuration for initial deployment testing
#
# LIMITATIONS:
# - Data persists ONLY during container lifetime
# - Data is LOST on: container restart, redeployment, scaling events
# - NOT suitable for production use beyond initial testing
# - Single file locking prevents horizontal scaling
#
# MIGRATION PATH:
# - Use this for initial deployment validation
# - Migrate to PostgreSQL/Supabase for production use
# - See: docs/production/DEPLOYMENT-PRODUCTION.md for PostgreSQL setup
```

**Conclusion:** This configuration is **intentionally temporary** and **documented as such**.

## Google Cloud Run Storage Limitations

Cloud Run is a **stateless** container platform:

### What Cloud Run Provides
- ✅ Ephemeral filesystem (container lifetime only)
- ✅ Environment variables
- ✅ Secrets (via Secret Manager)
- ❌ Persistent volumes (NOT supported)

### What Cloud Run Does NOT Provide
- ❌ Persistent disk storage
- ❌ Mounted volumes that survive container restarts
- ❌ Network-attached storage (NAS)

**Why?** Cloud Run is designed for **stateless applications**. State should be stored in external services (Cloud SQL, Firestore, Cloud Storage, etc.).

## Solutions for Persistent Data

### Option 1: Migrate to PostgreSQL (RECOMMENDED)

Already prepared in `application-prod.properties` lines 53-60:

```properties
# POSTGRESQL CONFIGURATION (for future use)
# Uncomment and use these settings when migrating to production database:
# spring.datasource.url=${DATABASE_URL:jdbc:postgresql://postgres.YOUR_PROJECT_REF.supabase.co:5432/postgres}
# spring.datasource.username=${DATABASE_USERNAME:postgres}
# spring.datasource.password=${DATABASE_PASSWORD}
# spring.datasource.driver-class-name=org.postgresql.Driver
# spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

**Benefits:**
- ✅ Data persists across deployments
- ✅ Automatic backups
- ✅ Horizontal scaling support
- ✅ Production-grade reliability
- ✅ Cloud SQL or Supabase options

**See:** `docs/production/DEPLOYMENT-PRODUCTION.md` for complete migration guide

### Option 2: Use Cloud SQL H2 (NOT RECOMMENDED)

Cloud SQL doesn't support H2. You'd need PostgreSQL, MySQL, or SQL Server.

### Option 3: Keep H2 for Testing Only (CURRENT STATUS)

**Use case:** Temporary deployment testing, demos, development

**Acceptable if:**
- ✅ You understand data will be lost on every deployment
- ✅ You don't need data persistence
- ✅ You're testing the application, not storing real data
- ✅ You plan to migrate to PostgreSQL before real use

**Not acceptable if:**
- ❌ Users are entering real data
- ❌ You need data to survive deployments
- ❌ You're using this for production workloads

## Verification: Does Data Persist Currently?

Let me test the current production behavior:

### Test Plan
1. Create a test record in production
2. Verify it's saved
3. Trigger a new deployment (or wait for container restart)
4. Check if the record still exists

### Expected Result (Based on Analysis)
- ✅ Record will exist immediately after creation
- ✅ Record will persist during container lifetime
- ❌ Record will be LOST after next deployment or container restart

## Recommendation

Based on the documented warnings in `application-prod.properties`, your current setup is:

**Status:** ✅ **Correctly configured for temporary testing**

**Action Required:**
- If this is just for testing/demos: ✅ No action needed, current setup is fine
- If you need persistent data: ❌ Migrate to PostgreSQL/Supabase (see migration guide)

**Timeline:**
- **Keep H2 if:** You're testing the deployment, not storing real data
- **Migrate to PostgreSQL if:** You need data to survive deployments

## Next Steps

### For Temporary Testing (Current Status)
```bash
# No changes needed - current setup is correct for testing
# Just understand data will be lost on each deployment
```

### For Production Use with Persistent Data

1. **Set up PostgreSQL database:**
   - Option A: Supabase (free tier available)
   - Option B: Google Cloud SQL

2. **Update application-prod.properties:**
   ```properties
   # Comment out H2 configuration
   # Uncomment PostgreSQL configuration
   ```

3. **Set GitHub Secrets:**
   ```bash
   gh secret set DATABASE_URL
   gh secret set DATABASE_USERNAME
   gh secret set DATABASE_PASSWORD
   ```

4. **Update deploy-cloud-run.yml:**
   ```yaml
   --set-env-vars="DATABASE_URL=${{ secrets.DATABASE_URL }}" \
   --set-env-vars="DATABASE_USERNAME=${{ secrets.DATABASE_USERNAME }}" \
   --set-env-vars="DATABASE_PASSWORD=${{ secrets.DATABASE_PASSWORD }}"
   ```

5. **Deploy:**
   ```bash
   git commit -m "Migrate to PostgreSQL for persistent storage"
   git push origin main
   ```

## Summary

| Question | Answer |
|----------|--------|
| Does H2 persist data to a file in development? | ✅ YES - `./data/testdb.mv.db` |
| Does H2 persist data to a file in production? | ✅ YES - `/app/data/proddb.mv.db` |
| Does that file survive container restarts? | ❌ NO - file is in container filesystem |
| Does that file survive deployments? | ❌ NO - new container = empty filesystem |
| Is this documented behavior? | ✅ YES - see `application-prod.properties` warnings |
| Is this suitable for production use? | ❌ NO - only for temporary testing |
| What's the fix for production? | Migrate to PostgreSQL/Supabase |

---

**Last Updated:** 2025-10-31
**Cloud Run Service:** `inmobiliaria-api`
**Region:** `us-central1`
**Current Profile:** `prod` (with H2 ephemeral storage)
