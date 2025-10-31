---
name: inmobiliaria-production-deployer
description: Expert guidance for deploying the Inmobiliaria Management System to Google Cloud Run production. Use this skill when the user requests production deployment, wants to set up Cloud Run, configure Supabase PostgreSQL, set up GitHub Actions CI/CD, or needs help with any production deployment tasks for the Inmobiliaria project. This skill provides step-by-step workflows for pre-deployment validation, infrastructure setup, security hardening, and post-deployment verification.
---

# Inmobiliaria Production Deployer

## Overview

Deploy the Inmobiliaria Management System (Java 25 Spring Boot application with Vertex AI integration) to production on Google Cloud Run with Supabase PostgreSQL and GitHub Actions CI/CD. This skill provides comprehensive guidance through security validation, infrastructure setup, file preparation, deployment execution, and verification.

## When to Use This Skill

Invoke this skill when the user:
- Requests production deployment or "deploy to production"
- Mentions Google Cloud Run, Supabase, or production environment
- Needs help with CI/CD setup or GitHub Actions
- Wants to prepare the application for production
- Asks about production configuration or deployment checklist
- Needs security validation before deployment

## Workflow Decision Tree

**Start here to determine the deployment path:**

1. **Is this the first production deployment?**
   - YES → Follow "First-Time Deployment Workflow"
   - NO → Go to step 2

2. **Is this a deployment update/redeployment?**
   - YES → Follow "Update Deployment Workflow"
   - NO → Go to step 3

3. **Is this pre-deployment validation only?**
   - YES → Follow "Pre-Deployment Validation" section only
   - NO → Follow "First-Time Deployment Workflow"

## First-Time Deployment Workflow

Execute these phases in order for initial production deployment:

### Phase 1: Pre-Deployment Validation

**Always start here, even for experienced users.**

Run comprehensive validation checks before any deployment:

```bash
# 1. Code quality checks
mvn fmt:check           # Google Java Style Guide compliance
mvn clean test          # All tests must pass

# 2. Local build verification
mvn clean package       # Verify application builds
docker build -t test-build -f docs/production/Dockerfile .  # Test Docker build

# 3. Review project standards
# Read docs/production/CRITICAL-PRODUCTION-CHECKLIST.md
# Read CLAUDE.md for architecture compliance
```

**Critical Security Validation:**

Check `src/main/java/com/inmobiliaria/gestion/config/SecurityConfig.java`:

1. **Agent Endpoints Authentication** (line ~75):
   ```java
   // ❌ MUST FIX before production:
   .requestMatchers("/api/agent/**").permitAll()  // REMOVE THIS

   // ✅ Change to:
   .requestMatchers("/api/agent/**").authenticated()
   ```

2. **CORS Configuration** (line ~50-60):
   - Remove `localhost` origins
   - Add ONLY production frontend domain(s)

3. **Development Features** in `docs/production/application-prod.properties`:
   ```properties
   # ✅ Verify these are disabled:
   spring.h2.console.enabled=false
   springdoc.swagger-ui.enabled=false
   spring.jpa.show-sql=false
   ```

**Stop if validation fails.** Fix all issues before proceeding.

### Phase 2: Infrastructure Setup

**2.1 Supabase PostgreSQL Database**

Guide the user through:
1. Create Supabase account at https://supabase.com
2. Create new project and note:
   - Project Reference ID (e.g., `abcdefghijklmnop`)
   - Database password (strong, auto-generated)
   - Connection string format: `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres`

3. Test connection from local machine:
   ```bash
   # Install PostgreSQL client if needed
   psql "postgresql://postgres:[PASSWORD]@aws-0-us-east-1.pooler.supabase.com:6543/postgres"
   ```

**2.2 Google Cloud Platform Setup**

Guide through GCP configuration:

```bash
# Set project ID
export GCP_PROJECT_ID="your-project-id"
gcloud config set project $GCP_PROJECT_ID

# Enable required APIs
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com
gcloud services enable aiplatform.googleapis.com
gcloud services enable secretmanager.googleapis.com

# Create Cloud Run service account
gcloud iam service-accounts create inmobiliaria-cloudrun \
  --display-name="Inmobiliaria Cloud Run Service Account"

# Grant Vertex AI permissions
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:inmobiliaria-cloudrun@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/aiplatform.user"

# Create GitHub Actions service account
gcloud iam service-accounts create inmobiliaria-github-actions \
  --display-name="GitHub Actions CI/CD"

# Grant necessary permissions
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:inmobiliaria-github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:inmobiliaria-github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/storage.admin"

gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:inmobiliaria-github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/iam.serviceAccountUser"

# Create and download service account key for GitHub Actions
gcloud iam service-accounts keys create ~/inmobiliaria-github-sa-key.json \
  --iam-account=inmobiliaria-github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com

# Display key content for GitHub Secrets (copy this)
cat ~/inmobiliaria-github-sa-key.json
```

