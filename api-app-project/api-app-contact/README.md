# api-app-contact – Contact & Job-Application Lambda

AWS Lambda module that processes contact-form and job-application submissions and sends transactional emails via Amazon SES.

## Lambda Handler

```
ca.elitecsp.contact.handler.LambdaHandler
```

## Environment Variables

| Variable | Description |
|---|---|
| `FROM_EMAIL` | Verified SES sender address |
| `DESTINATION_EMAIL` | Recipient address for all notifications |
| `AWS_REGION` | AWS region where SES is configured (e.g. `us-east-1`) |

## Request Format

```json
{
  "type": "CONTACT",
  "fullName": "Alice Example",
  "email": "alice@example.com",
  "phone": "514-000-0000",
  "company": "ACME Corp",
  "city": "Montreal",
  "subject": "Inquiry",
  "message": "Hello!"
}
```

For `type: JOB_APPLICATION`, also include:
- `attachment` – Base64-encoded CV (PDF or DOCX, max 5 MB)
- `attachmentFileName` – original filename, e.g. `resume.pdf`

## Deployment

Package:
```bash
mvn clean package -pl api-app-contact -am
```

Upload `api-app-contact/target/elite-csp-contact.jar` to AWS Lambda.

Set the handler to `ca.elitecsp.contact.handler.LambdaHandler` and configure the environment variables above.
