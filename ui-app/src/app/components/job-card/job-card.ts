import { Component, input, output } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { JobSummary } from '../../models/job-summary.model';
import { CurrentLangEntryPipe } from '../../pipes/current-lang-entry.pipe';

@Component({
  selector: 'app-job-card',
  standalone: true,
  imports: [TranslatePipe, CurrentLangEntryPipe],
  templateUrl: './job-card.html',
  styleUrl: './job-card.scss'
})
export class JobCardComponent {

  jobSummary = input.required<JobSummary>();
  apply = output<number>();

  available(displayUntil: Date): boolean {
    const now = new Date();
    return displayUntil > now;
  }

  onApply(): void {
    this.apply.emit(this.jobSummary().jobId);
  }
}
