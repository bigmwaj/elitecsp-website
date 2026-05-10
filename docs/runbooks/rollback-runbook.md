# Rollback Runbook

## Frontend Rollback

1. Re-run last known-good workflow run, or
2. Re-deploy prior frontend artifact to S3.
3. Invalidate `/index.html` in CloudFront.

## Backend Rollback

1. Re-deploy previous Lambda artifact/version.
2. Wait for function update completion.
3. Validate API health and SES delivery behavior.

## Post-Rollback Validation

- Contact form success and validation failure cases.
- Job application with valid CV.
- CloudWatch logs stable.
