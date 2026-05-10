# Operations Runbook

## Daily Checks

- Review latest GitHub Actions deployment runs.
- Check Lambda error rates and CloudWatch logs.
- Confirm SES send health and bounce/complaint trends.

## Incident Handling

1. Identify failing layer (frontend asset, API, Lambda, SES).
2. Correlate with latest deployment and logs.
3. Execute targeted rollback if needed.
4. Validate contact and job submission smoke tests.
