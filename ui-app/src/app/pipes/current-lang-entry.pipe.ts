import { Pipe, PipeTransform, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Pipe({
  name: 'currentLangEntry',
  standalone: true,
  pure: false
})
export class CurrentLangEntryPipe implements PipeTransform {
  private translationService = inject(TranslateService);

  transform(entry: { fr: string; en: string } | null | undefined): string {
    if (!entry) {
      return '';
    }

    const currentLang = this.translationService.currentLang?.startsWith('fr') ? 'fr' : 'en';
    return entry[currentLang] ?? '';
  }
}
