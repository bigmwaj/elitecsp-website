import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { Title, Meta } from '@angular/platform-browser';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { PartnersService } from '../../services/partners.service';
import { PartnerCardComponent } from '../../components/partner-card/partner-card';
import { HeroComponent } from '../../components/hero/hero';

@Component({
  selector: 'app-partners',
  standalone: true,
  imports: [PartnerCardComponent, HeroComponent, TranslatePipe],
  templateUrl: './partners.html',
  styleUrl: './partners.scss'
})
export class PartnersComponent implements OnInit, OnDestroy {
  private titleService = inject(Title);
  private meta = inject(Meta);
  private translateService = inject(TranslateService);
  private partnersService = inject(PartnersService);
  private langSub?: Subscription;

  partners = this.partnersService.partners;

  ngOnInit() {
    this.updateMeta();
    this.langSub = this.translateService.onLangChange.subscribe(() => this.updateMeta());
  }

  ngOnDestroy() {
    this.langSub?.unsubscribe();
  }

  private updateMeta(): void {
    this.translateService.get(['HERO.PARTNERS.TITLE', 'HERO.PARTNERS.SUBTITLE']).subscribe(t => {
      this.titleService.setTitle(`${t['HERO.PARTNERS.TITLE']} — Elite CSP`);
      this.meta.updateTag({ name: 'description', content: t['HERO.PARTNERS.SUBTITLE'] });
    });
  }
}
