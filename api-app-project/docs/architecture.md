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
└── api-app-cms/              ← CMS (S3 XML job listings) Lambda
    └── src/
        ├── main/java/ca/elitecsp/cms/
        │   ├── handler/      ← CmsLambdaHandler
        │   ├── model/        ← CmsRequest, JobDto, JobListXml
        │   └── service/      ← S3Service, XmlParserService
        └── test/java/...
```

## Module Dependency Graph

```
api-app-project (POM)
├── api-app-contact (JAR / Lambda fat-jar)
│   └── AWS SES SDK v2, AWS Lambda, Jackson, Jakarta Mail, SLF4J, Lombok
└── api-app-cms (JAR / Lambda fat-jar)
    └── AWS S3 SDK v2, AWS Lambda, Jackson + XML, SLF4J, Lombok
```

## Lambda Execution Flow

### api-app-contact

1. API Gateway forwards the HTTP request to Lambda
2. `LambdaHandler.handleRequest` parses and validates the JSON body
3. Dispatches to contact or job-application processing based on `type` field
4. `EmailService` constructs a MIME message (plain-text + HTML, optional attachment)
5. Sends the message via Amazon SES
6. Returns a structured JSON response (`{ success, message, error }`)

### api-app-cms

1. API Gateway forwards the HTTP request to Lambda
2. `CmsLambdaHandler.handleRequest` parses `bucketName` and `fileKey`
3. `S3Service.downloadAsString` fetches the XML file from the specified S3 object
4. `XmlParserService.parse` maps the XML `<jobs>/<job>` elements to `JobDto` objects
5. Returns a JSON array of job objects

## Security

- No hardcoded credentials – all AWS clients use the default SDK credential chain (Lambda execution role)
- Required environment variables are validated at startup
- User-supplied content is HTML-escaped before inclusion in email templates
- XML parsing uses Jackson's `XmlMapper` which is safe against XXE by default

## Build

```bash
cd api-app-project
mvn clean package
```

Each module produces a self-contained fat-JAR via `maven-shade-plugin`, ready to upload to AWS Lambda.
