import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./pages/home/home').then(m => m.HomeComponent) },
  { path: 'services', loadComponent: () => import('./pages/services/services').then(m => m.ServicesComponent) },
  { path: 'infogerance', loadComponent: () => import('./pages/infogerance/infogerance').then(m => m.InfogeranceComponent) },
  { path: 'infogérance', loadComponent: () => import('./pages/infogerance/infogerance').then(m => m.InfogeranceComponent) },
  { path: 'infogerance', loadComponent: () => import('./pages/infogerance/infogerance').then(m => m.InfogeranceComponent) },
  { path: 'about', loadComponent: () => import('./pages/about/about').then(m => m.AboutComponent) },
  { path: 'a-propos', loadComponent: () => import('./pages/about/about').then(m => m.AboutComponent) },
  { path: 'à-propos', loadComponent: () => import('./pages/about/about').then(m => m.AboutComponent) },
  { path: 'contact', loadComponent: () => import('./pages/contact/contact').then(m => m.ContactComponent) },
  { path: 'contact-us', loadComponent: () => import('./pages/contact/contact').then(m => m.ContactComponent) },
  { path: 'nous-contacter', loadComponent: () => import('./pages/contact/contact').then(m => m.ContactComponent) },
  { path: 'partners', loadComponent: () => import('./pages/partners/partners').then(m => m.PartnersComponent) },
  { path: 'partenaires', loadComponent: () => import('./pages/partners/partners').then(m => m.PartnersComponent) },
  { path: 'careers', loadComponent: () => import('./pages/careers/careers').then(m => m.CareersComponent) },
  { path: 'carrières', loadComponent: () => import('./pages/careers/careers').then(m => m.CareersComponent) },
  { path: 'carrieres', loadComponent: () => import('./pages/careers/careers').then(m => m.CareersComponent) },
  { path: 'careers/:slug', loadComponent: () => import('./pages/job-detail/job-detail').then(m => m.JobDetailComponent) },
  { path: 'carrières/:slug', loadComponent: () => import('./pages/job-detail/job-detail').then(m => m.JobDetailComponent) },
  { path: 'carrieres/:slug', loadComponent: () => import('./pages/job-detail/job-detail').then(m => m.JobDetailComponent) },
  { path: '**', redirectTo: '' }
];
