# System Architecture Overview

## High-Level Topology

Browser → CloudFront → S3 (Angular SPA)

Browser → API Gateway (`POST /contacts`, `x-api-key`) → Lambda (`ContactLambdaHandler`) → SES

## Components

- `ui-app`:
  - Standalone Angular app with lazy-loaded routes.
  - Reactive forms for contact and job application flows.
  - Functional interceptor for API key injection.
- `api-app-contact`:
  - Request parsing/validation and routing by `ContactType`.
  - Notification + confirmation email orchestration.
- `api-app-common`:
  - Shared validation, JSON, error, constants, and API response abstraction.

## Architecture Style

- Frontend: SPA + component/service layering.
- Backend: layered Lambda handler/service/utility structure.
- Infrastructure: serverless AWS managed services.
