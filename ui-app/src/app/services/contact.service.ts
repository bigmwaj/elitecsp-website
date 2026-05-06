import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ContactPayload } from '../models/payload.model';
import { ApiResponse } from '../models/api-response.model';

@Injectable({ providedIn: 'root' })
export class ContactService {
  private http = inject(HttpClient);

  submit(payload: ContactPayload): Observable<ApiResponse> {
    const body = {
      ...payload,
      type: payload.type ?? 'CONTACT'
    };
    
    const request = {
      body: JSON.stringify(body),
      isBase64Encoded: false
    };

    return this.http.post<ApiResponse>(`${environment.apiUrl}/contacts`, request);
  }
}
