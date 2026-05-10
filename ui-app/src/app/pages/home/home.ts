import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Title, Meta } from '@angular/platform-browser';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { DataService } from '../../services/data.service';
import { PartnersService } from '../../services/partners.service';
import { ServiceCardComponent } from '../../components/service-card/service-card';
import { PartnerCardComponent } from '../../components/partner-card/partner-card';
import { HeroComponent } from '../../components/hero/hero';
import { CtaSectionComponent } from '../../components/cta-section/cta-section';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, ServiceCardComponent, PartnerCardComponent, HeroComponent, CtaSectionComponent, TranslatePipe],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent implements OnInit {
  private title = inject(Title);
  private meta = inject(Meta);
  private dataService = inject(DataService);
  private partnersService = inject(PartnersService);
  private translateService = inject(TranslateService);
  
  maintenanceServices = this.dataService.maintenanceServices;
  maximoServices = this.dataService.maximoServices;
  testimonials = this.dataService.testimonials;
  processSteps = this.dataService.processSteps;
  partners = this.partnersService.partners;

  ngOnInit() {
    this.title.setTitle(this.translateService.instant('HOME.META.TITLE'));
    this.meta.updateTag({ name: 'description', content: this.translateService.instant('HOME.META.DESCRIPTION') });
  }
}
