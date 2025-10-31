#!/bin/bash
# GitHub Secrets Setup Script for Inmobiliaria Deployment
# This script helps you set up GitHub repository secrets using GitHub CLI

set -e

echo "=================================================="
echo "GitHub Secrets Setup for Inmobiliaria Deployment"
echo "=================================================="
echo ""

# Check if GitHub CLI is installed
if ! command -v gh &> /dev/null; then
    echo "❌ GitHub CLI (gh) is not installed."
    echo ""
    echo "To install GitHub CLI:"
    echo "  macOS: brew install gh"
    echo "  Or visit: https://cli.github.com/"
    echo ""
    exit 1
fi

echo "✅ GitHub CLI found"
echo ""

# Check if authenticated
if ! gh auth status &> /dev/null; then
    echo "❌ Not authenticated with GitHub CLI"
    echo ""
    echo "Please run: gh auth login"
    echo ""
    exit 1
fi

echo "✅ Authenticated with GitHub"
echo ""

# Repository details
REPO="cursosmrugerio/inmobiliariaAgent"
echo "Repository: $REPO"
echo ""

# Secret 1: GCP_PROJECT_ID
echo "Setting secret: GCP_PROJECT_ID"
echo "inmobiliaria-adk" | gh secret set GCP_PROJECT_ID --repo="$REPO"
echo "✅ GCP_PROJECT_ID set"
echo ""

# Secret 2: GCP_SA_KEY
echo "Setting secret: GCP_SA_KEY"
if [ -f "/tmp/inmobiliaria-github-sa-key.json" ]; then
    gh secret set GCP_SA_KEY --repo="$REPO" < /tmp/inmobiliaria-github-sa-key.json
    echo "✅ GCP_SA_KEY set"
else
    echo "❌ Service account key not found at /tmp/inmobiliaria-github-sa-key.json"
    exit 1
fi
echo ""

# Secret 3: JWT_SECRET
echo "Setting secret: JWT_SECRET"
echo "W/nmoL90TCaj2QwC5srVs99BSlqZJ9euP+UrsHKz0oo/ZC2zUkfZFGep9UzC8zlxVIJOek4wtAOCrNAbelCU4Q==" | gh secret set JWT_SECRET --repo="$REPO"
echo "✅ JWT_SECRET set"
echo ""

echo "=================================================="
echo "✅ All GitHub Secrets Configured Successfully!"
echo "=================================================="
echo ""
echo "Secrets set:"
echo "  ✅ GCP_PROJECT_ID"
echo "  ✅ GCP_SA_KEY"
echo "  ✅ JWT_SECRET"
echo ""
echo "Next steps:"
echo "  1. Verify secrets at: https://github.com/$REPO/settings/secrets/actions"
echo "  2. Trigger deployment:"
echo "     git commit --allow-empty -m 'Trigger deployment'"
echo "     git push origin main"
echo ""
echo "  Or manually trigger:"
echo "     gh workflow run deploy-cloud-run.yml --repo=$REPO"
echo ""
