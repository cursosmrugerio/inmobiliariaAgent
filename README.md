# Sistema de Gestión Inmobiliaria y Arrendamientos

Una plataforma robusta y flexible para la administración integral de carteras inmobiliarias y contratos de arrendamiento, potenciada con agentes conversacionales de IA.

---

## 🚀 Quick Start

**Choose your path:**

| I want to... | Go here |
|--------------|---------|
| 🆕 **Understand the system** | [docs/README-AGENT.md](docs/README-AGENT.md) |
| 🤖 **Build a new agent** (fast) | [docs/reference/GENERATOR-QUICK-START.md](docs/reference/GENERATOR-QUICK-START.md) |
| 📖 **Build a new agent** (manual) | [docs/AGENT-DEVELOPMENT-GUIDE.md](docs/AGENT-DEVELOPMENT-GUIDE.md) |
| ⚙️ **Set up my environment** | [docs/VERTEX-AI.md](docs/VERTEX-AI.md) |
| 🧪 **Test the system** | [docs/reference/README-TESTING.md](docs/reference/README-TESTING.md) |
| 🚀 **Deploy to production** | [docs/production/CRITICAL-PRODUCTION-CHECKLIST.md](docs/production/CRITICAL-PRODUCTION-CHECKLIST.md) |
| 📊 **Check current status** | [docs/reference/AGENT-STATUS.md](docs/reference/AGENT-STATUS.md) |

---

## 💡 What's This Project?

This system allows real estate agencies to:

- ✅ Manage property portfolios
- ✅ Configure dynamic payment concepts (rent, services, maintenance)
- ✅ Automate late payment penalties
- ✅ Track payments in detail
- ✅ Maintain complete financial history
- ✅ **Interact via natural language AI agents** (Spanish & English)

Instead of manually constructing API calls, users can chat with AI agents:

```
User: "List all agencies"
Agent: "I found 3 agencies: Inmobiliaria del Norte, Propiedades..."

User: "Create a new agency called Inmobiliaria Premium"
Agent: "Agency created successfully with ID 4"
```

---

## 🏗️ Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 25 |
| **Framework** | Spring Boot | 3.5.7 |
| **AI Agent SDK** | Google ADK | 0.3.0 |
| **LLM** | Gemini 2.0 Flash | Latest |
| **Platform** | Vertex AI | Current |
| **Database (Dev)** | H2 | In-memory |
| **Database (Prod)** | PostgreSQL | 16+ |
| **Build Tool** | Maven | 3.9.10 |

---

## 📚 Documentation

### By Role

#### 👨‍💻 Backend Developer

1. [docs/README-AGENT.md](docs/README-AGENT.md) - Architecture overview
2. [docs/AGENT-DEVELOPMENT-GUIDE.md](docs/AGENT-DEVELOPMENT-GUIDE.md) - Build agents step-by-step
3. [docs/reference/GENERATOR-QUICK-START.md](docs/reference/GENERATOR-QUICK-START.md) - Use the generator (2 minutes)
4. [docs/reference/AGENT-STATUS.md](docs/reference/AGENT-STATUS.md) - Current system status

#### 🔧 DevOps / Infrastructure

1. [docs/VERTEX-AI.md](docs/VERTEX-AI.md) - Vertex AI setup & authentication
2. [docs/reference/GOOGLE-CREDENTIALS-SETUP.md](docs/reference/GOOGLE-CREDENTIALS-SETUP.md) - Detailed credentials guide
3. [docs/production/CRITICAL-PRODUCTION-CHECKLIST.md](docs/production/CRITICAL-PRODUCTION-CHECKLIST.md) - **START HERE** ⚠️
4. [docs/production/DEPLOYMENT-PRODUCTION.md](docs/production/DEPLOYMENT-PRODUCTION.md) - Cloud Run deployment

#### 🎨 Frontend Developer

1. [docs/reference/FRONTEND-IMPLEMENTATION-PLAN.md](docs/reference/FRONTEND-IMPLEMENTATION-PLAN.md) - React frontend spec
2. [docs/README-AGENT.md](docs/README-AGENT.md) - Backend API endpoints
3. [docs/reference/README-TESTING.md](docs/reference/README-TESTING.md) - Test backend APIs

