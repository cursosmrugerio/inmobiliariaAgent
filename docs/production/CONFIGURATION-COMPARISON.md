# Configuration Comparison: Development vs Test vs Production

## Overview

This document compares the configuration across all three environments: local development, automated testing, and production deployment on Cloud Run.

## Configuration Matrix

| Setting | Development | Test | Production (Cloud Run) | Notes |
|---------|------------|------|------------------------|-------|
| **Java Version** | 25 | 25 | 25 | ✅ Consistent across environments |
| **Spring Boot** | 3.5.7 | 3.5.7 | 3.5.7 | ✅ Same version |
| **Database** | H2 file-based | H2 in-memory | PostgreSQL (Supabase) | Different per environment |
| **DDL Strategy** | `validate` | `create-drop` | `validate` | Test creates fresh schema |
| **Flyway** | Enabled | Disabled | Enabled | Test uses in-memory schema |
| **Authentication** | File-based credentials | N/A | Workload Identity | Production is more secure |
| **Credentials File** | `/Users/mike/Desarrollo/compyser/inmobiliaria/backend/credentials.json` | Not used | Not used | Dev only |
| **Project ID** | `inmobiliaria-adk` | N/A | From GitHub Secret `GCP_PROJECT_ID` | Update secret if different |
| **Location/Region** | `us-central1` | N/A | `us-central1` | ✅ Same |
| **Vertex AI Enabled** | `true` | N/A (mocked) | `true` | Tests don't call real AI |
| **Service Account** | Uses JSON key file | N/A | `inmobiliaria-cloudrun@{PROJECT}.iam.gserviceaccount.com` | Different accounts |
| **Swagger UI** | Enabled | N/A | Disabled | Security requirement |
| **H2 Console** | Enabled | N/A | Disabled | Development tool only |
| **Logging Level** | DEBUG (app) | DEBUG (app) | INFO (app) | Test/Dev more verbose |

## Environment Variables

### Development (`run-agent.sh`)

```bash
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1
```

### Production (Cloud Run)

Set via GitHub Actions workflow:

```yaml
--set-env-vars="SPRING_PROFILES_ACTIVE=prod"
--set-env-vars="GOOGLE_CLOUD_PROJECT=${{ env.GCP_PROJECT_ID }}"
--set-env-vars="GOOGLE_CLOUD_LOCATION=${{ env.REGION }}"
--set-env-vars="GOOGLE_GENAI_USE_VERTEXAI=true"
```

**Key Differences:**
1. ❌ No `GOOGLE_APPLICATION_CREDENTIALS` in production
2. ✅ Project ID comes from GitHub Secret
3. ✅ Region comes from workflow env var (`us-central1`)
4. ✅ `SPRING_PROFILES_ACTIVE=prod` activates production configuration

## Security Considerations

### Development
- **File-based authentication**: Uses service account JSON key
- **Location**: Stored locally at `~/inmobiliaria-service-account-key.json`
- **Security**: Key file in `.gitignore`, never committed
- **Permissions**: Full access defined by service account

### Production
- **Workload Identity**: Cloud Run service uses its own service account
- **No credential files**: Authentication handled by GCP infrastructure
- **Least privilege**: Service account has only required permissions:
  - `roles/aiplatform.user` - Access Vertex AI
  - `roles/secretmanager.secretAccessor` - Read secrets

## Application Configuration

### Development (`application.properties`)

```properties
google.genai.use.vertexai=${GOOGLE_GENAI_USE_VERTEXAI:true}
google.cloud.project=${GOOGLE_CLOUD_PROJECT:inmobiliaria-adk}
google.cloud.location=${GOOGLE_CLOUD_LOCATION:us-central1}
```

### Production (`application-prod.properties`)

```properties
google.genai.use.vertexai=${GOOGLE_GENAI_USE_VERTEXAI:true}
google.cloud.project=${GOOGLE_CLOUD_PROJECT}
google.cloud.location=${GOOGLE_CLOUD_LOCATION:us-central1}
```

**Note**: Production doesn't have a default project ID - it **must** be provided via environment variable.

## Verification Steps

### Verify Local Development Configuration

```bash
# Check environment variables
echo $GOOGLE_APPLICATION_CREDENTIALS
echo $GOOGLE_CLOUD_PROJECT
echo $GOOGLE_CLOUD_LOCATION
echo $GOOGLE_GENAI_USE_VERTEXAI

# Test Vertex AI access
mvn spring-boot:run
# Then test agent endpoint
curl -X POST http://localhost:8080/api/agent/inmobiliarias \
  -H "Content-Type: application/json" \
  -d '{"message": "List all agencies"}'
```

