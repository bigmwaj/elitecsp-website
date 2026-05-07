# api-app-contact – Contact & Job-Application Lambda

AWS Lambda module that processes contact-form and job-application submissions and sends
transactional emails via Amazon SES.

## Lambda Handler

```
ca.elitecsp.contact.handler.ContactLambdaHandler
```

## Email Flow

Two categories of emails are sent per submission:

| Category | Recipient | Trigger |
|---|---|---|
| **Notification** | `DESTINATION_EMAIL` (company inbox) | Every submission — informs the team |
| **Confirmation** | Submitter's own email | Every submission — acknowledges receipt |

### Contact form

1. Notification email sent to `DESTINATION_EMAIL` (Reply-To set to the submitter's address).
2. Confirmation email sent to the submitter — subject: *"Confirmation of your contact request – Elite CSP"*.

### Job application

1. Notification email (with CV attachment) sent to `DESTINATION_EMAIL`.
2. Confirmation email sent to the applicant — subject: *"Application received – \<position\>"*.

> **Resilience**: a confirmation-email failure is logged as a warning but never propagates to
> the API caller. The primary notification email still returns an HTTP 500 on failure.

## Environment Variables

| Variable | Required | Description |
|---|---|---|
| `FROM_EMAIL` | ✅ | SES-verified sender address (e.g. `noreply@elitecsp.ca`) |
| `DESTINATION_EMAIL` | ✅ | Company inbox that receives notification emails |
| `AWS_REGION` | ✅ | AWS region where SES is configured (e.g. `ca-central-1`) |

All values are read at Lambda cold-start. Missing variables cause an immediate HTTP 500 with a
descriptive error message.

## AWS SES Setup

### 1. Verify sender identity

```bash
# Verify the FROM_EMAIL domain (recommended) or single address
aws ses verify-domain-identity --domain elitecsp.ca --region ca-central-1

# Or verify a single email address
aws ses verify-email-identity --email-address noreply@elitecsp.ca --region ca-central-1
```

### 2. Verify destination address (sandbox only)

In **SES sandbox** mode every recipient address must also be verified.  
Request production access to lift this restriction.

```bash
aws ses verify-email-identity --email-address info@elitecsp.ca --region ca-central-1
```

### 3. Required IAM permissions

Attach the following policy to the Lambda execution role:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ses:SendEmail",
        "ses:SendRawEmail"
      ],
      "Resource": "arn:aws:ses:ca-central-1:<ACCOUNT_ID>:identity/elitecsp.ca"
    }
  ]
}
```

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

## Architecture

```
ContactLambdaHandler
  ├── EmailService (interface)
  │     └── SesEmailService (implements EmailService)
  │           ├── sendContactEmail()               → notification to DESTINATION_EMAIL
  │           ├── sendJobApplicationEmail()        → notification to DESTINATION_EMAIL (+ CV)
  │           ├── sendContactConfirmationEmail()   → confirmation to submitter
  │           └── sendJobApplicationConfirmationEmail() → confirmation to applicant
  └── EmailTemplateService
        ├── buildContactEmailHtml/Text()
        ├── buildJobApplicationEmailHtml/Text()
        ├── buildContactConfirmationHtml/Text()
        └── buildJobApplicationConfirmationHtml/Text()
```

## Email Templates

Templates are classpath resources under `templates/`:

| Template file | Purpose |
|---|---|
| `contact-email.html / .txt` | Contact notification to company |
| `job-application-email.html / .txt` | Job-application notification to company |
| `contact-confirmation.html / .txt` | Contact receipt confirmation to user |
| `job-application-confirmation.html / .txt` | Application receipt confirmation to applicant |

Placeholder syntax: `{{PLACEHOLDER}}` (HTML-escaped before insertion).

## Local Development / Testing

Set environment variables before running tests if real SES calls are needed:

```bash
export FROM_EMAIL=noreply@elitecsp.ca
export DESTINATION_EMAIL=info@elitecsp.ca
export AWS_REGION=ca-central-1
```

Unit tests mock `SesClient` — no live AWS credentials are required to run `mvn test`.

## Deployment

Package:
```bash
mvn clean package -pl api-app-contact -am
```

Upload `api-app-contact/target/elite-csp-contact.jar` to AWS Lambda.  
Set the handler to `ca.elitecsp.contact.handler.ContactLambdaHandler` and configure the
environment variables listed above.
