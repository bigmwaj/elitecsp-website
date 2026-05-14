import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

export interface ManagedOffer {
  id: string;
  icon: 'foundation' | 'guidance' | 'operations' | 'partnership';
  badgeKey: string;
  titleKey: string;
  descriptionKey: string;
  benefits: string[];
  highlightKey?: string;
  featured?: boolean;
}

@Component({
  selector: 'app-managed-offer-card',
  standalone: true,
  imports: [RouterLink, TranslatePipe],
  templateUrl: './managed-offer-card.html',
  styleUrl: './managed-offer-card.scss'
})
export class ManagedOfferCardComponent {
  offer = input.required<ManagedOffer>();
  ctaTextKey = input<string>('PAGE.INFOGERANCE.HERO.CTA');
  ctaLink = input<string>('/contact');
}
