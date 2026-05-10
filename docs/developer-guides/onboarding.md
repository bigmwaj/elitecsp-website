# Developer Onboarding Guide

## Prerequisites

- Node 22 + npm 10
- Java 21
- Maven 3.8+

## Local setup

### Frontend

```bash
cd ui-app
npm ci
npm start
```

### Backend

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
cd api-app-project
mvn clean test
```

## Key Commands

- Frontend production build: `npm run build -- --configuration=production`
- Backend package: `mvn clean package`
