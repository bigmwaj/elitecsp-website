import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Title, Meta } from '@angular/platform-browser';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { HeroComponent } from '../../components/hero/hero';
import { CtaSectionComponent } from '../../components/cta-section/cta-section';

@Component({
  selector: 'app-infogerance',
  standalone: true,
  imports: [RouterLink, HeroComponent, CtaSectionComponent, TranslatePipe],
  templateUrl: './infogerance.html',
  styleUrl: './infogerance.scss'
})
export class InfogeranceComponent implements OnInit {
  private title = inject(Title);
  private meta = inject(Meta);
  private translate = inject(TranslateService);

  readonly stats = [
    { value: '50+', labelKey: 'PAGE.INFOGERANCE.STATS.ASSETS' },
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
