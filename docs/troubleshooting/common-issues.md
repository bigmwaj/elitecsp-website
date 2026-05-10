# Troubleshooting Guide

## Build/CI Issues

### Angular build fails with dependency issues
- Run: `cd ui-app && npm ci`
- Rebuild: `npm run build -- --configuration=production`

### Maven build fails on Java version
- Ensure Java 21 is active (`JAVA_HOME` set to JDK 21).
- Run from `api-app-project`.

## Runtime/API Issues

### 403 from API Gateway
- Verify `x-api-key` is present and valid.
- Confirm usage plan and stage key mapping.

### 400 JSON parse error
- Ensure body is valid JSON and `Content-Type: application/json`.

### Attachment validation failure
- Accept only PDF/DOCX and <= 5 MB.
- Validate file extension + magic bytes.

### SES send failures
- Verify SES identity status and region.
- Confirm Lambda role permissions and sender/destination constraints.

## Frontend Functional Issues

### Careers page metadata or data anomalies
- Review subscription lifecycle and translation updates.

### Contact/job form submits but user gets no clear error feedback
- Add user-facing error state bound to API error response.
