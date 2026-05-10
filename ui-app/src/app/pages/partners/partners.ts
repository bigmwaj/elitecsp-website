import { Component, inject, OnInit } from '@angular/core';
import { Title, Meta } from '@angular/platform-browser';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
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
export class PartnersComponent implements OnInit {
  private titleService = inject(Title);
  private meta = inject(Meta);
  private translate = inject(TranslateService);
  private partnersService = inject(PartnersService);

  partners = this.partnersService.partners;

  ngOnInit() {
    this.titleService.setTitle(this.translate.instant('PARTNERS.META.TITLE'));
    this.meta.updateTag({ name: 'description', content: this.translate.instant('PARTNERS.META.DESCRIPTION') });
  }
}
