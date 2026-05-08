# Elite CSP API – Multi-Module Project

This directory contains the Maven multi-module parent project for the Elite CSP serverless backends.

## Requirements

- **Java 21** (Temurin 21 LTS recommended) — enforced at build time
- Maven 3.8+

## Modules

| Module | Description |
|---|---|
| `api-app-contact` | Lambda handler for contact-form and job-application submissions (SES email) |
| `api-app-job` | Lambda handler for reading Excel job data from S3 and exposing job listing/details APIs |

## Build

```bash
# From this directory (api-app-project/)
export JAVA_HOME=/path/to/jdk-21
mvn clean package
```

Generated artifacts:
- `api-app-contact/target/elite-csp-contact.jar`
- `api-app-job/target/elite-csp-job.jar`

## Dependency Versions

| Dependency | Version |
|---|---|
| Java | 21 |
| AWS Lambda Java Core | 1.4.0 |
| AWS Lambda Java Events | 3.16.1 |
| AWS SDK v2 | 2.31.50 |
| Jackson | 2.18.3 |
| Lombok | 1.18.46 |
| SLF4J | 2.0.17 |
| Eclipse Angus Jakarta Mail | 2.0.5 |
| JUnit Jupiter | 5.12.2 |
| Mockito | 5.17.0 |

## Module details

See each module's `README.md` for environment variables, Lambda handler class, and deployment instructions.

- [`api-app-contact/README.md`](api-app-contact/README.md)
- [`api-app-job/README.md`](api-app-job/README.md)

## Shared runtime configuration

The API response layer supports a configurable CORS origin.

| Setting | Type | Purpose | Fallback |
|---|---|---|---|
| `CORS_ALLOW` | Environment variable | Sets `Access-Control-Allow-Origin` for API responses | `https://www.eliteproservice-consulting.ca` |
| `cors.allow` | JVM system property | Overrides `CORS_ALLOW` when the JVM is started with `-Dcors.allow=...` | `CORS_ALLOW` |

Resolution order: JVM system property `cors.allow` → environment variable `CORS_ALLOW` → built-in default.

## Architecture

See [`docs/architecture.md`](docs/architecture.md) for the full architecture overview.
