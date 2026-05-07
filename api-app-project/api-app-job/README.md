# api-app-cms – CMS (S3 XML Job Listings) Lambda

AWS Lambda module that reads XML job data stored in S3, parses it, and returns a structured JSON array for front-end table rendering.

## Lambda Handler

```
ca.elitecsp.cms.handler.CmsLambdaHandler
```

## Environment Variables

| Variable | Description |
|---|---|
| `AWS_REGION` | AWS region where the S3 bucket resides (e.g. `ca-central-1`) |

AWS credentials are resolved automatically via the Lambda execution role (IAM). No secrets are hardcoded.

## Request Format

```json
{
  "bucketName": "my-bucket",
  "fileKey": "jobs/jobs.xml"
}
```

## Expected XML Format

```xml
<jobs>
  <job>
    <jobId>001</jobId>
    <title>Java Developer</title>
    <department>IT</department>
    <location>Montreal</location>
  </job>
  <job>
    <jobId>002</jobId>
    <title>Cloud Architect</title>
    <department>Engineering</department>
    <location>Toronto</location>
  </job>
</jobs>
```

## Response Format (HTTP 200)

```json
[
  { "jobId": "001", "title": "Java Developer", "department": "IT", "location": "Montreal" },
  { "jobId": "002", "title": "Cloud Architect", "department": "Engineering", "location": "Toronto" }
]
```

## Error Responses

| HTTP Status | Cause |
|---|---|
| 400 | Missing or invalid `bucketName` / `fileKey` |
| 500 | S3 download failure or XML parsing error |

## Deployment

Package:
```bash
mvn clean package -pl api-app-cms -am
```

Upload `api-app-cms/target/elite-csp-cms.jar` to AWS Lambda.

Set the handler to `ca.elitecsp.cms.handler.CmsLambdaHandler` and configure the environment variables above.

Grant the Lambda execution role `s3:GetObject` on the target bucket/prefix.
