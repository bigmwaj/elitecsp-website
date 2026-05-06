import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApplicationPayload } from '../models/payload.model';
import { ApiResponse } from '../models/api-response.model';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private http = inject(HttpClient);

  submit(payload: ApplicationPayload): Observable<ApiResponse> {
    const body = {
      ...payload,
      type: payload.type ?? 'JOB_APPLICATION'
    };

    return this.http.post<ApiResponse>(`${environment.apiUrl}/contacts`, body);
  }
}

