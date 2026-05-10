# SonarQube-Style Static Analysis

## Summary (Prioritized)

### Critical
1. Hardcoded API key in frontend environment files.

### High
2. API key as sole edge control for public submission endpoint.
3. Frontend dependency vulnerabilities (1 high, 3 moderate transitive).

### Medium
4. Duplicated frontend API submission services.
5. Duplicate frontend route entry (`services`).
6. Incomplete subscription lifecycle handling in `CareersComponent`.
7. Potential duplicate attachment decoding path across parse/validation.
8. User-facing error feedback is weak in form submission paths.

### Low
9. Empty base handler abstraction (`CommonLambdaHandler`).
10. Minor naming/doc inconsistencies (`ApiException` docs still reference CustomException wording).

## Remediation Recommendations

- Remove keys from source and rotate credentials.
- Add WAF + usage throttling + stricter key governance.
- Consolidate duplicated services/routes.
- Adopt `takeUntilDestroyed`/equivalent lifecycle-safe subscription approach.
- Add Angular unit tests and CI gate for frontend test target.
- Reduce sensitive logging in request paths.
