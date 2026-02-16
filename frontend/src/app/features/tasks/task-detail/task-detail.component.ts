import { Component, inject, OnInit, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/service/api.service';
import { Task } from '../../../shared/models/task';
import { Project } from '../../../shared/models/project';
import { User } from '../../../shared/models/user';

/**
 * Component for displaying and editing task details.
 * Allows updating task information and deleting tasks.
 */
@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container-fluid">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Détails de la tâche</h2>
        <a routerLink="/tasks" class="btn btn-secondary">Retour</a>
      </div>

      <div class="card" *ngIf="task">
        <div class="card-body">
          <form (ngSubmit)="updateTask()">
            <div class="row">
              <div class="col-md-6 mb-3">
                <label class="form-label">Titre</label>
                <input type="text" class="form-control" [(ngModel)]="task.titre" name="titre" required>
              </div>
              <div class="col-md-6 mb-3">
                <label class="form-label">Projet</label>
                <select class="form-select" [(ngModel)]="task.projetId" name="projetId" required>
                  <option *ngFor="let project of projects" [value]="project.id">{{ project.nom }}</option>
                </select>
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label">Description</label>
              <textarea class="form-control" [(ngModel)]="task.description" name="description" rows="4"></textarea>
            </div>
            <div class="row">
              <div class="col-md-4 mb-3">
                <label class="form-label">Statut</label>
                <select class="form-select" [(ngModel)]="task.statut" name="statut">
                  <option value="TODO">À faire</option>
                  <option value="IN_PROGRESS">En cours</option>
                  <option value="DONE">Terminé</option>
                </select>
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Priorité</label>
                <select class="form-select" [(ngModel)]="task.priorite" name="priorite">
                  <option value="BASSE">Basse</option>
                  <option value="MOYENNE">Moyenne</option>
                  <option value="HAUTE">Haute</option>
                </select>
              </div>
              <div class="col-md-4 mb-3">
                <label class="form-label">Assigné à</label>
                <select class="form-select" [(ngModel)]="task.assigneId" name="assigneId">
                  <option [ngValue]="undefined">Non assigné</option>
                  <option *ngFor="let user of users" [ngValue]="user.id">
                    {{ user.nom }} {{ user.prenom }}
                  </option>
                </select>
              </div>
            </div>
            <button type="submit" class="btn btn-primary">Mettre à jour</button>
            <button type="button" class="btn btn-danger ms-2" (click)="deleteTask()">Supprimer</button>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class TaskDetailComponent implements OnInit {
  private apiService = inject(ApiService);

  id = input.required<string>();
  task?: Task;
  projects: Project[] = [];
  users: User[] = [];

  /**
   * Initializes the component and loads task, projects, and users.
   */
  ngOnInit(): void {
    this.loadTask();
    this.loadProjects();
    this.loadUsers();
  }

  /**
   * Loads the task details by ID.
   */
  loadTask(): void {
    this.apiService.getTask(this.id()).subscribe({
      next: (task) => {
        this.task = task;
      }
    });
  }

  /**
   * Loads all projects from the API (for project selection).
   */
  loadProjects(): void {
    this.apiService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
      }
    });
  }

  /**
   * Loads all users from the API (for assignee selection).
   */
  loadUsers(): void {
    this.apiService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
      }
    });
  }

  /**
   * Updates the task with the current form data.
   * Shows an alert on success.
   */
  updateTask(): void {
    if (this.task) {
      this.apiService.updateTask(this.id(), this.task).subscribe({
        next: () => {
          alert('Tâche mise à jour avec succès');
        }
      });
    }
  }

  /**
   * Deletes the task after user confirmation.
   * Navigates back on success.
   */
  deleteTask(): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette tâche ?')) {
      this.apiService.deleteTask(this.id()).subscribe({
        next: () => {
          window.history.back();
        }
      });
    }
  }
}
