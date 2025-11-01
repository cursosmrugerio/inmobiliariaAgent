# Production CI/CD Migration Guide

## 🚨 CRITICAL: Your Production Deployment Needs Updating

### Current Situation (BEFORE)

Your GitHub Actions workflow is **missing the frontend build step**, which means:

❌ **Problems:**
1. Production is serving committed frontend files (build artifacts in Git)
2. Frontend changes aren't automatically deployed
3. Manual frontend build + commit required for each change
4. Build artifacts pollute version control (19 files committed)

✅ **Fixed:**
1. GitHub Actions now builds frontend automatically on every push
2. Frontend changes deploy automatically to production
3. No manual build steps required
4. Build artifacts removed from version control

---

## Migration Steps

### Step 1: Verify Updated GitHub Actions Workflow ✅ DONE

The file `.github/workflows/deploy-cloud-run.yml` has been updated with:

```yaml
- name: Set up Node.js
  uses: actions/setup-node@v4
  with:
    node-version: '20'
    cache: 'npm'
    cache-dependency-path: 'frontend/package-lock.json'

- name: Build Frontend
  working-directory: ./frontend
  run: |
    npm ci
    npm run build
```

**This runs BEFORE the Docker build**, ensuring fresh frontend files are available.

---

### Step 2: Remove Committed Frontend Files

**⚠️ IMPORTANT:** The built frontend files currently committed in `src/main/resources/static/` need to be removed from Git (but kept locally for now).

```bash
cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend

# Remove frontend build artifacts from Git tracking
git rm -r --cached src/main/resources/static/

# The files will remain on disk but won't be tracked by Git
# This is safe - CI/CD will rebuild them
```

**Why?**
- Build artifacts shouldn't be in version control
- `.gitignore` has been updated to prevent re-adding them
- CI/CD will build fresh files on every deployment

---

### Step 3: Commit the Changes

```bash
cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend

# Stage the updated workflow and .gitignore
git add .github/workflows/deploy-cloud-run.yml
git add .gitignore

# Commit the changes
git commit -m "$(cat <<'EOF'
Add automated frontend build to CI/CD pipeline

Changes:
- Add Node.js setup and frontend build steps to deploy-cloud-run.yml
- Build frontend automatically before Docker build
- Add src/main/resources/static/ to .gitignore
- Remove committed frontend build artifacts

This ensures frontend changes are automatically deployed to production
and prevents build artifacts from being committed to version control.

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"
```

---

### Step 4: Push to GitHub (Triggers Deployment)

```bash
git push origin main
```

**What happens:**
1. GitHub Actions workflow triggers on push to `main`
2. Node.js 20 is set up with npm caching
3. Frontend is built: `npm ci && npm run build`
4. Backend tests run
5. Application is packaged (includes fresh frontend)
6. Docker image is built and pushed
7. Cloud Run deployment updates
8. Health checks verify deployment

**Monitor the deployment:**
```bash
# Watch GitHub Actions
# Go to: https://github.com/YOUR_USERNAME/YOUR_REPO/actions

# Or watch Cloud Run logs
gcloud run services logs read inmobiliaria-api --region=us-central1 --follow
```

---

## Verification

### After First Deployment with New Workflow

1. **Check GitHub Actions run:**
   - Go to your repository → Actions tab
   - Find the latest workflow run
   - Verify "Build Frontend" step succeeded
   - Check build logs for `npm ci` and `npm run build` output

2. **Test the deployed frontend:**
   ```bash
   # Get your Cloud Run URL
   gcloud run services describe inmobiliaria-api \
     --region=us-central1 \
     --format='value(status.url)'

   # Test frontend loads
   curl -I https://YOUR-SERVICE-URL/

   # Should return:
   # HTTP/2 200
   # content-type: text/html
   ```

3. **Verify frontend is fresh:**
   - Open the Cloud Run URL in browser
   - Check browser DevTools → Network tab
   - Verify JavaScript/CSS files have recent timestamps
   - Test frontend functionality

---

## Future Workflow

### Making Frontend Changes

**OLD WORKFLOW (manual):**
```bash
# 1. Make frontend changes
vim frontend/src/...

# 2. Build frontend manually
cd frontend && npm run build && cd ..

# 3. Commit built files
git add src/main/resources/static/
git commit -m "Update frontend"

# 4. Push
git push
```

**NEW WORKFLOW (automated):**
```bash
# 1. Make frontend changes
vim frontend/src/...

# 2. Commit source files only
git add frontend/src/
git commit -m "Update frontend feature"

# 3. Push - frontend builds automatically in CI/CD
git push
```

