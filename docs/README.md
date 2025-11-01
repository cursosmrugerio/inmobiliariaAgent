# Documentation Index

**Sistema de Gestión Inmobiliaria y Arrendamientos**

Welcome to the comprehensive documentation for the Inmobiliaria Management System. This documentation is organized by role and topic to help you find what you need quickly.

**Last Updated:** 2025-10-31

---

## 🚀 Quick Start by Role

### 👨‍💻 Backend Developer

**New to the project?**
1. [README-AGENT.md](README-AGENT.md) - System architecture and features overview
2. [AGENT-DEVELOPMENT-GUIDE.md](AGENT-DEVELOPMENT-GUIDE.md) - Comprehensive development guide
3. [VERTEX-AI.md](VERTEX-AI.md) - Set up your development environment

**Building an agent?**
- **Fast:** [reference/GENERATOR-QUICK-START.md](reference/GENERATOR-QUICK-START.md) - 2-minute agent generation
- **Manual:** [AGENT-DEVELOPMENT-GUIDE.md](AGENT-DEVELOPMENT-GUIDE.md) - Step-by-step guide

**Testing:**
- [reference/README-TESTING.md](reference/README-TESTING.md) - Testing workflow and best practices
- [reference/AGENT-STATUS.md](reference/AGENT-STATUS.md) - Current system status and test results

---

### 🎨 Frontend Developer

**Getting started:**
1. [frontend/development-workflow.md](frontend/development-workflow.md) - **START HERE** - Development modes explained
2. [frontend/FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md](frontend/FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md) - Implementation patterns
3. [README-AGENT.md](README-AGENT.md) - Backend API reference

**Deployment:**
- [frontend/production-build.md](frontend/production-build.md) - Build and deployment workflows
- [frontend/CACHE-CLEARING-GUIDE.md](frontend/CACHE-CLEARING-GUIDE.md) - Browser cache troubleshooting

**Technical Spec:**
- [reference/FRONTEND-IMPLEMENTATION-PLAN.md](reference/FRONTEND-IMPLEMENTATION-PLAN.md) - Comprehensive frontend specification (React + TypeScript + MUI)

---

### 🔧 DevOps / Infrastructure Engineer

**Environment Setup:**
1. [VERTEX-AI.md](VERTEX-AI.md) - Vertex AI authentication and setup
2. [reference/GOOGLE-CREDENTIALS-SETUP.md](reference/GOOGLE-CREDENTIALS-SETUP.md) - Detailed credentials guide
3. [reference/ADK-SETUP-FIXES.md](reference/ADK-SETUP-FIXES.md) - Troubleshooting common setup issues

**Production Deployment:**
1. [production/CRITICAL-PRODUCTION-CHECKLIST.md](production/CRITICAL-PRODUCTION-CHECKLIST.md) - ⚠️ **READ THIS FIRST**
2. [production/DEPLOYMENT-PRODUCTION.md](production/DEPLOYMENT-PRODUCTION.md) - Complete Cloud Run deployment guide
3. [production/CONFIGURATION-COMPARISON.md](production/CONFIGURATION-COMPARISON.md) - Dev vs Production config
4. [production/README.md](production/README.md) - Production overview

**Database:**
- [production/H2-TEMPORARY-SETUP.md](production/H2-TEMPORARY-SETUP.md) - H2 configuration (development/testing)
- [production/DATABASE-PERSISTENCE-ANALYSIS.md](production/DATABASE-PERSISTENCE-ANALYSIS.md) - Persistence analysis
- [production/H2-SETUP-SUMMARY.md](production/H2-SETUP-SUMMARY.md) - Quick reference

---

### 🧪 QA / Testing Engineer

**Testing Workflow:**
1. [reference/README-TESTING.md](reference/README-TESTING.md) - **START HERE** - Complete testing guide
2. [reference/AGENT-STATUS.md](reference/AGENT-STATUS.md) - Current test status (43/43 passing)
3. [testing/playwright-personas.md](testing/playwright-personas.md) - E2E testing with Playwright

