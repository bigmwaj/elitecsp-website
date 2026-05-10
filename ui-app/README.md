# ui-app (Angular Frontend)

Angular 21 SPA for the Elite CSP website.

## Key Characteristics

- Standalone component architecture
- Lazy-loaded routes
- Signals + reactive forms
- `x-api-key` HTTP interceptor for API calls
- i18n via `@ngx-translate/*` with translation asset path configured in `src/app/app.config.ts`

## Commands

```bash
cd ui-app
npm ci
npm start
npm run build -- --configuration=production
```

## Integration

- API base URL and API key are provided through environment files.
- Frontend submits contact/job requests to API Gateway `/contacts`.

## Documentation

- Frontend developer guide: `../docs/developer-guides/frontend-guide.md`
- User guides: `../docs/user-guides/README.md`
- Troubleshooting: `../docs/troubleshooting/common-issues.md`
