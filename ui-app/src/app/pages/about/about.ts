import { Component, inject, OnInit } from '@angular/core';
import { Title, Meta } from '@angular/platform-browser';
import { TranslatePipe } from '@ngx-translate/core';
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

  ngOnInit() {
    this.title.setTitle('À propos — Elite CSP');
    this.meta.updateTag({ name: 'description', content: 'Découvrez Elite CSP — une firme canadienne de conseil TI spécialisée en IBM Maximo et solutions de gestion des actifs d\'entreprise.' });
  }
}
