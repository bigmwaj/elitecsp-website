# User Guide — Elite CSP Website

> This guide explains how to use the interactive features of the Elite CSP website: the **Contact Form** and the **Job Application Form**.

---

## Table of Contents

1. [Contact Form](#1-contact-form)
   - [How to Access](#11-how-to-access)
   - [Step-by-Step Instructions](#12-step-by-step-instructions)
   - [Field Reference](#13-field-reference)
   - [Expected Behaviors](#14-expected-behaviors)
   - [Error Cases](#15-error-cases)
2. [Job Application Form](#2-job-application-form)
   - [How to Access](#21-how-to-access)
   - [Step-by-Step Instructions](#22-step-by-step-instructions)
   - [Field Reference](#23-field-reference)
   - [File Requirements (CV)](#24-file-requirements-cv)
   - [Expected Behaviors](#25-expected-behaviors)
   - [Error Cases](#26-error-cases)
3. [Language Switching](#3-language-switching)
4. [Frequently Asked Questions](#4-frequently-asked-questions)

---

## 1. Contact Form

### 1.1 How to Access

Navigate to the **Contact** page using the top navigation bar, or click any **"Contact us"** call-to-action button on the website.

Direct URL: `/contact`

---

### 1.2 Step-by-Step Instructions

**Step 1 — Fill in your name**  
Enter your full name in the **Name** field. Minimum 2 characters.

**Step 2 — Enter your email address**  
Enter a valid email address. This will be used as the Reply-To address so Elite CSP can respond to you.

**Step 3 — Enter your company name (optional)**  
If you are contacting us on behalf of a company, enter the company name. This field is optional.

**Step 4 — Enter a subject**  
Describe the reason for your contact in a few words (e.g., *"Maximo implementation inquiry"*, *"Partnership opportunity"*). This field is required.

**Step 5 — Write your message**  
Type your message in the **Message** field. Minimum 20 characters.

**Step 6 — Submit the form**  
Click the **Send** button. While the form is submitting, the button will show a loading indicator.

**Step 7 — Confirmation**  
Upon successful submission, the form is replaced with a success message confirming that your message has been sent. Elite CSP will review your message and respond at their earliest convenience.

---

### 1.3 Field Reference

| Field | Required | Description |
|---|---|---|
| Name | ✅ Yes | Your full name (min. 2 characters) |
| Email | ✅ Yes | A valid email address |
| Company | No | Your organization or company name |
| Subject | ✅ Yes | Brief description of your enquiry |
| Message | ✅ Yes | Your message (min. 20 characters) |

---

### 1.4 Expected Behaviors

| Action | Expected Result |
|---|---|
| Submit with all valid fields | Success message displayed; form is cleared |
| Click **Send** on a new submission after success | Form returns to its empty initial state |
| Leave a required field blank and click **Send** | All touched/invalid fields are highlighted with validation errors |

---

### 1.5 Error Cases

| Error Message | Cause | Resolution |
|---|---|---|
| *"Name is required"* | Name field is empty | Enter your full name |
| *"Name must be at least 2 characters"* | Name is too short | Use your full name |
| *"Email is required"* | Email field is empty | Enter your email address |
| *"Invalid email address"* | Email format is invalid (e.g., missing `@`) | Correct the email address format |
| *"Subject is required"* | Subject field is empty | Enter a brief subject |
| *"Message is required"* | Message field is empty | Write your message |
| *"Message must be at least 20 characters"* | Message is too short | Expand your message |

---

## 2. Job Application Form

### 2.1 How to Access

Navigate to the **Careers** page using the top navigation bar.

Direct URL: `/careers`

The page displays all open positions. Clicking the **Apply** button on a job card will:
1. Automatically scroll the page to the application form.
2. Pre-fill the **Position** field with the selected job.

You can also scroll down manually to the application form and select the position manually.

---

### 2.2 Step-by-Step Instructions

**Step 1 — Browse job listings**  
Review the available positions on the Careers page. Each listing shows the job title, description, location, and type (full-time, contract, etc.).

**Step 2 — Click "Apply"**  
Click the **Apply** button on the job you are interested in. The form will scroll into view with the position pre-selected.

**Step 3 — Enter your full name**  
Enter your legal full name as it appears on your CV.

**Step 4 — Enter your email address**  
Enter a valid email address. Elite CSP will contact you at this address.

**Step 5 — Enter your city**  
Enter the city you are located in or applying from.

**Step 6 — Confirm or select the position**  
If you clicked **Apply** on a job card, the position is already selected. Otherwise, choose the relevant position from the dropdown.

**Step 7 — Write a cover letter**  
Write a cover letter (minimum 50 characters) describing your experience, motivation, and why you are interested in the position.

**Step 8 — Attach your CV**  
Click the file input and select your CV file. Requirements:
- **Formats accepted:** PDF (`.pdf`) or Word document (`.docx`)
- **Maximum size:** 5 MB

**Step 9 — Submit your application**  
Click the **Submit Application** button. The button shows a loading indicator while the application is being sent.

**Step 10 — Confirmation**  
Upon successful submission, a confirmation message replaces the form. Elite CSP will review your application and contact you.

---

### 2.3 Field Reference

| Field | Required | Description |
|---|---|---|
| Full Name | ✅ Yes | Your full legal name (min. 2 characters) |
| Email | ✅ Yes | A valid email address |
| City | ✅ Yes | Your city of residence or location |
| Position | ✅ Yes | The job position you are applying for |
| Cover Letter | ✅ Yes | Your motivation and experience summary (min. 50 characters) |
| CV File | ✅ Yes | Your résumé/CV in PDF or DOCX format (max. 5 MB) |

---

### 2.4 File Requirements (CV)

| Requirement | Details |
|---|---|
| **Accepted formats** | PDF (`.pdf`), Word (`.docx`) |
| **Maximum file size** | 5 MB |
| **File validation** | The server verifies the file type using magic bytes (file header), not just the extension |

> **Tips for preparing your CV:**
> - Save your CV as a PDF for best compatibility.
> - Ensure the file is not password-protected.
> - Use a filename without special characters (e.g., `jean-tremblay-cv.pdf`).

---

### 2.5 Expected Behaviors

| Action | Expected Result |
|---|---|
| Click **Apply** on a job card | Form scrolls into view; Position field pre-filled |
| Submit with all valid fields and a valid CV | Success message displayed; form and file input cleared |
| Upload a valid PDF or DOCX | File accepted; no error shown |
| Attempt to submit without a file | File error message displayed below the file input |

---

### 2.6 Error Cases

| Error | Cause | Resolution |
|---|---|---|
| *"Full name is required"* | Name field is empty | Enter your full name |
| *"Email is required"* or *"Invalid email"* | Missing or malformed email | Correct your email address |
| *"City is required"* | City field is empty | Enter your city |
| *"Position is required"* | No job selected | Select a position from the dropdown |
| *"Cover letter is required"* / *"at least 50 characters"* | Missing or too-short cover letter | Write a proper cover letter |
| *"Please attach your CV"* | No file selected | Click the file input and select your CV |
| *"Invalid file type (PDF or DOCX only)"* | Wrong file format selected | Convert your CV to PDF or DOCX |
| *"File too large (max 5 MB)"* | File exceeds size limit | Compress or re-export your CV |

---

## 3. Language Switching

The website is available in **French** (default) and **English**.

To switch languages:
1. Locate the language switcher in the top navigation bar.
2. Click **FR** for French or **EN** for English.

Your language preference is saved in your browser and will be restored on your next visit.

---

## 4. Frequently Asked Questions

**Q: How long will it take to receive a response after submitting the contact form?**  
A: Elite CSP typically responds within 1–2 business days. For urgent matters, contact us by phone.

**Q: I submitted my job application but did not receive a confirmation email. Was my application received?**  
A: The application form shows a success message when your application has been successfully transmitted. Elite CSP does not send an automated acknowledgement email to applicants at this time.

**Q: Can I attach a portfolio or additional documents with my contact form?**  
A: Yes. The contact form supports optional file attachments (PDF or DOCX, max 5 MB). Attach your document using the file input if it appears, or mention in your message that you have additional documents to share.

**Q: My CV is in `.doc` (old Word format) — can I upload it?**  
A: No. Only `.pdf` and `.docx` formats are accepted. Please re-save your document as `.docx` (Word 2007+) or export it as a PDF.

**Q: What happens to my CV after I submit it?**  
A: Your CV is transmitted directly to Elite CSP via a secure email. It is not stored in any public database.
