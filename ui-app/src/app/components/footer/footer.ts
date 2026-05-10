import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { TranslationService } from '../../services/translation.service';
import { assetPath } from '../../utils/asset-path.util';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  templateUrl: './footer.html',
  styleUrl: './footer.scss'
})
export class FooterComponent {
  readonly assetPath = assetPath;
  year = new Date().getFullYear();
  private translationService = inject(TranslationService);

  get currentLang(): string {
    return this.translationService.currentLang;
  }

  switchLang(lang: string): void {
    this.translationService.use(lang);
  }
}
