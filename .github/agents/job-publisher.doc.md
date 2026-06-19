# `job-publisher.md` —  Documentation

This file defines Claude Code agent for the job-application
feature. Each `.md` file follows a YAML frontmatter (`name`, `description`)
plus a Markdown body describing the agent's role, rules, and done criteria.

Use this agent when you just have **one job title and some free-text
context** (responsibilities, requirements, benefits, location, etc.) and
want a single bilingual `JobSummary` + `JobDetail` entry created or updated.

Example invocation:
> "Using the job-publisher agent, add a job posting for 'Ingénieur Cloud AWS' — full-time, Montreal, category career. Responsibilities: design and maintain AWS infrastructure, lead migrations to the cloud, mentor junior engineers. Requirements: 5+ years AWS experience, Terraform, strong English/French communication. Benefits: remote-friendly, health insurance, training budget."

## Shared conventions

Both agents follow the same conventions so the data files stay consistent
regardless of which one is used:

- **Bilingual fields**: `title`, `summary`, `description`, and the list
  fields (`responsibilities`, `requirements`, `benefits`) are always stored
  as `{ fr: ..., en: ... }` (or `{ fr: string[], en: string[] }` for lists).
  If only one language is provided, the agent translates to fill the other.
- **Slug generation**: `slug` is bilingual (`{ fr, en }`); each language's
  slug is derived from that language's own title (lowercased,
  accent-stripped, hyphenated). If only one language's title is given, the
  other is translated first, then slugified — the two slugs are not assumed
  to be identical. Slugs must stay stable across repeated runs for the same
  job.
- **`jobId` upsert rule**: match existing jobs by `slug.fr` or `slug.en`;
  update in place if found, otherwise insert with `jobId = max(existing jobId) + 1`. `jobId`
  must always match between `JobSummaryData.JOB_SUMMARIES` (in
  `ui-app/src/app/data/job-summary.data.ts`) and `JobDetailData.JOB_DETAILS`
  (in `ui-app/src/app/data/job-detail.data.ts`).
- **Scope**: data-import/creation tasks should only touch those two data
  files unless the user explicitly asks for broader changes.

When adding a new agent to this folder, keep it consistent with these rules
and add an entry to this README.
