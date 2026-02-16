import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KeycloakService } from '../service/keycloak.service';

/**
 * Auth guard to protect routes that require authentication.
 * Currently returns true for local development without backend.
 * When backend is running, should check Keycloak authentication status.
 * @returns true to allow route activation
 */
export const authGuard: CanActivateFn = (route, state) => {
  // Temporarily disabled for local development without backend
  return true;
};

/**
 * Admin guard to protect routes that require ADMIN role.
 * Currently returns true for local development without backend.
 * When backend is running, should check for ADMIN role in Keycloak.
 * @returns true to allow route activation
 */
export const adminGuard: CanActivateFn = (route, state) => {
  // Temporarily disabled for local development without backend
  return true;
};
