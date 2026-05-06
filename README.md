# Elite CSP Website

> **Elite CSP** is a Canadian IT consulting firm specializing in IBM Maximo EAM (Enterprise Asset Management) implementations, integrations, and support.

This repository contains the full-stack source code for the Elite CSP corporate website, composed of two independent modules:

| Module | Technology | Purpose |
|---|---|---|
| [`ui-app`](./ui-app/) | Angular 21 (TypeScript) | Frontend SPA deployed to AWS S3 + CloudFront |
| [`api-app`](./api-app/) | Java 17 · AWS Lambda | Serverless backend handling contact and job-application form submissions via Amazon SES |

---

## Architecture Overview

```
Browser
  │
  ▼
CloudFront (CDN) ──► S3 Bucket (Angular SPA)
  │
  │ API calls (HTTPS + x-api-key)
  ▼
API Gateway (REST)
  │
  ▼
AWS Lambda (Java 17)
  │
  ▼
Amazon SES (Transactional Email)
```

See [`docs/architecture.md`](./docs/architecture.md) for a detailed architecture description.

---

## Quick Start

### Frontend (ui-app)

```bash
cd ui-app
npm install
npm start          # Serve locally at http://localhost:4200
npm run build      # Production build → dist/ui-app/browser/
```

### Backend (api-app)

```bash
cd api-app
mvn clean package  # Produces target/elite-csp-contact.jar
```

For full local development setup, see the [Configuration Guide](./docs/configuration-guide.md).

---

## Documentation

| Document | Description |
|---|---|
| [Architecture](./docs/architecture.md) | System design, data flow, and component responsibilities |
| [API Specification](./docs/api-spec.md) | REST endpoint reference with request/response examples |
| [User Guide](./docs/user-guide.md) | How to use the contact form and job application feature |
| [Configuration Guide](./docs/configuration-guide.md) | Environment variables, AWS setup, local dev, and production |
| [Deployment Guide](./docs/deployment-guide.md) | CI/CD pipeline, build steps, S3/Lambda deployment, rollback |
| [Code Quality Report](./docs/code-quality-report.md) | SonarQube-style static analysis findings and recommendations |

---

## Module READMEs

- [`ui-app/README.md`](./ui-app/README.md) — Angular frontend developer guide
- [`api-app/README.md`](./api-app/README.md) — Lambda backend developer guide

---

## Repository Structure

```
elitecsp-website/
├── .github/
│   └── workflows/
│       └── deploy.yml          # CI/CD: build Angular + deploy to S3
├── api-app/                    # Java 17 AWS Lambda backend
│   ├── pom.xml
│   └── src/main/java/ca/elitecsp/
│       ├── common/             # Shared utilities, models, exception handling
│       └── contact/            # Contact/job-application Lambda handler
├── ui-app/                     # Angular 21 frontend SPA
│   ├── package.json
│   └── src/app/
│       ├── components/         # Reusable UI components
│       ├── interceptors/       # HTTP interceptors
│       ├── models/             # TypeScript interfaces
│       ├── pages/              # Routed page components
│       └── services/           # Angular services
├── docs/                       # Project-level documentation
└── README.md                   # This file
```

---

## CI/CD

Pushes to the `main` branch automatically:
1. Build the Angular app with `npm run build -- --configuration=production`
2. Sync the build output to AWS S3
3. Invalidate the CloudFront distribution cache

The Lambda JAR must be deployed manually (see [Deployment Guide](./docs/deployment-guide.md)).

---

## Technology Stack

**Frontend**
- Angular 21 with standalone components and signals
- `@ngx-translate/core` for French/English internationalization
- SCSS for styling
- Angular build (`@angular/build:application` / esbuild)

**Backend**
- Java 17, Maven
- AWS Lambda (RequestHandler pattern)
- AWS SDK v2 for SES
- Jackson for JSON, Lombok for boilerplate reduction, SLF4J for logging
- Jakarta Mail for MIME multipart email construction

**Infrastructure**
- AWS S3 (static hosting)
- AWS CloudFront (CDN + HTTPS)
- AWS API Gateway (REST proxy)
- AWS Lambda (serverless compute)
- Amazon SES (transactional email)
- GitHub Actions (CI/CD)

---

## License

Proprietary — © Elite CSP. All rights reserved.
