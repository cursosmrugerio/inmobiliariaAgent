# Frontend Development Documentation

**React 18 + TypeScript 5 + Vite 4 + Material-UI 5**

This section contains all documentation related to frontend development for the Inmobiliaria Management System.

**Last Updated:** 2025-10-31

---

## 🚀 Quick Start

### For Active Development

**IMPORTANT:** Always use the Vite dev server during development, NOT production builds.

**Terminal 1 - Backend:**
```bash
cd /path/to/backend
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd /path/to/backend/frontend
npm run dev
```

**Access:** http://localhost:5173

✅ **You get:**
- Instant hot reload (HMR)
- TypeScript errors in real-time
- Fast feedback loop
- API calls proxied to backend:8080

**See:** [development-workflow.md](development-workflow.md) for complete details

---

## 📚 Documentation Files

### Essential Reading (Start Here)

#### [development-workflow.md](development-workflow.md) ⭐
**Purpose:** Explains the two development modes and when to use each
**Audience:** All frontend developers
**Read this first if:**
- You're new to the project
- You're not sure which mode to use
- HMR isn't working

**Contents:**
- Mode 1: Full Development Mode (recommended) - Vite dev server
- Mode 2: Production-like Mode - Testing final builds
- Troubleshooting guide
- Scripts reference

---

#### [production-build.md](production-build.md)
**Purpose:** Build and deployment workflows for production
**Audience:** Frontend developers, DevOps
**Read this if:**
- Deploying to production
- Testing production builds locally
- Setting up CI/CD

**Contents:**
- Three build approaches (manual, Docker, CI/CD)
- GitHub Actions integration
- Verification steps
- Troubleshooting

---

### Implementation Guides

#### [FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md](FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md)
**Purpose:** Patterns and best practices for implementing CRUD operations
**Audience:** Frontend developers implementing features
**Read this if:**
- Building new CRUD pages
- Need React Hook Form + Yup patterns
- Working with Material-UI components

**Contents:**
- Component structure patterns
- Form validation with Yup
- API integration patterns
- Material-UI usage examples
- TypeScript type safety

---

#### [FRONTEND-CRUD-TESTING-PLAN.md](FRONTEND-CRUD-TESTING-PLAN.md)
**Purpose:** Testing strategy for frontend features
**Audience:** Frontend developers, QA engineers
**Read this if:**
- Writing frontend tests
- Setting up E2E tests
- Need testing best practices

**Contents:**
- Unit testing approach
- Integration testing
- E2E testing with Playwright
- Test coverage goals

---

### Troubleshooting

#### [CACHE-CLEARING-GUIDE.md](CACHE-CLEARING-GUIDE.md)
**Purpose:** Resolve browser caching issues
**Audience:** All frontend developers
**Read this if:**
- Changes aren't appearing in browser
- Seeing stale content
- HMR not working

**Contents:**
- Hard refresh keyboard shortcuts
- DevTools cache clearing
- Frontend rebuild steps
- Vite dev server restart

---

### Technical Specification

#### [../reference/FRONTEND-IMPLEMENTATION-PLAN.md](../reference/FRONTEND-IMPLEMENTATION-PLAN.md)
**Purpose:** Comprehensive frontend architecture and specification (66KB)
**Audience:** Frontend team leads, architects, new team members
**Read this if:**
- Need complete technical overview
- Planning new features
- Understanding system architecture

**Contents:**
- Complete tech stack details
- Architecture decisions
- Component specifications
- API integration patterns
- Authentication flow
- Deployment strategy
- Comprehensive feature list

---

## 🏗️ Project Structure

```
backend/frontend/
├── src/
│   ├── components/        # Reusable UI components
│   ├── pages/             # Page components (feature-based)
│   │   ├── Inmobiliarias/
│   │   ├── Propiedades/
│   │   └── Personas/
│   ├── services/          # API integration layer
│   ├── types/             # TypeScript interfaces/types
│   ├── hooks/             # Custom React hooks
│   ├── utils/             # Utility functions
│   └── App.tsx            # Root component
│
├── public/                # Static assets
├── tests/                 # E2E tests (Playwright)
├── package.json
├── vite.config.ts
└── tsconfig.json
```

---

## 🛠️ Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Framework** | React | 18.2 |
| **Language** | TypeScript | 5.2 |
| **Build Tool** | Vite | 4.4 |
| **UI Library** | Material-UI | 5.14 |
| **Form Management** | React Hook Form | 7.65 |
| **Validation** | Yup | 1.7 |
| **HTTP Client** | Axios | 1.5 |
| **Router** | React Router DOM | 6.15 |
| **Testing** | Playwright | 1.48 |

---

## ⚡ Common Tasks

### Start Development Server
```bash
cd frontend
npm run dev
```
Access at: http://localhost:5173

### Type Check
```bash
npm run type-check
```

### Build for Production
```bash
npm run build
```
Output: `../src/main/resources/static/`

### Run Tests
```bash
npm run test  # E2E tests
```

### Preview Production Build
```bash
npm run build
npm run preview
```

---

## 🎯 Development Workflow

### Typical Development Session

1. **Start backend** (Terminal 1):
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Start frontend** (Terminal 2):
   ```bash
   cd frontend
   npm run dev
   ```

3. **Make changes** - Files save automatically, browser updates instantly

4. **Test changes** at http://localhost:5173

5. **Run type check** before committing:
   ```bash
   npm run type-check
   ```

