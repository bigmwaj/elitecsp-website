# Architecture — Elite CSP Website

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Component Diagram](#2-component-diagram)
3. [Frontend Architecture (ui-app)](#3-frontend-architecture-ui-app)
4. [Backend Architecture (api-app)](#4-backend-architecture-api-app)
5. [Data Flow](#5-data-flow)
6. [Infrastructure](#6-infrastructure)
7. [Security Architecture](#7-security-architecture)
8. [Design Decisions](#8-design-decisions)

---

## 1. System Overview

The Elite CSP website is a decoupled web application composed of:

- **Frontend:** An Angular 21 Single-Page Application (SPA) hosted on AWS S3 and distributed via CloudFront.
- **Backend:** A single AWS Lambda function (Java 17) exposed through Amazon API Gateway, responsible for processing contact and job application form submissions.

The two modules communicate over HTTPS. There is no shared runtime dependency — the frontend is a static bundle and the backend is fully serverless.

---

## 2. Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                            Internet                             │
└─────────────────────────────────────────────────────────────────┘
         │                                   │
         ▼                                   ▼
┌─────────────────┐                ┌──────────────────────┐
│   CloudFront    │                │    API Gateway       │
│   (CDN/HTTPS)   │                │  (REST, x-api-key)   │
└────────┬────────┘                └──────────┬───────────┘
         │                                   │ POST /contacts
         ▼                                   ▼
┌─────────────────┐                ┌──────────────────────┐
│    S3 Bucket    │                │    AWS Lambda        │
│  (Angular SPA)  │                │  (Java 17)           │
└─────────────────┘                │  LambdaHandler       │
                                   └──────────┬───────────┘
                                              │
                                              ▼
                                   ┌──────────────────────┐
                                   │   Amazon SES         │
                                   │  (Transactional      │
                                   │   Email)             │
                                   └──────────────────────┘
```

---

## 3. Frontend Architecture (ui-app)

### Pattern: Feature-Based Module Structure

The frontend uses Angular 21's standalone component architecture. There are no NgModules — every component, service, and interceptor is independently bootstrapped via `app.config.ts`.

```
app.config.ts (ApplicationConfig)
  ├── provideRouter(routes, withViewTransitions())
  ├── provideHttpClient(withInterceptors([apiKeyInterceptor]))
  ├── provideTranslateService({ fallbackLang: 'fr' })
  └── provideTranslateHttpLoader({ prefix: 'assets/i18n/', suffix: '.json' })
```

### Routing

All page components are lazy-loaded to minimize the initial bundle:

```
/           → HomeComponent
/services   → ServicesComponent
/about      → AboutComponent
/contact    → ContactComponent
/partners   → PartnersComponent
/careers    → CareersComponent
**          → redirect to /
```

### Layer Responsibilities

| Layer | Files | Responsibility |
|---|---|---|
| **Pages** | `pages/*/` | Routed containers. Own forms, SEO meta, page layout. |
| **Components** | `components/*/` | Reusable presentational UI (no business logic). |
| **Services** | `services/*.service.ts` | Data access, API calls, state management (signals). |
| **Models** | `models/*.model.ts` | TypeScript interfaces mirroring backend DTOs. |
| **Interceptors** | `interceptors/` | Cross-cutting HTTP concerns (authentication headers). |

### State Management

Angular signals are used for local component state (no external state library):

```typescript
submitted  = signal(false);     // Form submission success flag
submitting = signal(false);     // Loading spinner flag
selectedFile = signal<File | null>(null);  // File input binding
```

Static application data (services list, jobs, partners) is held in `signal()` arrays within injectable services (`DataService`, `JobService`, `PartnersService`).

### Internationalization

Two language files under `public/assets/i18n/`:
- `fr.json` — French (default)
- `en.json` — English

Language preference is persisted in `localStorage` under the key `ui-app_lang`.

---

## 4. Backend Architecture (api-app)

### Pattern: Layered Architecture within a Single Lambda

```
┌──────────────────────────────────────────────┐
│  LambdaHandler (handler layer)               │
│  ├── Parse request body                      │
│  ├── Validate request (ValidationUtil)       │
│  ├── Route by ContactType                    │
│  └── Map exceptions → HTTP responses         │
├──────────────────────────────────────────────┤
│  EmailService (service layer)                │
│  ├── Build email subject + placeholders      │
│  ├── Load + render email templates           │
│  ├── Construct MIME multipart (attachments)  │
│  └── Send via SES (simple or raw)            │
├──────────────────────────────────────────────┤
│  Common utilities (shared layer)             │
│  ├── ValidationUtils  – field / file checks  │
│  ├── JsonUtils        – JSON serialisation   │
│  ├── EmailTemplateLoader – template I/O      │
│  ├── ApiResponseBuilder  – response factory  │
│  ├── Constants        – named constants      │
│  └── CustomException / ErrorCode             │
└──────────────────────────────────────────────┘
```

### Request Lifecycle

```
API Gateway invokes Lambda
        │
        ▼
LambdaHandler.handleRequest()
        │
        ├─► parseRequest()           – JSON body → ContactRequest
        │
        ├─► ValidationUtil.validateContactRequest()
        │         ├─ requireNonBlank(fullName, email, message)
        │         ├─ requireValidEmail(email)
        │         └─ [JOB_APPLICATION] requireAttachment + type + size check
        │
        ├─► [type == JOB_APPLICATION] handleJobApplication()
        │         └─ EmailService.sendJobApplicationEmail()
        │                   └─ SES SendRawEmailRequest (MIME + attachment)
        │
        └─► [type == CONTACT] handleContact()
                  └─ EmailService.sendContactEmail()
                            ├─ [no attachment] SES SendEmailRequest (simple)
                            └─ [with attachment] SES SendRawEmailRequest (MIME)
```

### Exception Handling

All errors are represented as `CustomException(ErrorCode, httpStatus, message)`. The handler catches:
1. `CustomException` → converts to structured error response using `ApiResponseBuilder.fromException(e)`
2. Any other `Exception` → returns HTTP 500 via `ApiResponseBuilder.internalError(...)`

### Email Template System

Templates are stored as classpath resources under `templates/`. The `EmailTemplateLoader` reads the file, then performs `String.replace()` for each `{{PLACEHOLDER}}` key. All user-supplied values are HTML-escaped before substitution to prevent content injection.

---

## 5. Data Flow

### Contact Form Submission

```
User (Browser)
  1. Fills contact form (name, email, company, subject, message)
  2. Angular validates form client-side
  3. ContactService.submit() → HTTP POST /contacts
  4. ApiKeyInterceptor adds x-api-key header
  5. API Gateway authenticates with API key
  6. Lambda invoked with JSON body
  7. LambdaHandler validates and routes (type=CONTACT)
  8. EmailService builds HTML + text email bodies
  9. SES sends email to DESTINATION_EMAIL
  10. Lambda returns { success: true, message: "..." }
  11. Browser shows success confirmation
```

### Job Application Submission

```
User (Browser)
  1. Selects job position, fills form, uploads CV (PDF/DOCX)
  2. Angular validates form; reads CV as Base64 (FileReader)
  3. ApplicationService.submit() → HTTP POST /contacts (with attachment)
  4. ApiKeyInterceptor adds x-api-key header
  5. API Gateway authenticates with API key
  6. Lambda invoked with JSON body (Base64 CV in attachment field)
  7. LambdaHandler validates (type=JOB_APPLICATION):
     - Decodes Base64 → raw bytes
     - Checks size ≤ 5 MB
     - Verifies magic bytes (PDF or DOCX)
  8. EmailService builds MIME multipart email with CV attached
  9. SES sends email to DESTINATION_EMAIL
  10. Lambda returns { success: true, message: "..." }
  11. Browser shows success confirmation
```

---

## 6. Infrastructure

### AWS Services

| Service | Role | Configuration |
|---|---|---|
| **S3** | Hosts the built Angular SPA | Static website hosting; versioning optional |
| **CloudFront** | CDN + HTTPS termination | Custom domain; cache invalidation on deploy |
| **API Gateway** | REST API entry point | API key auth; proxy integration to Lambda |
| **Lambda** | Serverless compute | Java 17 runtime; 512 MB memory; 30s timeout |
| **SES** | Transactional email | Verified sender domain; sending in production region |

### Deployment Topology

```
GitHub (main branch push)
  │
  ▼
GitHub Actions
  ├── Build Angular → dist/ui-app/browser/
  ├── aws s3 sync → S3 bucket
  └── CloudFront invalidation

Manual / CLI
  ├── mvn package → elite-csp-contact.jar
  └── aws lambda update-function-code → Lambda
```

---

## 7. Security Architecture

### Authentication & Authorization

- **API Gateway API key (`x-api-key`):** Provides basic caller authentication. The key is injected into the Angular build at compile time and stored in environment files.
- **Lambda execution role (IAM):** Grants only the permissions needed: `ses:SendEmail`, `ses:SendRawEmail`.

### Input Validation

All user input is validated server-side before use:
- Required field checks (`requireNonBlank`)
- Email format validation (regex)
- File size limit (5 MB)
- File type validation (extension + magic bytes)
- JSON parse error detection

### Content Injection Prevention

All user-supplied strings are HTML-escaped before insertion into email templates using the `htmlEscape()` helper in `EmailService`.

### Transport Security

All communication uses HTTPS (CloudFront + API Gateway enforce TLS).

---

## 8. Design Decisions

### Single Lambda for Both Contact Types

Rather than deploying two separate Lambda functions (one for contact, one for job applications), a single unified function with a `type` discriminator field was chosen. This simplifies deployment, reduces cold start overhead, and keeps shared validation/email logic in one place.

### No Database

The application is stateless — submissions are delivered by email and not persisted in a database. This eliminates database provisioning, maintenance, and associated costs for a low-volume contact form.

### Standalone Angular Components

Angular 21 standalone components with `inject()` instead of constructor injection were used throughout to align with current Angular best practices and enable simpler testing.

### SLF4J + slf4j-simple

The SLF4J simple binding is used instead of Logback or Log4j2 to keep the fat JAR size smaller and avoid complex logging configuration. Log output goes to `stdout`, which Lambda forwards to CloudWatch Logs automatically.
