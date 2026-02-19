import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { KeycloakService } from '../../../core/service/keycloak.service';

/**
 * Login component for handling user authentication.
 * Displays login options and redirects to Keycloak for authentication.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container">
      <div class="row justify-content-center mt-5">
        <div class="col-md-6">
          <div class="card">
            <div class="card-header text-center">
              <h3>Connexion</h3>
            </div>
            <div class="card-body text-center">
              <p class="mb-4">Veuillez vous connecter pour accéder à l'application</p>
              
              <button class="btn btn-primary btn-lg w-100 mb-3" (click)="login()">
                Se connecter avec Keycloak
              </button>
              
              <div *ngIf="errorMessage" class="alert alert-danger mt-3">
                {{ errorMessage }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .container {
      min-height: 100vh;
      display: flex;
      align-items: center;
    }
  `]
})
export class LoginComponent implements OnInit {
  private keycloakService = inject(KeycloakService);
  
  errorMessage = '';

  /**
   * Checks if user is already authenticated and redirects if so.
   */
  ngOnInit(): void {
    if (this.keycloakService.isLoggedIn()) {
      // User is already logged in, redirect to dashboard
      window.location.href = '/dashboard';
    }
  }

  /**
   * Initiates the login flow through Keycloak.
   */
  login(): void {
    try {
      this.keycloakService.login();
    } catch (error) {
      this.errorMessage = 'Erreur lors de la connexion. Veuillez réessayer.';
      console.error('Login error:', error);
    }
  }
}
