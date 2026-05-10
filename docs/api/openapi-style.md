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

### Frontend Integration Note

Current frontend services send an API-Gateway-like wrapper:

```json
{
  "body": "{...json string...}",
  "isBase64Encoded": false
}
```

The backend currently parses the request body into `ContactRequest`, so this wrapper should be revalidated/aligned to avoid contract drift.

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