**2.3 GitHub Secrets Configuration**

Guide the user to configure GitHub Secrets at `Settings → Secrets and variables → Actions`:

Required secrets:
- `GCP_PROJECT_ID`: The GCP project ID
- `GCP_SA_KEY`: Full JSON content of service account key
- `DATABASE_URL`: Supabase connection string
- `DATABASE_USERNAME`: `postgres`
- `DATABASE_PASSWORD`: Supabase database password
- `JWT_SECRET`: Generate with `openssl rand -base64 64`

### Phase 3: File Preparation

**3.1 Copy Production Template Files**

Execute these commands from project root:

```bash
# Copy Docker files
cp docs/production/Dockerfile .
cp docs/production/.dockerignore .

# Copy application configuration
cp docs/production/application-prod.properties src/main/resources/

# Copy GitHub workflows
mkdir -p .github/workflows
cp docs/production/github-workflows/test.yml .github/workflows/
cp docs/production/github-workflows/deploy-cloud-run.yml .github/workflows/
```

**3.2 Update Placeholders**

Update `src/main/resources/application-prod.properties`:
```properties
# Replace placeholder with actual Supabase connection string
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres}
```

**3.3 Update SecurityConfig.java**

Make these critical changes:

1. **Fix agent endpoints** (line ~75):
   ```java
   .requestMatchers("/api/agent/**").authenticated()
   ```

2. **Update CORS** (line ~50-60):
   ```java
   .allowedOrigins(
       "https://your-production-domain.com",
       "https://www.your-production-domain.com"
   )
   ```

**3.4 Verify Changes**

```bash
# Run tests with new configuration
mvn clean test

# Verify formatting
mvn fmt:check

# Test Docker build
docker build -t inmobiliaria-test .
docker run -p 8080:8080 --env-file .env.test inmobiliaria-test
```

### Phase 4: Deployment Execution

**4.1 Commit and Push**

```bash
git add .
git commit -m "Configure production deployment

- Add Dockerfile and production configuration
- Configure GitHub Actions workflows
- Update SecurityConfig for production
- Enable agent endpoint authentication

🤖 Generated with Claude Code"

git push origin main
```

**4.2 Monitor Deployment**

Guide the user to:
1. Go to GitHub repository → Actions tab
2. Watch the "Deploy to Google Cloud Run" workflow
3. Monitor each step for errors

**4.3 Handle Deployment Issues**

If deployment fails, check:
- GitHub Actions logs for specific error
- Cloud Run deployment logs: `gcloud run services logs read inmobiliaria-api --region=us-central1`
- Verify all GitHub Secrets are set correctly
- Check Flyway migration logs

### Phase 5: Post-Deployment Verification

**5.1 Get Service URL**

```bash
gcloud run services describe inmobiliaria-api \
  --region=us-central1 \
  --format='value(status.url)'
```

**5.2 Health Check**

```bash
export API_URL="https://inmobiliaria-api-xxxxx-uc.a.run.app"
curl $API_URL/actuator/health

# Expected: {"status":"UP"}
```

**5.3 Test Authentication**

```bash
# Register test user
curl -X POST $API_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "Admin123!",
    "name": "Admin User",
    "role": "ADMIN"
  }'

# Login and get token
TOKEN=$(curl -s -X POST $API_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "Admin123!"
  }' | jq -r '.token')

echo "Token: $TOKEN"
```

**5.4 Test Protected Endpoints**

```bash
# Test CRUD endpoint
curl -X GET $API_URL/api/inmobiliarias \
  -H "Authorization: Bearer $TOKEN"

# Test agent endpoint (verify authentication required)
curl -X POST $API_URL/api/agent/inmobiliarias \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message": "List all agencies"}'
```

**5.5 Verify Database Migrations**

Check Cloud Run logs for Flyway migration success:
```bash
gcloud run services logs read inmobiliaria-api \
  --region=us-central1 \
  --limit=100 | grep -i flyway
```

### Phase 6: Security Final Check

Run through the complete checklist from `docs/production/CRITICAL-PRODUCTION-CHECKLIST.md`:

