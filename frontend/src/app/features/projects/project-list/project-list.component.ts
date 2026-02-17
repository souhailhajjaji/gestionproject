import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/service/api.service';
import { Project } from '../../../shared/models/project';
import { User } from '../../../shared/models/user';

/**
 * Component for displaying and managing the list of projects.
 * Allows creating, viewing, and deleting projects.
 */
@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="container-fluid">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Gestion des projets</h2>
        <button class="btn btn-primary" (click)="showCreateForm = true">
          <i class="bi bi-plus"></i> Nouveau projet
        </button>
      </div>

      <!-- Error Alert -->
      <div class="alert alert-danger alert-dismissible fade show" *ngIf="projectError" role="alert">
        {{ projectError }}
        <button type="button" class="btn-close" (click)="projectError = ''"></button>
      </div>

      <!-- No Users Alert -->
      <div class="alert alert-warning" *ngIf="users.length === 0">
        <strong>Aucun utilisateur trouvé!</strong> Vous devez d'abord créer un utilisateur pour pouvoir créer un projet.
        <a routerLink="/users" class="alert-link">Gérer les utilisateurs</a>
      </div>

      <!-- Create Project Modal -->
      <div class="modal" [class.show]="showCreateForm" style="display: block;" *ngIf="showCreateForm">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Nouveau projet</h5>
              <button type="button" class="btn-close" (click)="showCreateForm = false"></button>
            </div>
            <div class="modal-body">
              <form (ngSubmit)="createProject()">
                <div class="mb-3">
                  <label class="form-label">Nom</label>
                  <input type="text" class="form-control" [(ngModel)]="newProject.nom" name="nom" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Description</label>
                  <textarea class="form-control" [(ngModel)]="newProject.description" name="description" rows="3"></textarea>
                </div>
                <div class="mb-3">
                  <label class="form-label">Date de début</label>
                  <input type="date" class="form-control" [(ngModel)]="newProject.dateDebut" name="dateDebut" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Date de fin</label>
                  <input type="date" class="form-control" [(ngModel)]="newProject.dateFin" name="dateFin">
                </div>
                <div class="mb-3">
                  <label class="form-label">Responsable <span class="text-danger">*</span></label>
                  <select class="form-select" [(ngModel)]="newProject.responsableId" name="responsableId" required>
                    <option [ngValue]="undefined" disabled>Sélectionner un responsable</option>
                    <option *ngFor="let user of users" [ngValue]="user.id">
                      {{ user.nom }} {{ user.prenom }} ({{ user.email }})
                    </option>
                  </select>
                  <div class="form-text">Un responsable est requis pour créer un projet.</div>
                </div>
                <button type="submit" class="btn btn-primary" [disabled]="!newProject.responsableId">Créer</button>
              </form>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop show" *ngIf="showCreateForm"></div>

      <!-- Projects Grid -->
      <div class="row">
        <div class="col-md-4 mb-4" *ngFor="let project of projects">
          <div class="card h-100">
            <div class="card-body">
              <h5 class="card-title">{{ project.nom }}</h5>
              <p class="card-text">{{ project.description || 'Aucune description' }}</p>
              <p class="mb-1"><strong>Début:</strong> {{ project.dateDebut }}</p>
              <p class="mb-1"><strong>Fin:</strong> {{ project.dateFin || 'Non définie' }}</p>
              <p class="mb-0">
                <strong>Responsable:</strong>
                {{ project.responsable?.nom }} {{ project.responsable?.prenom }}
              </p>
            </div>
            <div class="card-footer">
              <a [routerLink]="['/projects', project.id]" class="btn btn-sm btn-primary">Voir détails</a>
              <button class="btn btn-sm btn-outline-danger float-end" (click)="deleteProject(project.id)">
                Supprimer
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="alert alert-info" *ngIf="projects.length === 0">
        Aucun projet trouvé. Créez votre premier projet !
      </div>
    </div>
  `,
  styles: []
})
export class ProjectListComponent implements OnInit {
  private apiService = inject(ApiService);

  projects: Project[] = [];
  users: User[] = [];
  showCreateForm = false;
  newProject: Partial<Project> = {};
  projectError = '';

  /**
   * Initializes the component and loads projects and users.
   */
  ngOnInit(): void {
    this.loadProjects();
    this.loadUsers();
  }

  /**
   * Loads all projects from the API.
   */
  loadProjects(): void {
    this.apiService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.projectError = '';
      },
      error: (err) => {
        this.projectError = 'Erreur lors du chargement des projets: ' + err.message;
        console.error('Error loading projects:', err);
      }
    });
  }

  /**
   * Loads all users from the API (for selecting project responsible).
   */
  loadUsers(): void {
    this.apiService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
      },
      error: (err) => {
        console.error('Error loading users:', err);
      }
    });
  }

  /**
   * Creates a new project with the current form data.
   * Closes the modal and refreshes the project list on success.
   */
  createProject(): void {
    this.apiService.createProject(this.newProject).subscribe({
      next: () => {
        this.showCreateForm = false;
        this.newProject = {};
        this.projectError = '';
        this.loadProjects();
      },
      error: (err) => {
        this.projectError = 'Erreur lors de la création: ' + err.message;
        console.error('Error creating project:', err);
      }
    });
  }

  /**
   * Deletes a project after user confirmation.
   * @param id - The UUID of the project to delete
   */
  deleteProject(id: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce projet ?')) {
      this.apiService.deleteProject(id).subscribe({
        next: () => {
          this.loadProjects();
        },
        error: (err) => {
          this.projectError = 'Erreur lors de la suppression: ' + err.message;
          console.error('Error deleting project:', err);
        }
      });
    }
  }
}
