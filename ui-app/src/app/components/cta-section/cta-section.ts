import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-cta-section',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  templateUrl: './cta-section.html',
  styleUrl: './cta-section.scss'
})
export class CtaSectionComponent {
  title = input<string>('SHARED.CTA.DEFAULT_TITLE');
  subtitle = input<string>('SHARED.CTA.DEFAULT_SUBTITLE');
  ctaText = input<string>('SHARED.CTA.DEFAULT_CTA');
  ctaLink = input<string>('/contact');
}
