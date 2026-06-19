import { Component, inject, OnInit, signal } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ApplicationFormComponent } from '../../components/application-form/application-form';
import { JobDetail } from '../../models/job-detail.model';
import { JobService, JobPosting } from '../../services/job.service';

@Component({
  selector: 'app-job-detail',
  imports: [RouterLink, TranslatePipe, ApplicationFormComponent],
  templateUrl: './job-detail.html',
  styleUrl: './job-detail.scss'
})
export class JobDetailComponent implements OnInit {
  private titleService = inject(Title);
  private meta = inject(Meta);
  private route = inject(ActivatedRoute);
  private translate = inject(TranslateService);
  private jobService = inject(JobService);

  readonly job = signal<JobPosting | null>(null);
  readonly routeSlug = signal<string>('');

  ngOnInit() {
    const slug = this.route.snapshot.paramMap.get('slug') ?? '';
    this.routeSlug.set(slug);
    const job = this.jobService.getJobBySlug(slug);
    this.job.set(job);

    if (job) {
      const pageTitle = this.getLocalizedText(job.title);
      this.titleService.setTitle(`${this.translate.instant('PAGE.JOB_DETAILS.META.TITLE', { jobTitle: pageTitle })}`);
      this.meta.updateTag({
        name: 'description',
        content: this.translate.instant('PAGE.JOB_DETAILS.META.DESCRIPTION', {
          jobTitle: pageTitle,
          jobSummary: this.getLocalizedText(job.summary)
        })
      });
      return;
    }
    this.titleService.setTitle(this.translate.instant('PAGE.JOB_DETAILS.META.TITLE', { jobTitle: '' }));
    this.meta.updateTag({ name: 'description', content: this.translate.instant('PAGE.JOB_DETAILS.META.DEFAULT_DESCRIPTION', { jobTitle: '' }) });
  }

  jobTypeKey(type: string): string {
    return `SHARED.DATA.JOBS.${type.toUpperCase()}`;
  }

  getLocalizedText(entry?: { fr: string; en: string } | null): string {
    if (!entry) {
      return '';
    }

    const currentLang = this.translate.currentLang?.startsWith('fr') ? 'fr' : 'en';
    return entry[currentLang] ?? entry.en;
  }

  getLocalizedList(entry?: { fr: string[]; en: string[] } | null): string[] {
    if (!entry) {
      return [];
    }

    const currentLang = this.translate.currentLang?.startsWith('fr') ? 'fr' : 'en';
    return entry[currentLang] ?? entry.en;
  }

  hasDetailContent(details: JobDetail | null): boolean {
    if (!details) {
      return false;
    }

    return Boolean(
      details.description ||
        this.getLocalizedList(details.responsibilities).length ||
        this.getLocalizedList(details.requirements).length ||
        this.getLocalizedList(details.benefits).length
    );
  }
}
