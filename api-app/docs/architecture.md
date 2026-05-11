# Elite CSP API – Architecture

## Overview

The Elite CSP backend is a serverless architecture built on AWS Lambda and API Gateway. It is organized as a Maven multi-module project so that each functional domain is independently deployable.

```
api-app-project/              ← Parent POM (packaging=pom)
├── api-app-common/           ← Shared utilities (exceptions, response builder, JSON, validation)
└── api-app-contact/          ← Contact & Job-Application Lambda
    └── src/
        ├── main/java/ca/elitecsp/contact/
        │   ├── handler/      ← LambdaHandler
        │   ├── model/        ← ContactRequest, ApplicationPayload
        │   ├── service/      ← ContactService, EmailService
        │   └── validation/   ← Input validators
        └── test/java/...
```

## Module Dependency Graph

```
api-app-project (POM)
└── api-app-contact (JAR / Lambda fat-jar)
    └── AWS SES SDK v2, AWS Lambda, Jackson, Jakarta Mail, SLF4J, Lombok
```

## Lambda Execution Flow

### api-app-contact

1. API Gateway forwards the HTTP request to Lambda
2. `LambdaHandler.handleRequest` parses and validates the JSON body
3. Dispatches to contact or job-application processing based on `type` field
4. `EmailService` constructs a MIME message (plain-text + HTML, optional attachment)
5. Sends the message via Amazon SES
6. Returns a structured JSON response (`{ success, message, error }`)

> **Note:** Job listings and detail data are served as static TypeScript files embedded in the Angular
> frontend (`job-summaries.data.ts`, `job-details.data.ts`). There is no backend Lambda for job data.

## Security

- No hardcoded credentials – all AWS clients use the default SDK credential chain (Lambda execution role)
- Required environment variables are validated at startup
- User-supplied content is HTML-escaped before inclusion in email templates

## Build

```bash
cd api-app-project
mvn clean package
```

Each module produces a self-contained fat-JAR via `maven-shade-plugin`, ready to upload to AWS Lambda.
