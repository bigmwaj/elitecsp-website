import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

const LANG_KEY = 'app-language';
const SUPPORTED_LANGS = ['en', 'fr'] as const;
const DEFAULT_LANG = 'en';
type SupportedLanguage = (typeof SUPPORTED_LANGS)[number];

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private readonly translate = inject(TranslateService);
  private readonly platformId = inject(PLATFORM_ID);

  init(): void {
    this.translate.addLangs([...SUPPORTED_LANGS]);
    this.translate.setDefaultLang(DEFAULT_LANG);
    this.translate.use(this.getInitialLanguage());
  }

  use(lang: string): void {
    const normalizedLang = this.normalizeSupportedLanguage(lang);
    if (!normalizedLang) {
      return;
    }

    this.translate.use(normalizedLang);
    this.persistLanguage(normalizedLang);
  }

  get currentLang(): string {
    return this.translate.currentLang || this.translate.defaultLang || DEFAULT_LANG;
  }

  private getInitialLanguage(): SupportedLanguage {
    const savedLanguage = this.readStoredLanguage();
    if (savedLanguage) {
      return savedLanguage;
    }

    return this.detectBrowserLanguage() ?? DEFAULT_LANG;
  }

  private readStoredLanguage(): SupportedLanguage | null {
    if (!isPlatformBrowser(this.platformId) || typeof localStorage === 'undefined') {
      return null;
    }

    try {
      return this.normalizeSupportedLanguage(localStorage.getItem(LANG_KEY));
    } catch {
      return null;
    }
  }

  private detectBrowserLanguage(): SupportedLanguage | null {
    if (!isPlatformBrowser(this.platformId) || typeof navigator === 'undefined') {
      return null;
    }

    const candidates = [...(navigator.languages ?? []), navigator.language].filter((language): language is string =>
      Boolean(language)
    );

    for (const candidate of candidates) {
      const normalizedLanguage = this.normalizeSupportedLanguage(candidate);
      if (normalizedLanguage) {
        return normalizedLanguage;
      }
    }

    return null;
  }

  private normalizeSupportedLanguage(language: string | null): SupportedLanguage | null {
    if (!language) {
      return null;
    }

    const baseLanguage = language.toLowerCase().split('-')[0];
    return SUPPORTED_LANGS.find(supportedLanguage => supportedLanguage === baseLanguage) ?? null;
  }

  private persistLanguage(language: SupportedLanguage): void {
    if (!isPlatformBrowser(this.platformId) || typeof localStorage === 'undefined') {
      return;
    }

    try {
      localStorage.setItem(LANG_KEY, language);
    } catch {
      // Ignore persistence failures (e.g., storage disabled).
    }
  }
}
