import { inject, Injectable, signal } from '@angular/core';
import { Job } from '../models/job.model';
import { TranslateService } from '@ngx-translate/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { JobSummary } from '../models/job-summary.model';
import { environment } from '../../environments/environment';
import { LambdaBodyResponse } from '../models/api-response.model';
import { Observable, catchError, map, throwError } from 'rxjs';

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
    return this.http.get<unknown>(`${environment.apiUrl}/jobs`)
      .pipe(
        map((response) => this.deserializeJobSummaries(response)),
        catchError((err: unknown) => {
          let errorMsg = 'Failed to load jobs';

          if (err instanceof HttpErrorResponse) {
            errorMsg = `HTTP ${err.status}: ${err.statusText || 'Unknown error'}`;
            const apiError = this.extractApiErrorMessage(err.error);
            if (apiError) {
              errorMsg += ` - ${apiError}`;
            }
          } else if (err instanceof Error) {
            errorMsg = err.message;
          }

          return throwError(() => new Error(errorMsg));
        }));
  }

  private deserializeJobSummaries(response: unknown): JobSummary[] {
    const bodyResponse = this.extractLambdaBodyResponse(response);
    if (!bodyResponse.success) {
      throw new Error(bodyResponse.error || bodyResponse.message || 'Unknown API error');
    }

    const parsedMessage = this.parseJson<unknown>(bodyResponse.message, 'response message');
    if (!Array.isArray(parsedMessage)) {
      throw new Error('Invalid job payload: expected an array');
    }

    return parsedMessage.map((job, index) => this.toJobSummary(job, index));
  }

  private extractLambdaBodyResponse(response: unknown): LambdaBodyResponse<string> {
    if (
      this.isObject(response)
      && 'success' in response
      && 'message' in response
      && typeof response['success'] === 'boolean'
      && typeof response['message'] === 'string'
    ) {
      const error = response['error'];
      return {
        success: response['success'],
        message: response['message'],
        error: typeof error === 'string' || error === null ? error : undefined
      };
    }

    if (this.isObject(response) && 'body' in response && typeof response['body'] === 'string') {
      return this.parseJson<LambdaBodyResponse<string>>(response['body'], 'response body');
    }

    throw new Error('Invalid API response format');
  }

  private toJobSummary(value: unknown, index: number): JobSummary {
    if (!this.isObject(value)) {
      throw new Error(`Invalid job item at index ${index}`);
    }

    const postedDate = this.parseDate(value['postedDate'], `postedDate at index ${index}`);
    if (!postedDate) {
      throw new Error(`Missing postedDate at index ${index}`);
    }

    return {
      jobId: String(value['jobId'] ?? ''),
      icon: String(value['icon'] ?? ''),
      type: String(value['type'] ?? ''),
      category: String(value['category'] ?? ''),
      title: String(value['title'] ?? ''),
      location: String(value['location'] ?? ''),
      summary: String(value['summary'] ?? ''),
      postedDate,
      expirationDate: this.parseDate(value['expirationDate'], `expirationDate at index ${index}`)
    };
  }

  private parseDate(value: unknown, label: string): Date | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }

    if (value instanceof Date && !Number.isNaN(value.getTime())) {
      return value;
    }

    if (typeof value === 'string') {
      const parsedDate = new Date(value);
      if (!Number.isNaN(parsedDate.getTime())) {
        return parsedDate;
      }
    }

    throw new Error(`Invalid ${label}`);
  }

  private parseJson<T>(value: string, label: string): T {
    try {
      return JSON.parse(value) as T;
    } catch {
      throw new Error(`Failed to parse ${label}`);
    }
  }

  private extractApiErrorMessage(errorPayload: unknown): string | null {
    try {
      const bodyResponse = this.extractLambdaBodyResponse(errorPayload);
      return bodyResponse.error || bodyResponse.message || '';
    } catch {
      if (this.isObject(errorPayload) && typeof errorPayload['message'] === 'string') {
        return errorPayload['message'];
      }
      return null;
    }
  }

  private isObject(value: unknown): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null;
  }
}
