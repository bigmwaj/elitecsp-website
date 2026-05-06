# Deployment Guide — Elite CSP Website

## Table of Contents

1. [Overview](#1-overview)
2. [Frontend Deployment (Angular → S3)](#2-frontend-deployment-angular--s3)
   - [CI/CD via GitHub Actions](#21-cicd-via-github-actions)
   - [Manual Deployment](#22-manual-deployment)
3. [Backend Deployment (Lambda JAR)](#3-backend-deployment-lambda-jar)
   - [First-Time Setup](#31-first-time-setup)
   - [Updating an Existing Function](#32-updating-an-existing-function)
4. [CI/CD Workflow Details](#4-cicd-workflow-details)
5. [Rollback Strategy](#5-rollback-strategy)
   - [Frontend Rollback](#51-frontend-rollback)
   - [Backend Rollback](#52-backend-rollback)
6. [Post-Deployment Verification](#6-post-deployment-verification)
7. [Environment Promotion](#7-environment-promotion)

---

## 1. Overview

The project uses a split deployment strategy:

| Module | Deployment Trigger | Target |
|---|---|---|
| `ui-app` (Angular) | Push to `main` branch (GitHub Actions) | AWS S3 + CloudFront invalidation |
| `api-app` (Lambda) | Manual CLI / AWS Console | AWS Lambda |

The frontend is fully automated via CI/CD. The backend requires a manual deploy step because Lambda deployments may affect live traffic and benefit from deliberate review.

---

## 2. Frontend Deployment (Angular → S3)

### 2.1 CI/CD via GitHub Actions

**Trigger:** Every push to the `main` branch.

**Workflow file:** `.github/workflows/deploy.yml`

**Steps executed automatically:**

1. **Checkout** — clone the repository
2. **Set up Node.js 22** — with npm cache
3. **Install dependencies** — `npm ci` (clean install from lockfile)
4. **Build Angular app** — `npm run build -- --configuration=production`
5. **Configure AWS credentials** — using `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` secrets
6. **Deploy to S3** — `aws s3 sync` with cache-control headers:
   - Hashed static assets (`.js`, `.css`, images): `Cache-Control: public, max-age=31536000, immutable`
   - `index.html`: `Cache-Control: no-cache, no-store, must-revalidate`
7. **Invalidate CloudFront cache** — `aws cloudfront create-invalidation --paths "/*"` (only if `CLOUDFRONT_DISTRIBUTION_ID` is set)

**Required repository configuration:**

| Secret/Variable | Type | Value |
|---|---|---|
| `AWS_ACCESS_KEY_ID` | Secret | IAM user access key |
| `AWS_SECRET_ACCESS_KEY` | Secret | IAM user secret key |
| `AWS_REGION` | Variable | e.g., `ca-central-1` |
| `S3_BUCKET_NAME` | Variable | S3 bucket name (no `s3://` prefix) |
| `CLOUDFRONT_DISTRIBUTION_ID` | Variable | CloudFront distribution ID (optional) |

See [Configuration Guide](./configuration-guide.md#4-github-actions-secrets--variables) for setup instructions.

---

### 2.2 Manual Deployment

If you need to deploy the frontend outside of CI/CD:

```bash
# 1. Build
cd ui-app
npm ci
npm run build -- --configuration=production

# 2. Configure AWS credentials (if not already configured)
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
export AWS_REGION=ca-central-1

# 3. Deploy hashed assets (1-year cache)
aws s3 sync dist/ui-app/browser/ s3://YOUR_BUCKET_NAME/ \
  --delete \
  --cache-control "public, max-age=31536000, immutable" \
  --exclude "index.html"

# 4. Deploy index.html (no cache)
aws s3 cp dist/ui-app/browser/index.html s3://YOUR_BUCKET_NAME/index.html \
  --cache-control "no-cache, no-store, must-revalidate"

# 5. Invalidate CloudFront (if applicable)
aws cloudfront create-invalidation \
  --distribution-id YOUR_DISTRIBUTION_ID \
  --paths "/*"
```

---

## 3. Backend Deployment (Lambda JAR)

### 3.1 First-Time Setup

**Prerequisites:**
- Java 17+, Maven 3.8+
- AWS CLI configured with appropriate IAM permissions
- Lambda execution role with `AWSLambdaBasicExecutionRole` + SES permissions

```bash
# 1. Build the fat JAR
cd api-app
mvn clean package
# Output: target/elite-csp-contact.jar

# 2. Create the Lambda function
aws lambda create-function \
  --function-name elite-csp-contact \
  --runtime java17 \
  --role arn:aws:iam::ACCOUNT_ID:role/elite-csp-lambda-role \
  --handler ca.elitecsp.contact.handler.LambdaHandler::handleRequest \
  --zip-file fileb://target/elite-csp-contact.jar \
  --timeout 30 \
  --memory-size 512

# 3. Set environment variables
aws lambda update-function-configuration \
  --function-name elite-csp-contact \
  --environment "Variables={
    FROM_EMAIL=no-reply@elitecsp.ca,
    DESTINATION_EMAIL=info@elitecsp.ca,
    AWS_REGION=ca-central-1
  }"
```

---

### 3.2 Updating an Existing Function

```bash
# 1. Build
cd api-app
mvn clean package

# 2. Upload new JAR
aws lambda update-function-code \
  --function-name elite-csp-contact \
  --zip-file fileb://target/elite-csp-contact.jar

# 3. Wait for the update to complete
aws lambda wait function-updated \
  --function-name elite-csp-contact

# 4. (Recommended) Publish a new version
aws lambda publish-version \
  --function-name elite-csp-contact \
  --description "v$(date +%Y%m%d-%H%M%S)"

# 5. (Recommended) Update the 'prod' alias to the new version
aws lambda update-alias \
  --function-name elite-csp-contact \
  --name prod \
  --function-version $NEW_VERSION
```

> **Best practice:** Use a Lambda alias (`prod`) pointing to a published version. This enables instant rollback by updating the alias to the previous version without re-uploading any code.

---

## 4. CI/CD Workflow Details

### Workflow: `.github/workflows/deploy.yml`

```yaml
name: Build and Deploy to AWS S3
on:
  push:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    permissions:
      contents: read
    steps:
      - Checkout repository
      - Set up Node.js 22 (with npm cache)
      - npm ci (install from lockfile)
      - npm run build (production)
      - Configure AWS credentials
      - aws s3 sync (with cache headers)
      - aws cloudfront create-invalidation (if configured)
```

**Workflow diagram:**

```
Push to main
     │
     ▼
GitHub Actions runner (ubuntu-latest)
     │
     ├─ Checkout code
     ├─ node setup (v22) + npm ci
     ├─ ng build --configuration=production
     │     → dist/ui-app/browser/
     │
     ├─ Configure AWS credentials
     │
     ├─ aws s3 sync (hashed assets, 1yr cache)
     ├─ aws s3 cp index.html (no-cache)
     │
     └─ aws cloudfront create-invalidation (optional)
```

**Build artifacts:**

| Artifact | Location | Cache policy |
|---|---|---|
| Hashed JS/CSS/images | `dist/ui-app/browser/` (all except `index.html`) | `max-age=31536000, immutable` |
| `index.html` | `dist/ui-app/browser/index.html` | `no-cache, no-store, must-revalidate` |

---

## 5. Rollback Strategy

### 5.1 Frontend Rollback

**Option A: Re-trigger a previous workflow run**

1. Go to GitHub → **Actions** → **Build and Deploy to AWS S3**
2. Find the last known-good workflow run
3. Click **Re-run all jobs**

This re-deploys the exact code from that commit.

**Option B: Revert the commit and push**

```bash
# Revert the last commit
git revert HEAD
git push origin main
# → CI/CD will automatically re-deploy the reverted code
```

**Option C: Manual rollback to a previous build**

If you have a previous build artifact available:

```bash
aws s3 sync /path/to/previous-dist/ s3://YOUR_BUCKET_NAME/ \
  --delete \
  --cache-control "public, max-age=31536000, immutable" \
  --exclude "index.html"

aws s3 cp /path/to/previous-dist/index.html s3://YOUR_BUCKET_NAME/index.html \
  --cache-control "no-cache, no-store, must-revalidate"

aws cloudfront create-invalidation \
  --distribution-id YOUR_DISTRIBUTION_ID \
  --paths "/*"
```

---

### 5.2 Backend Rollback

**Using Lambda aliases (recommended):**

```bash
# List published versions
aws lambda list-versions-by-function \
  --function-name elite-csp-contact \
  --query 'Versions[*].{Version:Version,Description:Description}'

# Roll back the 'prod' alias to a previous version
aws lambda update-alias \
  --function-name elite-csp-contact \
  --name prod \
  --function-version PREVIOUS_VERSION_NUMBER
```

This takes effect immediately with **zero downtime** — no code re-upload required.

**Without aliases (fallback):**

```bash
# Re-upload the previous JAR from source control
git checkout PREVIOUS_GOOD_COMMIT -- api-app/
cd api-app
mvn clean package

aws lambda update-function-code \
  --function-name elite-csp-contact \
  --zip-file fileb://target/elite-csp-contact.jar
```

---

## 6. Post-Deployment Verification

After deploying, verify the following:

### Frontend

- [ ] Open the website in a browser (hard refresh with `Ctrl+Shift+R` to bypass cache)
- [ ] Navigate through all pages: Home, Services, About, Partners, Careers, Contact
- [ ] Check that the language switcher works (FR/EN)
- [ ] Check browser DevTools → Network tab for any `404` errors on assets

### Backend (Lambda)

- [ ] Submit a test contact form and verify the notification email arrives
- [ ] Submit a test job application with a PDF CV and verify the email arrives with attachment
- [ ] Check CloudWatch Logs for the Lambda function — no ERROR entries
- [ ] Test an intentional validation error (empty name) — should return HTTP 400 with a proper JSON error

### Quick smoke test via cURL

```bash
# Contact form — should return 200
curl -X POST https://YOUR_API_URL/contacts \
  -H "Content-Type: application/json" \
  -H "x-api-key: YOUR_API_KEY" \
  -d '{
    "fullName": "Deploy Test",
    "email": "test@example.com",
    "message": "Post-deployment smoke test."
  }'

# Expected: { "success": true, "message": "Your message has been sent successfully.", "error": null }
```

---

## 7. Environment Promotion

The recommended promotion flow from development to production:

```
feature branch
     │
     ▼ PR + code review
main branch
     │ (auto-deploy via GitHub Actions)
     ▼
S3 + CloudFront (frontend)

     │ (manual, after verification)
     ▼
Lambda update (backend)
```

**Checklist before promoting to production:**

- [ ] All CI/CD checks pass on the PR
- [ ] Contact form submission tested in development/test environment
- [ ] Job application tested with a PDF and DOCX attachment
- [ ] No new Critical or Major issues in the code quality report
- [ ] API key rotated if it was previously exposed
- [ ] CORS origin updated if the domain changed
- [ ] CloudWatch alarms reviewed — no active alerts
