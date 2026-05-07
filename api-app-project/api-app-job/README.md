# api-app-job – Job API (S3 Excel) Lambda

AWS Lambda module that reads a configured Excel workbook from S3 and exposes job listing and job details APIs.

## Lambda Handler

```
ca.elitecsp.job.handler.JobLambdaHandler
```

## Endpoints

| Method | Path | Behavior |
|---|---|---|
| `GET` | `/jobs` | Reads sheet `jobs` and returns job summaries |
| `GET` | `/jobs/{jobId}` | Reads sheet `job-details` and returns detailed job information |

## Configuration

Set environment variables on Lambda:

| Variable | Description |
|---|---|
| `JOB_EXCEL_BUCKET` | S3 bucket containing the Excel file |
| `JOB_EXCEL_KEY` | S3 object key of the Excel file (for example `jobs/jobs.xlsx`) |

AWS credentials and region are resolved through the Lambda execution role and default AWS SDK provider chain.

## Excel file format guide

The workbook must contain **both** sheets below:

### Sheet: `jobs`
Required column:
- `jobId`

Recommended columns:
- `title`
- `location`
- `department`
- `summary`
- `postedDate`

Any additional columns are returned under `attributes`.

### Sheet: `job-details`
Required column:
- `jobId`

Recommended columns:
- `description`
- `responsibilities`
- `requirements`
- `benefits`

List-like fields can be separated by newline, `|`, `;`, or `,`.

## API response examples

### GET /jobs

```json
[
  {
    "jobId": "001",
    "title": "Java Developer",
    "location": "Montreal",
    "department": "Engineering",
    "summary": "Build backend services",
    "postedDate": "2026-05-01",
    "attributes": {}
  }
]
```

### GET /jobs/001

```json
{
  "jobId": "001",
  "title": "Java Developer",
  "description": "Build and evolve services",
  "responsibilities": ["Design APIs", "Write tests"],
  "requirements": ["Java", "AWS"],
  "benefits": ["Health", "Dental"],
  "attributes": {}
}
```

## Deployment guide

Package:
```bash
mvn clean package -pl api-app-job -am
```

Upload `api-app-job/target/elite-csp-job.jar` to AWS Lambda and set handler:

```
ca.elitecsp.job.handler.JobLambdaHandler
```

Grant Lambda execution role permission `s3:GetObject` for the configured bucket/key.

## Migration notes

1. Legacy module renamed to `api-app-job`
2. Legacy parser/service replaced by Apache POI workbook parser
3. S3 config switched to `JOB_EXCEL_BUCKET` + `JOB_EXCEL_KEY`
4. Handler updated to `JobLambdaHandler`
5. API now supports listing and detail retrieval by `jobId`
