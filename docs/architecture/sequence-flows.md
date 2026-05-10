# Sequence Flows

## Contact Submission

1. User submits contact form in Angular.
2. `ContactService` posts to `${apiUrl}/contacts`.
3. Interceptor appends `x-api-key`.
4. API Gateway invokes Lambda.
5. `ContactLambdaHandler` parses JSON and validates.
6. `SesEmailService.sendContactEmail` sends notification.
7. `sendContactConfirmationEmail` attempts user confirmation (failure is non-blocking).
8. Lambda returns JSON envelope with status 200/4xx/5xx.

## Job Application Submission

1. User fills form and uploads CV.
2. CV is read client-side as base64.
3. `ApplicationService` posts payload to `/contacts` with `type=JOB_APPLICATION`.
4. Handler validates attachment presence/type/size.
5. SES raw MIME email is sent with attachment.
6. User confirmation email is attempted.
7. Structured API response is returned.
