# Frontend Developer Guide (`ui-app`)

## Patterns in use

- Standalone components
- Lazy loaded routes
- Signals for local state
- Reactive forms for user input
- Functional interceptor for API key header

## Quality observations

- Duplicate route entry exists for `services`.
- Submission services are duplicated and can be consolidated.
- Ensure subscription lifecycle management in all components.
- Add user-facing error message handling on submit failures.
