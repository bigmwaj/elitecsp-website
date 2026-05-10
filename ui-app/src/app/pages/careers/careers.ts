import { Component, ElementRef, OnInit, ViewChild, inject, signal } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { HeroComponent } from '../../components/hero/hero';
import { ApplicationFormComponent } from '../../components/application-form/application-form';
import { JobCardComponent } from '../../components/job-card/job-card';
import { JobSummary } from '../../models/job-summary.model';
import { JobService } from '../../services/job.service';

@Component({
  selector: 'app-careers',
  imports: [HeroComponent, JobCardComponent, ApplicationFormComponent, TranslatePipe],
  templateUrl: './careers.html',
  styleUrl: './careers.scss'
})
export class CareersComponent implements OnInit {
  @ViewChild('applySection') applySection!: ElementRef<HTMLElement>;

  private titleService = inject(Title);
  private meta = inject(Meta);
  private translate = inject(TranslateService);
  readonly jobService = inject(JobService);

  readonly jobSummaries = signal<JobSummary[]>([]);
  readonly selectedJobSlug = signal<string | null>(null);
  protected readonly subscriptions$: Subscription[] = [];

  ngOnInit(): void {
    this.titleService.setTitle(this.translate.instant('PAGE.CAREERS.META.TITLE'));
    this.meta.updateTag({ name: 'description', content: this.translate.instant('PAGE.CAREERS.META.DESCRIPTION') });

    const jobSub = this.jobService.loadJobs().subscribe({
      next: summaries => this.jobSummaries.set(summaries),
      error: err => console.error('Failed to load job summaries:', err)
    });

    this.subscriptions$.push(jobSub);
  }

  onApply(jobSlug: string): void {
    this.selectedJobSlug.set(jobSlug);
    setTimeout(() => {
      this.applySection?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 50);
  }
}
