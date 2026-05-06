# Code Quality Report — Elite CSP Website

> Generated: 2026-05-06  
> Scope: `ui-app` (Angular 21) · `api-app` (Java 17 / AWS Lambda)  
> Methodology: Manual static analysis following SonarQube standards

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Critical Issues](#2-critical-issues)
3. [Major Issues](#3-major-issues)
4. [Minor Issues](#4-minor-issues)
5. [Security Audit (OWASP)](#5-security-audit-owasp)
6. [Code Duplication](#6-code-duplication)
7. [Dead Code & Unused Dependencies](#7-dead-code--unused-dependencies)
8. [Complexity Analysis](#8-complexity-analysis)
9. [Naming Conventions](#9-naming-conventions)
10. [Error Handling Review](#10-error-handling-review)
11. [Logging Practices](#11-logging-practices)
12. [Recommended Fixes Summary](#12-recommended-fixes-summary)

---

## 1. Executive Summary

| Metric | Count |
|---|---|
| 🔴 Critical issues | 2 |
| 🟠 Major issues | 5 |
| 🟡 Minor issues | 9 |
| Total issues | 16 |

**Overall assessment:** The codebase is well-structured with a clear separation of concerns. The backend (api-app) follows clean architecture principles with proper exception handling, logging, and HTML escaping. The frontend (ui-app) uses modern Angular patterns (standalone components, signals, inject()). The two critical issues relate to secrets management and must be resolved before production use.

---

## 2. Critical Issues

### C-01 · API Keys Hardcoded in Source Code

**Severity:** 🔴 Critical  
**Module:** `ui-app`  
**Files:** `src/environments/environment.ts`, `src/environments/environment.prod.ts`  
**OWASP:** A02:2021 – Cryptographic Failures / A07:2021 – Identification and Authentication Failures

**Description:**  
Both environment files contain the same hardcoded API key value (`k9Q3WwwI828q0lvPMn5koVtIepZtnkR7wC4klD22`). Committing secrets to source control exposes them to anyone with repository access and in built JavaScript bundles served to browsers.

**Current code:**
```typescript
// environment.ts
export const environment = {
  production: false,
  apiUrl: 'https://wkz8o24uc4.execute-api.ca-central-1.amazonaws.com/prod',
  apiKey: 'k9Q3WwwI828q0lvPMn5koVtIepZtnkR7wC4klD22'
};

// environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://ek7n4dkkde.execute-api.ca-central-1.amazonaws.com/prod',
  apiKey: 'k9Q3WwwI828q0lvPMn5koVtIepZtnkR7wC4klD22'
};
```

**Recommended fix:**
1. Rotate the existing API key immediately in AWS API Gateway.
2. Remove the key value from source files; use a placeholder:
```typescript
export const environment = {
  production: false,
  apiUrl: 'https://wkz8o24uc4.execute-api.ca-central-1.amazonaws.com/prod',
  apiKey: '__API_KEY_PLACEHOLDER__'
};
```
3. Inject the real value at build time via GitHub Actions secrets:
```yaml
- name: Build Angular app
  working-directory: ui-app
  env:
    API_KEY: ${{ secrets.API_GATEWAY_KEY }}
  run: |
    sed -i "s/__API_KEY_PLACEHOLDER__/${API_KEY}/g" \
      src/environments/environment.prod.ts
    npm run build -- --configuration=production
```
4. Add environment files to `.gitignore` or ensure they only contain placeholders.

---

### C-02 · Wildcard CORS (`Access-Control-Allow-Origin: *`)

**Severity:** 🔴 Critical  
**Module:** `api-app`  
**File:** `src/main/java/ca/elitecsp/common/util/Constants.java`  
**OWASP:** A05:2021 – Security Misconfiguration

**Description:**  
The backend Lambda responds to every request with `Access-Control-Allow-Origin: *`. This allows any website to submit contact forms or job applications on behalf of users, enabling cross-site request forgery (CSRF) attacks and spam abuse.

**Current code:**
```java
public static final String CORS_ALLOW_ALL = "*";
// Used in ApiResponseBuilder:
Constants.HEADER_CORS_ORIGIN, Constants.CORS_ALLOW_ALL
```

**Recommended fix:**  
Restrict CORS to the production domain. Read the allowed origin from an environment variable:
```java
// In EmailService / LambdaHandler constructor:
private static final String CORS_ORIGIN =
    System.getenv().getOrDefault("ALLOWED_ORIGIN", "https://www.elitecsp.ca");

// In ApiResponseBuilder (inject or pass the origin):
Map.of(
    Constants.HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_JSON,
    Constants.HEADER_CORS_ORIGIN, corsOrigin
)
```
Add `ALLOWED_ORIGIN=https://www.elitecsp.ca` to the Lambda environment variables.

---

## 3. Major Issues

### M-01 · No Unit Tests in Either Module

**Severity:** 🟠 Major  
**Modules:** `ui-app`, `api-app`

**Description:**  
Neither module contains any test files. This means regressions are only caught in production. Karma/Jasmine is configured in the Angular app (`tsconfig.spec.json`) but no spec files exist. The Java backend has no JUnit tests.

**Recommended fix (api-app):**
```java
@Test
void handleRequest_withValidContactPayload_returns200() {
    ContactRequest req = new ContactRequest();
    req.setFullName("Alice Tremblay");
    req.setEmail("alice@example.com");
    req.setMessage("Hello, I'd like more information.");

    doNothing().when(emailService).sendContactEmail(any(), any(), any(), any(), any(), any(), any(), any());

    APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
        .withBody(JsonUtils.toJson(req));

    APIGatewayProxyResponseEvent response = handler.handleRequest(event, mockContext);
    assertEquals(200, response.getStatusCode());
}
```

**Recommended fix (ui-app):**
```typescript
it('should call contactService.submit on valid form submission', () => {
  component.form.setValue({
    name: 'Alice Tremblay', email: 'alice@example.com',
    company: 'ACME', subject: 'Info', message: 'A message of at least twenty characters.'
  });
  component.onSubmit();
  expect(contactServiceSpy.submit).toHaveBeenCalled();
});
```

---

### M-02 · No User Feedback on API Errors in UI Forms

**Severity:** 🟠 Major  
**Module:** `ui-app`  
**Files:** `pages/contact/contact.ts`, `pages/careers/careers.ts`

**Description:**  
Both form submission handlers have an `error` callback that only resets the `submitting` flag. The user receives no visual indication that the submission failed.

**Current code:**
```typescript
error: () => {
  this.submitting.set(false);
}
```

**Recommended fix:**
```typescript
// In component class:
errorMessage = signal<string | null>(null);

// In error callback:
error: (err) => {
  this.submitting.set(false);
  this.errorMessage.set(err?.error?.message ?? 'CONTACT.ERROR_GENERIC');
}
```
```html
<!-- In template: -->
@if (errorMessage()) {
  <div class="alert alert-error" role="alert">
    {{ errorMessage() | translate }}
  </div>
}
```

---

### M-03 · Attachment Decoded Twice for Contact Type

**Severity:** 🟠 Major  
**Module:** `api-app`  
**Files:** `contact/util/ValidationUtil.java`, `contact/handler/LambdaHandler.java`

**Description:**  
For `CONTACT` requests with an optional attachment, `ValidationUtil.validateContactRequest()` decodes the Base64 attachment (line: `byte[] fileBytes = ValidationUtils.decodeBase64File(req.getAttachment())`), and then `LambdaHandler.handleContact()` decodes it again. For large files (up to 5 MB) this doubles memory usage and CPU time.

**Recommended fix:**  
Return the decoded bytes from validation, or validate and cache the bytes on the model:
```java
// Return decoded bytes from validation so handler can reuse them
public static byte[] validateAndDecodeAttachment(String attachment, String fileName) {
    byte[] fileBytes = ValidationUtils.decodeBase64File(attachment);
    ValidationUtils.requireCvSizeWithinLimit(fileBytes);
    ValidationUtils.requireAllowedFileType(fileBytes, fileName);
    return fileBytes;
}
```

---

### M-04 · Duplicate API Service Logic (`ContactService` vs `ApplicationService`)

**Severity:** 🟠 Major  
**Module:** `ui-app`  
**Files:** `services/contact.service.ts`, `services/application.service.ts`

**Description:**  
Both services are nearly identical — they both POST to `${environment.apiUrl}/contacts` with the same pattern. The only difference is the default `type` value. This violates the DRY principle and doubles the maintenance surface.

**Current code (contact.service.ts):**
```typescript
submit(payload: ContactPayload): Observable<ApiResponse> {
  return this.http.post<ApiResponse>(`${environment.apiUrl}/contacts`, { ...payload, type: payload.type ?? 'CONTACT' });
}
```

**Current code (application.service.ts):**
```typescript
submit(payload: ApplicationPayload): Observable<ApiResponse> {
  return this.http.post<ApiResponse>(`${environment.apiUrl}/contacts`, { ...payload, type: payload.type ?? 'JOB_APPLICATION' });
}
```

**Recommended fix:**  
Merge into a single `ApiService` with typed overloads:
```typescript
@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private readonly endpoint = `${environment.apiUrl}/contacts`;

  submitContact(payload: ContactPayload): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(this.endpoint, { ...payload, type: 'CONTACT' });
  }

  submitApplication(payload: ApplicationPayload): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(this.endpoint, { ...payload, type: 'JOB_APPLICATION' });
  }
}
```

---

### M-05 · `TranslationService` Uses `localStorage` Directly (SSR Incompatibility)

**Severity:** 🟠 Major  
**Module:** `ui-app`  
**File:** `services/translation.service.ts`

**Description:**  
Direct calls to `localStorage.getItem` and `localStorage.setItem` will throw in a Server-Side Rendering (SSR) or pre-rendering context because `localStorage` is not available in Node.js. Even without SSR today, this makes the code fragile.

**Recommended fix:**
```typescript
import { PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private translate = inject(TranslateService);
  private platformId = inject(PLATFORM_ID);

  init(): void {
    const supported = ['fr', 'en'];
    this.translate.addLangs(supported);
    this.translate.setDefaultLang('fr');
    const saved = isPlatformBrowser(this.platformId)
      ? localStorage.getItem(LANG_KEY) ?? 'fr'
      : 'fr';
    const lang = supported.includes(saved) ? saved : 'fr';
    this.translate.use(lang);
  }

  use(lang: string): void {
    this.translate.use(lang);
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(LANG_KEY, lang);
    }
  }
}
```

---

## 4. Minor Issues

### m-01 · `jobId` Form Field Uses `String` Instead of Typed Value

**Severity:** 🟡 Minor  
**Module:** `ui-app`  
**File:** `pages/careers/careers.ts`

**Description:**  
The form stores `jobId` as a `string` (`this.fb.group({ jobId: ['', ...] })`), then casts it back to a number when sent (`subject: String(v.jobId)`). Using a number type throughout would be more explicit and less error-prone.

---

### m-02 · `DataService` Contains Hard-Coded Data with No Separation of Content

**Severity:** 🟡 Minor  
**Module:** `ui-app`  
**File:** `services/data.service.ts`

**Description:**  
`DataService` is a large file (150+ lines) holding four `signal` arrays with hard-coded translation key strings. While functional, the data definitions would be better placed in separate JSON/TS data files to improve readability and allow content changes without touching service logic.

**Recommended fix:**
```typescript
// data/services.data.ts
export const MAINTENANCE_SERVICES: Service[] = [
  { id: 1, title: 'DATA.MAINTENANCE.1.TITLE', ... },
  ...
];

// services/data.service.ts
import { MAINTENANCE_SERVICES } from '../data/services.data';
readonly maintenanceServices = signal<Service[]>(MAINTENANCE_SERVICES);
```

---

### m-03 · No `HttpErrorInterceptor` for Global Error Handling

**Severity:** 🟡 Minor  
**Module:** `ui-app`

**Description:**  
HTTP errors are handled inline in each component. A centralized `HttpErrorInterceptor` would improve consistency and allow global logging or user notification.

---

### m-04 · Missing `aria-*` Attributes on Form Controls

**Severity:** 🟡 Minor  
**Module:** `ui-app`  
**Files:** Form templates in `contact/`, `careers/`

**Description:**  
Form controls lack `aria-invalid`, `aria-describedby`, and `aria-required` attributes. This reduces accessibility for screen reader users.

**Recommended fix:**
```html
<input [attr.aria-invalid]="f.email.invalid && f.email.touched"
       aria-required="true"
       aria-describedby="email-error"
       formControlName="email" ... />
<span id="email-error" role="alert" *ngIf="f.email.invalid && f.email.touched">
  {{ 'CONTACT.EMAIL_ERROR' | translate }}
</span>
```

---

### m-05 · `requireDocxMagicBytes` is a Private Method Duplicating `requirePdfMagicBytes` Logic

**Severity:** 🟡 Minor  
**Module:** `api-app`  
**File:** `common/util/ValidationUtils.java`

**Description:**  
`requirePdfMagicBytes` and `requireDocxMagicBytes` are nearly identical loops. A single private `requireMagicBytes(byte[] fileBytes, byte[] magic, String errorMessage)` helper would reduce duplication.

**Recommended fix:**
```java
private static void requireMagicBytes(byte[] fileBytes, byte[] magic, String errorMessage) {
    if (fileBytes.length < magic.length) {
        throw new CustomException(ErrorCode.INVALID_FILE_TYPE, 400, errorMessage + " (file too small)");
    }
    for (int i = 0; i < magic.length; i++) {
        if (fileBytes[i] != magic[i]) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE, 400, errorMessage);
        }
    }
}

public static void requirePdfMagicBytes(byte[] fileBytes) {
    requireMagicBytes(fileBytes, Constants.PDF_MAGIC_BYTES, "CV file must be a valid PDF document");
}

private static void requireDocxMagicBytes(byte[] fileBytes) {
    requireMagicBytes(fileBytes, Constants.DOCX_MAGIC_BYTES, "DOCX file must be a valid Office Open XML document");
}
```

---

### m-06 · `groupId` Mismatch in `pom.xml`

**Severity:** 🟡 Minor  
**Module:** `api-app`  
**File:** `pom.xml`

**Description:**  
The `pom.xml` declares `<groupId>com.elitecsp</groupId>` but all Java packages use `ca.elitecsp`. These should match for Maven conventions.

**Recommended fix:**
```xml
<groupId>ca.elitecsp</groupId>
```

---

### m-07 · `LambdaHandler.parseRequest()` Logs Full Request Body at INFO Level

**Severity:** 🟡 Minor  
**Module:** `api-app`  
**File:** `contact/handler/LambdaHandler.java`

**Description:**  
The line `log.info("The message body is: {}", body)` logs the full raw request body at INFO level in production. This can expose PII (email addresses, names, cover letter content) in CloudWatch logs.

**Recommended fix:**
```java
// Replace:
log.info("The message body is: {}", body);
// With:
log.debug("The message body is: {}", body);
```
Alternatively, log only the content length: `log.info("Received request body ({} bytes)", body.length())`.

---

### m-08 · `careers.ts` Uses `setTimeout` for Scroll Behavior

**Severity:** 🟡 Minor  
**Module:** `ui-app`  
**File:** `pages/careers/careers.ts`

**Description:**  
`setTimeout(() => { ... }, 50)` is used to wait for the form to appear after `patchValue`. This is fragile — if Angular's change detection is slower on a low-end device, 50ms may not be enough. Use `afterNextRender` or `ChangeDetectorRef.detectChanges()` followed by the scroll.

---

### m-09 · No `robots.txt` or `sitemap.xml`

**Severity:** 🟡 Minor  
**Module:** `ui-app`

**Description:**  
The `public/` directory does not contain `robots.txt` or `sitemap.xml`. These are SEO fundamentals for a corporate marketing site.

---

## 5. Security Audit (OWASP)

| OWASP Category | Status | Notes |
|---|---|---|
| A01 – Broken Access Control | ✅ N/A | Public-facing site; API Gateway key provides basic access control |
| A02 – Cryptographic Failures | ❌ **C-01** | API key committed in source code |
| A03 – Injection | ✅ Pass | Backend HTML-escapes all user input before template substitution |
| A04 – Insecure Design | ✅ Pass | Clean separation of handler/service/validation layers |
| A05 – Security Misconfiguration | ❌ **C-02** | Wildcard CORS header |
| A06 – Vulnerable Components | ✅ Pass | Dependencies are current; no known CVEs in declared versions |
| A07 – Auth Failures | ⚠️ Warning | API key is in JavaScript bundle (client-side); rate limiting should be enforced at API Gateway level |
| A08 – Software and Data Integrity | ✅ Pass | Maven Shade plugin filters out signature files |
| A09 – Security Logging | ⚠️ Warning | PII logged at INFO level in body preview (see m-07) |
| A10 – SSRF | ✅ N/A | No outbound HTTP calls from frontend to arbitrary URLs |

---

## 6. Code Duplication

| Location | Duplication Type | Severity |
|---|---|---|
| `contact.service.ts` ↔ `application.service.ts` | Near-identical HTTP POST pattern | 🟠 Major (M-04) |
| `requirePdfMagicBytes` ↔ `requireDocxMagicBytes` | Identical loop logic | 🟡 Minor (m-05) |
| `buildContactPlaceholders` ↔ `buildJobApplicationPlaceholders` | Shared `htmlEscape` + map pattern | 🟡 Minor – acceptable given different keys |

---

## 7. Dead Code & Unused Dependencies

| Item | Location | Notes |
|---|---|---|
| `@ngx-translate/http-loader` | `ui-app/package.json` | Used via `provideTranslateHttpLoader` — **not dead** |
| `tslib` | `ui-app/package.json` | Required by Angular compilation — **not dead** |
| No unused imports detected | — | Angular compiler removes tree-shaking candidates at build |
| `sonar-maven-plugin` in `pom.xml` | `api-app/pom.xml` | Declared but no `sonar:sonar` goal is invoked in CI — can remove or add CI step |

---

## 8. Complexity Analysis

### api-app

| Class / Method | Cyclomatic Complexity | Assessment |
|---|---|---|
| `LambdaHandler.handleRequest` | 3 | ✅ Low |
| `ValidationUtil.validateContactRequest` | 5 | ✅ Acceptable |
| `ValidationUtils.requireAllowedFileType` | 4 | ✅ Low |
| `EmailService.sendContactEmail` | 3 | ✅ Low |
| `EmailService.buildRawMimeMessage` | 4 | ✅ Low |

### ui-app

| Component / Method | Cyclomatic Complexity | Assessment |
|---|---|---|
| `CareersComponent.onSubmit` | 5 | ✅ Acceptable |
| `CareersComponent.onFileChange` | 3 | ✅ Low |
| `ContactComponent.onSubmit` | 2 | ✅ Low |
| `TranslationService.init` | 3 | ✅ Low |

No methods exceed a cyclomatic complexity of 10. **No refactoring required for complexity.**

---

## 9. Naming Conventions

### api-app (Java)

| Convention | Status | Notes |
|---|---|---|
| Classes: `PascalCase` | ✅ Pass | All classes follow convention |
| Methods/variables: `camelCase` | ✅ Pass | Consistent |
| Constants: `UPPER_SNAKE_CASE` | ✅ Pass | All constants in `Constants.java` |
| Packages: `lowercase` | ✅ Pass | `ca.elitecsp.*` |
| `groupId` in pom.xml: `com.elitecsp` | ❌ Minor (m-06) | Should be `ca.elitecsp` |

### ui-app (TypeScript)

| Convention | Status | Notes |
|---|---|---|
| Components/services: `PascalCase` | ✅ Pass | |
| Files: `kebab-case` | ✅ Pass | e.g. `api-key.interceptor.ts` |
| Variables/methods: `camelCase` | ✅ Pass | |
| Signal naming: descriptive nouns | ✅ Pass | `submitted`, `submitting`, `selectedFile` |
| Interfaces: `PascalCase` without `I` prefix | ✅ Pass | `ApiResponse`, `ContactPayload` |

---

## 10. Error Handling Review

### api-app

| Scenario | Handled | Mechanism |
|---|---|---|
| Missing/blank request body | ✅ | `CustomException(MISSING_REQUIRED_FIELD, 400)` |
| Invalid JSON body | ✅ | `CustomException(JSON_PARSE_ERROR, 400)` |
| URL-encoded body detection | ✅ | Custom check in `parseRequest` |
| Validation failures | ✅ | `CustomException` thrown by `ValidationUtil` |
| SES send failure | ✅ | Caught and re-thrown as `CustomException(EMAIL_SEND_FAILURE, 500)` |
| Unexpected exceptions | ✅ | Caught in `handleRequest`, returns 500 |
| Missing environment variables | ✅ | `requireEnv()` throws on startup |

### ui-app

| Scenario | Handled | Mechanism |
|---|---|---|
| Form validation (client-side) | ✅ | Angular Validators + `markAllAsTouched()` |
| API success | ✅ | `submitted.set(true)` |
| API error | ❌ **M-02** | Only resets spinner; no user message |
| File read failure | ✅ | `.catch()` in `readFileAsBase64` |
| Invalid file type | ✅ | `fileError` signal + template display |

---

## 11. Logging Practices

### api-app

| Practice | Status | Notes |
|---|---|---|
| SLF4J + Lombok `@Slf4j` | ✅ Good | Consistent across all classes |
| Parameterized log messages (`{}`) | ✅ Good | No string concatenation in log calls |
| ERROR level for unexpected exceptions | ✅ Good | `log.error(msg, e)` includes stack trace |
| WARN for recoverable errors | ✅ Good | `log.warn` for `CustomException` |
| INFO for lifecycle events | ✅ Good | Handler invoked, email sent |
| PII in INFO logs | ❌ Minor (m-07) | Full body logged, email/name in INFO |

### ui-app

| Practice | Status | Notes |
|---|---|---|
| No `console.log` in production code | ✅ Good | No stray console calls detected |
| No error logging service | ⚠️ | Errors are silently swallowed in `error:` callbacks |

---

## 12. Recommended Fixes Summary

| ID | Severity | Module | Action |
|---|---|---|---|
| C-01 | 🔴 Critical | ui-app | Rotate API key; remove from source; inject via CI secrets |
| C-02 | 🔴 Critical | api-app | Replace `CORS_ALLOW_ALL = "*"` with env-var-driven allowed origin |
| M-01 | 🟠 Major | both | Add unit tests (JUnit 5 for api-app, Jasmine for ui-app) |
| M-02 | 🟠 Major | ui-app | Show error message in UI when API call fails |
| M-03 | 🟠 Major | api-app | Avoid double Base64 decode; cache decoded bytes from validation |
| M-04 | 🟠 Major | ui-app | Merge `ContactService` + `ApplicationService` into `ApiService` |
| M-05 | 🟠 Major | ui-app | Guard `localStorage` access with `isPlatformBrowser` |
| m-01 | 🟡 Minor | ui-app | Type `jobId` form field as `number` |
| m-02 | 🟡 Minor | ui-app | Extract hard-coded data arrays to separate data files |
| m-03 | 🟡 Minor | ui-app | Add global `HttpErrorInterceptor` |
| m-04 | 🟡 Minor | ui-app | Add `aria-*` attributes to form controls |
| m-05 | 🟡 Minor | api-app | Extract shared magic-bytes check into private helper |
| m-06 | 🟡 Minor | api-app | Fix `groupId` in `pom.xml` from `com.elitecsp` to `ca.elitecsp` |
| m-07 | 🟡 Minor | api-app | Downgrade full-body log from INFO to DEBUG |
| m-08 | 🟡 Minor | ui-app | Replace `setTimeout` scroll with `afterNextRender` |
| m-09 | 🟡 Minor | ui-app | Add `robots.txt` and `sitemap.xml` to `public/` |