#### 🧪 QA / Testing

1. [docs/reference/README-TESTING.md](docs/reference/README-TESTING.md) - Testing workflow
2. [docs/reference/AGENT-STATUS.md](docs/reference/AGENT-STATUS.md) - Test pass rates
3. [scripts/test-agent_inmobiliarias.sh](scripts/test-agent_inmobiliarias.sh) - Run automated tests (43 tests)

---

## 🤖 Available AI Agents

| Agent | Endpoint | Purpose | Status |
|-------|----------|---------|--------|
| **InmobiliariaAgent** | `/api/agent/inmobiliarias/chat` | Manage real estate agencies | ✅ Production Ready |
| **PropiedadAgent** | `/api/agent/propiedades/chat` | Manage properties | ✅ Production Ready |
| **PersonaAgent** | `/api/agent/personas/chat` | Manage contacts/people | ✅ Production Ready |

**Test Pass Rate:** 43/43 (100% with Vertex AI credentials)

---

## ⚡ Quick Setup

### 1. Prerequisites

```bash
# Required
- Java 25
- Maven 3.9+
- Google Cloud project with Vertex AI enabled
- Service account credentials JSON file
```

### 2. Environment Variables

```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/credentials.json"
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=your-project-id
export GOOGLE_CLOUD_LOCATION=us-central1
```

### 3. Run Application

```bash
mvn spring-boot:run
```

### 4. Test an Agent

```bash
curl -X POST http://localhost:8080/api/agent/inmobiliarias/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "List all agencies"}'
```

**For detailed setup:** [docs/VERTEX-AI.md](docs/VERTEX-AI.md)

---

## 🛠️ Create Your Own Agent (2 Minutes!)

We have a generator that creates 90% of the code for you:

```bash
./scripts/generate-crud-agent.sh
```

Answer 8 simple questions → Get production-ready code!

**See:** [docs/reference/GENERATOR-QUICK-START.md](docs/reference/GENERATOR-QUICK-START.md)

---

## 📂 Project Structure

```
backend/
├── src/main/java/com/inmobiliaria/gestion/
│   ├── agent/                      # AI agents (ADK integration)
│   │   ├── tools/                  # FunctionTools wrappers
│   │   ├── config/                 # Spring configuration
│   │   └── controller/             # REST endpoints
│   ├── inmobiliaria/               # Agency domain
│   ├── propiedad/                  # Property domain
│   └── persona/                    # Person domain
│
├── docs/                           # All documentation
│   ├── README-AGENT.md             # Start here
│   ├── AGENT-DEVELOPMENT-GUIDE.md  # Comprehensive guide
│   ├── VERTEX-AI.md                # Setup guide
│   ├── reference/                  # Detailed docs
│   └── production/                 # Deployment docs
│
├── scripts/
│   ├── generate-crud-agent.sh      # Agent generator
│   └── test-agent_inmobiliarias.sh # Test suite
│
└── CLAUDE.md                       # Project standards & guidelines
```

---

## 🎯 Key Features

### Conversational AI Agents
- Natural language interface (Spanish & English)
- Full CRUD operations through conversation
- Partial update support (only change what you need)
- Session management (context maintained)
- Delete confirmation flows

### Enterprise Features
- Spring Boot 3.5.7 with Java 25
- Virtual Threads for high concurrency
- H2 for development, PostgreSQL for production
- Comprehensive test suite (43 automated tests)
- Production-ready deployment (Cloud Run)

### Developer Experience
- Automated agent generator (2-minute setup)
- Step-by-step development guide
- Comprehensive documentation
- Pre-configured security
- CI/CD with GitHub Actions

---

## 📖 Important Documents

