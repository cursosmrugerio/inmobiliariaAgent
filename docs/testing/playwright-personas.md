# Playwright Personas CRUD Test

This guide explains how to run the automated end-to-end regression that covers the Personas CRUD flow in the frontend.

## Prerequisites

- Backend application running locally on `http://localhost:8080`. Start it from the project root with:

  ```bash
  ./mvnw spring-boot:run
  ```

- Node.js 20+ available on your PATH (the frontend already targets that runtime).
- Network access to install dev dependencies (for `@playwright/test`) and download browser binaries the first time you set up Playwright.

## One-Time Setup

All commands run from `frontend/`.

1. Install project dependencies (this will also fetch `@playwright/test` that was added to `devDependencies`):

   ```bash
  npm install
   ```

2. Install the Playwright browser binaries:

   ```bash
  npx playwright install
   ```

## Configurable Environment Variables

The test uses sensible defaults, but you can override them before running the suite:

| Variable | Default | Purpose |
| --- | --- | --- |
| `PLAYWRIGHT_BASE_URL` | `http://localhost:8080` | URL where the web app is served. |
| `PLAYWRIGHT_ADMIN_EMAIL` | `admin@test.com` | Login used for the flow. |
| `PLAYWRIGHT_ADMIN_PASSWORD` | `admin123` | Password for the login. |

Example:

```bash
export PLAYWRIGHT_BASE_URL=http://localhost:8081
export PLAYWRIGHT_ADMIN_EMAIL=my.user@example.com
export PLAYWRIGHT_ADMIN_PASSWORD=s3cret
```

## Running the Test

With the backend already running and environment variables set (if needed):

```bash
npm run test:e2e
```

This command executes the spec found at `frontend/tests/e2e/personas-crud.spec.ts`, which performs:

1. Login using the configured credentials.
2. Navigation to the Personas page.
3. Creation of a unique persona record.
4. Update of the same record (phone number).
5. Deletion of the persona to leave the dataset unchanged.

Playwright is configured to produce an HTML report in `frontend/playwright-report/` and to capture traces, screenshots, and videos on failures. After a run you can inspect the report with:

```bash
npx playwright show-report
```

## Troubleshooting

- **Dependency installation fails (`ENOTFOUND`)**: ensure the sandbox or environment allows outbound access to `registry.npmjs.org`. Retry `npm install` once connectivity is available.
- **Browser download fails**: run `npx playwright install --with-deps` in an environment that has the required OS libraries.
- **Test fails due to login**: verify that the seeded admin credentials still exist or update the environment variables to match current users.

When modifying the Personas UI, update the selectors in `frontend/tests/e2e/personas-crud.spec.ts` accordingly and rerun `npm run test:e2e` to confirm the regression passes.
