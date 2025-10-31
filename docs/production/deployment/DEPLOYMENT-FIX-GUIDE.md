# 🔧 Deployment Fix Guide - Inmobiliaria Agent

## Current Status
Your deployment is failing. This guide will help you fix all issues systematically.

---

## Part 1: Generate and Configure GitHub Secrets

### Secret 1: JWT_SECRET ✅ GENERATED

Your secure JWT secret (copy this exactly):
```
W/nmoL90TCaj2QwC5srVs99BSlqZJ9euP+UrsHKz0oo/ZC2zUkfZFGep9UzC8zlxVIJOek4wtAOCrNAbelCU4Q==
```

### Secret 2: GCP_PROJECT_ID ✅ KNOWN

Your GCP project ID:
```
inmobiliaria-adk
```

### Secret 3: GCP_SA_KEY ⚠️ NEEDS GENERATION

**You need to generate this using Google Cloud Console or gcloud CLI.**

---

## Part 2: Google Cloud Platform Setup

### Step 2.1: Install Google Cloud SDK (if not installed)

**Option A: macOS (using Homebrew)**
```bash
brew install --cask google-cloud-sdk
```

**Option B: Download installer**
Visit: https://cloud.google.com/sdk/docs/install

**After installation, initialize:**
```bash
gcloud init
```

### Step 2.2: Set Your Project

```bash
# Set the project
gcloud config set project inmobiliaria-adk

# Verify it's set correctly
gcloud config get-value project
```

### Step 2.3: Enable Required APIs

```bash
# Enable all required APIs at once
gcloud services enable \
  run.googleapis.com \
  containerregistry.googleapis.com \
  aiplatform.googleapis.com \
  secretmanager.googleapis.com \
  cloudbuild.googleapis.com

# Verify APIs are enabled (should see all 5)
gcloud services list --enabled | grep -E "run|container|aiplatform|secretmanager|cloudbuild"
```

### Step 2.4: Create Service Account for GitHub Actions

```bash
# Create the service account
gcloud iam service-accounts create inmobiliaria-github-actions \
  --display-name="GitHub Actions CI/CD for Inmobiliaria" \
  --description="Service account for deploying to Cloud Run via GitHub Actions"

# Grant necessary permissions
gcloud projects add-iam-policy-binding inmobiliaria-adk \
  --member="serviceAccount:inmobiliaria-github-actions@inmobiliaria-adk.iam.gserviceaccount.com" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding inmobiliaria-adk \
  --member="serviceAccount:inmobiliaria-github-actions@inmobiliaria-adk.iam.gserviceaccount.com" \
  --role="roles/storage.admin"

gcloud projects add-iam-policy-binding inmobiliaria-adk \
  --member="serviceAccount:inmobiliaria-github-actions@inmobiliaria-adk.iam.gserviceaccount.com" \
  --role="roles/iam.serviceAccountUser"

# Verify permissions
gcloud projects get-iam-policy inmobiliaria-adk \
  --flatten="bindings[].members" \
  --filter="bindings.members:inmobiliaria-github-actions@inmobiliaria-adk.iam.gserviceaccount.com"
```

### Step 2.5: Create Service Account for Cloud Run

```bash
# Create service account for the Cloud Run service itself
gcloud iam service-accounts create inmobiliaria-cloudrun \
  --display-name="Inmobiliaria Cloud Run Service Account" \
  --description="Service account for the running Cloud Run service"

# Grant Vertex AI permissions (for agent functionality)
gcloud projects add-iam-policy-binding inmobiliaria-adk \
  --member="serviceAccount:inmobiliaria-cloudrun@inmobiliaria-adk.iam.gserviceaccount.com" \
  --role="roles/aiplatform.user"

# Verify permissions
gcloud projects get-iam-policy inmobiliaria-adk \
  --flatten="bindings[].members" \
  --filter="bindings.members:inmobiliaria-cloudrun@inmobiliaria-adk.iam.gserviceaccount.com"
```

### Step 2.6: Generate Service Account Key for GitHub Actions

```bash
# Generate the key file
gcloud iam service-accounts keys create ~/inmobiliaria-github-sa-key.json \
  --iam-account=inmobiliaria-github-actions@inmobiliaria-adk.iam.gserviceaccount.com

# Display the key content (you'll copy this entire JSON)
cat ~/inmobiliaria-github-sa-key.json

# IMPORTANT: Copy the ENTIRE JSON output
# It should look like:
# {
#   "type": "service_account",
#   "project_id": "inmobiliaria-adk",
#   "private_key_id": "...",
#   "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
#   ...
# }
```

**⚠️ SECURITY NOTE**: After copying this JSON to GitHub Secrets, delete the local file:
```bash
rm ~/inmobiliaria-github-sa-key.json
```

---

## Part 3: Configure GitHub Repository Secrets

### Step 3.1: Navigate to GitHub Secrets

Go to: https://github.com/cursosmrugerio/inmobiliariaAgent/settings/secrets/actions

### Step 3.2: Add/Update Each Secret

Click "New repository secret" (or "Update" if it already exists) for each:

#### Secret 1: `GCP_PROJECT_ID`
- **Name**: `GCP_PROJECT_ID`
- **Value**: `inmobiliaria-adk`

#### Secret 2: `GCP_SA_KEY`
- **Name**: `GCP_SA_KEY`
- **Value**: [Paste the ENTIRE JSON from Step 2.6]
  - Make sure to copy the complete JSON including all curly braces
  - Should be around 2000+ characters
  - Starts with `{` and ends with `}`

