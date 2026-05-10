# Frontend Developer Guide (`ui-app`)

## Patterns in use

- Standalone components
- Lazy loaded routes
- Signals for local state
- Reactive forms for user input
- Functional interceptor for API key header
- ngx-translate with standardized namespaces (`PAGE`, `MENU`, `SHARED`, `COMMON`)

## Translation validation

- Run `npm run i18n:check` from `ui-app` to validate:
  - key existence for all keys used in templates/TypeScript
  - unused key detection
  - locale structure parity between English and French

## Quality observations

- Duplicate route entry exists for `services`.
- Submission services are duplicated and can be consolidated.
- Ensure subscription lifecycle management in all components.
- Add user-facing error message handling on submit failures.
