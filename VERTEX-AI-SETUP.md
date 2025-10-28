# Vertex AI Configuration Fix

**Date:** 2025-10-27
**Issue:** API key error when using service account credentials
**Status:** ✅ RESOLVED

---

## Problem

When trying to use the agent, you received this error:
```
API key must either be provided or set in the environment variable
GOOGLE_API_KEY or GEMINI_API_KEY
```

Even though `GOOGLE_APPLICATION_CREDENTIALS` was set correctly.

---

## Root Cause

The ADK was trying to use the **Gemini API** directly (which requires an API key), instead of using **Vertex AI** (which uses service account credentials).

By default, when you set model to `"gemini-2.0-flash"`, ADK tries to:
1. First look for `GOOGLE_API_KEY` or `GEMINI_API_KEY`
2. If not found, it fails

We have service account credentials for Vertex AI, so we need to explicitly tell ADK to use Vertex AI.

---

## Solution

### Required Environment Variables

To use Vertex AI with service account credentials, you need these environment variables:

```bash
# Authentication (you already have this)
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"

# Vertex AI Configuration (NEW - these were missing)
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1
```

### What Was Done

1. **Added to `~/.zshrc`** - These variables will auto-load in new terminals
2. **Added to `application.properties`** - Defaults for Spring Boot application
3. **Created `run-agent.sh`** - Convenience script that sets all variables

---

## How to Run the Application

### Option 1: Using the Run Script (Easiest)

```bash
./run-agent.sh
```

This script automatically sets all required environment variables and starts the application.

### Option 2: Manual Start

```bash
# Set environment variables (if not already in your shell)
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1

# Start application
mvn spring-boot:run
```

### Option 3: New Terminal Window

Simply open a new terminal (the variables are in `~/.zshrc`), then:

```bash
cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend
mvn spring-boot:run
```

---

## Testing the Agent

Once the application starts, test with:

```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello! Can you help me?"
  }'
```

**Expected response:**
```json
{
  "response": "Hello! Yes, I can help you...",
  "sessionId": "user-xxxxx",
  "success": true,
  "error": null
}
```

---

## Verification Checklist

Before running, verify:

- ✅ **Service account file exists:**
  ```bash
  ls -lh ~/inmobiliaria-service-account-key.json
  ```

- ✅ **Environment variables are set:**
  ```bash
  echo $GOOGLE_APPLICATION_CREDENTIALS
  echo $GOOGLE_GENAI_USE_VERTEXAI
  echo $GOOGLE_CLOUD_PROJECT
  echo $GOOGLE_CLOUD_LOCATION
  ```

- ✅ **Application.properties updated:**
  ```bash
  grep "vertexai" src/main/resources/application.properties
  ```

---

## Understanding the Configuration

### GOOGLE_APPLICATION_CREDENTIALS
- **What:** Path to service account JSON key file
- **Purpose:** Authenticates your application with Google Cloud
- **Required:** YES

### GOOGLE_GENAI_USE_VERTEXAI
- **What:** Boolean flag (true/false)
- **Purpose:** Tells ADK to use Vertex AI instead of direct Gemini API
- **Required:** YES (when using service account)
- **Value:** `true`

### GOOGLE_CLOUD_PROJECT
- **What:** Your Google Cloud project ID
- **Purpose:** Tells Vertex AI which project to bill/log to
- **Required:** YES
- **Value:** `inmobiliaria-adk` (from your service account file)

### GOOGLE_CLOUD_LOCATION
- **What:** Google Cloud region
- **Purpose:** Which Vertex AI regional endpoint to use
- **Required:** YES
- **Value:** `us-central1` (default, can be changed)
- **Other options:** `us-east1`, `europe-west1`, `asia-southeast1`, etc.

---

## Troubleshooting

### Error: "API key must be provided"

**Cause:** `GOOGLE_GENAI_USE_VERTEXAI` is not set or is false

**Fix:**
```bash
export GOOGLE_GENAI_USE_VERTEXAI=true
```

### Error: "Project not found"

**Cause:** `GOOGLE_CLOUD_PROJECT` is wrong or not set

**Fix:** Check your service account file:
```bash
grep project_id ~/inmobiliaria-service-account-key.json
```

### Error: "Permission denied"

**Cause:** Service account doesn't have Vertex AI permissions

**Fix:** Add "Vertex AI User" role in Google Cloud Console:
1. Go to IAM & Admin → IAM
2. Find your service account
3. Add role: "Vertex AI User"

### Error: "Location not available"

**Cause:** Vertex AI not available in that region

**Fix:** Use a supported region:
```bash
export GOOGLE_CLOUD_LOCATION=us-central1  # Most common
# OR
export GOOGLE_CLOUD_LOCATION=us-east1
# OR
export GOOGLE_CLOUD_LOCATION=europe-west1
```

---

## API Key vs Service Account

### API Key (Direct Gemini API)
- ✅ Simpler for development
- ✅ Faster to set up
- ❌ Less secure
- ❌ Not recommended for production
- ❌ Rate limits per key

**Get from:** https://makersuite.google.com/app/apikey

### Service Account (Vertex AI)
- ✅ More secure
- ✅ Better for production
- ✅ Project-level billing and quotas
- ✅ Fine-grained IAM permissions
- ❌ More complex setup

**Get from:** Google Cloud Console → IAM & Service Accounts

**You are using:** Service Account (Vertex AI) ✅

---

## Production Considerations

### For Deployment

When deploying to production (Cloud Run, GKE, etc.):

1. **Don't use file-based credentials** - Use Workload Identity
2. **Set environment variables** in your deployment config
3. **Use Secret Manager** for sensitive configs
4. **Monitor usage** via Cloud Console → Vertex AI

### Environment Variables in Production

```yaml
# Cloud Run / GKE example
env:
  - name: GOOGLE_GENAI_USE_VERTEXAI
    value: "true"
  - name: GOOGLE_CLOUD_PROJECT
    value: "inmobiliaria-adk"
  - name: GOOGLE_CLOUD_LOCATION
    value: "us-central1"
```

---

## Quick Reference

### Complete Setup Commands

```bash
# 1. Verify service account file
ls -lh ~/inmobiliaria-service-account-key.json

# 2. Set all variables (for current session)
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1

# 3. Run application
mvn spring-boot:run

# 4. Test (in another terminal)
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "List all agencies"}'
```

---

## Files Modified

- ✅ `~/.zshrc` - Added environment variables for persistence
- ✅ `application.properties` - Added Vertex AI defaults
- ✅ `run-agent.sh` - Created convenience script
- ✅ `VERTEX-AI-SETUP.md` - This documentation

---

## Resources

- **Vertex AI Authentication:** https://cloud.google.com/vertex-ai/docs/authentication
- **ADK Authentication:** https://google.github.io/adk-docs/tools/authentication/
- **Service Account Docs:** https://cloud.google.com/iam/docs/service-accounts
- **Vertex AI Locations:** https://cloud.google.com/vertex-ai/docs/general/locations

---

**Status:** ✅ Configuration complete. Application ready to run with Vertex AI.
