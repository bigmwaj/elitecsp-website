# Repository Analysis Summary

## Scope

- Frontend: `ui-app` (Angular 21, standalone components, signals)
- Backend: `api-app-project` (Java 21 Maven multi-module)
  - `api-app-common`
  - `api-app-contact`
- CI/CD: `.github/workflows/deploy.yml`

## Architecture Summary

- SPA hosted on S3 and distributed by CloudFront.
- API Gateway exposes `POST /contacts`.
- Lambda handler (`ContactLambdaHandler`) validates payloads and sends emails via SES.
- Shared utilities and response handling are centralized in `api-app-common`.

## Module and Dependency Map

- `ui-app` depends on Angular core/router/forms/http, RxJS, ngx-translate.
- `api-app-contact` depends on `api-app-common`, AWS Lambda Java libs, AWS SDK v2 SES, Jakarta Mail.
- `api-app-common` provides response builder, exception model, validation, constants, JSON utilities.

## Coupling and Abstractions

- Good separation: frontend and backend are independently deployable.
- Good backend abstraction: `EmailService` interface with `SesEmailService` implementation.
- Moderate coupling risk: frontend payload shape includes API-Gateway-like wrapper (`{ body, isBase64Encoded }`) in service layer.

## Duplication and Dead/Low-Value Code

- Duplicate route entry in frontend: two identical `path: 'services'` entries.
- Service duplication: `contact.service.ts` and `application.service.ts` share almost identical logic.
- `CommonLambdaHandler` currently empty (low-value abstraction).
- `ErrorCode` includes values not currently exercised (`MISSING_REQUIRED_PARAM`, `UNSUPPORTED_LANGUAGE`).

## Security Observations

- Critical: API key is hardcoded in both Angular environment files.
- CORS origin is configurable and no longer wildcard by default; verify production `CORS_ALLOW` value.
- Backend validates file type with extension + magic bytes and escapes HTML in templates.
- Frontend dependency audit reports 4 known vulnerabilities (3 moderate, 1 high), mostly transitive dev-time dependencies.

## Performance and Maintainability Risks

- Attachment can be decoded in both parsing and validation paths (potential duplicate CPU/memory work).
- Several component subscriptions are not lifecycle-managed (leak/readability risk).
- No active Angular unit test target in the current workspace configuration.
- Logging can include email addresses and request previews (PII/log-sensitivity review needed).

## Current Baseline Validation

- Backend: `mvn clean test` passes (61 tests in contact module + 30 in common module).
- Frontend: `npm ci && npm run build -- --configuration=production` passes.

