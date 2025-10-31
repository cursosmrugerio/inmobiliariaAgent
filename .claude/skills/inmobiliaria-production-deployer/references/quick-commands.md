# Quick Commands Reference

Frequently-used deployment and management commands for the Inmobiliaria production environment.

## Pre-Deployment Validation

```bash
# Code quality checks
mvn fmt:check                    # Verify Google Java Style Guide compliance
mvn fmt:format                   # Auto-format code
mvn clean test                   # Run all tests
mvn clean package                # Build JAR

# Docker build test
docker build -t inmobiliaria-test -f docs/production/Dockerfile .
docker run -p 8080:8080 --env-file .env.test inmobiliaria-test

# Security checks
git ls-files | grep -E 'credentials|key\.json|\.env'  # Should return nothing
```

## Google Cloud Platform

### Project Setup

```bash
# Set active project
export GCP_PROJECT_ID="your-project-id"
gcloud config set project $GCP_PROJECT_ID

# Enable required APIs
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com
gcloud services enable aiplatform.googleapis.com
gcloud services enable secretmanager.googleapis.com
```

### Service Accounts

```bash
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

# Grant deployment permissions
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:inmobiliaria-github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:inmobiliaria-github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/storage.admin"

gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:inmobiliaria-github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/iam.serviceAccountUser"

# Create service account key for GitHub Actions
gcloud iam service-accounts keys create ~/inmobiliaria-github-sa-key.json \
  --iam-account=inmobiliaria-github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com

# Display key (for copying to GitHub Secrets)
cat ~/inmobiliaria-github-sa-key.json
```

## Cloud Run Management

### Deployment

```bash
# Get service URL
gcloud run services describe inmobiliaria-api \
  --region=us-central1 \
  --format='value(status.url)'

# View service details
gcloud run services describe inmobiliaria-api \
  --region=us-central1

# View environment variables
gcloud run services describe inmobiliaria-api \
  --region=us-central1 \
  --format="yaml(spec.template.spec.containers[0].env)"

# Update environment variable
gcloud run services update inmobiliaria-api \
  --region=us-central1 \
  --set-env-vars="KEY=value"

# Update service configuration
gcloud run services update inmobiliaria-api \
  --region=us-central1 \
  --memory=2Gi \
  --cpu=2 \
  --max-instances=20
```

### Logs and Monitoring

```bash
# View recent logs
gcloud run services logs read inmobiliaria-api \
  --region=us-central1 \
  --limit=100

# Stream logs in real-time
gcloud run services logs tail inmobiliaria-api \
  --region=us-central1

# Search for specific error
gcloud run services logs read inmobiliaria-api \
  --region=us-central1 \
  --limit=500 | grep -i "error"

# Check Flyway migrations
gcloud run services logs read inmobiliaria-api \
  --region=us-central1 \
  --limit=100 | grep -i "flyway"

# Check Vertex AI calls
gcloud run services logs read inmobiliaria-api \
  --region=us-central1 \
  --limit=100 | grep -i "vertex"
```

### Revisions and Rollback

```bash
# List all revisions
gcloud run revisions list \
  --service=inmobiliaria-api \
  --region=us-central1

# Describe specific revision
gcloud run revisions describe inmobiliaria-api-00001-abc \
  --region=us-central1

# Rollback to previous revision
gcloud run services update-traffic inmobiliaria-api \
  --region=us-central1 \
  --to-revisions=inmobiliaria-api-00001-abc=100

# Split traffic between revisions (canary deployment)
gcloud run services update-traffic inmobiliaria-api \
  --region=us-central1 \
  --to-revisions=inmobiliaria-api-00002-def=10,inmobiliaria-api-00001-abc=90
```

## Testing and Verification

### Health Checks

```bash
# Set API URL
export API_URL="https://inmobiliaria-api-xxxxx-uc.a.run.app"

# Health check
curl $API_URL/actuator/health

# Detailed health (requires auth)
curl $API_URL/actuator/health \
  -H "Authorization: Bearer $TOKEN"

# Check info endpoint
curl $API_URL/actuator/info
```

### Authentication Testing

```bash
# Register user
curl -X POST $API_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "Admin123!",
    "name": "Admin User",
    "role": "ADMIN"
  }'

# Login and extract token
TOKEN=$(curl -s -X POST $API_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "Admin123!"
  }' | jq -r '.token')

echo "Token: $TOKEN"

# Verify token works
curl -X GET $API_URL/api/inmobiliarias \
  -H "Authorization: Bearer $TOKEN"
```

### Endpoint Testing

```bash
# Test inmobiliarias CRUD
curl -X GET $API_URL/api/inmobiliarias \
  -H "Authorization: Bearer $TOKEN"

curl -X POST $API_URL/api/inmobiliarias \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Agency",
    "rfc": "TEST123456ABC",
    "email": "test@agency.com",
    "telefono": "555-1234"
  }'

# Test agent endpoint
curl -X POST $API_URL/api/agent/inmobiliarias \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "List all agencies"
  }'

# Test propiedades endpoint
curl -X GET $API_URL/api/propiedades \
  -H "Authorization: Bearer $TOKEN"

# Test personas endpoint
curl -X GET $API_URL/api/personas \
  -H "Authorization: Bearer $TOKEN"
```

## Database Management

### Supabase Connection

