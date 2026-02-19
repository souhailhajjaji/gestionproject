import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KeycloakService } from '../service/keycloak.service';

/**
 * Auth guard to protect routes that require authentication.
 * Redirects to Keycloak login if user is not authenticated.
 * @returns true to allow route activation, false to block
 */
export const authGuard: CanActivateFn = (route, state) => {
  const keycloakService = inject(KeycloakService);
  const router = inject(Router);

  if (keycloakService.isLoggedIn()) {
    return true;
  }

  // Check if we're processing a Keycloak callback (contains code or state in URL)
  // In this case, we should not redirect to avoid infinite loops
  const url = window.location.href;
  const isKeycloakCallback = url.includes('code=') || url.includes('state=');
  
  if (isKeycloakCallback) {
    // Allow navigation to proceed - Keycloak will handle the callback
    // The init() method in AppComponent will process authentication
    return true;
  }

  // Store the attempted URL for redirecting after login
  const returnUrl = state.url;
  
  // Redirect to login page
  keycloakService.login();
  return false;
};

/**
 * Admin guard to protect routes that require ADMIN role.
 * Redirects to dashboard if user is not an admin.
 * @returns true to allow route activation, false to block
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const keycloakService = inject(KeycloakService);
  const router = inject(Router);

  if (keycloakService.isLoggedIn() && keycloakService.isAdmin()) {
    return true;
  }

  // Redirect to dashboard if not admin
  router.navigate(['/dashboard']);
  return false;
};
