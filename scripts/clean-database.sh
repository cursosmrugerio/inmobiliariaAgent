#!/bin/bash

# Script to clean the H2 database by deleting all inmobiliarias
# This should be run before test-agent_inmobiliarias.sh for clean tests

BASE_URL="http://localhost:8080"
API_ENDPOINT="${BASE_URL}/inmobiliarias"

# Get the security password from the logs
echo "Cleaning database..."

# Get all agencies
agencies=$(curl -s -u user:$(curl -s http://localhost:8080/actuator/health 2>/dev/null | head -1) "$API_ENDPOINT" 2>/dev/null)

# If we can't get agencies without auth, user needs to provide password
if [ $? -ne 0 ]; then
    echo "Note: You may need to provide the Spring Security password"
    echo "Find it in the application logs (look for 'Using generated security password')"
    echo ""
    echo "Usage: ./clean-database.sh <password>"
    exit 1
fi

# Delete all agencies
echo "Deleting all existing agencies..."
for id in $(echo "$agencies" | jq -r '.[].id' 2>/dev/null); do
    echo "Deleting agency ID: $id"
    curl -s -X DELETE "$API_ENDPOINT/$id" > /dev/null 2>&1
done

echo "Database cleaned!"
echo "You can now run: ./test-agent_inmobiliarias.sh"
