import { Component, input } from '@angular/core';
import { Service } from '../../models/service.model';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-service-card',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './service-card.html',
  styleUrl: './service-card.scss'
})
export class ServiceCardComponent {
  service = input.required<Service>();
}
