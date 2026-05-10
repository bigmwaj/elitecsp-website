import { Injectable, signal } from '@angular/core';
import { Partner } from '../models/partner.model';
import { assetPath } from '../utils/asset-path.util';

@Injectable({ providedIn: 'root' })
export class PartnersService {
  readonly partners = signal<Partner[]>([
    {
      id: 1,
      key: 'ibm',
      logoUrl: assetPath('images/partners/ibm.png'),
      websiteUrl: 'https://www.ibm.com/partnerplus/directory/company/9395'
    },
    {
      id: 2,
      key: 'pemac',
      logoUrl: assetPath('images/partners/pemac.png'),
      websiteUrl: 'https://www.pemac.org'
    }
  ]);
}
