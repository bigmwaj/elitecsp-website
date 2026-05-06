# API Specification — Elite CSP Backend

> Base URL (production): `https://ek7n4dkkde.execute-api.ca-central-1.amazonaws.com/prod`  
> Authentication: `x-api-key` header (required on all requests)  
> Content-Type: `application/json`

---

## Table of Contents

1. [Overview](#1-overview)
2. [Authentication](#2-authentication)
3. [Common Response Envelope](#3-common-response-envelope)
4. [Error Codes](#4-error-codes)
5. [Endpoints](#5-endpoints)
   - [POST /contacts](#post-contacts)
6. [Request Schemas](#6-request-schemas)
   - [Contact Request](#contact-request)
   - [Job Application Request](#job-application-request)
7. [Response Schemas](#7-response-schemas)
8. [Examples](#8-examples)
   - [Contact Form (cURL)](#contact-form-curl)
   - [Job Application (cURL)](#job-application-curl)
   - [Error Responses](#error-responses)
9. [CORS](#9-cors)

---

## 1. Overview

The Elite CSP API exposes a single endpoint that handles both contact form submissions and job application submissions. The processing path is determined by the `type` field in the request body.

| `type` value | Behavior |
|---|---|
| `CONTACT` | Sends a notification email via Amazon SES (optional file attachment) |
| `JOB_APPLICATION` | Sends a notification email via Amazon SES with the CV attached |

---

## 2. Authentication

All requests must include a valid API key in the `x-api-key` HTTP header.

```
x-api-key: <your-api-key>
```

Requests without a valid key return HTTP `403 Forbidden` (returned by API Gateway, not the Lambda).

---

## 3. Common Response Envelope

All successful Lambda responses use the following JSON structure:

```json
{
  "success": true | false,
  "message": "Human-readable description of the result",
  "error":   null | "ERROR_CODE"
}
```

| Field | Type | Description |
|---|---|---|
| `success` | `boolean` | `true` on success, `false` on error |
| `message` | `string` | Human-readable message |
| `error` | `string \| null` | Machine-readable error code; `null` on success |

---

## 4. Error Codes

| Code | HTTP Status | Description |
|---|---|---|
| `MISSING_REQUIRED_FIELD` | 400 | A required field is null or blank |
| `VALIDATION_ERROR` | 400 | A field value fails format or business-rule validation |
| `INVALID_EMAIL` | 400 | The email address format is invalid |
| `INVALID_FILE_TYPE` | 400 | The file is not a PDF or DOCX, or magic bytes don't match |
| `FILE_TOO_LARGE` | 400 | The file exceeds the 5 MB size limit |
| `JSON_PARSE_ERROR` | 400 | The request body is not valid JSON |
| `EMAIL_SEND_FAILURE` | 500 | Amazon SES rejected the send request |
| `INTERNAL_ERROR` | 500 | Unexpected internal error |

---

## 5. Endpoints

### POST /contacts

Submits a contact form message or a job application.

| Attribute | Value |
|---|---|
| **Method** | `POST` |
| **Path** | `/contacts` |
| **Content-Type** | `application/json` |
| **Auth** | `x-api-key` header |

#### Request Headers

```
Content-Type: application/json
x-api-key: <api-key>
```

#### Request Body

See [Request Schemas](#6-request-schemas) below.

#### Response Codes

| Status | Condition |
|---|---|
| `200 OK` | Request processed successfully; email sent |
| `400 Bad Request` | Validation error (missing fields, invalid email, bad file) |
| `403 Forbidden` | Missing or invalid API key (API Gateway response) |
| `500 Internal Server Error` | Unexpected server-side failure |

---

## 6. Request Schemas

### Contact Request

Used when `type` is `CONTACT` (or omitted).

```json
{
  "type":               "CONTACT",
  "fullName":           "string (required, min 1 char)",
  "email":              "string (required, valid email format)",
  "company":            "string (optional)",
  "city":               "string (optional)",
  "subject":            "string (optional — auto-generated if blank)",
  "message":            "string (required, min 1 char)",
  "attachment":         "string (optional — Base64-encoded file, PDF or DOCX, max 5 MB)",
  "attachmentFileName": "string (required when attachment is provided)"
}
```

**Field details:**

| Field | Required | Type | Constraints |
|---|---|---|---|
| `type` | No | `"CONTACT"` | Defaults to `CONTACT` when omitted |
| `fullName` | Yes | string | Must not be blank. Alias: `name` |
| `email` | Yes | string | Must match `^[^@\s]+@[^@\s]+\.[^@\s]+$` |
| `company` | No | string | — |
| `city` | No | string | — |
| `subject` | No | string | Auto-filled as `"Elite CSP – Contact Form: {fullName}"` when blank |
| `message` | Yes | string | Must not be blank |
| `attachment` | No | string (Base64) | Data-URI prefix (`data:...;base64,`) is stripped automatically |
| `attachmentFileName` | Conditional | string | Required when `attachment` is provided; must end with `.pdf` or `.docx` |

---

### Job Application Request

Used when `type` is `JOB_APPLICATION`.

```json
{
  "type":               "JOB_APPLICATION",
  "fullName":           "string (required)",
  "email":              "string (required, valid email)",
  "city":               "string (optional)",
  "subject":            "string (optional — job position ID or title)",
  "message":            "string (required — cover letter)",
  "attachment":         "string (required — Base64-encoded CV, PDF or DOCX, max 5 MB)",
  "attachmentFileName": "string (required — original filename, e.g. 'resume.pdf')"
}
```

**Field details:**

| Field | Required | Type | Constraints |
|---|---|---|---|
| `type` | Yes | `"JOB_APPLICATION"` | — |
| `fullName` | Yes | string | Must not be blank. Alias: `name` |
| `email` | Yes | string | Must match email regex |
| `city` | No | string | — |
| `subject` | No | string | Job position title or ID |
| `message` | Yes | string | Cover letter; must not be blank |
| `attachment` | Yes | string (Base64) | PDF or DOCX; ≤ 5 MB; magic bytes validated |
| `attachmentFileName` | Yes | string | Must end with `.pdf` or `.docx` |

---

## 7. Response Schemas

### Success (HTTP 200)

```json
{
  "success": true,
  "message": "Your message has been sent successfully.",
  "error": null
}
```

For job applications:
```json
{
  "success": true,
  "message": "Your application has been submitted successfully.",
  "error": null
}
```

### Validation Error (HTTP 400)

```json
{
  "success": false,
  "message": "Email address is invalid: notanemail",
  "error": "INVALID_EMAIL"
}
```

```json
{
  "success": false,
  "message": "Full name must not be blank",
  "error": "MISSING_REQUIRED_FIELD"
}
```

```json
{
  "success": false,
  "message": "CV file exceeds the maximum allowed size of 5 MB (actual: 6291456 bytes)",
  "error": "FILE_TOO_LARGE"
}
```

### Server Error (HTTP 500)

```json
{
  "success": false,
  "message": "An unexpected error occurred. Please try again later.",
  "error": "INTERNAL_ERROR"
}
```

---

## 8. Examples

### Contact Form (cURL)

**Minimal contact submission:**
```bash
curl -X POST https://ek7n4dkkde.execute-api.ca-central-1.amazonaws.com/prod/contacts \
  -H "Content-Type: application/json" \
  -H "x-api-key: YOUR_API_KEY" \
  -d '{
    "fullName": "Jean Tremblay",
    "email":    "jean.tremblay@example.com",
    "message":  "I would like to learn more about your IBM Maximo services."
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Your message has been sent successfully.",
  "error": null
}
```

---

**Full contact submission with optional fields:**
```bash
curl -X POST https://ek7n4dkkde.execute-api.ca-central-1.amazonaws.com/prod/contacts \
  -H "Content-Type: application/json" \
  -H "x-api-key: YOUR_API_KEY" \
  -d '{
    "type":    "CONTACT",
    "fullName": "Marie Côté",
    "email":   "marie.cote@hydro.com",
    "company": "Hydro Solutions Québec",
    "city":    "Québec City",
    "subject": "Maximo Implementation Inquiry",
    "message": "We are looking for an implementation partner for our Maximo upgrade project."
  }'
```

---

### Job Application (cURL)

```bash
# Encode the CV file first
CV_BASE64=$(base64 -w 0 resume.pdf)

curl -X POST https://ek7n4dkkde.execute-api.ca-central-1.amazonaws.com/prod/contacts \
  -H "Content-Type: application/json" \
  -H "x-api-key: YOUR_API_KEY" \
  -d "{
    \"type\":               \"JOB_APPLICATION\",
    \"fullName\":           \"Sophie Bergeron\",
    \"email\":              \"sophie.bergeron@example.com\",
    \"city\":               \"Montréal\",
    \"subject\":            \"1\",
    \"message\":            \"I am excited to apply for the Maximo Consultant position. I have 5 years of experience with IBM Maximo implementations in the utilities sector.\",
    \"attachment\":         \"${CV_BASE64}\",
    \"attachmentFileName\": \"resume.pdf\"
  }"
```

**Response:**
```json
{
  "success": true,
  "message": "Your application has been submitted successfully.",
  "error": null
}
```

---

### Error Responses

**Missing required field:**
```bash
curl -X POST .../contacts \
  -H "Content-Type: application/json" \
  -H "x-api-key: YOUR_API_KEY" \
  -d '{ "email": "test@example.com", "message": "Hello" }'
```
```json
{
  "success": false,
  "message": "Full name must not be blank",
  "error": "MISSING_REQUIRED_FIELD"
}
```

**Invalid email:**
```bash
-d '{ "fullName": "Test", "email": "not-an-email", "message": "Hello" }'
```
```json
{
  "success": false,
  "message": "Email address is invalid: not-an-email",
  "error": "INVALID_EMAIL"
}
```

**File too large (> 5 MB):**
```json
{
  "success": false,
  "message": "CV file exceeds the maximum allowed size of 5 MB (actual: 6291456 bytes)",
  "error": "FILE_TOO_LARGE"
}
```

**Unsupported file type:**
```json
{
  "success": false,
  "message": "Unsupported file type. Allowed types: PDF, DOCX",
  "error": "INVALID_FILE_TYPE"
}
```

---

## 9. CORS

The API currently returns `Access-Control-Allow-Origin: *` on all responses. This allows requests from any origin.

> ⚠️ For production hardening, restrict this to the specific frontend domain (e.g., `https://www.elitecsp.ca`). See issue C-02 in the [Code Quality Report](./code-quality-report.md).

CORS headers returned on all responses:
```
Access-Control-Allow-Origin: *
Content-Type: application/json
```
