import { Injectable, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

const LANG_KEY = 'ui-app_lang';

@Injectable({ providedIn: 'root' })
export class TranslationService {
  private translate = inject(TranslateService);

  init(): void {
    const supported = ['fr', 'en'];
    this.translate.addLangs(supported);
    this.translate.setDefaultLang('en');
    const saved = localStorage.getItem(LANG_KEY) ?? 'en';
    const lang = supported.includes(saved) ? saved : 'en';
    this.translate.use(lang);
  }

  use(lang: string): void {
    this.translate.use(lang);
    localStorage.setItem(LANG_KEY, lang);
  }

  get currentLang(): string {
    return this.translate.currentLang;
  }
}
