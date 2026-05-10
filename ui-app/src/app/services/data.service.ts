import { Injectable, signal } from '@angular/core';
import { Service } from '../models/service.model';
import { Testimonial } from '../models/testimonial.model';
import { ProcessStep } from '../models/process-step.model';

@Injectable({ providedIn: 'root' })
export class DataService {
  readonly maintenanceServices = signal<Service[]>([
    {
      id: 1,
      title: 'SHARED.DATA.MAINTENANCE.1.TITLE',
      description: 'SHARED.DATA.MAINTENANCE.1.DESCRIPTION',
      icon: '🔍',
      features: [
        'SHARED.DATA.MAINTENANCE.1.F1',
        'SHARED.DATA.MAINTENANCE.1.F2',
        'SHARED.DATA.MAINTENANCE.1.F3',
        'SHARED.DATA.MAINTENANCE.1.F4',
        'SHARED.DATA.MAINTENANCE.1.F5'
      ]
    },
    {
      id: 2,
      title: 'SHARED.DATA.MAINTENANCE.2.TITLE',
      description: 'SHARED.DATA.MAINTENANCE.2.DESCRIPTION',
      icon: '🗺️',
      features: [
        'SHARED.DATA.MAINTENANCE.2.F1',
        'SHARED.DATA.MAINTENANCE.2.F2',
        'SHARED.DATA.MAINTENANCE.2.F3',
        'SHARED.DATA.MAINTENANCE.2.F4',
        'SHARED.DATA.MAINTENANCE.2.F5'
      ]
    },
    {
      id: 3,
      title: 'SHARED.DATA.MAINTENANCE.3.TITLE',
      description: 'SHARED.DATA.MAINTENANCE.3.DESCRIPTION',
      icon: '🧠',
      features: [
        'SHARED.DATA.MAINTENANCE.3.F1',
        'SHARED.DATA.MAINTENANCE.3.F2',
        'SHARED.DATA.MAINTENANCE.3.F3',
        'SHARED.DATA.MAINTENANCE.3.F4',
        'SHARED.DATA.MAINTENANCE.3.F5'
      ]
    },
    {
      id: 4,
      title: 'SHARED.DATA.MAINTENANCE.4.TITLE',
      description: 'SHARED.DATA.MAINTENANCE.4.DESCRIPTION',
      icon: '📊',
      features: [
        'SHARED.DATA.MAINTENANCE.4.F1',
        'SHARED.DATA.MAINTENANCE.4.F2',
        'SHARED.DATA.MAINTENANCE.4.F3',
        'SHARED.DATA.MAINTENANCE.4.F4',
        'SHARED.DATA.MAINTENANCE.4.F5'
      ]
    }
  ]);


  readonly maximoServices = signal<Service[]>([
    {
      id: 1,
      title: 'SHARED.DATA.SERVICES.1.TITLE',
      description: 'SHARED.DATA.SERVICES.1.DESCRIPTION',
      icon: '⚙️',
      features: [
        'SHARED.DATA.SERVICES.1.F1',
        'SHARED.DATA.SERVICES.1.F2',
        'SHARED.DATA.SERVICES.1.F3',
        'SHARED.DATA.SERVICES.1.F4',
        'SHARED.DATA.SERVICES.1.F5'
      ]
    },
    {
      id: 2,
      title: 'SHARED.DATA.SERVICES.2.TITLE',
      description: 'SHARED.DATA.SERVICES.2.DESCRIPTION',
      icon: '🔗',
      features: [
        'SHARED.DATA.SERVICES.2.F1',
        'SHARED.DATA.SERVICES.2.F2',
        'SHARED.DATA.SERVICES.2.F3',
        'SHARED.DATA.SERVICES.2.F4',
        'SHARED.DATA.SERVICES.2.F5'
      ]
    },
    {
      id: 3,
      title: 'SHARED.DATA.SERVICES.3.TITLE',
      description: 'SHARED.DATA.SERVICES.3.DESCRIPTION',
      icon: '💻',
      features: [
        'SHARED.DATA.SERVICES.3.F1',
        'SHARED.DATA.SERVICES.3.F2',
        'SHARED.DATA.SERVICES.3.F3',
        'SHARED.DATA.SERVICES.3.F4',
        'SHARED.DATA.SERVICES.3.F5'
      ]
    },
    {
      id: 4,
      title: 'SHARED.DATA.SERVICES.4.TITLE',
      description: 'SHARED.DATA.SERVICES.4.DESCRIPTION',
      icon: '🛡️',
      features: [
        'SHARED.DATA.SERVICES.4.F1',
        'SHARED.DATA.SERVICES.4.F2',
        'SHARED.DATA.SERVICES.4.F3',
        'SHARED.DATA.SERVICES.4.F4',
        'SHARED.DATA.SERVICES.4.F5'
      ]
    }
  ]);

  readonly testimonials = signal<Testimonial[]>([
    {
      id: 1,
      name: 'Marc Tremblay',
      role: 'SHARED.DATA.TESTIMONIALS.1.ROLE',
      company: 'Hydro Solutions Québec',
      content: 'SHARED.DATA.TESTIMONIALS.1.CONTENT',
      rating: 5
    },
    {
      id: 2,
      name: 'Sophie Bergeron',
      role: 'SHARED.DATA.TESTIMONIALS.2.ROLE',
      company: 'Industries Noranda',
      content: 'SHARED.DATA.TESTIMONIALS.2.CONTENT',
      rating: 5
    },
    {
      id: 3,
      name: 'Jean-François Côté',
      role: 'SHARED.DATA.TESTIMONIALS.3.ROLE',
      company: 'TransPort Canada',
      content: 'SHARED.DATA.TESTIMONIALS.3.CONTENT',
      rating: 5
    }
  ]);

  readonly processSteps = signal<ProcessStep[]>([
    {
      step: 1,
      title: 'SHARED.DATA.PROCESS.1.TITLE',
      description: 'SHARED.DATA.PROCESS.1.DESCRIPTION',
      icon: '🔍'
    },
    {
      step: 2,
      title: 'SHARED.DATA.PROCESS.2.TITLE',
      description: 'SHARED.DATA.PROCESS.2.DESCRIPTION',
      icon: '📋'
    },
    {
      step: 3,
      title: 'SHARED.DATA.PROCESS.3.TITLE',
      description: 'SHARED.DATA.PROCESS.3.DESCRIPTION',
      icon: '🚀'
    },
    {
      step: 4,
      title: 'SHARED.DATA.PROCESS.4.TITLE',
      description: 'SHARED.DATA.PROCESS.4.DESCRIPTION',
      icon: '🤝'
    }
  ]);
}
