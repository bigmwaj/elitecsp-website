# Elite CSP API Project (`api-app-project`)

Java 21 Maven multi-module project for Elite CSP backend Lambdas.

## Modules

- `api-app-common` — shared response, exception, validation, JSON, and constants utilities
- `api-app-contact` — contact/job submission Lambda handler and SES delivery services

## Build and Test

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
cd api-app-project
mvn clean test
mvn clean package
```

## Artifacts

- `api-app-contact/target/elite-csp-contact.jar`

## Documentation

- Project docs index: `../docs/README.md`
- Backend guide: `../docs/developer-guides/backend-guide.md`
- API docs: `../docs/api/openapi-style.md`
