# api-app-common

Shared backend module used by Lambda implementations.

## Responsibilities

- `ApiResponseBuilder` for consistent API Gateway responses
- `ApiException` and `ErrorCode` for structured error handling
- `ValidationUtils` for field/email/file validation
- `JsonUtils` for JSON serialization/deserialization
- `Constants` for headers/content-types and CORS resolution
- `EmailTemplateLoader` for classpath template rendering

## Notes

- CORS origin is resolved from `cors.allow` system property, then `CORS_ALLOW` env var, then default.
- Built and tested as part of parent project reactor.
