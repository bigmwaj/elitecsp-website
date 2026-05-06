import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  templateUrl: './hero.html',
  styleUrl: './hero.scss'
})
export class HeroComponent {
  title = input<string>('');
  subtitle = input<string>('');
  ctaText = input<string>('CTA.DEFAULT_CTA');
  ctaFragment = input<string>('');
  ctaLink = input<string>('/contact');
  secondaryCtaText = input<string>('');
  secondaryCtaLink = input<string>('');
  secondaryCtaFragment = input<string>('');
  compact = input<boolean>(false);
}
