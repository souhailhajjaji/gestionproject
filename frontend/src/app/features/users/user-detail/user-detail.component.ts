import { Component, inject, OnInit, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/service/api.service';
import { User, Role } from '../../../shared/models/user';

/**
 * Component for displaying and editing user details.
 * Allows updating user information and uploading identity documents.
 */
@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container-fluid">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Détails de l'utilisateur</h2>
        <a routerLink="/users" class="btn btn-secondary">Retour</a>
      </div>

      <!-- Error Alert -->
      <div class="alert alert-danger alert-dismissible fade show mb-4" *ngIf="error" role="alert">
        {{ error }}
        <button type="button" class="btn-close" (click)="error = ''"></button>
      </div>

      <!-- Success Alert -->
      <div class="alert alert-success alert-dismissible fade show mb-4" *ngIf="successMessage" role="alert">
        {{ successMessage }}
        <button type="button" class="btn-close" (click)="successMessage = ''"></button>
      </div>

      <div class="row" *ngIf="user">
        <div class="col-md-8">
          <div class="card mb-4">
            <div class="card-header">Informations</div>
            <div class="card-body">
              <form (ngSubmit)="updateUser()">
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Nom</label>
                    <input type="text" class="form-control" [(ngModel)]="user.nom" name="nom" required>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Prénom</label>
                    <input type="text" class="form-control" [(ngModel)]="user.prenom" name="prenom" required>
                  </div>
                </div>
                <div class="row">
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Email</label>
                    <input type="email" class="form-control" [(ngModel)]="user.email" name="email" required>
                  </div>
                  <div class="col-md-6 mb-3">
                    <label class="form-label">Téléphone</label>
                    <input type="text" class="form-control" [(ngModel)]="user.telephone" name="telephone">
                  </div>
                </div>
                <div class="mb-3">
                  <label class="form-label">Date de naissance</label>
                  <input type="date" class="form-control" [(ngModel)]="user.dateNaissance" name="dateNaissance">
                </div>
                <div class="mb-3">
                  <label class="form-label">Pièce d'identité</label>
                  <input type="file" class="form-control" (change)="onFileSelected($event)">
                  <small class="text-muted" *ngIf="user.pieceIdentiteUrl">
                    Fichier actuel: {{ user.pieceIdentiteUrl }}
                  </small>
                </div>
                <button type="submit" class="btn btn-primary">Mettre à jour</button>
              </form>
            </div>
          </div>
        </div>

        <div class="col-md-4">
          <div class="card">
            <div class="card-header">Rôles</div>
            <div class="card-body">
              <div class="mb-3">
                <label class="form-label">Rôles actuels</label>
                <div>
                  <span class="badge bg-primary me-1 mb-1" *ngFor="let role of user.roles">{{ role }}</span>
                  <span class="text-muted" *ngIf="!user.roles || user.roles.length === 0">Aucun rôle</span>
                </div>
              </div>
              <hr>
              <div class="mb-3">
                <label class="form-label">Gérer les rôles</label>
                <div class="d-grid gap-2">
                  <button class="btn btn-sm" [class.btn-success]="!hasRole('ADMIN')" [class.btn-danger]="hasRole('ADMIN')" (click)="toggleRole('ADMIN')">
                    <span *ngIf="!hasRole('ADMIN')">+ Ajouter ADMIN</span>
                    <span *ngIf="hasRole('ADMIN')">- Retirer ADMIN</span>
                  </button>
                  <button class="btn btn-sm" [class.btn-success]="!hasRole('USER')" [class.btn-danger]="hasRole('USER')" (click)="toggleRole('USER')">
                    <span *ngIf="!hasRole('USER')">+ Ajouter USER</span>
                    <span *ngIf="hasRole('USER')">- Retirer USER</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class UserDetailComponent implements OnInit {
  private apiService = inject(ApiService);

  id = input.required<string>();
  user?: User;
  selectedFile?: File;
  error = '';
  successMessage = '';

  /**
   * Initializes the component and loads the user details.
   */
  ngOnInit(): void {
    this.loadUser();
  }

  /**
   * Loads the user details by ID.
   */
  loadUser(): void {
    this.error = '';
    this.apiService.getUser(this.id()).subscribe({
      next: (user) => {
        this.user = user;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement: ' + err.message;
        console.error('Error loading user:', err);
      }
    });
  }

  /**
   * Updates the user with the current form data.
   * Shows an alert on success.
   */
  updateUser(): void {
    if (this.user) {
      this.error = '';
      this.successMessage = '';
      this.apiService.updateUser(this.id(), this.user).subscribe({
        next: () => {
          this.successMessage = 'Utilisateur mis à jour avec succès';
        },
        error: (err) => {
          this.error = 'Erreur lors de la mise à jour: ' + err.message;
          console.error('Error updating user:', err);
        }
      });
    }
  }

  /**
   * Checks if the user has a specific role.
   * @param role - The role to check
   * @returns true if user has the role
   */
  hasRole(role: Role): boolean {
    return this.user?.roles?.includes(role) ?? false;
  }

  /**
   * Toggles a role for the user (add or remove).
   * @param role - The role to toggle
   */
  toggleRole(role: Role): void {
    if (!this.user) return;

    this.error = '';
    this.successMessage = '';

    if (this.hasRole(role)) {
      this.apiService.removeRole(this.id(), role).subscribe({
        next: (updatedUser) => {
          this.user = updatedUser;
          this.successMessage = `Rôle ${role} retiré avec succès`;
        },
        error: (err) => {
          this.error = 'Erreur lors du retrait du rôle: ' + err.message;
          console.error('Error removing role:', err);
        }
      });
    } else {
      this.apiService.assignRole(this.id(), role).subscribe({
        next: (updatedUser) => {
          this.user = updatedUser;
          this.successMessage = `Rôle ${role} ajouté avec succès`;
        },
        error: (err) => {
          this.error = 'Erreur lors de l\'ajout du rôle: ' + err.message;
          console.error('Error assigning role:', err);
        }
      });
    }
  }

  /**
   * Handles file selection from the file input.
   * Triggers the document upload when a file is selected.
   * @param event - The file input change event
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
      this.uploadDocument();
    }
  }

  /**
   * Uploads the selected identity document for the user.
   * Shows an alert on success.
   */
  uploadDocument(): void {
    if (this.selectedFile && this.user) {
      this.apiService.uploadIdentityDocument(this.id(), this.selectedFile).subscribe({
        next: (updatedUser) => {
          this.user = updatedUser;
          alert('Document téléchargé avec succès');
        }
      });
    }
  }
}
