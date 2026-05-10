import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { JobSummary } from '../../models/job-summary.model';
import { CurrentLangEntryPipe } from '../../pipes/current-lang-entry.pipe';

@Component({
  selector: 'app-job-card',
  imports: [RouterLink, TranslatePipe, CurrentLangEntryPipe],
  templateUrl: './job-card.html',
  styleUrl: './job-card.scss'
})
export class JobCardComponent {
  readonly jobSummary = input.required<JobSummary>();
  readonly apply = output<string>();

  available(displayUntil: Date): boolean {
    return displayUntil > new Date();
  }

  jobTypeKey(): string {
    return `SHARED.DATA.JOBS.${this.jobSummary().type.toUpperCase()}`;
  }

  onApply(): void {
    this.apply.emit(this.jobSummary().slug);
  }
}
