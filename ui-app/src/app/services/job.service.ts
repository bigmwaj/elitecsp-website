import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { JobData } from '../job-summaries.data';
import { JobSummary } from '../models/job-summary.model';

@Injectable({ providedIn: 'root' })
export class JobService {

  loadJobs(): Observable<JobSummary[]> {
    return of(this.getJobSummaries());
  }

  private getJobSummaries(): JobSummary[] {
    return JobData.JOB_SUMMARIES;
  }

}
