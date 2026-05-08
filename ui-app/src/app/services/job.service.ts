import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { JobSummary } from '../models/job-summary.model';
import { environment } from '../../environments/environment';
import { LambdaBodyResponse } from '../models/api-response.model';
import { Observable, catchError, map, throwError } from 'rxjs';
import { Utils } from '../utils';
import { TranslateService } from '@ngx-translate/core';

@Injectable({ providedIn: 'root' })
export class JobService {

  private http = inject(HttpClient);
  private translationService = inject(TranslateService);

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

    const parsedMessage = Utils.parseJson<unknown>(bodyResponse.message, 'response message');
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
        error: typeof error === 'string' || error === null ? error : null
      };
    }

    if (this.isObject(response) && 'body' in response && typeof response['body'] === 'string') {
      return Utils.parseJson<LambdaBodyResponse<string>>(response['body'], 'response body');
    }

    throw new Error('Invalid API response format');
  }

  private toJobSummary(value: unknown, index: number): JobSummary {
    if (!this.isObject(value)) {
      throw new Error(`Invalid job item at index ${index}`);
    }

    let type = String(value['type'] ?? '').toUpperCase();

    return {
      jobId: String(value['jobId'] ?? ''),
      icon: String(value['icon'] ?? ''),
      type: this.translationService.instant(`DATA.JOBS.${type}`),
      category: String(value['category'] ?? ''),
      title: String(value['title'] ?? ''),
      location: String(value['location'] ?? ''),
      summary: String(value['summary'] ?? ''),
      postedDate: Utils.parseDate(value['postedDate'], `postedDate at index ${index}`),
      expirationDate: Utils.parseDate(value['expirationDate'], `expirationDate at index ${index}`)
    };
  }

  private extractApiErrorMessage(errorPayload: unknown): string | null {
    try {
      const bodyResponse = this.extractLambdaBodyResponse(errorPayload);
      return bodyResponse.error || bodyResponse.message || null;
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
