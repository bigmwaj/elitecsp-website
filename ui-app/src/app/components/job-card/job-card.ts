import { Component, input, output } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { JobSummary } from '../../models/job-summary.model';

@Component({
  selector: 'app-job-card',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './job-card.html',
  styleUrl: './job-card.scss'
})
export class JobCardComponent {
  jobSummary = input.required<JobSummary>();
  apply = output<string>();

  available(displayUntil: Date): boolean {
    const now = new Date();
    return displayUntil > now;
  }

  onApply(): void {
    this.apply.emit(this.jobSummary().jobId);
  }
}
