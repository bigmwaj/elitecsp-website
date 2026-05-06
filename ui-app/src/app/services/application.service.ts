import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApplicationPayload } from '../models/payload.model';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private http = inject(HttpClient);

  submit(payload: ApplicationPayload): Observable<void> {
    const body = {
      ...payload,
      type: payload.type ?? "JOB_APPLICATION"
    };

    const apiEvent = {
      body: JSON.stringify(body),
      isBase64Encoded: false
    };
    
    return this.http.post<void>(`${environment.apiUrl}/contacts`, apiEvent,   {
        headers: {
          'x-api-key': environment.apiKey
        }
      });
  }
}

