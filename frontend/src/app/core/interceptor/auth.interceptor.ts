import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * HTTP interceptor for adding authentication tokens to outgoing requests.
 * Currently disabled for local development without backend.
 * When backend is running with Keycloak, should add Bearer token to Authorization header.
 * @param req - The outgoing HTTP request
 * @param next - The next handler in the interceptor chain
 * @returns Observable of the HTTP event
 */
export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  // Skip authentication for local development
  // Add token here when backend is running with Keycloak
  return next(req);
};
