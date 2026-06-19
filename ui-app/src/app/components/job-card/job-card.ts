import { Component, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { JobSummary } from '../../models/job-summary.model';
import { CurrentLangEntryPipe } from '../../pipes/current-lang-entry.pipe';

@Component({
  selector: 'app-job-card',
  imports: [RouterLink, TranslatePipe, CurrentLangEntryPipe],
  templateUrl: './job-card.html',
  styleUrl: './job-card.scss'
})
export class JobCardComponent {
  private translate = inject(TranslateService);

  readonly jobSummary = input.required<JobSummary>();
  readonly apply = output<string>();

  available(displayUntil: Date): boolean {
    return displayUntil > new Date();
  }

  jobTypeKey(): string {
    return `SHARED.DATA.JOBS.${this.jobSummary().type.toUpperCase()}`;
  }

  localizedSlug(): string {
    const currentLang = this.translate.currentLang?.startsWith('fr') ? 'fr' : 'en';
    return this.jobSummary().slug[currentLang];
  }

  onApply(): void {
    this.apply.emit(this.localizedSlug());
  }
}
