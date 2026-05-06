import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./pages/home/home').then(m => m.HomeComponent) },
  { path: 'services', loadComponent: () => import('./pages/services/services').then(m => m.ServicesComponent) },
  { path: 'about', loadComponent: () => import('./pages/about/about').then(m => m.AboutComponent) },
  { path: 'contact', loadComponent: () => import('./pages/contact/contact').then(m => m.ContactComponent) },
  { path: 'partners', loadComponent: () => import('./pages/partners/partners').then(m => m.PartnersComponent) },
  { path: 'careers', loadComponent: () => import('./pages/careers/careers').then(m => m.CareersComponent) },
  { path: '**', redirectTo: '' }
];
