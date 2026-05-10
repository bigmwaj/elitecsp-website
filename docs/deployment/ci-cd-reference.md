# CI/CD Documentation

Workflow: `.github/workflows/deploy.yml`

## Stages

1. `backend-build`
   - Java 21 setup
   - `mvn clean test`
   - `mvn clean package`
   - upload Lambda jar artifact
2. `frontend-build`
   - Node 22 setup
   - `npm ci`
   - production Angular build
   - upload frontend build artifact
3. `deploy-backend-lambdas`
   - download jar artifact
   - configure AWS credentials
   - update Lambda function code
4. `deploy-frontend`
   - download frontend artifact
   - S3 sync with cache strategy
   - optional CloudFront invalidation (`/index.html`)

## Artifact Flow

- `lambda-api-app-contact` → consumed by backend deploy job.
- `ui-app-dist` → consumed by frontend deploy job.

## CloudFront Caching Strategy

- Hashed assets: long-lived immutable cache.
- `index.html`: no-cache to force new app shell fetch.
- Invalidation path: `/index.html`.

## Rollback Strategy (Operational)

- Frontend: re-run known-good workflow or restore previous build artifact.
- Backend: re-deploy prior Lambda version/artifact.
