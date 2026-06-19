# `job-linkedin-poster.md` —  Documentation

This file defines a Claude Code agent for the job-application
feature. Each `.md` file follows a YAML frontmatter (`name`, `description`)
plus a Markdown body describing the agent's role, rules, and done criteria.

Use this agent when you want to **promote an existing job posting on
LinkedIn**. Given a job title, it produces:
- a deep link to the job's public detail page per language
  (`/careers/<slug.fr>` for the French post, `/careers/<slug.en>` for the
  English post);
- a banner image illustrating the role;
- two ready-to-publish LinkedIn post drafts — one in French, one in English.

Example invocation:
> "Using the job-linkedin-poster agent, generate LinkedIn posts for 'Développeur Java'. Base URL: https://www.elitecsp.com."

## How it works

1. **Lookup** — the job is matched by title (or `slug.fr`/`slug.en`) against
   `JobSummaryData.JOB_SUMMARIES` / `JobDetailData.JOB_DETAILS`. If the title
   is ambiguous or not found, the agent asks for clarification instead of
   guessing.
2. **Link** — built from the confirmed production base URL plus the job's
   per-language slug, e.g. `https://www.elitecsp.com/careers/developpeur-java`
   (`slug.fr`) for the French post and
   `https://www.elitecsp.com/careers/java-developer` (`slug.en`) for the
   English post.
3. **Banner image** — generated to illustrate the role and saved under
   `ui-app/src/assets/jobs/banners/<slug>.png`. If no image-generation tool
   is available, the agent provides a detailed image brief instead and flags
   that the image still needs to be produced manually.
4. **Posts** — both languages follow the same structure (hook, pitch,
   highlights, call to action, hashtags), grounded only in the job's real
   data — no invented requirements or benefits.

This agent is **read-only with respect to the job data files**: it never
modifies `job-summary.data.ts` or `job-detail.data.ts`, it only reads them to
ground the post content.

## Shared conventions

This agent follows the same conventions as `job-publisher.md` and
`job-unpublisher.md` so behavior stays consistent across the job-application
agents:

- **Source of truth**: `ui-app/src/app/data/job-summary.data.ts`
  (`JobSummaryData.JOB_SUMMARIES`) and `ui-app/src/app/data/job-detail.data.ts`
  (`JobDetailData.JOB_DETAILS`), matched by `slug.fr`/`slug.en` or `jobId`.
- **Matching by title**: case-insensitive, accent-stripped comparison against
  `title.fr` / `title.en`, same as `job-unpublisher.md`'s title-matching rule.
  Ambiguous or missing matches require asking the user, not guessing.
- **Scope**: this agent only produces content (text + banner image asset);
  it does not edit the job datasets.

When adding a new agent to this folder, keep it consistent with these rules
and add an entry to this README.