---

## 🚨 Common Pitfalls to Avoid

### ❌ DON'T: Run `npm run build` during development
**Problem:** No hot reload, slow feedback loop
**Solution:** Use `npm run dev` instead

### ❌ DON'T: Access localhost:8080 for frontend development
**Problem:** No HMR, must rebuild after every change
**Solution:** Use localhost:5173 (Vite dev server)

### ❌ DON'T: Commit build artifacts to Git
**Problem:** Pollutes version control, merge conflicts
**Solution:** Build artifacts (in `src/main/resources/static/`) are gitignored

### ✅ DO: Use Vite dev server for all active development
**Why:** Instant feedback, HMR, TypeScript checking

### ✅ DO: Test production builds before deploying
**How:** See [production-build.md](production-build.md)

### ✅ DO: Run type-check before committing
**Why:** Catch TypeScript errors early

---

## 🔧 Troubleshooting

### HMR Not Working?

1. **Restart Vite dev server:**
   ```bash
   pkill -f "vite"
   npm run dev
   ```

2. **Hard refresh browser:**
   - Mac: `Cmd+Shift+R`
   - Windows/Linux: `Ctrl+Shift+R`

3. **Check Vite config** - Ensure polling is enabled:
   ```typescript
   // vite.config.ts
   server: {
     watch: {
       usePolling: true,
       interval: 300
     }
   }
   ```

**See:** [CACHE-CLEARING-GUIDE.md](CACHE-CLEARING-GUIDE.md) for detailed steps

---

### Changes Not Appearing?

1. **Verify you're on localhost:5173** (NOT localhost:8080)
2. **Check Vite console** for errors
3. **Hard refresh browser**
4. **Restart Vite dev server**

---

### TypeScript Errors?

1. **Run type checker:**
   ```bash
   npm run type-check
   ```

2. **Check imports** - Use path aliases:
   ```typescript
   import { api } from '@services/api';  // ✅ Correct
   import { api } from '../../../services/api';  // ❌ Avoid
   ```

3. **Verify types** - Check `src/types/` for DTOs

---

## 📖 Related Documentation

### Backend Integration
- [../README-AGENT.md](../README-AGENT.md) - Backend API reference
- [../VERTEX-AI.md](../VERTEX-AI.md) - Conversational agents setup
- [../reference/README-TESTING.md](../reference/README-TESTING.md) - Test backend APIs

### Deployment
- [production-build.md](production-build.md) - Frontend build process
- [../production/DEPLOYMENT-PRODUCTION.md](../production/DEPLOYMENT-PRODUCTION.md) - Full deployment guide
- [../production/CRITICAL-PRODUCTION-CHECKLIST.md](../production/CRITICAL-PRODUCTION-CHECKLIST.md) - Pre-deployment checklist

### Project Standards
- [../../CLAUDE.md](../../CLAUDE.md) - **Project Constitution** - Code style, architecture, standards
- [FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md](FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md) - Implementation patterns

---

## 🎓 Learning Path

**For new frontend developers:**

1. **Day 1: Setup & Overview**
   - Read [development-workflow.md](development-workflow.md)
   - Set up dev environment
   - Run `npm run dev` and explore

2. **Day 2: Architecture**
   - Review [../reference/FRONTEND-IMPLEMENTATION-PLAN.md](../reference/FRONTEND-IMPLEMENTATION-PLAN.md)
   - Study existing pages in `src/pages/`
   - Understand API integration patterns

3. **Day 3: Implementation**
   - Read [FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md](FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md)
   - Build a simple CRUD page
   - Follow form validation patterns

4. **Day 4: Testing**
   - Review [FRONTEND-CRUD-TESTING-PLAN.md](FRONTEND-CRUD-TESTING-PLAN.md)
   - Write tests for your feature
   - Learn Playwright basics

5. **Day 5: Production**
   - Read [production-build.md](production-build.md)
   - Test production build locally
   - Understand CI/CD workflow

---

## 🚀 Production Deployment

Frontend builds are **automatically deployed** via GitHub Actions:

1. **Make changes** to frontend code
2. **Commit and push** to main branch
3. **GitHub Actions automatically:**
   - Installs Node.js
   - Runs `npm ci`
   - Runs `npm run build`
   - Builds Docker image (with fresh frontend)
   - Deploys to Cloud Run

**No manual build steps required!**

**See:** [production-build.md](production-build.md) for details

---

## 📞 Support & Resources

### External Documentation
- **React 18:** https://react.dev
- **TypeScript:** https://www.typescriptlang.org/docs/
- **Vite:** https://vitejs.dev
- **Material-UI:** https://mui.com/material-ui/
- **React Hook Form:** https://react-hook-form.com
- **Yup:** https://github.com/jquense/yup
- **Playwright:** https://playwright.dev

### Internal Resources
- **Backend API:** [../README-AGENT.md](../README-AGENT.md)
- **Project Standards:** [../../CLAUDE.md](../../CLAUDE.md)
- **Testing Guide:** [../reference/README-TESTING.md](../reference/README-TESTING.md)

---

## 🔄 Documentation Updates

**Last Major Update:** 2025-10-31

**Recent Changes:**
- ✅ Consolidated frontend documentation
- ✅ Created comprehensive index
- ✅ Added troubleshooting guides
- ✅ Clarified development vs production workflows

---

**Ready to start developing? Begin with [development-workflow.md](development-workflow.md)!** 🚀