### Verify Production Configuration

```bash
# Check Cloud Run environment variables
gcloud run services describe inmobiliaria-api \
  --region=us-central1 \
  --format="yaml(spec.template.spec.containers[0].env)"

# Check service account
gcloud run services describe inmobiliaria-api \
  --region=us-central1 \
  --format="value(spec.template.spec.serviceAccountName)"

# Test production endpoint
export API_URL="https://inmobiliaria-api-xxxxx-uc.a.run.app"
curl -X POST $API_URL/api/agent/inmobiliarias \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"message": "List all agencies"}'
```

## Common Issues

### Issue 1: "Authentication failed" in Cloud Run

**Cause**: Service account doesn't have Vertex AI permissions

**Solution**:
```bash
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member="serviceAccount:inmobiliaria-cloudrun@${GCP_PROJECT_ID}.iam.gserviceaccount.com" \
  --role="roles/aiplatform.user"
```

### Issue 2: "Project ID not set"

**Cause**: Environment variable `GOOGLE_CLOUD_PROJECT` not set in Cloud Run

**Solution**: Verify deployment includes:
```bash
--set-env-vars="GOOGLE_CLOUD_PROJECT=your-project-id"
```

### Issue 3: Wrong project ID

**Cause**: GitHub Secret `GCP_PROJECT_ID` doesn't match actual GCP project

**Solution**:
1. Get correct project ID: `gcloud config get-value project`
2. Update GitHub Secret at: `Settings → Secrets and variables → Actions`
3. Redeploy

## Migration Checklist

When moving from development to production:

- [ ] Service account created in production GCP project
- [ ] Service account has `roles/aiplatform.user` permission
- [ ] GitHub Secret `GCP_PROJECT_ID` matches production project
- [ ] Cloud Run service account configured in deployment
- [ ] Vertex AI API enabled in production project
- [ ] Test agent endpoints after deployment
- [ ] Verify logs show successful Vertex AI connections
- [ ] Remove any local credential files from Docker images

## Code Patterns & Dependencies

### DTO Implementation Pattern
- **Pattern Used**: Final classes with manual constructors
- **Why Not Records**: Compatibility with Jakarta Bean Validation annotations
- **Example**:
  ```java
  public final class CreateInmobiliariaDTO {
      private final String nombre;
      private final String rfc;
      // Constructor, getters, validation annotations...
  }
  ```

### Lombok Usage
- **Status**: Dependency available but **NOT actively used**
- **Reason**: Manual implementations preferred for consistency
- **If Adding Lombok**: Ensure IDE annotation processing is configured
- **Current Approach**: Manual getters/setters, constructors, and logger initialization

### Code Formatting
- **Tool**: `fmt-maven-plugin` v2.9.1
- **Standard**: Google Java Style Guide
- **Enforcement**: CI/CD pipeline checks formatting
- **Commands**:
  ```bash
  mvn fmt:check   # Verify formatting
  mvn fmt:format  # Auto-format code
  ```

## Best Practices

### Development
1. ✅ Use dedicated service account for development
2. ✅ Store credentials in `~/.config/gcloud/` or home directory
3. ✅ Never commit credential files
4. ✅ Use `.gitignore` for `credentials.json` and `*-key.json`
5. ✅ Rotate service account keys periodically
6. ✅ Follow Google Java Style Guide (`mvn fmt:format`)
7. ✅ Run tests before committing (`mvn clean test`)
8. ✅ Review `CLAUDE.md` for project standards

### Production
1. ✅ Use Workload Identity (no credential files)
2. ✅ Apply least privilege to service accounts
3. ✅ Store secrets in Secret Manager
4. ✅ Use different GCP projects for dev/staging/prod
5. ✅ Monitor service account usage
6. ✅ Enable audit logging
7. ✅ Verify code formatting passes (`mvn fmt:check`)

## Additional Resources

- [Workload Identity Documentation](https://cloud.google.com/run/docs/securing/service-identity)
- [Vertex AI Authentication](https://cloud.google.com/vertex-ai/docs/authentication)
- [Service Account Best Practices](https://cloud.google.com/iam/docs/best-practices-service-accounts)
- Project docs: `docs/VERTEX-AI.md`

---

**Last Updated**: 2025-10-29
