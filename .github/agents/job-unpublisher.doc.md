# `job-unpublisher.md` —  Documentation

This file defines a Claude Code agent for the job-application
feature. Each `.md` file follows a YAML frontmatter (`name`, `description`)
plus a Markdown body describing the agent's role, rules, and done criteria.

Use this agent when you want to **remove a job posting**, either:
- a single job identified by its **title** (or slug); or
- **all expired jobs** whose `expirationDate` is more than 6 months in the
  past (bulk cleanup, no title needed).

Example invocations:
> "Using the job-unpublisher agent, remove the job posting 'Ingénieur Cloud AWS'."

> "Using the job-unpublisher agent, clean up jobs that expired more than 6 months ago."

## Deletion modes

- **By title** — matches the given title (case-insensitive, accent-stripped)
  against `title.fr` or `title.en` (or against `slug.fr`/`slug.en` if a slug
  is given instead). If the title matches zero or more than one job, the
  agent asks for clarification instead of guessing.
- **By expiration** — triggered when no title is given. The agent computes
  `cutoffDate = today - 6 months` and removes every job whose
  `expirationDate` is set and older than that cutoff. Jobs with no
  `expirationDate` are never removed by this rule. If more than a couple of
  jobs match, the agent lists them and confirms before deleting.

## Shared conventions

This agent follows the same conventions as `job-publisher.md` so the data
files stay consistent regardless of which agent touched them:

- **Target files**: `ui-app/src/app/data/job-summary.data.ts`
  (`JobSummaryData.JOB_SUMMARIES`) and `ui-app/src/app/data/job-detail.data.ts`
  (`JobDetailData.JOB_DETAILS`).
- **`jobId` pairing**: a job's summary and detail entries always share the
  same `jobId`. Deleting a job means removing that `jobId` from **both**
  files — never leave a dangling entry in only one of them.
- **No renumbering**: deleting a job does not renumber or reassign the
  `jobId`/`sortIndex` of the remaining jobs.
- **Scope**: deletion tasks should only touch those two data files unless the
  user explicitly asks for broader changes.

When adding a new agent to this folder, keep it consistent with these rules
and add an entry to this README.
