# ui-app — Angular Frontend

> Angular 21 SPA for the Elite CSP corporate website.  
> Deployed to AWS S3 and served via CloudFront.

---

## Overview

`ui-app` is the public-facing website for Elite CSP. It presents the company's services, partners, careers, and contact information. Users can submit a contact form or apply for a job directly from the browser. The app is fully bilingual (French / English) and uses lazy-loaded standalone components with Angular signals for reactive state.

---

## Architecture

```
src/
├── app/
│   ├── app.ts                  # Root component (navbar + router-outlet + footer)
│   ├── app.config.ts           # Application providers (router, HTTP, i18n)
│   ├── app.routes.ts           # Lazy-loaded route definitions
│   │
│   ├── components/             # Reusable presentational components
│   │   ├── navbar/             # Top navigation bar with language switcher
│   │   ├── footer/             # Site footer
│   │   ├── hero/               # Page hero banner (title + subtitle)
│   │   ├── cta-section/        # Call-to-action section
│   │   ├── service-card/       # Service tile card
│   │   ├── job-card/           # Job listing card with Apply button
│   │   └── partner-card/       # Partner logo card with external link
│   │
│   ├── interceptors/
│   │   └── api-key.interceptor.ts  # Adds x-api-key header to API requests
│   │
│   ├── models/                 # TypeScript interfaces (DTOs)
│   │   ├── api-response.model.ts
│   │   ├── job.model.ts
│   │   ├── partner.model.ts
│   │   ├── payload.model.ts    # ContactPayload, ApplicationPayload
│   │   ├── process-step.model.ts
│   │   ├── service.model.ts
│   │   └── testimonial.model.ts
│   │
│   ├── pages/                  # Routed page components (lazy-loaded)
│   │   ├── home/               # Landing page with services, process, testimonials
│   │   ├── services/           # Full services listing
│   │   ├── about/              # Company overview
│   │   ├── contact/            # Contact form
│   │   ├── partners/           # Partners listing
│   │   └── careers/            # Job listings + application form
│   │
│   └── services/               # Angular injectable services
│       ├── contact.service.ts  # POST /contacts (type: CONTACT)
│       ├── application.service.ts # POST /contacts (type: JOB_APPLICATION)
│       ├── data.service.ts     # In-memory static data (services, testimonials)
│       ├── job.service.ts      # In-memory job listings
│       ├── partners.service.ts # Partner list with logos and URLs
│       └── translation.service.ts # Language init and persistence
│
├── environments/
│   ├── environment.ts          # Development config (apiUrl, apiKey placeholder)
│   └── environment.prod.ts     # Production config (apiUrl, apiKey placeholder)
│
├── index.html
├── main.ts
└── styles.scss                 # Global styles
```

---

## Key Components and Responsibilities

### `AppComponent` (`app.ts`)
Root shell component. Mounts `NavbarComponent`, `RouterOutlet`, and `FooterComponent`. Initialises the translation service in `ngOnInit`.

### `NavbarComponent`
Top navigation bar. Renders route links and a language switcher (FR/EN).

### `HeroComponent`
Reusable page hero banner accepting translated title and subtitle inputs.

### `ContactComponent` (`pages/contact/`)
Reactive form with fields: name, email, company (optional), subject, message.
On submit, calls `ContactService.submit()` and shows a success confirmation.

### `CareersComponent` (`pages/careers/`)
Displays job listings from `JobService`. Each job card has an **Apply** button that scrolls to and pre-fills the application form. The form accepts: fullName, email, city, jobId, coverLetter, and a CV file (PDF or DOCX, max 5 MB). On submit, the CV is read as Base64 and sent via `ApplicationService.submit()`.

### `ApiKeyInterceptor`
Functional HTTP interceptor. Adds the `x-api-key` header to any request whose URL starts with `environment.apiUrl`. Other requests (e.g., i18n JSON files) are passed through unchanged.

---

## Data Flow

```
User fills form
    │
    ▼
ReactiveForm (Validators)
    │  invalid → show validation errors
    ▼  valid
Component.onSubmit()
    │
    ▼
ContactService / ApplicationService
    │  .post() via HttpClient
    ▼
ApiKeyInterceptor (adds x-api-key header)
    │
    ▼
API Gateway → Lambda → SES
    │
    ▼
ApiResponse { success, message, error }
    │
    ├─ success → submitted.set(true) → show success message
    └─ error   → submitting.set(false)
```

---

## Internationalization

The app supports French (`fr`) and English (`en`). Translation files are loaded from `assets/i18n/fr.json` and `assets/i18n/en.json` via `provideTranslateHttpLoader`. The default language is French. The selected language is persisted in `localStorage` under the key `ui-app_lang`.

---

## Dependencies

| Package | Version | Purpose |
|---|---|---|
| `@angular/core` | ^21.2.0 | Framework |
| `@angular/forms` | ^21.2.0 | Reactive forms |
| `@angular/router` | ^21.2.0 | Client-side routing |
| `@ngx-translate/core` | ^17.0.0 | i18n |
| `@ngx-translate/http-loader` | ^17.0.0 | Load translation JSON over HTTP |
| `rxjs` | ~7.8.0 | Reactive programming |

---

## Development Commands

```bash
# Install dependencies
npm install

# Start development server (http://localhost:4200)
npm start

# Production build
npm run build -- --configuration=production

# Watch mode (development)
npm run watch

# Run unit tests
npm test
```

---

## Environment Configuration

| Variable | File | Description |
|---|---|---|
| `apiUrl` | `environment.ts` / `environment.prod.ts` | Base URL of API Gateway |
| `apiKey` | `environment.ts` / `environment.prod.ts` | API Gateway key (`x-api-key`) |

> ⚠️ **Do not commit real API keys.** Use placeholders and inject the real value via CI/CD secrets at build time. See [Configuration Guide](../docs/configuration-guide.md).

---

## Build Output

Production build output: `dist/ui-app/browser/`

Hashed filenames are used for all assets except `index.html`. The CI/CD workflow uploads hashed assets with a 1-year cache header and `index.html` with `no-cache`.

---

## Code Scaffolding

```bash
# Generate a new component
ng generate component components/my-component

# Generate a new service
ng generate service services/my-service
```

## Additional Resources

- [Angular CLI Reference](https://angular.dev/tools/cli)
- [Angular Signals](https://angular.dev/guide/signals)
- [ngx-translate docs](https://github.com/ngx-translate/core)
