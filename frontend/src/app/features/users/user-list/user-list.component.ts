import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/service/api.service';
import { KeycloakService } from '../../../core/service/keycloak.service';
import { User } from '../../../shared/models/user';

/**
 * Component for displaying and managing the list of users.
 * Allows creating, viewing, and deleting users.
 */
@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="container-fluid">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Gestion des utilisateurs</h2>
        <button class="btn btn-primary" (click)="showCreateForm = true">
          <i class="bi bi-plus"></i> Nouvel utilisateur
        </button>
      </div>

      <!-- Error Alert -->
      <div class="alert alert-danger alert-dismissible fade show" *ngIf="userError" role="alert">
        {{ userError }}
        <button type="button" class="btn-close" (click)="userError = ''"></button>
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
                  <select class="form-select" [(ngModel)]="newUser.roles" name="roles" multiple>
                    <option [value]="'ADMIN'">ADMIN</option>
                    <option [value]="'USER'">USER</option>
                  </select>
                </div>
                <button type="submit" class="btn btn-primary">Créer</button>
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
                  <button class="btn btn-sm btn-outline-danger ms-1" (click)="deleteUser(user.id)">
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
  newUser: Partial<User> = { roles: ['USER'] };
  password = '';

  userError = '';

  /**
   * Initializes the component and loads users.
   */
  ngOnInit(): void {
    this.loadUsers();
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
   * Creates a new user with the current form data.
   * Closes the modal and refreshes the user list on success.
   */
  createUser(): void {
    this.apiService.createUser(this.newUser, this.password).subscribe({
      next: () => {
        this.showCreateForm = false;
        this.newUser = { roles: ['USER'] };
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
