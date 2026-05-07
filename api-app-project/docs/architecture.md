# Elite CSP API – Architecture

## Overview

The Elite CSP backend is a serverless architecture built on AWS Lambda and API Gateway. It is organized as a Maven multi-module project so that each functional domain is independently deployable.

```
api-app-project/              ← Parent POM (packaging=pom)
├── api-app-contact/          ← Contact & Job-Application Lambda
│   └── src/
│       ├── main/java/ca/elitecsp/
│       │   ├── common/       ← Shared utilities (exceptions, response builder, JSON, validation)
│       │   └── contact/      ← Contact handler, model, service, validation
│       └── test/java/...
│
└── api-app-job/              ← Job (S3 Excel job listings/details) Lambda
    └── src/
        ├── main/java/ca/elitecsp/job/
        │   ├── handler/      ← JobLambdaHandler
        │   ├── model/        ← JobSummaryDto, JobDetailsDto
        │   ├── parser/       ← ExcelJobParserService
        │   └── service/      ← S3FileLoaderService, JobService
        └── test/java/...
```

## Module Dependency Graph

```
api-app-project (POM)
├── api-app-contact (JAR / Lambda fat-jar)
│   └── AWS SES SDK v2, AWS Lambda, Jackson, Jakarta Mail, SLF4J, Lombok
└── api-app-job (JAR / Lambda fat-jar)
    └── AWS S3 SDK v2, AWS Lambda, Jackson, Apache POI, SLF4J, Lombok
```

## Lambda Execution Flow

### api-app-contact

1. API Gateway forwards the HTTP request to Lambda
2. `LambdaHandler.handleRequest` parses and validates the JSON body
3. Dispatches to contact or job-application processing based on `type` field
4. `EmailService` constructs a MIME message (plain-text + HTML, optional attachment)
5. Sends the message via Amazon SES
6. Returns a structured JSON response (`{ success, message, error }`)

### api-app-job

1. API Gateway forwards the HTTP request to Lambda
2. `JobLambdaHandler.handleRequest` routes to list (`GET /jobs`) or detail (`GET /jobs/{jobId}`)
3. `S3FileLoaderService.loadExcelFile` fetches the configured Excel object from S3
4. `ExcelJobParserService.parseWorkbook` validates sheets and maps rows to DTOs
5. `JobService` merges summary/detail records and returns JSON responses

## Security

- No hardcoded credentials – all AWS clients use the default SDK credential chain (Lambda execution role)
- Required environment variables are validated at startup
- User-supplied content is HTML-escaped before inclusion in email templates
- Excel parsing validates required sheets/columns and skips empty/malformed rows safely

## Build

```bash
cd api-app-project
mvn clean package
```

Each module produces a self-contained fat-JAR via `maven-shade-plugin`, ready to upload to AWS Lambda.
