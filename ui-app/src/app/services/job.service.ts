import { Injectable, signal } from '@angular/core';
import { Job } from '../models/job.model';

@Injectable({ providedIn: 'root' })
export class JobService {
  readonly jobs = signal<Job[]>([
    {
      id: 1,
      titleKey: 'DATA.JOBS.1.TITLE',
      descriptionKey: 'DATA.JOBS.1.DESCRIPTION',
      locationKey: 'DATA.JOBS.1.LOCATION',
      typeKey: 'DATA.JOBS.1.TYPE',
      icon: '⚙️'
    },
    {
      id: 2,
      titleKey: 'DATA.JOBS.2.TITLE',
      descriptionKey: 'DATA.JOBS.2.DESCRIPTION',
      locationKey: 'DATA.JOBS.2.LOCATION',
      typeKey: 'DATA.JOBS.2.TYPE',
      icon: '☕'
    },
    {
      id: 3,
      titleKey: 'DATA.JOBS.3.TITLE',
      descriptionKey: 'DATA.JOBS.3.DESCRIPTION',
      locationKey: 'DATA.JOBS.3.LOCATION',
      typeKey: 'DATA.JOBS.3.TYPE',
      icon: '📊'
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
}
