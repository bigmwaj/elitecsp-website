import { Component, input, output } from '@angular/core';
import { Job } from '../../models/job.model';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-job-card',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './job-card.html',
  styleUrl: './job-card.scss'
})
export class JobCardComponent {
  job = input.required<Job>();
  apply = output<number>();

  onApply(): void {
    this.apply.emit(this.job().id);
  }
}