**Frontend Testing:**
- [frontend/FRONTEND-CRUD-TESTING-PLAN.md](frontend/FRONTEND-CRUD-TESTING-PLAN.md) - Frontend testing strategy

---

## 📚 Documentation by Topic

### AI Agents & Conversational Interfaces

| Document | Purpose | Audience |
|----------|---------|----------|
| [README-AGENT.md](README-AGENT.md) | Architecture overview & quick start | All developers |
| [AGENT-DEVELOPMENT-GUIDE.md](AGENT-DEVELOPMENT-GUIDE.md) | Step-by-step agent development | Backend developers |
| [reference/GENERATOR-QUICK-START.md](reference/GENERATOR-QUICK-START.md) | 2-minute agent generation | Backend developers |
| [reference/AGENT-STATUS.md](reference/AGENT-STATUS.md) | Current capabilities & test status | All developers, QA |
| [reference/AGENTS-OVERVIEW.md](reference/AGENTS-OVERVIEW.md) | Available agents summary | All |

**Key Features:**
- Natural language CRUD operations (Spanish & English)
- Gemini 2.0 Flash via Vertex AI
- Google ADK 0.3.0 integration
- 43/43 automated tests passing

---

### Environment & Infrastructure

| Document | Purpose | Audience |
|----------|---------|----------|
| [VERTEX-AI.md](VERTEX-AI.md) | Vertex AI setup & authentication | Developers, DevOps |
| [reference/GOOGLE-CREDENTIALS-SETUP.md](reference/GOOGLE-CREDENTIALS-SETUP.md) | Service account setup | DevOps, new developers |
| [reference/ADK-SETUP-FIXES.md](reference/ADK-SETUP-FIXES.md) | Troubleshooting guide | Developers |
| [production/CONFIGURATION-COMPARISON.md](production/CONFIGURATION-COMPARISON.md) | Environment configuration comparison | DevOps, developers |

---

### Frontend Development

| Document | Purpose | Audience |
|----------|---------|----------|
| [frontend/development-workflow.md](frontend/development-workflow.md) | **Development modes & workflows** | Frontend developers |
| [frontend/production-build.md](frontend/production-build.md) | Production build process | Frontend developers, DevOps |
| [frontend/FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md](frontend/FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md) | Implementation patterns | Frontend developers |
| [frontend/FRONTEND-CRUD-TESTING-PLAN.md](frontend/FRONTEND-CRUD-TESTING-PLAN.md) | Testing strategy | Frontend developers, QA |
| [frontend/CACHE-CLEARING-GUIDE.md](frontend/CACHE-CLEARING-GUIDE.md) | Cache troubleshooting | Frontend developers |
| [reference/FRONTEND-IMPLEMENTATION-PLAN.md](reference/FRONTEND-IMPLEMENTATION-PLAN.md) | Complete frontend specification | Frontend team, architects |

**Tech Stack:** React 18 + TypeScript 5 + Vite 4 + Material-UI 5

---

### Production Deployment

| Document | Purpose | Audience |
|----------|---------|----------|
| [production/README.md](production/README.md) | Production overview | All |
| [production/CRITICAL-PRODUCTION-CHECKLIST.md](production/CRITICAL-PRODUCTION-CHECKLIST.md) | ⚠️ **Pre-deployment checklist** | DevOps, leads |
| [production/DEPLOYMENT-PRODUCTION.md](production/DEPLOYMENT-PRODUCTION.md) | Cloud Run deployment guide | DevOps |
| [production/H2-TEMPORARY-SETUP.md](production/H2-TEMPORARY-SETUP.md) | H2 database setup | DevOps, developers |
| [production/DATABASE-PERSISTENCE-ANALYSIS.md](production/DATABASE-PERSISTENCE-ANALYSIS.md) | Database persistence analysis | DevOps, architects |

**Platform:** Google Cloud Run | PostgreSQL (Supabase) | GitHub Actions CI/CD

---

### Testing & Quality Assurance

