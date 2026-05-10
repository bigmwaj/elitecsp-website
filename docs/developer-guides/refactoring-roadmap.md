# Refactoring Roadmap

## Phase 1 — Security and Contract Correctness

- Externalize frontend API key injection.
- Simplify and clarify request payload contract across frontend/backend.

## Phase 2 — Maintainability

- Merge contact/application submission services.
- Remove duplicate routes and standardize route aliases.
- Introduce centralized API error handling and user-friendly form error state.

## Phase 3 — Reliability and Quality

- Add frontend tests and CI quality gate.
- Standardize subscription management pattern.
- Reduce noisy/sensitive logging in request and exception paths.

## Phase 4 — Architecture Hygiene

- Revisit empty abstractions and remove dead/unused constants.
- Externalize static content where frequent updates are expected.
