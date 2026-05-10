# Architecture and Deployment Diagrams

## System Diagram

```text
Browser
  ├─ CloudFront ─ S3 (Angular static assets)
  └─ API Gateway (x-api-key)
         └─ Lambda (ContactLambdaHandler)
               └─ SES (notification + confirmation emails)
```

## CI/CD Diagram

```text
Push main
  ├─ backend-build (mvn test/package) -> lambda artifact
  ├─ frontend-build (npm ci/build) -> dist artifact
  ├─ deploy-backend-lambdas (update-function-code)
  └─ deploy-frontend (s3 sync + cloudfront invalidation /index.html)
```
