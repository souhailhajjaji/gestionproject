import { Component, inject, OnInit, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/service/api.service';
import { User } from '../../../shared/models/user';

/**
 * Component for displaying and editing user details.
 * Allows updating user information and uploading identity documents.
 */
@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container-fluid">
      <h2 class="mb-4">Détails de l'utilisateur</h2>

      <div class="card" *ngIf="user">
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
  `,
  styles: []
})
export class UserDetailComponent implements OnInit {
  private apiService = inject(ApiService);

  id = input.required<string>();
  user?: User;
  selectedFile?: File;

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
    this.apiService.getUser(this.id()).subscribe({
      next: (user) => {
        this.user = user;
      }
    });
  }

  /**
   * Updates the user with the current form data.
   * Shows an alert on success.
   */
  updateUser(): void {
    if (this.user) {
      this.apiService.updateUser(this.id(), this.user).subscribe({
        next: () => {
          alert('Utilisateur mis à jour avec succès');
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