```bash
# Connect to Supabase PostgreSQL
psql "postgresql://postgres:[PASSWORD]@aws-0-us-east-1.pooler.supabase.com:6543/postgres"

# Execute SQL query
psql "postgresql://postgres:[PASSWORD]@aws-0-us-east-1.pooler.supabase.com:6543/postgres" \
  -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"

# Check table structure
psql "postgresql://postgres:[PASSWORD]@aws-0-us-east-1.pooler.supabase.com:6543/postgres" \
  -c "\d inmobiliarias"
```

## Security Commands

### Generate Secrets

```bash
# Generate strong JWT secret (64+ characters)
openssl rand -base64 64

# Generate random password
openssl rand -base64 32
```

### Check for Exposed Credentials

```bash
# Check git history for credentials
git log --all --full-history --source -- credentials.json
git log --all --full-history --source -- "*.json" | grep -i "key"

# Verify .gitignore
cat .gitignore | grep -E "credentials|key\.json|\.env"

# Check for files that should not be committed
git ls-files | grep -E "credentials|key\.json|\.env"
```

## Docker Commands

### Local Testing

```bash
# Build Docker image
docker build -t inmobiliaria-local -f docs/production/Dockerfile .

# Run container with environment variables
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL="jdbc:postgresql://..." \
  -e DATABASE_USERNAME="postgres" \
  -e DATABASE_PASSWORD="..." \
  -e JWT_SECRET="..." \
  -e GOOGLE_CLOUD_PROJECT="..." \
  -e GOOGLE_CLOUD_LOCATION="us-central1" \
  -e GOOGLE_GENAI_USE_VERTEXAI=true \
  inmobiliaria-local

# Check container logs
docker logs <container-id>

# Access container shell
docker exec -it <container-id> /bin/bash

# Clean up
docker system prune -a
```

## GitHub Actions

### Trigger Deployment

```bash
# Push to main branch triggers automatic deployment
git push origin main

# Manual trigger via GitHub CLI
gh workflow run deploy-cloud-run.yml
```

### View Workflow Status

```bash
# List workflow runs
gh run list --workflow=deploy-cloud-run.yml

# View specific run
gh run view <run-id>

# Watch run in real-time
gh run watch <run-id>

# View logs
gh run view <run-id> --log
```

## Monitoring and Alerts

### Cost Monitoring

```bash
# View billing for current month
gcloud billing accounts list
gcloud beta billing projects describe $GCP_PROJECT_ID

# Create budget alert (via Console recommended)
# Go to: Billing → Budgets & Alerts
```

### Uptime Checks

```bash
# Create uptime check
gcloud monitoring uptime-checks create \
  --display-name="Inmobiliaria API Health" \
  --resource-type=uptime-url \
  --monitored-resource=url="${API_URL}/actuator/health"
```

## Troubleshooting

### Common Diagnostic Commands

```bash
# Check Cloud Run status
gcloud run services describe inmobiliaria-api \
  --region=us-central1 \
  --format="value(status.conditions)"

# Check latest deployment time
gcloud run services describe inmobiliaria-api \
  --region=us-central1 \
  --format="value(status.latestCreatedRevisionName,status.latestReadyRevisionName)"

# Verify environment variables are set
gcloud run services describe inmobiliaria-api \
  --region=us-central1 \
  --format="yaml(spec.template.spec.containers[0].env)" \
  | grep -E "GOOGLE_CLOUD_PROJECT|DATABASE_URL|JWT_SECRET"

# Check service account
gcloud run services describe inmobiliaria-api \
  --region=us-central1 \
  --format="value(spec.template.spec.serviceAccountName)"

# Verify service account has Vertex AI permissions
gcloud projects get-iam-policy $GCP_PROJECT_ID \
  --flatten="bindings[].members" \
  --filter="bindings.role:roles/aiplatform.user" \
  --format="table(bindings.members)"
```

### Error Investigation

```bash
# Get error logs from last hour
gcloud run services logs read inmobiliaria-api \
  --region=us-central1 \
  --limit=500 \
  --format="table(timestamp,severity,textPayload)" \
  | grep -i "error\|exception\|failed"

# Check startup errors
gcloud run services logs read inmobiliaria-api \
  --region=us-central1 \
  --limit=100 \
  | grep -i "started\|application\|failed"

# Database connection errors
gcloud run services logs read inmobiliaria-api \
  --region=us-central1 \
  --limit=100 \
  | grep -i "database\|connection\|hikari"
```

## Maintenance

### Update Dependencies

```bash
# Check for Maven dependency updates
mvn versions:display-dependency-updates

# Update Spring Boot version
mvn versions:set-property -Dproperty=spring-boot.version -DnewVersion=3.5.8

# Update parent version
mvn versions:update-parent
```

### Rotate Secrets

```bash
# Generate new JWT secret
NEW_JWT_SECRET=$(openssl rand -base64 64)
echo "New JWT Secret: $NEW_JWT_SECRET"

# Update in GitHub Secrets manually
# Then redeploy via GitHub Actions

# Rotate service account key
gcloud iam service-accounts keys create ~/new-key.json \
  --iam-account=inmobiliaria-github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com

# Delete old key
gcloud iam service-accounts keys delete <OLD_KEY_ID> \
  --iam-account=inmobiliaria-github-actions@${GCP_PROJECT_ID}.iam.gserviceaccount.com
```
