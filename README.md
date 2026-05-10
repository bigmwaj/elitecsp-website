# Elite CSP Website

Full-stack corporate website for Elite CSP.

## Repository Modules

- `ui-app/` — Angular 21 SPA frontend
- `api-app-project/` — Java 21 Maven multi-module backend
  - `api-app-common`
  - `api-app-contact`

## Runtime Architecture

- Frontend assets: S3 + CloudFront
- API entry: API Gateway (`POST /contacts` with `x-api-key`)
- Backend execution: AWS Lambda (Java 21)
- Email delivery: Amazon SES

## Validation Baseline

- Backend: `cd api-app-project && mvn clean test`
- Frontend: `cd ui-app && npm ci && npm run build -- --configuration=production`

## Documentation

Primary docs index: [`docs/README.md`](docs/README.md)

Key documents:
- Repository analysis: [`docs/reports/repository-analysis-summary.md`](docs/reports/repository-analysis-summary.md)
- Sonar-style quality report: [`docs/developer-guides/code-quality-sonar-style.md`](docs/developer-guides/code-quality-sonar-style.md)
- Security review: [`docs/reports/security-review.md`](docs/reports/security-review.md)
- CI/CD reference: [`docs/deployment/ci-cd-reference.md`](docs/deployment/ci-cd-reference.md)
- User guides: [`docs/user-guides/README.md`](docs/user-guides/README.md)

## CI/CD

Workflow: `.github/workflows/deploy.yml`

Stages:
1. Backend build + tests + packaging
2. Frontend build
3. Lambda deployment
4. Frontend S3 deployment + optional CloudFront invalidation (`/index.html`)
