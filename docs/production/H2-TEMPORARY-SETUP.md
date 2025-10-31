# H2 Temporary Production Setup

## ⚠️ CRITICAL WARNING

**This H2 configuration is TEMPORARY and for initial deployment testing ONLY.**

DO NOT use this configuration for production beyond initial validation. You WILL lose data.

---

## Why H2 File-Based?

This temporary setup allows you to:
- ✅ Deploy and test the application on Cloud Run immediately
- ✅ Validate the deployment pipeline works
- ✅ Test authentication, endpoints, and agent functionality
- ✅ Verify Cloud Run configuration and environment variables
- ✅ Test the embedded frontend serves correctly

---

## How It Works

### Configuration
- **Database**: H2 embedded database
- **Storage Mode**: File-based (`/app/data/proddb`)
- **Location**: Inside the Docker container filesystem
- **Dialect**: H2Dialect (configured in `application-prod.properties`)

### H2 URL Parameters Explained
```properties
jdbc:h2:file:/app/data/proddb;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

| Parameter | Purpose |
|-----------|---------|
| `file:/app/data/proddb` | Store database in file (not memory) |
| `AUTO_SERVER=TRUE` | Allow multiple connections |
| `DB_CLOSE_DELAY=-1` | Keep database open |
| `DB_CLOSE_ON_EXIT=FALSE` | Don't close on last connection close |

---

## ⚠️ CRITICAL LIMITATIONS

### 1. Data Loss Scenarios

Your data will be **PERMANENTLY LOST** when:

| Event | When It Happens | Data Lost? |
|-------|----------------|------------|
| 🔄 Container restart | Cloud Run auto-scales down to 0 instances | ✅ YES |
| 🚀 New deployment | You push code changes | ✅ YES |
| 📈 Scale up/down | Cloud Run creates new instances | ✅ YES |
| 💥 Container crash | Application error causes restart | ✅ YES |
| ⏰ Idle timeout | No requests for several minutes | ✅ YES |
| 🔧 Configuration change | Update environment variables | ✅ YES |

### 2. Cloud Run Ephemeral Storage

Cloud Run containers are **stateless and ephemeral**:
- Container filesystem is **read/write** but **temporary**
- No persistent volumes available in Cloud Run
- Each new container starts with fresh filesystem
- Files written during runtime are lost on restart

### 3. Scaling Limitations

- **Cannot scale horizontally**: File locking prevents multiple instances
- **Single instance only**: Cloud Run will create only 1 instance
- **Performance bottleneck**: Single file-based database

### 4. Backup Impossibility

- ❌ No automatic backups
- ❌ No point-in-time recovery
- ❌ No replication
- ❌ No disaster recovery

---

## What Data Persists?

### ✅ Data That Survives
- During active container lifetime
- Between requests while container is running
- While Cloud Run keeps the same instance alive

### ❌ Data That DOES NOT Survive
- Container restarts
- New deployments
- Scale up/down events
- Crashes or errors
- Idle timeouts (Cloud Run scales to 0)

---

## H2 Console Access

The H2 console is temporarily enabled for debugging.

### Access URL
```
https://your-cloud-run-url/h2-console
```

### Connection Settings
```
JDBC URL:    jdbc:h2:file:/app/data/proddb
User Name:   sa
Password:    (leave empty)
Driver:      org.h2.Driver
```

### Security Note
⚠️ **DISABLE H2 Console** when migrating to PostgreSQL:
```properties
spring.h2.console.enabled=false
```

---

## Testing Checklist

Use this temporary H2 setup to validate:

- [ ] Application starts successfully on Cloud Run
- [ ] Flyway migrations execute correctly
- [ ] Authentication endpoints work (register/login)
- [ ] JWT token generation and validation
- [ ] CRUD endpoints for Inmobiliarias, Propiedades, Personas
- [ ] Agent endpoints respond correctly
- [ ] Vertex AI integration works
- [ ] Frontend serves correctly from `/`
- [ ] Health checks pass (`/actuator/health`)
- [ ] H2 console accessible (for debugging)

---

## Migration Path to PostgreSQL

### When to Migrate?

Migrate to PostgreSQL **IMMEDIATELY after** initial testing confirms:
- ✅ Application deploys successfully
- ✅ All endpoints work correctly
- ✅ Authentication functions properly
- ✅ No critical deployment issues

### Migration Steps

See `DEPLOYMENT-PRODUCTION.md` for detailed PostgreSQL setup.

**Quick migration checklist:**

1. **Set up Supabase PostgreSQL**
   ```bash
   # Create account at https://supabase.com
   # Create new project
   # Copy connection string
   ```

2. **Update application-prod.properties**
   ```properties
   # Comment out H2 configuration
   # Uncomment PostgreSQL configuration
   spring.datasource.url=${DATABASE_URL}
   spring.datasource.username=${DATABASE_USERNAME}
   spring.datasource.password=${DATABASE_PASSWORD}
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   ```

3. **Update GitHub Secrets**
   ```bash
   # Add to GitHub repository secrets:
   DATABASE_URL=jdbc:postgresql://...
   DATABASE_USERNAME=postgres
   DATABASE_PASSWORD=your-secure-password
   ```

4. **Update Cloud Run environment variables**
   ```bash
   gcloud run services update inmobiliaria-api \
     --region=us-central1 \
     --set-env-vars="DATABASE_URL=jdbc:postgresql://..." \
     --set-env-vars="DATABASE_USERNAME=postgres" \
     --set-env-vars="DATABASE_PASSWORD=your-secure-password"
   ```

5. **Disable H2 Console**
   ```properties
   spring.h2.console.enabled=false
   ```

6. **Redeploy**
   ```bash
   git add .
   git commit -m "Migrate to PostgreSQL production database"
   git push origin main
   ```

7. **Verify Migration**
   ```bash
   # Check logs
   gcloud run services logs read inmobiliaria-api --region=us-central1

   # Test endpoints
   curl https://your-cloud-run-url/actuator/health
   ```

---

## FAQ

### Q: Can I use persistent volumes in Cloud Run?
**A:** No. Cloud Run does not support persistent volumes. Use Cloud SQL, Supabase, or another managed database.

### Q: Will data survive if I don't deploy for a while?
**A:** No. Cloud Run scales to 0 after idle time, destroying the container and all data.

### Q: Can I export data before migrating?
**A:** Only if you do it before the container restarts. Use the H2 console to export data, but don't rely on this.

### Q: How long does a container stay alive?
**A:** Unpredictable. Cloud Run may keep it alive for minutes or hours, but there's no guarantee.

### Q: Can I prevent container restarts?
**A:** No. Cloud Run is designed for stateless applications. Restarts are normal and expected.

### Q: Should I use this for demos?
**A:** Only if you can recreate test data quickly. Better to migrate to PostgreSQL first.

---

## Summary

| Aspect | H2 Temporary | PostgreSQL Production |
|--------|-------------|---------------------|
| Data persistence | ❌ Ephemeral | ✅ Permanent |
| Survives restarts | ❌ No | ✅ Yes |
| Survives deployments | ❌ No | ✅ Yes |
| Horizontal scaling | ❌ No | ✅ Yes |
| Backups | ❌ No | ✅ Yes |
| Production ready | ❌ NO | ✅ YES |
| Use case | 🧪 Testing only | 🚀 Production |

---

## Next Steps

1. ✅ Complete initial deployment with H2
2. ✅ Validate all functionality works
3. ⚠️ **Migrate to PostgreSQL ASAP**
4. ✅ Test PostgreSQL deployment
5. ✅ Disable H2 console
6. ✅ Update documentation

**Remember:** This H2 setup is a stepping stone, not a destination. Plan your PostgreSQL migration now.