| Document | Purpose |
|----------|---------|
| [CLAUDE.md](CLAUDE.md) | **Project constitution** - coding standards, architecture principles |
| [docs/DOCUMENTATION-REVIEW.md](docs/DOCUMENTATION-REVIEW.md) | Documentation structure analysis |
| [docs/reference/AGENT-STATUS.md](docs/reference/AGENT-STATUS.md) | Current system status & capabilities |

---

## 🔒 Security Notes

- ⚠️ **Development mode:** Agent endpoints are currently unauthenticated
- ⚠️ **Never commit** `credentials.json` (already in `.gitignore`)
- ⚠️ **Production deployment:** See [docs/production/CRITICAL-PRODUCTION-CHECKLIST.md](docs/production/CRITICAL-PRODUCTION-CHECKLIST.md)

---

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Agent Integration Tests
```bash
# Ensure app is running first
mvn spring-boot:run

# In another terminal
./scripts/test-agent_inmobiliarias.sh
```

**Expected:** 43/43 passing (requires Vertex AI credentials)

**See:** [docs/reference/README-TESTING.md](docs/reference/README-TESTING.md)

---

## 🚀 Deployment

**Production deployment guide:** [docs/production/DEPLOYMENT-PRODUCTION.md](docs/production/DEPLOYMENT-PRODUCTION.md)

**IMPORTANT:** Read [docs/production/CRITICAL-PRODUCTION-CHECKLIST.md](docs/production/CRITICAL-PRODUCTION-CHECKLIST.md) first!

Platform support:
- ✅ Google Cloud Run (recommended)
- ✅ Google Kubernetes Engine (GKE)
- ✅ Any Docker-compatible platform

---

## 🤝 Contributing

Before making changes, please review:

1. [CLAUDE.md](CLAUDE.md) - Project standards and architectural principles
2. [docs/AGENT-DEVELOPMENT-GUIDE.md](docs/AGENT-DEVELOPMENT-GUIDE.md) - Development guidelines

All contributions must:
- Follow Google Java Style Guide (enforced by formatter-maven-plugin)
- Include tests for new business logic
- Document new REST endpoints with OpenAPI annotations

---

## 📞 Support & Resources

### Documentation
- **Architecture:** [docs/README-AGENT.md](docs/README-AGENT.md)
- **Development:** [docs/AGENT-DEVELOPMENT-GUIDE.md](docs/AGENT-DEVELOPMENT-GUIDE.md)
- **Testing:** [docs/reference/README-TESTING.md](docs/reference/README-TESTING.md)
- **Troubleshooting:** [docs/reference/ADK-SETUP-FIXES.md](docs/reference/ADK-SETUP-FIXES.md)

### External Resources
- [Google ADK Documentation](https://google.github.io/adk-docs/)
- [Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs)
- [Spring Boot 3.5.7 Docs](https://docs.spring.io/spring-boot/docs/3.5.7/reference/html/)

---

## 📊 Project Status

| Component | Status | Notes |
|-----------|--------|-------|
| **Backend API** | ✅ Production Ready | Full CRUD for all entities |
| **AI Agents** | ✅ Production Ready | 3 agents operational |
| **Testing** | ✅ 100% Pass | 43 automated tests |
| **Documentation** | ✅ Complete | Comprehensive guides |
| **Generator** | ✅ Available | Creates agents in 2 min |
| **Frontend** | 📋 Planned | React spec available |
| **Production Deployment** | ⚠️ Security Hardening | Auth/rate limiting pending |

**Last Updated:** 2025-10-29

---

## 🎉 Getting Started Checklist

- [ ] Read [CLAUDE.md](CLAUDE.md) for project overview
- [ ] Set up environment following [docs/VERTEX-AI.md](docs/VERTEX-AI.md)
- [ ] Run `mvn spring-boot:run` and test an agent
- [ ] Review [docs/README-AGENT.md](docs/README-AGENT.md) for architecture
- [ ] Try the generator: `./scripts/generate-crud-agent.sh`
- [ ] Read [docs/AGENT-DEVELOPMENT-GUIDE.md](docs/AGENT-DEVELOPMENT-GUIDE.md)
- [ ] Join the team and start building! 🚀

---

**Happy coding!** 💻
