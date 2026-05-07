# Elite CSP API – Multi-Module Project

This directory contains the Maven multi-module parent project for the Elite CSP serverless backends.

## Modules

| Module | Description |
|---|---|
| `api-app-contact` | Lambda handler for contact-form and job-application submissions (SES email) |
| `api-app-cms` | Lambda handler for reading XML job data from S3 and returning structured JSON |

## Build

```bash
# From this directory (api-app-project/)
mvn clean package
```

Generated artifacts:
- `api-app-contact/target/elite-csp-contact.jar`
- `api-app-cms/target/elite-csp-cms.jar`

## Module details

See each module's `README.md` for environment variables, Lambda handler class, and deployment instructions.

- [`api-app-contact/README.md`](api-app-contact/README.md)
- [`api-app-cms/README.md`](api-app-cms/README.md)

## Architecture

See [`docs/architecture.md`](docs/architecture.md) for the full architecture overview.
