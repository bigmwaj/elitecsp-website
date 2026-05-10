import fs from 'node:fs';
import path from 'node:path';

const projectRoot = path.resolve(process.cwd());
const srcRoot = path.join(projectRoot, 'src');
const localesRoot = path.join(projectRoot, 'public/assets/i18n-v1.1');
const locales = ['en', 'fr'];
// Key extraction is intentionally aligned with current governance (uppercase dotted namespaces).
const keyRegex = /['"]([A-Z][A-Z0-9_]*(?:\.[A-Z0-9_]+)+)['"]/g;
const ignoredNamespacePlaceholders = new Set(['PAGE.CAREERS.FORM', 'SHARED.DATA.JOBS']);
const strictUnused = process.env.I18N_STRICT_UNUSED !== 'false';
const dynamicKeys = [
  'PAGE.CAREERS.FORM.CV_ERROR',
  'PAGE.CAREERS.FORM.CV_TYPE_ERROR',
  'SHARED.DATA.JOBS.CONTRACT',
  'SHARED.DATA.JOBS.FULL_TIME',
  'SHARED.PARTNERS.IBM.DESCRIPTION',
  'SHARED.PARTNERS.IBM.NAME',
  'SHARED.PARTNERS.IBM.TIER',
  'SHARED.PARTNERS.PEMAC.DESCRIPTION',
  'SHARED.PARTNERS.PEMAC.NAME',
  'SHARED.PARTNERS.PEMAC.TIER'
];

function walkFiles(dir, exts, out = []) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const entryPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walkFiles(entryPath, exts, out);
    } else if (exts.has(path.extname(entry.name))) {
      out.push(entryPath);
    }
  }
  return out;
}

function extractUsedKeys() {
  const used = new Set(dynamicKeys);
  const files = walkFiles(srcRoot, new Set(['.ts', '.html']));

  for (const file of files) {
    const content = fs.readFileSync(file, 'utf8');
    for (const match of content.matchAll(keyRegex)) {
      const key = match[1];
      if (ignoredNamespacePlaceholders.has(key)) {
        continue;
      }
      used.add(key);
    }
  }

  return used;
}

function flattenKeys(obj, prefix = '', out = new Set()) {
  if (obj && typeof obj === 'object' && !Array.isArray(obj)) {
    for (const [k, v] of Object.entries(obj)) {
      flattenKeys(v, prefix ? `${prefix}.${k}` : k, out);
    }
    return out;
  }

  out.add(prefix);
  return out;
}

function readJson(file) {
  return JSON.parse(fs.readFileSync(file, 'utf8'));
}

function printReport(type, entries) {
  console.log(`\\n${type} (${entries.length})`);
  for (const entry of entries) {
    console.log(`- ${entry}`);
  }
}

const used = extractUsedKeys();
const localeKeyMap = new Map();
let hasErrors = false;

for (const locale of locales) {
  const localeFile = path.join(localesRoot, `${locale}.json`);
  const localeData = readJson(localeFile);
  const keys = flattenKeys(localeData);
  localeKeyMap.set(locale, keys);

  const missing = [...used].filter(k => !keys.has(k)).sort();
  const unused = [...keys].filter(k => !used.has(k)).sort();

  if (missing.length) {
    hasErrors = true;
    printReport(`Missing in ${locale}`, missing);
  }

  if (unused.length) {
    printReport(`Unused in ${locale}`, unused);
    if (strictUnused) {
      hasErrors = true;
    }
  }
}

for (const locale of locales) {
  const other = locales.find(l => l !== locale);
  const missingInLocale = [...localeKeyMap.get(other)].filter(k => !localeKeyMap.get(locale).has(k)).sort();
  if (missingInLocale.length) {
    hasErrors = true;
    printReport(`Locale mismatch ${locale} missing keys from ${other}`, missingInLocale);
  }
}

if (!hasErrors) {
  console.log('i18n validation passed: no missing, unused, or cross-locale key mismatches detected.');
}

process.exit(hasErrors ? 1 : 0);
