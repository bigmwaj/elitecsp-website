import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { JobDetailData } from '../data/job-detail.data';
import { JobSummaryData } from '../data/job-summary.data';
import { JobDetail } from '../models/job-detail.model';
import { JobSummary } from '../models/job-summary.model';

export type JobPosting = JobSummary & {
  details: JobDetail | null;
};

@Injectable({ providedIn: 'root' })
export class JobService {
  loadJobs(): Observable<JobSummary[]> {
    return of(this.getJobSummaries());
  }

  getJobBySlug(slug: string): JobPosting | null {
    const summary = this.getJobSummaries().find(job => job.slug.fr === slug || job.slug.en === slug);
    if (!summary) {
      return null;
    }

    return {
      ...summary,
      details: this.getJobDetails(summary.jobId)
    };
  }

  private getJobSummaries(): JobSummary[] {
    return JobSummaryData.JOB_SUMMARIES.sort((a, b) => (a.sortIndex ?? 0) - (b.sortIndex ?? 0)) || [];
  }

  private getJobDetails(jobId: number): JobDetail | null {
    return JobDetailData.JOB_DETAILS.find(job => job.jobId === jobId) ?? null;
  }
}
