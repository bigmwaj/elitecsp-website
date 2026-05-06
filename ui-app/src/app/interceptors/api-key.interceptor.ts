import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';

export const apiKeyInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }

  const apiReq = req.clone({
    setHeaders: {
      'x-api-key': environment.apiKey
    }
  });

  return next(apiReq);
};
