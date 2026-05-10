# Technical Debt Report

## High Priority Debt

1. Hardcoded API key in frontend environment files.
2. Duplicate route definition (`services`) in router config.
3. Service duplication between contact/application submission services.
4. Incomplete subscription cleanup pattern in some components.

## Medium Priority Debt

1. Empty abstract base class (`CommonLambdaHandler`) without shared behavior.
2. API contract confusion risk from frontend request wrapping (`{ body, isBase64Encoded }`).
3. Mixed/inconsistent docs references (`api-app` vs `api-app-project`, Java version history drift).
4. Excessive static data embedded in services (consider externalized content pipeline).

## Low Priority Debt

1. ErrorCode enum includes currently unused constants.
2. Minor naming inconsistency in comments (`CustomException` wording in `ApiException` docs).

## Debt Impact

- Security: key exposure risk and uncontrolled client propagation.
- Maintainability: duplicate logic and route duplication increase change risk.
- Operability: partial observability practices and inconsistent docs increase onboarding/incident time.
