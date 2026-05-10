import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withViewTransitions } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { routes } from './app.routes';
import { apiKeyInterceptor } from './interceptors/api-key.interceptor';
import { assetPath } from './utils/asset-path.util';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withViewTransitions()),
    provideHttpClient(withInterceptors([apiKeyInterceptor])),
    provideTranslateService({ fallbackLang: 'fr' }),
    provideTranslateHttpLoader({ prefix: assetPath('i18n/'), suffix: '.json' })
  ]
};
