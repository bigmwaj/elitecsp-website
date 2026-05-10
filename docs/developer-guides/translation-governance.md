# Translation Governance (`ui-app`)

## Naming conventions

- Page-level keys must be under `PAGE.<PAGE_NAME>.*` (for example `PAGE.HOME.*`, `PAGE.CONTACT.*`, `PAGE.JOB_DETAILS.*`).
- Navigation keys must be under `MENU.*`.
- Shared/global reusable keys must be under `SHARED.*`.
- Brand/company keys must be under `COMMON.*`.

## Standard namespaces

- `PAGE.HOME.*`
- `PAGE.ABOUT.*`
- `PAGE.SERVICES.*`
- `PAGE.CONTACT.*`
- `PAGE.CAREERS.*`
- `PAGE.PARTNERS.*`
- `PAGE.JOB_DETAILS.*`
- `MENU.*`
- `SHARED.*`
- `COMMON.*`

## Forbidden patterns

- Page keys without the `PAGE.<PAGE_NAME>` prefix (for example `HOME.*`, `SERVICES_PAGE.*`, `ABOUT_PAGE.*`).
- Mixed page namespace styles in the same feature (`PAGE.CONTACT.*` and `CONTACT_PAGE.*` together).
- New top-level namespaces for page content outside `PAGE.*`.

## How to add translations

1. Add the key in both `ui-app/public/static/i18n/en.json` and `fr.json`.
2. Keep the same hierarchy and key path in both locales.
3. Use the matching namespace for the feature (`PAGE`, `MENU`, `SHARED`, `COMMON`).
4. Replace hardcoded user-facing text in templates/TypeScript with the translation key.
5. Run `npm run i18n:check` from `ui-app` before opening a PR.

## Validation and CI recommendation

- Local validation command: `npm run i18n:check`.
- Add `npm run i18n:check` to CI so PRs fail on:
  - missing keys
  - unused keys
  - locale key mismatches between English and French
