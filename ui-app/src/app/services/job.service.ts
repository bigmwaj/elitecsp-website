import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { JobDetailsData } from '../data/job-details.data';
import { JobData } from '../data/job-summaries.data';
import { JobDetails } from '../models/job-details.model';
import { JobSummary } from '../models/job-summary.model';

export type JobPosting = JobSummary & {
  details: JobDetails | null;
};

@Injectable({ providedIn: 'root' })
export class JobService {
  loadJobs(): Observable<JobSummary[]> {
    return of(this.getJobSummaries());
  }

  getJobBySlug(slug: string): JobPosting | null {
    const summary = this.getJobSummaries().find(job => job.slug === slug);
    if (!summary) {
      return null;
    }

    return {
      ...summary,
      details: this.getJobDetails(summary.jobId)
    };
  }

  private getJobSummaries(): JobSummary[] {
    return JobData.JOB_SUMMARIES;
  }

  private getJobDetails(jobId: number): JobDetails | null {
    return JobDetailsData.JOB_DETAILS.find(job => job.jobId === jobId) ?? null;
  }
}