| Document | Purpose | Audience |
|----------|---------|----------|
| [reference/README-TESTING.md](reference/README-TESTING.md) | Complete testing workflow | QA, developers |
| [reference/AGENT-STATUS.md](reference/AGENT-STATUS.md) | Test results & status | All |
| [testing/playwright-personas.md](testing/playwright-personas.md) | E2E testing guide | Frontend QA |
| [frontend/FRONTEND-CRUD-TESTING-PLAN.md](frontend/FRONTEND-CRUD-TESTING-PLAN.md) | Frontend testing plan | Frontend QA |

**Test Coverage:** 43/43 agent tests passing | Unit tests | Integration tests | E2E tests

---

### Reference & Troubleshooting

| Document | Purpose | Audience |
|----------|---------|----------|
| [reference/AGENT-STATUS.md](reference/AGENT-STATUS.md) | Current system capabilities | All |
| [reference/FIXES-SUMMARY.md](reference/FIXES-SUMMARY.md) | Historical fixes & changes | Developers |
| [reference/ADK-SETUP-FIXES.md](reference/ADK-SETUP-FIXES.md) | ADK troubleshooting | Developers |
| [DOCUMENTATION-REVIEW.md](DOCUMENTATION-REVIEW.md) | Documentation audit results | Documentation maintainers |

---

## 🏗️ Project Standards

**Before making any changes, review:**

- [../CLAUDE.md](../CLAUDE.md) - **Project Constitution** (coding standards, architecture principles, DDD patterns)
- [AGENT-DEVELOPMENT-GUIDE.md](AGENT-DEVELOPMENT-GUIDE.md) - Development best practices
- [README-AGENT.md](README-AGENT.md) - System architecture

**Key Standards:**
- ✅ Google Java Style Guide (enforced by formatter-maven-plugin)
- ✅ Domain-Driven Design (DDD) package structure
- ✅ OpenAPI documentation for all endpoints
- ✅ Tests required for new business logic
- ✅ TypeScript strict mode for frontend

---

## 📦 Archive

Historical documents and completed work:

| Document | Category | Last Relevant |
|----------|----------|---------------|
| [archive/URGENT-PRODUCTION-FIX.md](archive/URGENT-PRODUCTION-FIX.md) | Deployment | 2024-10 (Fixed in commit 545f8f8) |
| [archive/PRODUCTION-MIGRATION-GUIDE.md](archive/PRODUCTION-MIGRATION-GUIDE.md) | Deployment | 2024-10 (CI/CD updated) |
| [archive/FRONTEND-CRUD-IMPLEMENTATION-REPORT.md](archive/FRONTEND-CRUD-IMPLEMENTATION-REPORT.md) | Frontend | Implementation complete |
| [archive/validation/PHASE1-VALIDATION-REPORT.md](archive/validation/PHASE1-VALIDATION-REPORT.md) | QA | Phase 1 validated |
| [archive/validation/PHASE5-I18N-VALIDATION-REPORT.md](archive/validation/PHASE5-I18N-VALIDATION-REPORT.md) | QA | Phase 5 validated |
| [archive/COMPLETION-SUMMARY.md](archive/COMPLETION-SUMMARY.md) | Project | Session summary |
| [archive/FINAL-SETUP-SUMMARY.md](archive/FINAL-SETUP-SUMMARY.md) | Project | Setup complete |
| [archive/GENERATOR-SUMMARY.md](archive/GENERATOR-SUMMARY.md) | Tools | Generator documented |
| [archive/TEST-AGENT-INMO-README.md](archive/TEST-AGENT-INMO-README.md) | Testing | Legacy test docs |
| [archive/TEST-FAILURES-EXPLAINED.md](archive/TEST-FAILURES-EXPLAINED.md) | Testing | Historical failures |

---

## 🗺️ Documentation Structure

