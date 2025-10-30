# Production Deployment Guide - Sistema de Gestión Inmobiliaria

This guide provides step-by-step instructions for deploying the Inmobiliaria Management System to production on **Google Cloud Run** with **Supabase PostgreSQL** and **GitHub Actions CI/CD**.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Environment Setup](#environment-setup)
4. [Supabase Database Setup](#supabase-database-setup)
5. [Google Cloud Project Setup](#google-cloud-project-setup)
6. [Application Configuration](#application-configuration)
7. [Docker Containerization](#docker-containerization)
8. [GitHub Actions CI/CD Pipeline](#github-actions-cicd-pipeline)
9. [Google Cloud Run Deployment](#google-cloud-run-deployment)
10. [Database Migration](#database-migration)
11. [Security Hardening](#security-hardening)
12. [Verification & Testing](#verification-and-testing)
13. [Post-Deployment Tasks](#post-deployment-tasks)
14. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before starting the deployment process, ensure you have:

### Accounts
- ✅ **Google Cloud Platform** account with billing enabled
- ✅ **Supabase** account (free tier available at [supabase.com](https://supabase.com))
- ✅ **GitHub** account with repository access
- ✅ **Domain name** (optional, for custom domain)

### Local Tools
```bash
# Verify installed tools
gcloud --version          # Google Cloud SDK
docker --version          # Docker Engine
mvn --version            # Apache Maven 3.8+
java --version           # Java 25
git --version            # Git
```

### Installation (if needed)
- **gcloud CLI**: https://cloud.google.com/sdk/docs/install
- **Docker**: https://docs.docker.com/get-docker/
- **Maven**: https://maven.apache.org/install.html

---

## Local Development Setup

Before deploying to production, you should set up and test the application locally with Vertex AI integration.

### 1. Create Google Cloud Service Account for Development

```bash
# Set your development project
export DEV_PROJECT_ID="inmobiliaria-adk"  # Use your dev project ID
gcloud config set project $DEV_PROJECT_ID

# Create service account for local development
gcloud iam service-accounts create inmobiliaria-dev \
  --display-name="Inmobiliaria Development Service Account"

# Grant Vertex AI permissions
gcloud projects add-iam-policy-binding $DEV_PROJECT_ID \
  --member="serviceAccount:inmobiliaria-dev@${DEV_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/aiplatform.user"

# Create and download credentials key
gcloud iam service-accounts keys create ~/inmobiliaria-service-account-key.json \
  --iam-account=inmobiliaria-dev@${DEV_PROJECT_ID}.iam.gserviceaccount.com

echo "✅ Credentials saved to: ~/inmobiliaria-service-account-key.json"
```

### 2. Configure Local Environment

The project includes a `run-agent.sh` script that sets up all required environment variables:

```bash
#!/bin/bash
# Run Inmobiliaria Agent Application with proper Vertex AI configuration

export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1

# Run the application
mvn spring-boot:run
```

### 3. Test Local Development

```bash
# Make script executable (if not already)
chmod +x run-agent.sh

# Start application with Vertex AI configuration
./run-agent.sh

# In another terminal, test the agent endpoints
curl -X POST http://localhost:8080/api/agent/inmobiliarias \
  -H "Content-Type: application/json" \
  -d '{"message": "List all agencies"}'
```

### 4. Development vs Production Configuration

| Configuration | Development | Production |
|--------------|-------------|------------|
| **Authentication** | File-based credentials | Workload Identity |
| **Credentials File** | `~/inmobiliaria-service-account-key.json` | Not used |
| **Project ID** | `inmobiliaria-adk` | From GitHub Secret |
| **Region** | `us-central1` | `us-central1` |

**Important Notes:**
- ⚠️ The credentials file is **only for local development**
- ⚠️ Never commit `*-key.json` or `credentials.json` files (already in `.gitignore`)
- ⚠️ Production uses **Workload Identity** - no credential files needed
- ℹ️ See `docs/production/CONFIGURATION-COMPARISON.md` for detailed comparison

---

## Environment Setup

### 1. Clone Repository (if not already done)

```bash
git clone <your-repo-url>
cd inmobiliaria/backend
```

### 2. Verify Local Build

```bash
# Clean build
mvn clean package -DskipTests

# Run tests
mvn test

# Verify build artifact
ls -lh target/gestion-0.0.1-SNAPSHOT.jar
```

---

## Supabase Database Setup

### 1. Create Supabase Project

1. Go to [https://supabase.com/dashboard](https://supabase.com/dashboard)
2. Click **"New Project"**
3. Fill in project details:
   - **Name**: `inmobiliaria-prod`
   - **Database Password**: Generate a strong password (save it securely!)
   - **Region**: Choose closest to your users (e.g., `us-east-1`)
4. Click **"Create new project"** (takes ~2 minutes)

### 2. Get Database Connection Details

Once the project is ready:

1. Go to **Project Settings** → **Database**
2. Scroll to **Connection String** section
3. Copy the **URI** (JDBC format):

```
postgresql://postgres.[PROJECT-REF].supabase.co:5432/postgres
```

4. Note your credentials:
   - **Host**: `postgres.[PROJECT-REF].supabase.co`
   - **Port**: `5432`
   - **Database**: `postgres`
   - **User**: `postgres`
   - **Password**: [your database password]

### 3. Configure Supabase Connection Pooler (Recommended)

For production workloads, use Supabase's connection pooler:

1. In **Database Settings**, find **Connection Pooling**
2. Use the **Transaction Mode** pooler:
   - **Host**: `[PROJECT-REF].pooler.supabase.com`
   - **Port**: `5432`

### 4. Create Application Database User (Optional but Recommended)

```sql
-- Connect to Supabase SQL Editor
-- Create dedicated application user
CREATE USER inmobiliaria_app WITH PASSWORD 'your-secure-password';
GRANT ALL PRIVILEGES ON DATABASE postgres TO inmobiliaria_app;
GRANT ALL PRIVILEGES ON SCHEMA public TO inmobiliaria_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO inmobiliaria_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO inmobiliaria_app;
```

---

## Google Cloud Project Setup

### 1. Create GCP Project

```bash
# Set variables
export GCP_PROJECT_ID="inmobiliaria-prod"
export GCP_REGION="us-central1"
export SERVICE_NAME="inmobiliaria-api"

# Create project
gcloud projects create $GCP_PROJECT_ID --name="Inmobiliaria Production"

# Set as active project
gcloud config set project $GCP_PROJECT_ID

# Enable billing (required - do this via console)
echo "⚠️  Enable billing at: https://console.cloud.google.com/billing"
```

### 2. Enable Required APIs

```bash
# Enable required Google Cloud APIs
gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  containerregistry.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  aiplatform.googleapis.com \
  compute.googleapis.com

# Verify APIs are enabled
gcloud services list --enabled
```

### 3. Enable Vertex AI

```bash
# Vertex AI for Gemini 2.0 Flash model
gcloud services enable aiplatform.googleapis.com

# Verify Vertex AI
gcloud ai models list --region=$GCP_REGION
```

### 4. Create Service Account for Cloud Run

```bash
# Create service account
gcloud iam service-accounts create inmobiliaria-cloudrun \
  --display-name="Inmobiliaria Cloud Run Service Account"

export SA_EMAIL="inmobiliaria-cloudrun@${GCP_PROJECT_ID}.iam.gserviceaccount.com"

# Grant necessary permissions
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/aiplatform.user"

gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/secretmanager.secretAccessor"

# Verify service account
gcloud iam service-accounts describe $SA_EMAIL
```

### 5. Create GitHub Service Account (for CI/CD)

```bash
# Create service account for GitHub Actions
gcloud iam service-accounts create github-actions \
  --display-name="GitHub Actions Deployer"

export GITHUB_SA_EMAIL="github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com"

# Grant deployment permissions
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:${GITHUB_SA_EMAIL}" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:${GITHUB_SA_EMAIL}" \
  --role="roles/storage.admin"

gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:${GITHUB_SA_EMAIL}" \
  --role="roles/iam.serviceAccountUser"

# Create and download key for GitHub Actions
gcloud iam service-accounts keys create github-actions-key.json \
  --iam-account=$GITHUB_SA_EMAIL

echo "✅ Save github-actions-key.json securely - you'll add it to GitHub Secrets"
```

### 6. Setup Google Secret Manager

```bash
# Create secrets for sensitive configuration
echo -n "your-supabase-db-password" | \
  gcloud secrets create db-password --data-file=-

echo -n "$(openssl rand -base64 32)" | \
  gcloud secrets create jwt-secret --data-file=-

# Grant Cloud Run service account access to secrets
gcloud secrets add-iam-policy-binding db-password \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding jwt-secret \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/secretmanager.secretAccessor"

# Verify secrets
gcloud secrets list
```

---

## Application Configuration

### 1. Create Production Properties File

Create `src/main/resources/application-prod.properties`:

```properties
# ============================================
# PRODUCTION CONFIGURATION
# ============================================

# Application
spring.application.name=inmobiliaria-gestion
server.port=${PORT:8080}

# Profile
spring.profiles.active=prod

# Database - Supabase PostgreSQL
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://postgres.[PROJECT-REF].supabase.co:5432/postgres}
spring.datasource.username=${DATABASE_USERNAME:postgres}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# Flyway Migration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.validate-on-migrate=true

# Security - JWT
app.security.jwt.secret=${JWT_SECRET}
app.security.jwt.expiration-seconds=${JWT_EXPIRATION_SECONDS:3600}

# Google Vertex AI
google.genai.use.vertexai=${GOOGLE_GENAI_USE_VERTEXAI:true}
google.cloud.project=${GOOGLE_CLOUD_PROJECT}
google.cloud.location=${GOOGLE_CLOUD_LOCATION:us-central1}

# Agent Configuration
agent.model=gemini-2.0-flash
agent.name=inmobiliaria-assistant
agent.session.timeout=3600

# Virtual Threads
spring.threads.virtual.enabled=true

# Actuator (Health Checks)
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.health.livenessstate.enabled=true
management.health.readinessstate.enabled=true

# OpenAPI/Swagger (disable in production or secure)
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false

# H2 Console (MUST be disabled in production)
spring.h2.console.enabled=false

# Logging
logging.level.root=INFO
logging.level.com.inmobiliaria.gestion=INFO
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

### 2. Update Security Configuration for Production

Update `src/main/java/com/inmobiliaria/gestion/config/SecurityConfig.java`:

```java
// Add production frontend URL to CORS
.allowedOrigins(
    "http://localhost:5173",
    "http://localhost:8080",
    "https://your-production-domain.com",  // ADD THIS
    "https://inmobiliaria-prod.web.app"     // If using Firebase Hosting
)
```

---

## Docker Containerization

### 1. Create Dockerfile

The Dockerfile is already created at the root of the project. Review it:

```dockerfile
# Multi-stage build for optimized production image
# See: /Dockerfile
```

### 2. Create .dockerignore

The `.dockerignore` file is already created. Verify it includes:

```
# See: /.dockerignore
```

### 3. Build Docker Image Locally

```bash
# Build image
docker build -t inmobiliaria-api:latest .

# Test image locally
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL="jdbc:postgresql://host.docker.internal:5432/testdb" \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=yourpassword \
  -e JWT_SECRET=test-secret \
  -e GOOGLE_CLOUD_PROJECT=inmobiliaria-prod \
  -e GOOGLE_GENAI_USE_VERTEXAI=false \
  inmobiliaria-api:latest

# Test health endpoint
curl http://localhost:8080/actuator/health
```

---

## GitHub Actions CI/CD Pipeline

### 1. Create GitHub Repository Secrets

Go to your GitHub repository → **Settings** → **Secrets and variables** → **Actions**

Add the following secrets:

| Secret Name | Value | Description |
|------------|-------|-------------|
| `GCP_PROJECT_ID` | `inmobiliaria-prod` | Your GCP project ID |
| `GCP_SA_KEY` | [contents of github-actions-key.json] | Service account JSON key |
| `DATABASE_URL` | `jdbc:postgresql://...supabase.co:5432/postgres` | Supabase connection string |
| `DATABASE_USERNAME` | `postgres` | Database username |
| `DATABASE_PASSWORD` | [your db password] | Supabase database password |
| `JWT_SECRET` | [generate with: `openssl rand -base64 32`] | JWT signing secret |

### 2. Create Test Workflow

The GitHub Actions workflow file `.github/workflows/test.yml` has been created.

### 3. Create Deployment Workflow

The deployment workflow file `.github/workflows/deploy-cloud-run.yml` has been created.

### 4. Verify Workflows

```bash
# Commit new files
git add .github/workflows/
git commit -m "Add GitHub Actions CI/CD workflows"
git push origin main

# Check workflow status
# Go to: https://github.com/[your-org]/[your-repo]/actions
```

---

## Google Cloud Run Deployment

### Option A: Deploy via GitHub Actions (Recommended)

Once workflows are set up, every push to `main` branch will:
1. Run tests
2. Build Docker image
3. Push to Google Container Registry
4. Deploy to Cloud Run

### Option B: Manual Deployment

```bash
# Set variables
export GCP_PROJECT_ID="inmobiliaria-prod"
export SERVICE_NAME="inmobiliaria-api"
export REGION="us-central1"
export IMAGE="gcr.io/${GCP_PROJECT_ID}/${SERVICE_NAME}:latest"

# Build and push image
gcloud builds submit --tag $IMAGE

# Deploy to Cloud Run
gcloud run deploy $SERVICE_NAME \
  --image=$IMAGE \
  --region=$REGION \
  --platform=managed \
  --allow-unauthenticated \
  --service-account=inmobiliaria-cloudrun@${GCP_PROJECT_ID}.iam.gserviceaccount.com \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars="GOOGLE_CLOUD_PROJECT=${GCP_PROJECT_ID}" \
  --set-env-vars="GOOGLE_CLOUD_LOCATION=${REGION}" \
  --set-env-vars="GOOGLE_GENAI_USE_VERTEXAI=true" \
  --set-env-vars="DATABASE_URL=jdbc:postgresql://postgres.[PROJECT-REF].supabase.co:5432/postgres" \
  --set-env-vars="DATABASE_USERNAME=postgres" \
  --set-secrets="DATABASE_PASSWORD=db-password:latest" \
  --set-secrets="JWT_SECRET=jwt-secret:latest" \
  --memory=1Gi \
  --cpu=1 \
  --timeout=300s \
  --max-instances=10 \
  --min-instances=1 \
  --port=8080

# Get service URL
gcloud run services describe $SERVICE_NAME \
  --region=$REGION \
  --format='value(status.url)'
```

Your API will be available at: `https://inmobiliaria-api-[hash]-uc.a.run.app`

---

## Database Migration

### 1. Verify Flyway Migrations

```bash
# Check migration files
ls -la src/main/resources/db/migration/

# Expected files:
# V1__Initial_schema.sql
# V2__create_inmobiliarias_table.sql
# V3__create_propiedades_table.sql
# V4__create_personas_table.sql
# V5__create_users_table.sql
```

### 2. Run Migrations on First Deployment

Migrations run automatically on application startup when `spring.flyway.enabled=true`.

Check logs:

```bash
# View Cloud Run logs
gcloud run services logs read $SERVICE_NAME \
  --region=$REGION \
  --limit=50

# Look for Flyway migration logs:
# "Successfully applied 5 migration(s)"
```

### 3. Verify Database Schema

```sql
-- Connect to Supabase SQL Editor
-- Check tables
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public';

-- Expected tables:
-- users, inmobiliarias, propiedades, personas, flyway_schema_history
```

---

## Security Hardening

### 1. Disable Development Features

Verify in `application-prod.properties`:

```properties
# H2 Console DISABLED
spring.h2.console.enabled=false

# Swagger UI DISABLED (or secure with authentication)
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

### 2. Secure Agent Endpoints

Update `SecurityConfig.java` to require authentication for agent endpoints:

```java
// Remove this temporary public access:
// .requestMatchers("/api/agent/**").permitAll()

// Add authentication requirement:
.requestMatchers("/api/agent/**").authenticated()
```

### 3. Configure CORS for Production

Update allowed origins in `SecurityConfig.java`:

```java
.allowedOrigins(
    "https://your-production-domain.com",
    "https://your-frontend.web.app"
)
```

### 4. Rotate JWT Secret

```bash
# Generate new strong secret
openssl rand -base64 64

# Update in Google Secret Manager
echo -n "your-new-secret" | gcloud secrets versions add jwt-secret --data-file=-
```

### 5. Configure Cloud Run IAM

```bash
# If API should be public (for frontend access)
gcloud run services add-iam-policy-binding $SERVICE_NAME \
  --region=$REGION \
  --member="allUsers" \
  --role="roles/run.invoker"

# If API should be private (requires authentication)
gcloud run services remove-iam-policy-binding $SERVICE_NAME \
  --region=$REGION \
  --member="allUsers" \
  --role="roles/run.invoker"
```

---

## Verification and Testing

### 1. Health Check

```bash
export API_URL="https://inmobiliaria-api-[hash]-uc.a.run.app"

# Check health
curl $API_URL/actuator/health

# Expected response:
# {"status":"UP"}
```

### 2. Test Authentication

```bash
# Register new user
curl -X POST $API_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "Admin123!",
    "name": "Admin User",
    "role": "ADMIN"
  }'

# Login
curl -X POST $API_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "Admin123!"
  }'

# Save the token from response
export TOKEN="eyJhbGc..."
```

### 3. Test Protected Endpoints

```bash
# Test inmobiliarias endpoint
curl -X GET $API_URL/api/inmobiliarias \
  -H "Authorization: Bearer $TOKEN"

# Test agent endpoint
curl -X POST $API_URL/api/agent/inmobiliarias \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "List all agencies"
  }'
```

### 4. Load Testing (Optional)

```bash
# Install Apache Bench
# apt-get install apache2-utils  (Linux)
# brew install httpd  (Mac)

# Simple load test
ab -n 1000 -c 10 \
  -H "Authorization: Bearer $TOKEN" \
  $API_URL/actuator/health
```

---

## Post-Deployment Tasks

### 1. Setup Custom Domain (Optional)

```bash
# Map custom domain to Cloud Run
gcloud run domain-mappings create \
  --service=$SERVICE_NAME \
  --domain=api.yourdomain.com \
  --region=$REGION

# Follow DNS configuration instructions provided
```

### 2. Configure Monitoring

```bash
# Enable Cloud Monitoring
gcloud services enable monitoring.googleapis.com

# Create uptime check
gcloud monitoring uptime create uptime-check \
  --display-name="Inmobiliaria API Health Check" \
  --resource-type=uptime-url \
  --host=$API_URL \
  --path=/actuator/health
```

### 3. Setup Alerts

Go to Cloud Console → **Monitoring** → **Alerting** and create alerts for:
- High error rate (>5%)
- High latency (>2s p95)
- Low availability (<99%)
- High memory usage (>80%)

### 4. Database Backups

Supabase automatically backs up your database. Verify:
1. Go to **Project Settings** → **Database**
2. Check **Backup Settings**
3. Enable **Point-in-time Recovery** (paid plans)

### 5. Update Frontend Configuration

Update your frontend environment variables:

```javascript
// .env.production
VITE_API_URL=https://inmobiliaria-api-[hash]-uc.a.run.app
```

---

## Troubleshooting

### Issue: Cloud Run service fails to start

**Check logs:**
```bash
gcloud run services logs read $SERVICE_NAME --region=$REGION --limit=100
```

**Common causes:**
- Missing environment variables
- Database connection failure
- Flyway migration errors

### Issue: Database connection timeout

**Solution:**
1. Verify Supabase connection string
2. Check if Supabase database is active
3. Ensure Cloud Run service account has network access
4. Try using Supabase connection pooler

### Issue: Vertex AI authentication fails

**Check:**
```bash
# Verify service account has Vertex AI permissions
gcloud projects get-iam-policy $GCP_PROJECT_ID \
  --flatten="bindings[].members" \
  --filter="bindings.members:serviceAccount:inmobiliaria-cloudrun@*"
```

**Solution:**
```bash
# Grant Vertex AI User role
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:inmobiliaria-cloudrun@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/aiplatform.user"
```

### Issue: 403 Forbidden on agent endpoints

**Cause:** Workload Identity not properly configured

**Solution:**
```bash
# Re-bind service account to Cloud Run
gcloud run services update $SERVICE_NAME \
  --region=$REGION \
  --service-account=inmobiliaria-cloudrun@${GCP_PROJECT_ID}.iam.gserviceaccount.com
```

### Issue: High memory usage / OOM errors

**Solution:**
```bash
# Increase Cloud Run memory
gcloud run services update $SERVICE_NAME \
  --region=$REGION \
  --memory=2Gi
```

### Issue: Slow cold starts

**Solutions:**
1. Increase minimum instances:
```bash
gcloud run services update $SERVICE_NAME \
  --region=$REGION \
  --min-instances=1
```

2. Use startup CPU boost:
```bash
gcloud run services update $SERVICE_NAME \
  --region=$REGION \
  --cpu-boost
```

### Issue: GitHub Actions deployment fails

**Check:**
1. Verify all GitHub secrets are set correctly
2. Check service account permissions
3. Review workflow logs in GitHub Actions tab

### Getting Help

- **Cloud Run Documentation**: https://cloud.google.com/run/docs
- **Supabase Documentation**: https://supabase.com/docs
- **Spring Boot Documentation**: https://docs.spring.io/spring-boot/
- **Project Issues**: [Your GitHub Issues URL]

---

## Checklist

Use this checklist to track your deployment progress:

- [ ] Supabase project created and database credentials saved
- [ ] GCP project created with billing enabled
- [ ] Required GCP APIs enabled
- [ ] Service accounts created (Cloud Run + GitHub Actions)
- [ ] Google Secret Manager configured
- [ ] `application-prod.properties` created
- [ ] GitHub repository secrets configured
- [ ] Docker image builds successfully
- [ ] GitHub Actions workflows created
- [ ] First deployment to Cloud Run successful
- [ ] Database migrations completed
- [ ] Health check endpoint responding
- [ ] Authentication working (login/register)
- [ ] Agent endpoints tested with Vertex AI
- [ ] H2 console disabled
- [ ] Swagger UI disabled or secured
- [ ] CORS configured for production frontend
- [ ] Custom domain mapped (optional)
- [ ] Monitoring and alerts configured
- [ ] Frontend updated with production API URL

---

**Congratulations!** Your Inmobiliaria Management System is now running in production. 🚀

For questions or issues, refer to the [Troubleshooting](#troubleshooting) section or check the project documentation.
