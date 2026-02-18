import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';

/**
 * Service for managing Keycloak authentication.
 * Handles user authentication, token management, and role checking.
 */
@Injectable({
  providedIn: 'root'
})
export class KeycloakService {
  private _keycloak: Keycloak | undefined;

  /**
   * Gets or creates the Keycloak instance.
   * @returns The Keycloak instance
   */
  get keycloak() {
    if (!this._keycloak) {
      this._keycloak = new Keycloak({
        url: this.getKeycloakUrl(),
        realm: 'gestion-projet',
        clientId: 'frontend-app'
      });
    }
    return this._keycloak;
  }

  /**
   * Gets the Keycloak server URL from environment or uses default.
   * @returns The Keycloak server URL
   */
  private getKeycloakUrl(): string {
    return (window as any).env?.KEYCLOAK_URL || 'http://localhost:8080';
  }

  /**
   * Initializes the Keycloak authentication.
   * Attempts silent SSO login and logs authentication status.
   * @returns Promise that resolves when initialization is complete
   */
  async init(): Promise<boolean> {
    try {
      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        pkceMethod: 'S256',
        checkLoginIframe: false
      });

      if (authenticated) {
        console.log('User authenticated', this.keycloak.token);
      } else {
        console.log('User not authenticated');
      }
      return authenticated;
    } catch (error) {
      console.error('Keycloak initialization failed', error);
      return false;
    }
  }

  /**
   * Initiates the login flow by redirecting to Keycloak.
   */
  login(): void {
    this.keycloak.login();
  }

  /**
   * Logs out the user and redirects to the application origin.
   */
  logout(): void {
    this.keycloak.logout({ redirectUri: window.location.origin });
  }

  /**
   * Checks if the user is currently authenticated.
   * @returns true if authenticated, false otherwise
   */
  isLoggedIn(): boolean {
    return !!this.keycloak.authenticated;
  }

  /**
   * Gets the current authentication token.
   * @returns The JWT token string or undefined if not authenticated
   */
  getToken(): string | undefined {
    return this.keycloak.token;
  }

  /**
   * Gets the username from the token.
   * @returns The username or empty string if not available
   */
  getUsername(): string {
    return this.keycloak.tokenParsed?.['preferred_username'] || '';
  }

  /**
   * Gets the user's first name from the token.
   * @returns The first name or empty string if not available
   */
  getFirstName(): string {
    return this.keycloak.tokenParsed?.['firstName'] || '';
  }

  /**
   * Gets the user's last name from the token.
   * @returns The last name or empty string if not available
   */
  getLastName(): string {
    return this.keycloak.tokenParsed?.['lastName'] || '';
  }

  /**
   * Gets the user's email from the token.
   * @returns The email or empty string if not available
   */
  getEmail(): string {
    return this.keycloak.tokenParsed?.['email'] || '';
  }

  /**
   * Checks if the user has a specific realm role.
   * @param role - The role name to check
   * @returns true if the user has the role, false otherwise
   */
  hasRole(role: string): boolean {
    return this.keycloak.hasRealmRole(role);
  }

  /**
   * Checks if the user has the ADMIN role.
   * @returns true if the user is an admin, false otherwise
   */
  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  /**
   * Checks if the user has the USER role.
   * @returns true if the user is a regular user, false otherwise
   */
  isUser(): boolean {
    return this.hasRole('USER');
  }

  /**
   * Gets the token expiration timestamp.
   * @returns The expiration timestamp or 0 if not available
   */
  getTokenExpiration(): number {
    return this.keycloak.tokenParsed?.exp || 0;
  }

  /**
   * Checks if the token is expired or will expire soon.
   * @param minValidity - Minimum validity in seconds (default: 30)
   * @returns true if token is expired or will expire soon, false otherwise
   */
  isTokenExpired(minValidity: number = 30): boolean {
    return this.keycloak.isTokenExpired(minValidity);
  }

  /**
   * Updates the token if it's close to expiration.
   * @param minValidity - Minimum validity in seconds (default: 30)
   * @returns Promise that resolves to true if token was updated, false otherwise
   */
  async updateToken(minValidity: number = 30): Promise<boolean> {
    return this.keycloak.updateToken(minValidity);
  }
}
