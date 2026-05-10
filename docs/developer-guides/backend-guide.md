# Backend Developer Guide (`api-app-project`)

## Module layout

- `api-app-common`: shared utility and response classes
- `api-app-contact`: Lambda handler + SES email service

## Request handling flow

- parse request body
- validate fields/files
- route by `ContactType`
- send notification + optional confirmation
- map exceptions to structured API response

## Java 21 and Lambda notes

- Build enforces Java 21+.
- Keep object allocations in hot paths low (especially attachment handling).
- Ensure exception messages are safe for API consumers.
