# Prioritized Remediation Plan

## Priority 0 (Immediate)

1. Rotate and remove hardcoded API keys from frontend source.
2. Enforce CI-based secret injection and block deployments with placeholders in production artifacts.

## Priority 1 (Next Sprint)

1. Consolidate duplicate frontend submission services into one API client.
2. Remove duplicate `services` route entry.
3. Add lifecycle-safe subscription handling in `CareersComponent`.
4. Align API payload contract to avoid unnecessary wrapper semantics.

## Priority 2 (Near-Term)

1. Add Angular test target and baseline component/service tests.
2. Review and reduce PII in logs.
3. Triage and patch npm audit vulnerabilities.
4. Clarify or remove low-value abstractions (`CommonLambdaHandler`).

## Priority 3 (Ongoing)

1. Introduce architecture decision records (ADRs).
2. Add quality gates (lint/test/build/security scan) on pull requests.
3. Expand runbook automation and operational SLO tracking.
