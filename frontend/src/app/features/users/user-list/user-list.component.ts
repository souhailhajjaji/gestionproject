import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/service/api.service';
import { KeycloakService } from '../../../core/service/keycloak.service';
import { User, Role } from '../../../shared/models/user';

/**
 * Component for displaying and managing the list of users.
 * Allows creating, viewing, and deleting users (ADMIN only for creation/deletion).
 */
@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="container-fluid">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Gestion des utilisateurs</h2>
        <button class="btn btn-primary" (click)="openCreateForm()" *ngIf="isAdmin">
          <i class="bi bi-plus"></i> Nouvel utilisateur
        </button>
      </div>

      <!-- Authentication Warning -->
      <div class="alert alert-warning" *ngIf="!isLoggedIn">
        <strong>Non connecté:</strong> Veuillez vous <a href="javascript:void(0)" (click)="login()">connecter</a> pour accéder à toutes les fonctionnalités.
      </div>

      <!-- Admin Notice -->
      <div class="alert alert-info" *ngIf="isLoggedIn && !isAdmin">
        <strong>Information:</strong> Seuls les administrateurs peuvent créer ou supprimer des utilisateurs.
      </div>

      <!-- Error Alert -->
      <div class="alert alert-danger alert-dismissible fade show" *ngIf="userError" role="alert">
        {{ userError }}
        <button type="button" class="btn-close" (click)="userError = ''"></button>
      </div>

      <!-- Admin Only Alert -->
      <div class="alert alert-warning" *ngIf="showAdminRequiredAlert">
        <strong>Accès refusé:</strong> Cette action nécessite le rôle ADMIN. Veuillez contacter votre administrateur.
        <button type="button" class="btn-close" (click)="showAdminRequiredAlert = false"></button>
      </div>

      <!-- Create User Modal -->
      <div class="modal" [class.show]="showCreateForm" style="display: block;" *ngIf="showCreateForm">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Nouvel utilisateur</h5>
              <button type="button" class="btn-close" (click)="showCreateForm = false"></button>
            </div>
            <div class="modal-body">
              <form (ngSubmit)="createUser()">
                <div class="mb-3">
                  <label class="form-label">Nom</label>
                  <input type="text" class="form-control" [(ngModel)]="newUser.nom" name="nom" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Prénom</label>
                  <input type="text" class="form-control" [(ngModel)]="newUser.prenom" name="prenom" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Email</label>
                  <input type="email" class="form-control" [(ngModel)]="newUser.email" name="email" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Mot de passe</label>
                  <input type="password" class="form-control" [(ngModel)]="password" name="password" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Rôles</label>
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="roleAdmin" 
                           [checked]="selectedRoles.includes('ADMIN')"
                           (change)="toggleRole('ADMIN', $event)">
                    <label class="form-check-label" for="roleAdmin">ADMIN</label>
                  </div>
                  <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="roleUser" 
                           [checked]="selectedRoles.includes('USER')"
                           (change)="toggleRole('USER', $event)">
                    <label class="form-check-label" for="roleUser">USER</label>
                  </div>
                </div>
                <button type="submit" class="btn btn-primary" [disabled]="selectedRoles.length === 0">Créer</button>
              </form>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop show" *ngIf="showCreateForm"></div>

      <!-- Users Table -->
      <div class="card">
        <div class="card-body">
          <table class="table table-hover">
            <thead>
              <tr>
                <th>Nom</th>
                <th>Prénom</th>
                <th>Email</th>
                <th>Rôles</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let user of users">
                <td>{{ user.nom }}</td>
                <td>{{ user.prenom }}</td>
                <td>{{ user.email }}</td>
                <td>
                  <span class="badge bg-primary me-1" *ngFor="let role of user.roles">{{ role }}</span>
                </td>
                <td>
                  <a [routerLink]="['/users', user.id]" class="btn btn-sm btn-outline-primary">Voir</a>
                  <button class="btn btn-sm btn-outline-danger ms-1" (click)="deleteUser(user.id)" *ngIf="isAdmin">
                    Supprimer
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class UserListComponent implements OnInit {
  private apiService = inject(ApiService);
  keycloakService = inject(KeycloakService);

  users: User[] = [];
  showCreateForm = false;
  newUser: Partial<User> = {};
  password = '';
  selectedRoles: Role[] = ['USER'];

  userError = '';
  showAdminRequiredAlert = false;

  /**
   * Gets whether the current user is logged in.
   */
  get isLoggedIn(): boolean {
    return this.keycloakService.isLoggedIn();
  }

  /**
   * Gets whether the current user has ADMIN role.
   */
  get isAdmin(): boolean {
    return this.keycloakService.isAdmin();
  }

  /**
   * Initializes the component and loads users.
   */
  ngOnInit(): void {
    this.loadUsers();
  }

  /**
   * Redirects to Keycloak login page.
   */
  login(): void {
    this.keycloakService.login();
  }

  /**
   * Opens the create user form if user is admin, otherwise shows warning.
   */
  openCreateForm(): void {
    if (this.isAdmin) {
      this.showCreateForm = true;
    } else {
      this.showAdminRequiredAlert = true;
    }
  }

  /**
   * Loads all users from the API.
   */
  loadUsers(): void {
    this.apiService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.userError = '';
      },
      error: (err) => {
        this.userError = 'Erreur lors du chargement des utilisateurs: ' + err.message;
        console.error('Error loading users:', err);
      }
    });
  }

  /**
   * Toggles a role in the selected roles array.
   * @param role - The role to toggle
   * @param event - The checkbox change event
   */
  toggleRole(role: Role, event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    if (checkbox.checked) {
      if (!this.selectedRoles.includes(role)) {
        this.selectedRoles.push(role);
      }
    } else {
      this.selectedRoles = this.selectedRoles.filter(r => r !== role);
    }
  }

  /**
   * Creates a new user with the current form data.
   * Closes the modal and refreshes the user list on success.
   */
  createUser(): void {
    const userToCreate: Partial<User> = {
      ...this.newUser,
      roles: this.selectedRoles
    };
    
    this.apiService.createUser(userToCreate, this.password).subscribe({
      next: () => {
        this.showCreateForm = false;
        this.newUser = {};
        this.selectedRoles = ['USER'];
        this.password = '';
        this.userError = '';
        this.loadUsers();
      },
      error: (err) => {
        this.userError = 'Erreur lors de la création: ' + err.message;
        console.error('Error creating user:', err);
      }
    });
  }

  /**
   * Deletes a user after user confirmation.
   * @param id - The UUID of the user to delete
   */
  deleteUser(id: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      this.apiService.deleteUser(id).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (err) => {
          this.userError = 'Erreur lors de la suppression: ' + err.message;
          console.error('Error deleting user:', err);
        }
      });
    }
  }
}
