import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { Partner } from '../../models/partner.model';

@Component({
  selector: 'app-partner-card',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './partner-card.html',
  styleUrl: './partner-card.scss'
})
export class PartnerCardComponent {
  partner = input.required<Partner>();
}
