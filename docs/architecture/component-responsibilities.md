# Component Responsibilities

## Frontend (`ui-app`)

- `pages/*`: page-level orchestration, metadata, form workflows.
- `components/*`: reusable presentational units.
- `services/*`: data access and local data providers.
- `interceptors/api-key.interceptor.ts`: cross-cutting auth header logic.

## Backend (`api-app-project`)

- `ContactLambdaHandler`: request entrypoint, parse/validate/route/response mapping.
- `ValidationUtil` + `ValidationUtils`: field/file validation.
- `SesEmailService`: SES transport and MIME handling.
- `EmailTemplateService` + `EmailTemplateLoader`: template rendering with HTML escaping.
- `ApiResponseBuilder`: consistent status/body/header output.
