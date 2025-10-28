#!/bin/bash

# Run Inmobiliaria Agent Application with proper Vertex AI configuration

echo "🚀 Starting Inmobiliaria AI Agent Application..."
echo ""
echo "📋 Configuration:"
echo "   GOOGLE_APPLICATION_CREDENTIALS: $HOME/inmobiliaria-service-account-key.json"
echo "   GOOGLE_CLOUD_PROJECT: inmobiliaria-adk"
echo "   GOOGLE_CLOUD_LOCATION: us-central1"
echo "   GOOGLE_GENAI_USE_VERTEXAI: true"
echo ""

# Set environment variables
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1

# Run the application
mvn spring-boot:run