**Security Items:**
- [ ] JWT secret is strong (64+ characters) in GitHub Secrets
- [ ] Database password is strong in GitHub Secrets
- [ ] No credentials committed to git
- [ ] H2 console disabled in production config
- [ ] Swagger UI disabled
- [ ] Agent endpoints require authentication
- [ ] CORS configured with production domain only
- [ ] SQL logging disabled

**Configuration Items:**
- [ ] Supabase connection string correct
- [ ] All environment variables configured
- [ ] Service accounts have correct permissions
- [ ] Vertex AI API enabled

**Monitoring Setup:**
- [ ] Set up billing alerts in GCP
- [ ] Configure uptime monitoring
- [ ] Set up error alerts
- [ ] Plan daily log reviews for first week

## Update Deployment Workflow

For subsequent deployments after the first:

### Quick Update Process

```bash
# 1. Make code changes
# 2. Run validation
mvn clean test
mvn fmt:check

# 3. Commit and push to main
git add .
git commit -m "Your update message"
git push origin main

# 4. GitHub Actions automatically deploys
# 5. Verify deployment at the service URL
```

### Configuration Updates

If updating environment variables or secrets:

```bash
# Update via GitHub Secrets (preferred)
# OR update Cloud Run service directly:
gcloud run services update inmobiliaria-api \
  --region=us-central1 \
  --set-env-vars="NEW_VAR=value"
```

### Rollback Procedure

If deployment has issues:

```bash
# List revisions
gcloud run revisions list --service=inmobiliaria-api --region=us-central1

# Rollback to previous revision
gcloud run services update-traffic inmobiliaria-api \
  --region=us-central1 \
  --to-revisions=inmobiliaria-api-00001-xxx=100
```

## Technology Stack Reference

Current versions (as of project):
- **Java**: 25
- **Spring Boot**: 3.5.7
- **Maven**: 3.9
- **Google ADK**: 0.3.0
- **Springdoc OpenAPI**: 2.5.0
- **Model**: Gemini 2.0 Flash (via Vertex AI)

## Common Issues and Solutions

### Issue: "Application failed to start"

Check Cloud Run logs:
```bash
gcloud run services logs read inmobiliaria-api --region=us-central1 --limit=50
```

Common causes:
- Missing environment variables
- Database connection failure
- Flyway migration errors
- Port binding issues

### Issue: "Database connection timeout"

Verify:
1. Supabase connection string is correct
2. Connection pooler is used (`:6543` port)
3. Database password is correct in GitHub Secrets
4. Cloud Run service account has network access

### Issue: "Vertex AI authentication failed"

Verify:
1. Service account has `roles/aiplatform.user`
2. Vertex AI API is enabled
3. Environment variables are set:
   - `GOOGLE_CLOUD_PROJECT`
   - `GOOGLE_CLOUD_LOCATION`
   - `GOOGLE_GENAI_USE_VERTEXAI=true`

### Issue: "Agent endpoints return 401"

This is expected if authentication is properly configured. Frontend must send JWT token:
```javascript
Authorization: Bearer ${token}
```

### Issue: "GitHub Actions deployment fails"

Check:
1. All GitHub Secrets are configured
2. Service account JSON is valid
3. GCP APIs are enabled
4. Service account has correct permissions

## Resources

### Documentation References

Key documentation files in the project:
- `docs/production/CRITICAL-PRODUCTION-CHECKLIST.md` - Security checklist
- `docs/production/DEPLOYMENT-PRODUCTION.md` - Complete deployment guide
- `docs/production/CONFIGURATION-COMPARISON.md` - Environment comparison
- `CLAUDE.md` - Project architecture and standards

### Quick Commands Reference

See `references/quick-commands.md` for a comprehensive list of frequently-used deployment commands.

## Best Practices

**Before Every Deployment:**
1. Run full test suite (`mvn clean test`)
2. Verify code formatting (`mvn fmt:check`)
3. Review CRITICAL-PRODUCTION-CHECKLIST.md
4. Test Docker build locally
5. Verify all secrets are configured

**After Every Deployment:**
1. Test health endpoint
2. Test authentication flow
3. Verify protected endpoints require auth
4. Check Cloud Run logs for errors
5. Monitor for first 30 minutes

**Security:**
- Never commit credentials or secrets
- Rotate JWT secret every 90 days
- Monitor API usage for anomalies
- Keep dependencies updated
- Review security logs weekly

**Cost Management:**
- Monitor Vertex AI usage (Gemini 2.0 Flash costs)
- Set up billing alerts
- Use Cloud Run min instances = 0 for dev/staging
- Consider request quotas for agent endpoints
