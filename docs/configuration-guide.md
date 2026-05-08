# Configuration Guide — Elite CSP Website

## Table of Contents

1. [Environment Variables — Frontend (ui-app)](#1-environment-variables--frontend-ui-app)
2. [Environment Variables — Backend (api-app)](#2-environment-variables--backend-api-app)
3. [AWS Configuration](#3-aws-configuration)
   - [SES (Simple Email Service)](#31-ses-simple-email-service)
   - [Lambda](#32-lambda)
   - [API Gateway](#33-api-gateway)
   - [S3 (Static Hosting)](#34-s3-static-hosting)
   - [CloudFront (CDN)](#35-cloudfront-cdn)
4. [GitHub Actions Secrets & Variables](#4-github-actions-secrets--variables)
5. [Local Development Environment](#5-local-development-environment)
6. [Test Environment](#6-test-environment)
7. [Production Environment](#7-production-environment)

---

## 1. Environment Variables — Frontend (ui-app)

The Angular app reads configuration from environment files at **build time**.

### Files

| File | Used when |
|---|---|
| `src/environments/environment.ts` | `ng serve` (development) and `ng build --configuration=development` |
| `src/environments/environment.prod.ts` | `ng build --configuration=production` (default) |

### Variables

| Variable | Type | Description |
|---|---|---|
| `production` | boolean | Set to `true` in production build; enables Angular production mode |
| `apiUrl` | string | Base URL of the API Gateway endpoint (no trailing slash) |
| `apiKey` | string | API Gateway `x-api-key` value |

### Example (development)

```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'https://wkz8o24uc4.execute-api.ca-central-1.amazonaws.com/prod',
  apiKey: '__API_KEY_PLACEHOLDER__'
};
```

### Example (production)

```typescript
// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://ek7n4dkkde.execute-api.ca-central-1.amazonaws.com/prod',
  apiKey: '__API_KEY_PLACEHOLDER__'
};
```

> ⚠️ **Security:** Never commit real API key values. Replace `__API_KEY_PLACEHOLDER__` at build time using a CI secret (see [GitHub Actions Secrets](#4-github-actions-secrets--variables)).

---

## 2. Environment Variables — Backend (api-app)

The Lambda function reads configuration from environment variables **at runtime**. Set these on the Lambda function configuration.

| Variable | Required | Description | Example |
|---|---|---|---|
| `FROM_EMAIL` | ✅ | Verified SES sender address | `no-reply@elitecsp.ca` |
| `DESTINATION_EMAIL` | ✅ | Recipient for all notifications | `info@elitecsp.ca` |
| `AWS_REGION` | ✅ | AWS region of the SES endpoint | `ca-central-1` |

> **Note:** `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` are **not** required. The Lambda execution role provides credentials via the AWS default credential chain.

### Setting Lambda Environment Variables via AWS CLI

```bash
aws lambda update-function-configuration \
  --function-name elite-csp-contact \
  --environment "Variables={
    FROM_EMAIL=no-reply@elitecsp.ca,
    DESTINATION_EMAIL=info@elitecsp.ca,
    AWS_REGION=ca-central-1
  }"
```

### Setting via AWS Console

1. Open **AWS Lambda** → select `elite-csp-contact`
2. Click **Configuration** → **Environment variables**
3. Click **Edit** and add each variable

---

## 3. AWS Configuration

### 3.1 SES (Simple Email Service)

**Prerequisites:**
- The `FROM_EMAIL` domain must be verified in Amazon SES.
- If the SES account is in **sandbox mode**, `DESTINATION_EMAIL` must also be verified.
- To send to any address, request production access (move out of sandbox).

**Verify a domain in SES:**
1. Open **Amazon SES** in the AWS Console
2. Navigate to **Verified identities** → **Create identity**
3. Choose **Domain**, enter your domain, and follow the DNS verification steps
4. (Or) Choose **Email address** to verify individual addresses

**Required IAM permissions for the Lambda execution role:**
```json
{
  "Effect": "Allow",
  "Action": [
    "ses:SendEmail",
    "ses:SendRawEmail"
  ],
  "Resource": "*"
}
```

---

### 3.2 Lambda

**Runtime:** Java 21  
**Handler:** `ca.elitecsp.contact.handler.LambdaHandler::handleRequest`  
**Memory:** 512 MB (recommended; reduce to 256 MB after testing cold start times)  
**Timeout:** 30 seconds  
**Architecture:** x86_64

**Create the function:**
```bash
aws lambda create-function \
  --function-name elite-csp-contact \
  --runtime java21 \
  --role arn:aws:iam::<ACCOUNT_ID>:role/elite-csp-lambda-role \
  --handler ca.elitecsp.contact.handler.LambdaHandler::handleRequest \
  --zip-file fileb://api-app-project/api-app-contact/target/elite-csp-contact.jar \
  --timeout 30 \
  --memory-size 512
```

**Required IAM role trust policy:**
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "Service": "lambda.amazonaws.com" },
    "Action": "sts:AssumeRole"
  }]
}
```

**Required IAM role permissions:**
- `AWSLambdaBasicExecutionRole` (CloudWatch Logs)
- SES permissions (see above)

---

### 3.3 API Gateway

**Type:** REST API  
**Integration:** Lambda Proxy  
**Authentication:** API key

**Setup steps:**

1. Create a REST API in API Gateway
2. Create a resource `/contacts`
3. Add a `POST` method with Lambda Proxy integration pointing to `elite-csp-contact`
4. Enable API key required on the `POST` method
5. Create a usage plan and API key
6. Associate the API key with the usage plan
7. Deploy the API to a stage named `prod`

**Sample stage URL:**
```
https://<api-id>.execute-api.ca-central-1.amazonaws.com/prod
```

**CORS Configuration:**  
Enable CORS on the `/contacts` resource. For production, set the allowed origin to your CloudFront domain rather than `*`.

---

### 3.4 S3 (Static Hosting)

The S3 bucket hosts the Angular build output.

**Bucket settings:**
- Block all public access: **Disabled** (required for static hosting, unless using CloudFront OAC)
- Static website hosting: **Enabled**  
  - Index document: `index.html`  
  - Error document: `index.html` (required for SPA routing)

**Recommended: Use CloudFront Origin Access Control (OAC)**  
Keep the bucket private and serve content exclusively through CloudFront:

1. Create a CloudFront OAC
2. Attach it to the CloudFront distribution
3. Add a bucket policy allowing only CloudFront:
```json
{
  "Statement": [{
    "Effect": "Allow",
    "Principal": {
      "Service": "cloudfront.amazonaws.com"
    },
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::YOUR_BUCKET_NAME/*",
    "Condition": {
      "StringEquals": {
        "AWS:SourceArn": "arn:aws:cloudfront::<ACCOUNT_ID>:distribution/<DISTRIBUTION_ID>"
      }
    }
  }]
}
```

---

### 3.5 CloudFront (CDN)

**Distribution settings:**

| Setting | Value |
|---|---|
| Origin | S3 bucket (or OAC endpoint) |
| Default root object | `index.html` |
| Price class | `PriceClass_100` (North America + Europe) or `All` |
| HTTPS | Redirect HTTP to HTTPS |
| Custom domain (optional) | `www.elitecsp.ca` + ACM certificate |

**Custom error pages (required for Angular SPA routing):**

| HTTP Error Code | Response Page | HTTP Response Code |
|---|---|---|
| 403 | `/index.html` | 200 |
| 404 | `/index.html` | 200 |

---

## 4. GitHub Actions Secrets & Variables

Configure the following in the GitHub repository settings under **Settings → Secrets and variables → Actions**.

### Secrets (encrypted)

| Secret Name | Description |
|---|---|
| `AWS_ACCESS_KEY_ID` | IAM user access key for GitHub Actions deployments |
| `AWS_SECRET_ACCESS_KEY` | IAM user secret key |

### Variables (plain text)

| Variable Name | Description | Example |
|---|---|---|
| `AWS_REGION` | AWS region for deployment | `ca-central-1` |
| `S3_BUCKET_NAME` | S3 bucket name (no `s3://` prefix) | `elitecsp-website-prod` |
| `CLOUDFRONT_DISTRIBUTION_ID` | CloudFront distribution ID (optional, enables cache invalidation) | `E1ABCDEFGHIJKL` |

> Workflow note: backend Lambda function names are currently managed in workflow environment constants (`elite-csp-contact`, `elite-csp-jobs`).

### IAM Permissions for the CI/CD User

The GitHub Actions IAM user needs the following permissions (principle of least privilege):

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:PutObject", "s3:DeleteObject", "s3:ListBucket"],
      "Resource": [
        "arn:aws:s3:::elitecsp-website-prod",
        "arn:aws:s3:::elitecsp-website-prod/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": "cloudfront:CreateInvalidation",
      "Resource": "arn:aws:cloudfront::<ACCOUNT_ID>:distribution/<DISTRIBUTION_ID>"
    },
    {
      "Effect": "Allow",
      "Action": [
        "lambda:GetFunction",
        "lambda:UpdateFunctionCode"
      ],
      "Resource": [
        "arn:aws:lambda:<REGION>:<ACCOUNT_ID>:function:elite-csp-contact",
        "arn:aws:lambda:<REGION>:<ACCOUNT_ID>:function:elite-csp-jobs"
      ]
    }
  ]
}
```

---

## 5. Local Development Environment

### Prerequisites

| Tool | Minimum Version | Install |
|---|---|---|
| Node.js | 22 | [nodejs.org](https://nodejs.org/) |
| npm | 10 | Bundled with Node.js |
| Java JDK | 21 | [adoptium.net](https://adoptium.net/) |
| Apache Maven | 3.8 | [maven.apache.org](https://maven.apache.org/) |
| AWS CLI | 2.x (optional) | [aws.amazon.com/cli](https://aws.amazon.com/cli/) |

### Frontend Setup

```bash
cd ui-app
npm install
npm start
# → Application available at http://localhost:4200
```

The development environment (`environment.ts`) points to the development API Gateway URL. You can point it to a local mock or the development stage of API Gateway.

**Using a local API mock:**

To test form submissions without a real Lambda, you can use a tool like `json-server` or modify `environment.ts` to point to `http://localhost:3000`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:3000',
  apiKey: 'dev-key'
};
```

### Backend Setup

```bash
cd api-app-project
mvn clean package
# → Produces:
#    api-app-contact/target/elite-csp-contact.jar
#    api-app-job/target/elite-csp-job.jar
```

**Run Lambda locally with AWS SAM (optional):**

```yaml
# template.yaml (minimal SAM template)
AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31
Resources:
  ContactFunction:
    Type: AWS::Serverless::Function
    Properties:
      Handler: ca.elitecsp.contact.handler.LambdaHandler::handleRequest
      Runtime: java21
      CodeUri: target/elite-csp-contact.jar
      MemorySize: 512
      Timeout: 30
      Environment:
        Variables:
          FROM_EMAIL: test@example.com
          DESTINATION_EMAIL: dev@example.com
          AWS_REGION: ca-central-1
      Events:
        ContactApi:
          Type: Api
          Properties:
            Path: /contacts
            Method: post
```

```bash
sam build
sam local start-api --port 4000
```

---

## 6. Test Environment

The project has a separate API Gateway stage configured for testing. Update the development environment file to point to the test stage URL before running integration tests.

**Recommended test environment checklist:**
- [ ] Separate Lambda function or alias (e.g., `elite-csp-contact:test`)
- [ ] Separate API Gateway stage (`test`)
- [ ] Verified test email address in SES for `FROM_EMAIL` and `DESTINATION_EMAIL`
- [ ] SES sandbox mode acceptable (only verified addresses receive email)
- [ ] Separate API key for test stage

---

## 7. Production Environment

**Production environment checklist:**

### Frontend
- [ ] `environment.prod.ts` uses the production `apiUrl`
- [ ] `apiKey` is injected via `API_GATEWAY_KEY` GitHub secret at build time (not committed)
- [ ] Angular production build (`--configuration=production`) enables optimizations and minification

### Backend (Lambda)
- [ ] `FROM_EMAIL` domain is verified in SES and out of sandbox mode
- [ ] `DESTINATION_EMAIL` is a monitored mailbox
- [ ] Lambda memory and timeout tuned based on observed performance
- [ ] Lambda function version published and alias (`prod`) pointing to it

### API Gateway
- [ ] API key rotated (original key from source code is revoked)
- [ ] Usage plan with throttling configured (e.g., 10 requests/second, 1000/day)
- [ ] API Gateway stage logging enabled

### CloudFront
- [ ] Custom domain configured with ACM TLS certificate
- [ ] Custom error pages for 403/404 → `index.html` configured
- [ ] Cache behaviors reviewed (hashed assets: 1 year; `index.html`: no-cache)

### Security
- [ ] CORS origin in Lambda updated from `*` to `https://www.elitecsp.ca`
- [ ] AWS WAF rules attached to API Gateway to block malicious traffic
- [ ] CloudWatch alarms configured for Lambda errors and SES bounce rates
