# Production Deployment Guide

## Prerequisites

- AWS resources provisioned (S3, CloudFront, API Gateway, Lambda, SES)
- Required GitHub secrets/variables configured
- Java 21 and Node 22 compatibility preserved

## Deployment Flow

1. Merge to `main`.
2. GitHub Actions builds backend and frontend.
3. Lambda code updates first.
4. Frontend assets deploy to S3.
5. CloudFront invalidates `/index.html`.

## Verification Checklist

- [ ] API contact submission returns 200 for valid payload.
- [ ] API validation failure returns 400 with structured error.
- [ ] Contact and job notification emails are delivered.
- [ ] Frontend routes and localized pages render correctly.
- [ ] CloudWatch logs show no sustained error spikes.
