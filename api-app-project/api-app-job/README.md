# api-app-job — S3 Excel Job API Lambda

AWS Lambda module that reads an Excel file from S3 and exposes job listing and job detail APIs.

## Lambda Handler

```
ca.elitecsp.job.handler.JobLambdaHandler
```

## Endpoints

### GET /jobs
Returns all jobs from the `jobs` sheet.

### GET /jobs/{jobId}
Returns detailed job content from the `job-details` sheet for the matching `jobId`.

## Required Environment Variables

| Variable | Description |
|---|---|
| `AWS_REGION` | AWS region where the S3 bucket resides (e.g. `ca-central-1`) |
| `JOB_EXCEL_BUCKET` | S3 bucket containing the Excel file |
| `JOB_EXCEL_KEY` | S3 object key to the Excel file (e.g. `jobs/jobs.xlsx`) |

## Excel File Format

The workbook must contain these sheets:

1. `jobs`
   - Required columns: `jobId`, `title`
   - Common optional columns: `location`, `department`, `summary`, `postedDate`

2. `job-details`
   - Required columns: `jobId`, `description`
   - Common optional columns: `responsibilities`, `requirements`, `benefits`

Multi-value fields like `responsibilities`, `requirements`, and `benefits` can be newline-separated or semicolon-separated.

## API Response Examples

### GET /jobs

```json
[
  {
    "jobId": "001",
    "title": "Java Developer",
    "location": "Montreal"
  }
]
```

### GET /jobs/{jobId}

```json
{
  "jobId": "001",
  "title": "Java Developer",
  "description": "Design and implement services",
  "responsibilities": ["Build APIs", "Write tests"],
  "requirements": ["Java 21", "AWS Lambda"]
}
```

## Error Handling

| HTTP Status | Cause |
|---|---|
| 400 | Invalid input (e.g. blank `jobId`) |
| 404 | `jobId` not found |
| 500 | S3 download failure, malformed Excel, missing required sheets/columns, or missing env vars |

## Build and Packaging

```bash
mvn clean package -pl api-app-job -am
```

Generated artifact:

- `api-app-job/target/elite-csp-job.jar`

## Deployment Guide (Quick)

1. Build and upload `elite-csp-job.jar` to your Lambda function.
2. Set handler to `ca.elitecsp.job.handler.JobLambdaHandler`.
3. Set environment variables (`AWS_REGION`, `JOB_EXCEL_BUCKET`, `JOB_EXCEL_KEY`).
4. Ensure Lambda role has `s3:GetObject` for the configured bucket/key.
5. Configure API Gateway routes:
   - `GET /jobs`
   - `GET /jobs/{jobId}`
