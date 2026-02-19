import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { KeycloakService } from '../../../core/service/keycloak.service';

/**
 * Navigation bar component displayed at the top of all pages.
 * Provides navigation links to dashboard, projects, tasks, and users sections.
 * Includes authentication controls for login/logout.
 */
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
      <div class="container-fluid">
        <a class="navbar-brand" routerLink="/dashboard">
          <i class="bi bi-kanban"></i> Gestion Projet
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto" *ngIf="isLoggedIn">
            <li class="nav-item">
              <a class="nav-link" routerLink="/dashboard" routerLinkActive="active">
                Tableau de bord
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/projects" routerLinkActive="active">
                Projets
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/tasks" routerLinkActive="active">
                Tâches
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/users" routerLinkActive="active">
                Utilisateurs
              </a>
            </li>
          </ul>
          <div class="d-flex align-items-center">
            <ng-container *ngIf="isLoggedIn; else loginButton">
              <span class="navbar-text me-3">
                {{ username }}
                <span class="badge bg-secondary ms-2" *ngIf="isAdmin">ADMIN</span>
              </span>
              <button class="btn btn-outline-light btn-sm" (click)="logout()">
                Déconnexion
              </button>
            </ng-container>
            <ng-template #loginButton>
              <button class="btn btn-outline-light btn-sm" (click)="login()">
                Connexion
              </button>
            </ng-template>
          </div>
        </div>
      </div>
    </nav>
  `,
  styles: []
})
export class NavbarComponent {
  private keycloakService = inject(KeycloakService);

  /**
   * Gets whether the user is logged in.
   */
  get isLoggedIn(): boolean {
    return this.keycloakService.isLoggedIn();
  }

  /**
   * Gets whether the user has ADMIN role.
   */
  get isAdmin(): boolean {
    return this.keycloakService.isAdmin();
  }

  /**
   * Gets the username of the logged in user.
   */
  get username(): string {
    return this.keycloakService.getUsername();
  }

  /**
   * Initiates the login flow.
   */
  login(): void {
    this.keycloakService.login();
  }

  /**
   * Logs out the current user.
   */
  logout(): void {
    this.keycloakService.logout();
  }
}