**Benefits:**
- ✅ Faster workflow (no manual build)
- ✅ Can't forget to build
- ✅ Consistent builds (same Node.js version)
- ✅ Cleaner Git history (no build artifacts)

---

## Rollback Plan

If something goes wrong with the new workflow:

### Option 1: Revert the Workflow Change

```bash
# Revert the workflow commit
git revert HEAD

# Push to restore old workflow
git push origin main
```

### Option 2: Manually Build Frontend (Emergency)

```bash
# Build frontend locally
cd frontend
npm ci
npm run build
cd ..

# Add back to Git temporarily
git add src/main/resources/static/
git commit -m "Emergency: Add pre-built frontend"
git push
```

**Note:** This is ONLY for emergencies. Fix the workflow afterward.

---

## Troubleshooting

### Issue: "Build Frontend" step fails in GitHub Actions

**Error:** `npm: command not found` or `npm ci` fails

**Solution:**
1. Check Node.js setup step is present and runs first
2. Verify `frontend/package-lock.json` exists in repository
3. Check `cache-dependency-path` points to correct file

**Fix:**
```yaml
- name: Set up Node.js
  uses: actions/setup-node@v4
  with:
    node-version: '20'
    cache: 'npm'
    cache-dependency-path: 'frontend/package-lock.json'  # ← Verify this path
```

---

### Issue: Frontend not loading after deployment

**Symptoms:**
- 404 errors for JavaScript files
- Blank page
- "Cannot GET /" error

**Diagnosis:**
```bash
# Check if frontend files were built in CI/CD
# Look at GitHub Actions logs for "Build Frontend" step

# Check Cloud Run logs
gcloud run services logs read inmobiliaria-api --region=us-central1 --limit=50
```

**Solution:**
1. Verify frontend build step ran successfully in GitHub Actions
2. Check build logs for errors
3. Verify `npm run build` output shows files created
4. Check Docker build logs to ensure static files were included

---

### Issue: Old frontend still showing after deployment

**Cause:** Browser caching

**Solution:**
```bash
# 1. Hard refresh browser
# Mac: Cmd+Shift+R
# Windows/Linux: Ctrl+Shift+R

# 2. Clear browser cache
# Or open in incognito/private mode

# 3. Verify Cloud Run is serving new version
curl -I https://YOUR-SERVICE-URL/assets/index-HASH.js
# Check Last-Modified header
```

---

## Configuration Reference

### Updated Files

| File | Change | Purpose |
|------|--------|---------|
| `.github/workflows/deploy-cloud-run.yml` | Added Node.js setup and frontend build | Automates frontend build in CI/CD |
| `.gitignore` | Added `src/main/resources/static/` | Prevents committing build artifacts |

### Workflow Build Order

```
1. Checkout code
2. Set up Java 25
3. Set up Node.js 20          ← NEW
4. Build Frontend              ← NEW
   - npm ci
   - npm run build
   - Outputs to src/main/resources/static/
5. Run backend tests
6. Build application (mvn package)
   - Includes frontend from step 4
7. Build Docker image
8. Push to Artifact Registry
9. Deploy to Cloud Run
```

---

## Next Steps (Optional Enhancements)

### Enhancement 1: Add Frontend Type Checking

Add TypeScript type checking to CI/CD:

```yaml
- name: Check Frontend Types
  working-directory: ./frontend
  run: npm run type-check
```

### Enhancement 2: Cache npm Dependencies

GitHub Actions already caches npm with:
```yaml
cache: 'npm'
cache-dependency-path: 'frontend/package-lock.json'
```

This speeds up builds by caching `node_modules`.

### Enhancement 3: Use Integrated Dockerfile (Future)

For even better automation, migrate to multi-stage Dockerfile:
- Builds frontend inside Docker
- Single `docker build` command
- See `Dockerfile.integrated` for template

---

## Summary

### What Changed

| Before | After |
|--------|-------|
| ❌ Manual frontend build required | ✅ Automated in CI/CD |
| ❌ Build artifacts in Git | ✅ Build artifacts ignored |
| ❌ Easy to deploy stale frontend | ✅ Always fresh frontend |
| ❌ 2-step commit process | ✅ Single commit & push |

### Your Next Push Will:

1. ✅ Automatically build the frontend
2. ✅ Include fresh frontend in Docker image
3. ✅ Deploy complete app to Cloud Run
4. ✅ No manual build steps needed

**You're ready to push to production!** 🚀

---

**Last Updated:** 2025-10-31
