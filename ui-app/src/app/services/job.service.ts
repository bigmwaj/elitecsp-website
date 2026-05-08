import { inject, Injectable, signal } from '@angular/core';
import { Job } from '../models/job.model';
import { TranslateService } from '@ngx-translate/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { JobSummary } from '../models/job-summary.model';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Observable } from 'rxjs/internal/Observable';
import { map, catchError, throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class JobService {

  private translationService = inject(TranslateService);

  private http = inject(HttpClient);

  readonly jobs = signal<Job[]>([
    {
      id: 1,
      titleKey: 'DATA.JOBS.1.TITLE',
      descriptionKey: 'DATA.JOBS.1.DESCRIPTION',
      locationKey: 'DATA.JOBS.1.LOCATION',
      typeKey: 'DATA.JOBS.1.TYPE',
      icon: '⚙️',
      displayUntil: new Date('2024-12-31')
    },
    {
      id: 2,
      titleKey: 'DATA.JOBS.2.TITLE',
      descriptionKey: 'DATA.JOBS.2.DESCRIPTION',
      locationKey: 'DATA.JOBS.2.LOCATION',
      typeKey: 'DATA.JOBS.2.TYPE',
      icon: '☕',
      displayUntil: new Date('2024-12-31')
    },
    {
      id: 3,
      titleKey: 'DATA.JOBS.3.TITLE',
      descriptionKey: 'DATA.JOBS.3.DESCRIPTION',
      locationKey: 'DATA.JOBS.3.LOCATION',
      typeKey: 'DATA.JOBS.3.TYPE',
      icon: '📊',
      displayUntil: new Date('2026-12-31')
    },
    {
      id: 4,
      titleKey: 'DATA.JOBS.4.TITLE',
      descriptionKey: 'DATA.JOBS.4.DESCRIPTION',
      locationKey: 'DATA.JOBS.4.LOCATION',
      typeKey: 'DATA.JOBS.4.TYPE',
      icon: '🔧'
    }
  ]);

  get availableJobs(): Job[] {
    const now = new Date();
    return this.jobs().filter(job => !job.displayUntil || job.displayUntil > now);
  }

  getJobById(id: number): Job | undefined {
    return this.jobs().find(job => job.id === id);
  }

  getJobTranslatedTitle(id: number): string {
    const job = this.getJobById(id);
    return job ? this.translationService.instant(job.titleKey) : '';
  }

  loadJobs(): Observable<JobSummary[]> {
    return this.http.get<ApiResponse>(`${environment.apiUrl}/jobs`)
      .pipe(
        map((response: ApiResponse) => {
          if (!response.success) {
            const errMsg = response.message || response.error || 'Unknown API error';
            throw new Error(`API Error: ${errMsg}`);
          }
          try {
            return response.body ? JSON.parse(response.body) as JobSummary[] : [];
          } catch (e) {
            throw new Error(`Failed to parse job data: ${e instanceof Error ? e.message : 'Invalid JSON'}`);
          }
        }),
        catchError((err: any) => {
          let errorMsg = 'Failed to load jobs';
          if (err instanceof HttpErrorResponse) {
            errorMsg = `HTTP ${err.status}: ${err.statusText || 'Unknown error'}`;
            if (err.error?.message) {
              errorMsg += ` - ${err.error.message}`;
            }
          } else if (err instanceof Error) {
            errorMsg = err.message;
          }
          return throwError(() => new Error(errorMsg));
        }));
  }
}
