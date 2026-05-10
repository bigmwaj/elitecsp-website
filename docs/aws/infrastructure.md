# AWS Infrastructure Documentation

## Services

- S3: static hosting origin for Angular assets.
- CloudFront: CDN distribution and HTTPS entry.
- API Gateway: REST endpoint with API key enforcement.
- Lambda: Java 21 execution of contact/job submission logic.
- SES: notification and confirmation email delivery.

## IAM Considerations

### Lambda Execution Role

Minimum permissions:
- CloudWatch Logs write (`AWSLambdaBasicExecutionRole`)
- SES send actions (`ses:SendEmail`, `ses:SendRawEmail`)

### CI/CD IAM Principal

Minimum permissions:
- S3 object sync operations for frontend deploy
- CloudFront invalidation
- Lambda get/update-function-code for target function

## Environment Variables

### Lambda

- `FROM_EMAIL` (required)
- `DESTINATION_EMAIL` (required)
- `AWS_REGION` (required)
- `CORS_ALLOW` (recommended explicit)

### GitHub Actions

- Secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
- Variables: `AWS_REGION`, `S3_BUCKET_NAME`, `CLOUDFRONT_DISTRIBUTION_ID`
