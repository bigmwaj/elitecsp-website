import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { assetPath } from '../../utils/asset-path.util';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  templateUrl: './hero.html',
  styleUrl: './hero.scss'
})
export class HeroComponent {
  readonly assetPath = assetPath;
  title = input<string>('');
  subtitle = input<string>('');
  ctaText = input<string>('SHARED.CTA.DEFAULT_CTA');
  ctaFragment = input<string>('');
  ctaLink = input<string>('/contact');
  secondaryCtaText = input<string>('');
  secondaryCtaLink = input<string>('');
  secondaryCtaFragment = input<string>('');
  compact = input<boolean>(false);
  showPartnerLogos = input<boolean>(false);
}
