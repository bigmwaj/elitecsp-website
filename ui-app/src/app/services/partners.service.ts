import { Injectable, signal } from '@angular/core';
import { Partner } from '../models/partner.model';

@Injectable({ providedIn: 'root' })
export class PartnersService {
  readonly partners = signal<Partner[]>([
    {
      id: 1,
      key: 'ibm',
      logoUrl: 'assets/images/partners/ibm.png',
      websiteUrl: 'https://www.ibm.com/partnerplus/directory/company/9395'
    },
    {
      id: 2,
      key: 'pemac',
      logoUrl: 'assets/images/partners/pemac.png',
      websiteUrl: 'https://www.pemac.org'
    }
  ]);
}
