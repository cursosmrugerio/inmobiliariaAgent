# Production Deployment Files

This directory contains all the necessary configuration files and documentation for deploying the Inmobiliaria Management System to production.

## 📁 Directory Contents

### Main Documentation
- **`CRITICAL-PRODUCTION-CHECKLIST.md`** - ⚠️ **READ THIS FIRST!** Critical security, configuration, and deployment considerations
- **`DEPLOYMENT-PRODUCTION.md`** - Complete step-by-step deployment guide for Google Cloud Run with Supabase PostgreSQL and GitHub Actions CI/CD
- **`CONFIGURATION-COMPARISON.md`** - Detailed comparison of development vs test vs production configuration

### Project Standards
- **`../CLAUDE.md`** - ⭐ **Project Constitution** - Architecture principles, code style (Google Java Style Guide), DTO patterns, testing standards, and AI agent implementation guidelines. **Review this before making any code changes.**

### Docker Configuration
- **`Dockerfile`** - Multi-stage Docker build configuration optimized for production
- **`.dockerignore`** - Files to exclude from Docker build context

### Application Configuration
- **`application-prod.properties`** - Spring Boot production configuration with PostgreSQL, Vertex AI, and security settings

### CI/CD Workflows
- **`github-workflows/`** - GitHub Actions workflow files
  - **`test.yml`** - Automated testing workflow (runs on push/PR)
  - **`deploy-cloud-run.yml`** - Automated deployment to Google Cloud Run

## 🚀 Quick Start

### Option 1: Test Locally First (Recommended)

Before production deployment, test the application locally with Vertex AI:

1. **Set up local development** (see `DEPLOYMENT-PRODUCTION.md` section "Local Development Setup"):
   - Create Google Cloud service account for development
   - Download credentials to `~/inmobiliaria-service-account-key.json`
   - Use `run-agent.sh` script to start the application

2. **Test conversational agents** with Vertex AI locally

3. **Proceed to production deployment**

### Option 2: Deploy to Production Directly

1. **Read the deployment guide first**: `DEPLOYMENT-PRODUCTION.md`

2. **Copy files to their proper locations** when ready to deploy:
   ```bash
   # From the backend directory
   cp docs/production/Dockerfile .
   cp docs/production/.dockerignore .
   cp docs/production/application-prod.properties src/main/resources/
   cp -r docs/production/github-workflows/* .github/workflows/
   ```

3. **Follow the deployment guide** step by step

## 📋 Deployment Checklist

Before deploying, ensure you have:

### Infrastructure
- [ ] Supabase account and PostgreSQL database created
- [ ] Google Cloud Platform project with billing enabled
- [ ] GitHub repository with required secrets configured
- [ ] Service accounts created (Cloud Run + GitHub Actions)
- [ ] Vertex AI APIs enabled
- [ ] JWT secret generated (64+ characters)
- [ ] Production frontend URL configured in SecurityConfig.java

### Code Quality
- [ ] All tests pass (`mvn clean test`)
- [ ] Code formatting passes (`mvn fmt:check`)
- [ ] Review `CLAUDE.md` for compliance with project standards
- [ ] Agent endpoints security reviewed (remove `.permitAll()`)
- [ ] Test scripts executed successfully (see `scripts/` directory)

## 🔗 Related Documentation

- Project Architecture: `../README-AGENT.md`
- Vertex AI Setup: `../VERTEX-AI.md`
- Testing Guide: `../reference/README-TESTING.md`

## ⚠️ Important Notes

1. **These are template files** - They MUST be copied to the project root before deployment:
   - `Dockerfile` → project root
   - `.dockerignore` → project root
   - `application-prod.properties` → `src/main/resources/`
   - `github-workflows/*.yml` → `.github/workflows/`

   **Update placeholders with your actual values:**
   - Supabase PROJECT_REF in `application-prod.properties`
   - GCP_PROJECT_ID in GitHub workflows
   - Production domain URLs in SecurityConfig

2. **Technology versions** (Java 25, Spring Boot 3.5.7):
   - All files have been updated to use Java 25
   - Ensure local environment matches production versions
   - See `DEPLOYMENT-PRODUCTION.md` for complete version information

3. **Never commit secrets** - Use environment variables and GitHub Secrets

4. **Test locally first** - Build and test Docker image before deploying

5. **Review security settings** - Ensure H2 console is disabled, Swagger is secured, and agent endpoints require authentication

6. **Follow project standards** - Review `CLAUDE.md` for code style, DTO patterns, and architectural guidelines

## 🆘 Getting Help

If you encounter issues:
1. Check the Troubleshooting section in `DEPLOYMENT-PRODUCTION.md`
2. Review Cloud Run logs: `gcloud run services logs read inmobiliaria-api`
3. Verify environment variables are set correctly
4. Check GitHub Actions workflow logs

---

**Last Updated**: 2025-10-29
