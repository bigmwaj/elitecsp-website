import { Component, inject, OnInit } from '@angular/core';
import { Title, Meta } from '@angular/platform-browser';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { HeroComponent } from '../../components/hero/hero';
import { CtaSectionComponent } from '../../components/cta-section/cta-section';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [HeroComponent, CtaSectionComponent, TranslatePipe],
  templateUrl: './about.html',
  styleUrl: './about.scss'
})
export class AboutComponent implements OnInit {
  private title = inject(Title);
  private meta = inject(Meta);  
  private translate = inject(TranslateService);

  ngOnInit() {
    this.title.setTitle(this.translate.instant('PAGE.ABOUT.META.TITLE'));
    this.meta.updateTag({ name: 'description', content: this.translate.instant('PAGE.ABOUT.META.DESCRIPTION') });
  }
}
