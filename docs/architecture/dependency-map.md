# Dependency Map

## Frontend Dependencies

- Framework: Angular 21 (`@angular/*`)
- Reactive: `rxjs`
- i18n: `@ngx-translate/core`, `@ngx-translate/http-loader`

## Backend Dependencies

- Runtime: Java 21
- Lambda: `aws-lambda-java-core`, `aws-lambda-java-events`
- AWS SDK: `software.amazon.awssdk:ses`
- Serialization: Jackson
- Mail/MIME: Eclipse Angus Jakarta Mail
- Boilerplate/logging: Lombok + SLF4J
- Testing: JUnit 5 + Mockito + JaCoCo

## Module Graph

- `api-app-project`
  - `api-app-common`
  - `api-app-contact` → depends on `api-app-common`
