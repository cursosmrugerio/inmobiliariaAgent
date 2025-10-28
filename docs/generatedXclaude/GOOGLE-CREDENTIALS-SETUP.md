# Google Cloud Credentials Setup

This guide explains how to set up Google Cloud credentials to enable the ADK agent functionality.

## Prerequisites

1. A Google Cloud Project with Vertex AI API enabled
2. Service account with appropriate permissions

## Step 1: Create Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Navigate to **IAM & Admin** > **Service Accounts**
3. Click **Create Service Account**
4. Fill in the details:
   - Name: `inmobiliaria-adk-agent`
   - Description: `Service account for inmobiliaria ADK agent`
5. Click **Create and Continue**

## Step 2: Grant Permissions

Grant the following roles to the service account:
- **Vertex AI User** (`roles/aiplatform.user`)
- **Cloud Functions Invoker** (if using Cloud Functions)

## Step 3: Create and Download Key

1. In the service accounts list, click on the newly created account
2. Go to the **Keys** tab
3. Click **Add Key** > **Create new key**
4. Select **JSON** format
5. Click **Create** - the key file will be downloaded automatically
6. **IMPORTANT:** Save this file securely and never commit it to version control

## Step 4: Place Credentials File

Move the downloaded JSON file to the backend directory:

```bash
mv ~/Downloads/your-project-xxxxx-xxxxx.json /Users/mike/Desarrollo/compyser/inmobiliaria/backend/credentials.json
```

## Step 5: Set Environment Variables

Add these to your shell profile (`~/.zshrc` or `~/.bash_profile`):

```bash
# Google Cloud ADK Configuration
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1
export GOOGLE_APPLICATION_CREDENTIALS=/Users/mike/Desarrollo/compyser/inmobiliaria/backend/credentials.json
```

Reload your shell:
```bash
source ~/.zshrc  # or ~/.bash_profile
```

## Step 6: Verify Setup

Test that credentials are properly configured:

```bash
# Check environment variables
echo $GOOGLE_APPLICATION_CREDENTIALS
echo $GOOGLE_CLOUD_PROJECT

# Verify credentials file exists
ls -la $GOOGLE_APPLICATION_CREDENTIALS

# Test the application
mvn spring-boot:run

# In another terminal, run the tests
./scripts/test-agent_inmobiliarias.sh
```

## Alternative: Use a Test Script

Create a convenience script `scripts/test-with-credentials.sh`:

```bash
#!/bin/bash

# Set Google Cloud credentials for testing
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1
export GOOGLE_APPLICATION_CREDENTIALS=/Users/mike/Desarrollo/compyser/inmobiliaria/backend/credentials.json

# Run tests
./scripts/test-agent_inmobiliarias.sh
```

Make it executable:
```bash
chmod +x scripts/test-with-credentials.sh
```

## Troubleshooting

### Error: "credentials.json: No such file or directory"
- Verify the file exists at the specified path
- Check the `GOOGLE_APPLICATION_CREDENTIALS` environment variable

### Error: "403 Permission Denied"
- Ensure Vertex AI API is enabled in your project
- Verify the service account has the correct roles

### Error: "Project not found"
- Check that `GOOGLE_CLOUD_PROJECT` matches your actual project ID
- The project ID can be found in the Google Cloud Console

### Error: "Region not available"
- Verify the region (us-central1) is available for Vertex AI
- Check [Vertex AI locations](https://cloud.google.com/vertex-ai/docs/general/locations)

## Security Best Practices

1. **Never commit credentials.json to version control**
   - Add to `.gitignore` (already done)
2. **Use service accounts with minimal permissions**
   - Only grant roles needed for the application
3. **Rotate keys regularly**
   - Create new keys and delete old ones periodically
4. **Use Secret Manager in production**
   - For production deployments, use Google Cloud Secret Manager instead of JSON files

## Cost Considerations

- Vertex AI API calls are billed per request
- The Gemini 2.0 model used by the agent incurs costs
- Monitor usage in Google Cloud Console > Billing
- Set up budget alerts to avoid unexpected charges

## Resources

- [Google Cloud Console](https://console.cloud.google.com)
- [Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs)
- [Service Account Best Practices](https://cloud.google.com/iam/docs/best-practices-service-accounts)
- [ADK Java Documentation](https://github.com/google-cloud/genai-adk-java)
