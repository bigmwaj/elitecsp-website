import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Title, Meta } from '@angular/platform-browser';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { HeroComponent } from '../../components/hero/hero';
import { CtaSectionComponent } from '../../components/cta-section/cta-section';
import { ManagedOffer, ManagedOfferCardComponent } from '../../components/managed-offer-card/managed-offer-card';
import { RevealOnScrollDirective } from '../../directives/reveal-on-scroll/reveal-on-scroll';

@Component({
  selector: 'app-infogerance',
  standalone: true,
  imports: [
    RouterLink,
    HeroComponent,
    CtaSectionComponent,
    TranslatePipe,
    ManagedOfferCardComponent,
    RevealOnScrollDirective
  ],
  templateUrl: './infogerance.html',
  styleUrl: './infogerance.scss'
})
export class InfogeranceComponent implements OnInit {
  private title = inject(Title);
  private meta = inject(Meta);
  private translate = inject(TranslateService);

  readonly offers: ManagedOffer[] = [
    {
      id: 'foundation',
      icon: 'foundation',
      badgeKey: 'PAGE.INFOGERANCE.OFFERS.FREE.BADGE',
      titleKey: 'PAGE.INFOGERANCE.OFFERS.FREE.TITLE',
      descriptionKey: 'PAGE.INFOGERANCE.OFFERS.FREE.DESC',
      benefits: [
        'PAGE.INFOGERANCE.OFFERS.FREE.F1',
        'PAGE.INFOGERANCE.OFFERS.FREE.F2',
        'PAGE.INFOGERANCE.OFFERS.FREE.F3',
        'PAGE.INFOGERANCE.OFFERS.FREE.F4'
      ]
    },
    {
      id: 'starter',
      icon: 'guidance',
      badgeKey: 'PAGE.INFOGERANCE.OFFERS.STARTER.BADGE',
      titleKey: 'PAGE.INFOGERANCE.OFFERS.STARTER.TITLE',
      descriptionKey: 'PAGE.INFOGERANCE.OFFERS.STARTER.DESC',
      benefits: [
        'PAGE.INFOGERANCE.OFFERS.STARTER.F1',
        'PAGE.INFOGERANCE.OFFERS.STARTER.F2',
        'PAGE.INFOGERANCE.OFFERS.STARTER.F3',
        'PAGE.INFOGERANCE.OFFERS.STARTER.F4'
      ]
    },
    {
      id: 'standard',
      icon: 'operations',
      badgeKey: 'PAGE.INFOGERANCE.OFFERS.STANDARD.BADGE',
      titleKey: 'PAGE.INFOGERANCE.OFFERS.STANDARD.TITLE',
      descriptionKey: 'PAGE.INFOGERANCE.OFFERS.STANDARD.DESC',
      benefits: [
        'PAGE.INFOGERANCE.OFFERS.STANDARD.F1',
        'PAGE.INFOGERANCE.OFFERS.STANDARD.F2',
        'PAGE.INFOGERANCE.OFFERS.STANDARD.F3',
        'PAGE.INFOGERANCE.OFFERS.STANDARD.F4'
      ],
      highlightKey: 'PAGE.INFOGERANCE.OFFERS.STANDARD.HIGHLIGHT',
      featured: true
    },
    {
      id: 'emerite',
      icon: 'partnership',
      badgeKey: 'PAGE.INFOGERANCE.OFFERS.EMERITE.BADGE',
      titleKey: 'PAGE.INFOGERANCE.OFFERS.EMERITE.TITLE',
      descriptionKey: 'PAGE.INFOGERANCE.OFFERS.EMERITE.DESC',
      benefits: [
        'PAGE.INFOGERANCE.OFFERS.EMERITE.F1',
        'PAGE.INFOGERANCE.OFFERS.EMERITE.F2',
        'PAGE.INFOGERANCE.OFFERS.EMERITE.F3',
        'PAGE.INFOGERANCE.OFFERS.EMERITE.F4'
      ],
      highlightKey: 'PAGE.INFOGERANCE.OFFERS.EMERITE.HIGHLIGHT'
    }
  ];

  readonly servicePillars = [
    {
      icon: 'strategy',
      titleKey: 'PAGE.INFOGERANCE.PILLARS.STRATEGY.TITLE',
      descriptionKey: 'PAGE.INFOGERANCE.PILLARS.STRATEGY.DESCRIPTION'
    },
    {
      icon: 'expertise',
      titleKey: 'PAGE.INFOGERANCE.PILLARS.EXPERTISE.TITLE',
      descriptionKey: 'PAGE.INFOGERANCE.PILLARS.EXPERTISE.DESCRIPTION'
    },
    {
      icon: 'operations',
      titleKey: 'PAGE.INFOGERANCE.PILLARS.OPERATIONS.TITLE',
      descriptionKey: 'PAGE.INFOGERANCE.PILLARS.OPERATIONS.DESCRIPTION'
    },
    {
      icon: 'improvement',
      titleKey: 'PAGE.INFOGERANCE.PILLARS.IMPROVEMENT.TITLE',
      descriptionKey: 'PAGE.INFOGERANCE.PILLARS.IMPROVEMENT.DESCRIPTION'
    }
  ];

  readonly stats = [
    { value: '24/7', labelKey: 'PAGE.INFOGERANCE.STATS.ASSETS' },
    { value: '15+', labelKey: 'PAGE.INFOGERANCE.STATS.CLIENTS' },
    { value: '98%', labelKey: 'PAGE.INFOGERANCE.STATS.SATISFACTION' },
    { value: '99.9%', labelKey: 'PAGE.INFOGERANCE.STATS.UPTIME' }
  ];

  readonly faqItems = [
    { questionKey: 'PAGE.INFOGERANCE.FAQ.Q1.Q', answerKey: 'PAGE.INFOGERANCE.FAQ.Q1.A', open: false },
    { questionKey: 'PAGE.INFOGERANCE.FAQ.Q2.Q', answerKey: 'PAGE.INFOGERANCE.FAQ.Q2.A', open: false },
    { questionKey: 'PAGE.INFOGERANCE.FAQ.Q3.Q', answerKey: 'PAGE.INFOGERANCE.FAQ.Q3.A', open: false },
    { questionKey: 'PAGE.INFOGERANCE.FAQ.Q4.Q', answerKey: 'PAGE.INFOGERANCE.FAQ.Q4.A', open: false },
    { questionKey: 'PAGE.INFOGERANCE.FAQ.Q5.Q', answerKey: 'PAGE.INFOGERANCE.FAQ.Q5.A', open: false }
  ];

  ngOnInit() {
    this.title.setTitle(this.translate.instant('PAGE.INFOGERANCE.META.TITLE'));
    this.meta.updateTag({ name: 'description', content: this.translate.instant('PAGE.INFOGERANCE.META.DESCRIPTION') });
    this.meta.updateTag({ name: 'keywords', content: this.translate.instant('PAGE.INFOGERANCE.META.KEYWORDS') });
  }

  toggleFaq(index: number): void {
    this.faqItems[index].open = !this.faqItems[index].open;
  }
}
