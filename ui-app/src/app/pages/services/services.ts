import { Component, inject, OnInit } from '@angular/core';
import { Title, Meta } from '@angular/platform-browser';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { DataService } from '../../services/data.service';
import { HeroComponent } from '../../components/hero/hero';
import { ServiceCardComponent } from '../../components/service-card/service-card';
import { CtaSectionComponent } from '../../components/cta-section/cta-section';

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [HeroComponent, ServiceCardComponent, CtaSectionComponent, TranslatePipe],
  templateUrl: './services.html',
  styleUrl: './services.scss'
})
export class ServicesComponent implements OnInit {
  private title = inject(Title);
  private meta = inject(Meta);
  private dataService = inject(DataService);
  private translate = inject(TranslateService);

  maintenanceServices = this.dataService.maintenanceServices;
  maximoServices = this.dataService.maximoServices;

  ngOnInit() {
    this.title.setTitle(this.translate.instant('PAGE.SERVICES.META.TITLE'));
    this.meta.updateTag({ name: 'description', content: this.translate.instant('PAGE.SERVICES.META.DESCRIPTION') });
  }
}
