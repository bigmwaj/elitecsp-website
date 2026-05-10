# Security Review

## Summary

Overall posture is moderate with one critical configuration issue and several hardening opportunities.

## Findings

### S-01 (Critical) — Hardcoded frontend API key
- Location: `ui-app/src/environments/environment.ts`, `environment.prod.ts`
- Risk: key disclosure in source and bundled client assets.
- Recommendation:
  1. Rotate API Gateway key.
  2. Keep placeholders in source.
  3. Inject real key at CI build time from GitHub Secrets.
  4. Enforce key scanning and commit protection.

### S-02 (High) — API key auth alone is weak perimeter control
- Risk: key reuse/leak and abuse from scraped frontend bundles.
- Recommendation: add usage plan throttling, AWS WAF, per-origin restrictions, and optional reCAPTCHA for public forms.

### S-03 (Medium) — CORS policy requires explicit runtime governance
- Current behavior: response header uses `CORS_ALLOW`/`cors.allow`/default resolution.
- Recommendation: set `CORS_ALLOW` explicitly per environment and block wildcard values in deployment checks.

### S-04 (Medium) — Potential sensitive data in logs
- Current behavior: logs include sender email and request body preview.
- Recommendation: redact or hash personal identifiers in info/debug logs.

### S-05 (Medium) — Dependency vulnerability alerts in frontend toolchain
- `npm audit` reports: 1 high, 3 moderate transitive vulnerabilities.
- Recommendation: update lockfile/dependencies and verify with `npm audit` after update.

## IAM and Secrets Review

- CI requires AWS credentials and region/bucket/distribution variables.
- Principle of least privilege should scope Lambda update, S3 sync, and CloudFront invalidation actions.
- Ensure Lambda execution role only includes SES send + CloudWatch log permissions required for this function.

## Hardening Checklist

- [ ] Rotate exposed API key and revoke old key.
- [ ] Enforce CI-time secret injection and placeholder checks.
- [ ] Configure WAF and API usage limits.
- [ ] Validate strict `CORS_ALLOW` value per environment.
- [ ] Add structured log redaction policy.
- [ ] Resolve npm audit findings.
