# API Documentation (OpenAPI-Style)

## OpenAPI Summary

```yaml
openapi: 3.0.3
info:
  title: Elite CSP Contact API
  version: 1.0.0
servers:
  - url: https://<api-id>.execute-api.<region>.amazonaws.com/prod
paths:
  /contacts:
    post:
      security:
        - ApiKeyAuth: []
      requestBody:
        required: true
      responses:
        '200': { description: Success }
        '400': { description: Validation error }
        '403': { description: Invalid/missing API key }
        '500': { description: Internal error }
components:
  securitySchemes:
    ApiKeyAuth:
      type: apiKey
      in: header
      name: x-api-key
```

## Endpoint

### `POST /contacts`

Handles both contact and job application workflows.

### Request Model (logical)

```json
{
  "type": "CONTACT | JOB_APPLICATION",
  "name": "string",
  "email": "string",
  "phone": "string",
  "company": "string",
  "city": "string",
  "subject": "string",
  "message": "string",
  "attachment": "base64 string",
  "attachmentFileName": "resume.pdf"
}
```

### Canonical Request Contract

The backend handler parses `request.body` directly into `ContactRequest`, so the canonical client payload for `/contacts` is the logical JSON model above (raw contact/application fields, not wrapped).

### Current Frontend Implementation Note (Known Gap)

Current frontend services construct and send an API-Gateway-like wrapper object:

```json
{
  "body": "{...json string...}",
  "isBase64Encoded": false
}
```

This wrapper shape is not the backend contract and is tracked as a contract-alignment remediation item in `docs/developer-guides/code-quality-sonar-style.md` and `docs/reports/prioritized-remediation-plan.md`.

### Response Envelope

```json
{
  "success": true,
  "message": "...",
  "error": null
}
```

### Error Codes

- `MISSING_REQUIRED_FIELD`
- `VALIDATION_ERROR`
- `INVALID_EMAIL`
- `INVALID_FILE_TYPE`
- `FILE_TOO_LARGE`
- `JSON_PARSE_ERROR`
- `EMAIL_SEND_FAILURE`
- `INTERNAL_ERROR`

### CORS

`Access-Control-Allow-Origin` is derived from runtime config (`cors.allow` / `CORS_ALLOW` / default).