```
docs/
├── README.md                          # This file - main index
├── AGENT-DEVELOPMENT-GUIDE.md         # Comprehensive agent development guide
├── README-AGENT.md                    # Architecture overview
├── VERTEX-AI.md                       # Vertex AI setup
├── DOCUMENTATION-REVIEW.md            # Documentation audit
│
├── api/                               # API Documentation (future)
│
├── frontend/                          # Frontend Development
│   ├── development-workflow.md        # Dev modes & workflows ⭐
│   ├── production-build.md            # Build & deployment
│   ├── FRONTEND-CRUD-IMPLEMENTATION-GUIDE.md
│   ├── FRONTEND-CRUD-TESTING-PLAN.md
│   └── CACHE-CLEARING-GUIDE.md
│
├── production/                        # Deployment & Infrastructure
│   ├── README.md                      # Overview
│   ├── CRITICAL-PRODUCTION-CHECKLIST.md  # ⚠️ READ FIRST
│   ├── DEPLOYMENT-PRODUCTION.md       # Cloud Run guide
│   ├── CONFIGURATION-COMPARISON.md
│   ├── DATABASE-PERSISTENCE-ANALYSIS.md
│   ├── H2-TEMPORARY-SETUP.md
│   ├── H2-SETUP-SUMMARY.md
│   ├── Dockerfile
│   ├── application-prod.properties
│   ├── deployment/                    # Deployment scripts
│   └── github-workflows/              # CI/CD workflows
│
├── reference/                         # Detailed Reference
│   ├── README-TESTING.md              # Testing guide
│   ├── AGENT-STATUS.md                # Current status ⭐
│   ├── AGENTS-OVERVIEW.md             # Available agents
│   ├── GENERATOR-QUICK-START.md       # Quick agent generation
│   ├── GOOGLE-CREDENTIALS-SETUP.md
│   ├── ADK-SETUP-FIXES.md
│   ├── FIXES-SUMMARY.md
│   └── FRONTEND-IMPLEMENTATION-PLAN.md  # Complete frontend spec
│
├── archive/                           # Historical Documents
│   ├── validation/                    # Old validation reports
│   └── [completed deliverables]
│
└── testing/                           # Testing Documentation
    └── playwright-personas.md
```

---

## 🎯 Common Tasks - Quick Links

| I want to... | Go here |
|--------------|---------|
| 🆕 Understand the system | [README-AGENT.md](README-AGENT.md) |
| 🤖 Build a new agent (fast) | [reference/GENERATOR-QUICK-START.md](reference/GENERATOR-QUICK-START.md) |
| 📖 Build a new agent (manual) | [AGENT-DEVELOPMENT-GUIDE.md](AGENT-DEVELOPMENT-GUIDE.md) |
| ⚙️ Set up my environment | [VERTEX-AI.md](VERTEX-AI.md) |
| 💻 Start frontend development | [frontend/development-workflow.md](frontend/development-workflow.md) |
| 🧪 Test the system | [reference/README-TESTING.md](reference/README-TESTING.md) |
| 🚀 Deploy to production | [production/CRITICAL-PRODUCTION-CHECKLIST.md](production/CRITICAL-PRODUCTION-CHECKLIST.md) |
| 📊 Check current status | [reference/AGENT-STATUS.md](reference/AGENT-STATUS.md) |
| 🔧 Troubleshoot issues | [reference/ADK-SETUP-FIXES.md](reference/ADK-SETUP-FIXES.md) |
| 📝 Review standards | [../CLAUDE.md](../CLAUDE.md) |

---

## 📞 External Resources

- **Google ADK Documentation:** https://google.github.io/adk-docs/
- **Vertex AI Documentation:** https://cloud.google.com/vertex-ai/docs
- **Spring Boot 3.5.7 Reference:** https://docs.spring.io/spring-boot/docs/3.5.7/reference/html/
- **React 18 Documentation:** https://react.dev
- **Material-UI Documentation:** https://mui.com/material-ui/

---

## 🔄 Documentation Maintenance

**Last Major Reorganization:** 2025-10-31

**Recent Changes:**
- ✅ Reorganized docs by topic (frontend/, production/, reference/, archive/)
- ✅ Moved obsolete "urgent" docs to archive (issues already fixed)
- ✅ Consolidated frontend documentation
- ✅ Archived completed validation reports
- ✅ Created comprehensive role-based index

**Maintenance Guidelines:**
- Update dates when making significant changes
- Keep archive/ for historical reference, not active docs
- Follow naming convention: lowercase-with-hyphens.md
- Add new docs to appropriate category folder
- Update this index when adding new documentation

---

**Need help navigating? Check the Quick Start by Role section at the top!** 🚀
