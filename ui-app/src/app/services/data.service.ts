import { Injectable, signal } from '@angular/core';
import { Service } from '../models/service.model';
import { Testimonial } from '../models/testimonial.model';
import { ProcessStep } from '../models/process-step.model';

@Injectable({ providedIn: 'root' })
export class DataService {
  readonly maintenanceServices = signal<Service[]>([
    {
      id: 1,
      title: 'DATA.MAINTENANCE.1.TITLE',
      description: 'DATA.MAINTENANCE.1.DESCRIPTION',
      icon: '🔍',
      features: [
        'DATA.MAINTENANCE.1.F1',
        'DATA.MAINTENANCE.1.F2',
        'DATA.MAINTENANCE.1.F3',
        'DATA.MAINTENANCE.1.F4',
        'DATA.MAINTENANCE.1.F5'
      ]
    },
    {
      id: 2,
      title: 'DATA.MAINTENANCE.2.TITLE',
      description: 'DATA.MAINTENANCE.2.DESCRIPTION',
      icon: '🗺️',
      features: [
        'DATA.MAINTENANCE.2.F1',
        'DATA.MAINTENANCE.2.F2',
        'DATA.MAINTENANCE.2.F3',
        'DATA.MAINTENANCE.2.F4',
        'DATA.MAINTENANCE.2.F5'
      ]
    },
    {
      id: 3,
      title: 'DATA.MAINTENANCE.3.TITLE',
      description: 'DATA.MAINTENANCE.3.DESCRIPTION',
      icon: '🧠',
      features: [
        'DATA.MAINTENANCE.3.F1',
        'DATA.MAINTENANCE.3.F2',
        'DATA.MAINTENANCE.3.F3',
        'DATA.MAINTENANCE.3.F4',
        'DATA.MAINTENANCE.3.F5'
      ]
    },
    {
      id: 4,
      title: 'DATA.MAINTENANCE.4.TITLE',
      description: 'DATA.MAINTENANCE.4.DESCRIPTION',
      icon: '📊',
      features: [
        'DATA.MAINTENANCE.4.F1',
        'DATA.MAINTENANCE.4.F2',
        'DATA.MAINTENANCE.4.F3',
        'DATA.MAINTENANCE.4.F4',
        'DATA.MAINTENANCE.4.F5'
      ]
    }
  ]);


  readonly maximoServices = signal<Service[]>([
    {
      id: 1,
      title: 'DATA.SERVICES.1.TITLE',
      description: 'DATA.SERVICES.1.DESCRIPTION',
      icon: '⚙️',
      features: [
        'DATA.SERVICES.1.F1',
        'DATA.SERVICES.1.F2',
        'DATA.SERVICES.1.F3',
        'DATA.SERVICES.1.F4',
        'DATA.SERVICES.1.F5'
      ]
    },
    {
      id: 2,
      title: 'DATA.SERVICES.2.TITLE',
      description: 'DATA.SERVICES.2.DESCRIPTION',
      icon: '🔗',
      features: [
        'DATA.SERVICES.2.F1',
        'DATA.SERVICES.2.F2',
        'DATA.SERVICES.2.F3',
        'DATA.SERVICES.2.F4',
        'DATA.SERVICES.2.F5'
      ]
    },
    {
      id: 3,
      title: 'DATA.SERVICES.3.TITLE',
      description: 'DATA.SERVICES.3.DESCRIPTION',
      icon: '💻',
      features: [
        'DATA.SERVICES.3.F1',
        'DATA.SERVICES.3.F2',
        'DATA.SERVICES.3.F3',
        'DATA.SERVICES.3.F4',
        'DATA.SERVICES.3.F5'
      ]
    },
    {
      id: 4,
      title: 'DATA.SERVICES.4.TITLE',
      description: 'DATA.SERVICES.4.DESCRIPTION',
      icon: '🛡️',
      features: [
        'DATA.SERVICES.4.F1',
        'DATA.SERVICES.4.F2',
        'DATA.SERVICES.4.F3',
        'DATA.SERVICES.4.F4',
        'DATA.SERVICES.4.F5'
      ]
    }
  ]);

  readonly testimonials = signal<Testimonial[]>([
    {
      id: 1,
      name: 'Marc Tremblay',
      role: 'DATA.TESTIMONIALS.1.ROLE',
      company: 'Hydro Solutions Québec',
      content: 'DATA.TESTIMONIALS.1.CONTENT',
      rating: 5
    },
    {
      id: 2,
      name: 'Sophie Bergeron',
      role: 'DATA.TESTIMONIALS.2.ROLE',
      company: 'Industries Noranda',
      content: 'DATA.TESTIMONIALS.2.CONTENT',
      rating: 5
    },
    {
      id: 3,
      name: 'Jean-François Côté',
      role: 'DATA.TESTIMONIALS.3.ROLE',
      company: 'TransPort Canada',
      content: 'DATA.TESTIMONIALS.3.CONTENT',
      rating: 5
    }
  ]);

  readonly processSteps = signal<ProcessStep[]>([
    {
      step: 1,
      title: 'DATA.PROCESS.1.TITLE',
      description: 'DATA.PROCESS.1.DESCRIPTION',
      icon: '🔍'
    },
    {
      step: 2,
      title: 'DATA.PROCESS.2.TITLE',
      description: 'DATA.PROCESS.2.DESCRIPTION',
      icon: '📋'
    },
    {
      step: 3,
      title: 'DATA.PROCESS.3.TITLE',
      description: 'DATA.PROCESS.3.DESCRIPTION',
      icon: '🚀'
    },
    {
      step: 4,
      title: 'DATA.PROCESS.4.TITLE',
      description: 'DATA.PROCESS.4.DESCRIPTION',
      icon: '🤝'
    }
  ]);
}