#### Secret 3: `JWT_SECRET`
- **Name**: `JWT_SECRET`
- **Value**: `W/nmoL90TCaj2QwC5srVs99BSlqZJ9euP+UrsHKz0oo/ZC2zUkfZFGep9UzC8zlxVIJOek4wtAOCrNAbelCU4Q==`

### Step 3.3: Verify All Secrets Are Set

After adding all secrets, you should see:
- ✅ `GCP_PROJECT_ID`
- ✅ `GCP_SA_KEY`
- ✅ `JWT_SECRET`

**Total: 3 secrets** (no database secrets needed for H2 setup)

---

## Part 4: Verify GitHub Actions Workflow

The workflow file should already be correct, but let's verify:

```bash
# Check the workflow file
cat .github/workflows/deploy-cloud-run.yml | grep -A 5 "set-env-vars"
```

You should see:
- ✅ `SPRING_PROFILES_ACTIVE=prod`
- ✅ `GOOGLE_CLOUD_PROJECT`
- ✅ `GOOGLE_CLOUD_LOCATION`
- ✅ `GOOGLE_GENAI_USE_VERTEXAI=true`
- ✅ `JWT_SECRET`
- ❌ NO database environment variables

---

## Part 5: Test Deployment

### Step 5.1: Trigger Deployment

```bash
# Option A: Push a new commit
git commit --allow-empty -m "Trigger deployment with fixed configuration"
git push origin main

# Option B: Manually trigger via GitHub Actions
# Go to: https://github.com/cursosmrugerio/inmobiliariaAgent/actions
# Click on "Deploy to Google Cloud Run"
# Click "Run workflow" button
```

### Step 5.2: Monitor Deployment

1. Go to: https://github.com/cursosmrugerio/inmobiliariaAgent/actions
2. Click on the running workflow
3. Watch each step for errors

### Step 5.3: Common Error Solutions

#### Error: "Error creating Auth Client: Could not load the default credentials"
**Solution**:
- Verify `GCP_SA_KEY` is set correctly in GitHub Secrets
- Make sure the JSON is complete and valid
- Regenerate the service account key if needed

#### Error: "Permission denied" or "forbidden"
**Solution**:
```bash
# Re-grant permissions
gcloud projects add-iam-policy-binding inmobiliaria-adk \
  --member="serviceAccount:inmobiliaria-github-actions@inmobiliaria-adk.iam.gserviceaccount.com" \
  --role="roles/run.admin"
```

#### Error: "API [run.googleapis.com] not enabled"
**Solution**:
```bash
gcloud services enable run.googleapis.com
```

#### Error: "Service account does not exist"
**Solution**: Re-run Step 2.4 and 2.5 to create the service accounts

---

## Part 6: Post-Deployment Verification

Once deployment succeeds:

### Step 6.1: Get Service URL
```bash
gcloud run services describe inmobiliaria-api \
  --region=us-central1 \
  --format='value(status.url)'
```

### Step 6.2: Test Health Endpoint
```bash
# Replace URL with your actual service URL
curl https://inmobiliaria-api-xxxxx-uc.a.run.app/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### Step 6.3: Test Authentication
```bash
# Set your service URL
export API_URL="https://inmobiliaria-api-xxxxx-uc.a.run.app"

# Register a test user
curl -X POST $API_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "Admin123!",
    "name": "Admin User",
    "role": "ADMIN"
  }'

# Login and get token
curl -X POST $API_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "Admin123!"
  }'
```

---

## Checklist

Use this checklist to track your progress:

### Google Cloud Platform
- [ ] Google Cloud SDK installed and initialized
- [ ] Project set to `inmobiliaria-adk`
- [ ] All required APIs enabled
- [ ] `inmobiliaria-github-actions` service account created
- [ ] GitHub Actions service account has correct permissions
- [ ] `inmobiliaria-cloudrun` service account created
- [ ] Cloud Run service account has Vertex AI permissions
- [ ] Service account key JSON generated

### GitHub Secrets
- [ ] `GCP_PROJECT_ID` secret set
- [ ] `GCP_SA_KEY` secret set (complete JSON)
- [ ] `JWT_SECRET` secret set

### Deployment
- [ ] Workflow file is correct (no database env vars)
- [ ] Deployment triggered
- [ ] Deployment succeeded
- [ ] Service URL obtained
- [ ] Health endpoint returns `{"status":"UP"}`
- [ ] Authentication endpoints work

---

## Quick Reference Commands

```bash
# Check GCP configuration
gcloud config list
gcloud projects list

# List service accounts
gcloud iam service-accounts list

# Check enabled APIs
gcloud services list --enabled

# View Cloud Run services
gcloud run services list --region=us-central1

# View deployment logs
gcloud run services logs read inmobiliaria-api --region=us-central1 --limit=50

# Delete and recreate service (if needed)
gcloud run services delete inmobiliaria-api --region=us-central1
# Then trigger GitHub Actions deployment
```

---

## Need Help?

If you encounter issues:

1. **Check GitHub Actions logs**: https://github.com/cursosmrugerio/inmobiliariaAgent/actions
2. **Check Cloud Run logs**:
   ```bash
   gcloud run services logs read inmobiliaria-api --region=us-central1 --limit=100
   ```
3. **Verify all secrets are set**: https://github.com/cursosmrugerio/inmobiliariaAgent/settings/secrets/actions

---

**Generated**: 2025-10-31
**Project**: Inmobiliaria Management System
**GCP Project**: inmobiliaria-adk
**Region**: us-central1
