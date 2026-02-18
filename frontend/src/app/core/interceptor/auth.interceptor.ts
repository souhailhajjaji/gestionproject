import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { inject } from '@angular/core';
import { KeycloakService } from '../service/keycloak.service';

/**
 * HTTP interceptor for adding authentication tokens to outgoing requests.
 * Adds Bearer token from Keycloak to Authorization header for API requests.
 * @param req - The outgoing HTTP request
 * @param next - The next handler in the interceptor chain
 * @returns Observable of the HTTP event
 */
export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const keycloakService = inject(KeycloakService);
  
  // Skip if not authenticated or if request is not for our API
  if (!keycloakService.isLoggedIn() || !req.url.startsWith('/api')) {
    return next(req);
  }
  
  // Get token and add Authorization header
  const token = keycloakService.getToken();
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  
  return next(req);
};
