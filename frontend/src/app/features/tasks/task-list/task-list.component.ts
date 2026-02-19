import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/service/api.service';
import { Task } from '../../../shared/models/task';
import { Project } from '../../../shared/models/project';
import { User } from '../../../shared/models/user';

/**
 * Component for displaying and managing the list of tasks.
 * Provides filtering by project, status, and assignee.
 * Allows updating task status directly from the list.
 */
@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="container-fluid">
      <h2 class="mb-4">Gestion des tâches</h2>

      <!-- Filters -->
      <div class="card mb-4">
        <div class="card-body">
          <div class="row">
            <div class="col-md-3">
              <label class="form-label">Projet</label>
              <select class="form-select" [(ngModel)]="filterProjetId">
                <option value="">Tous les projets</option>
                <option *ngFor="let project of projects" [value]="project.id">{{ project.nom }}</option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label">Statut</label>
              <select class="form-select" [(ngModel)]="filterStatut">
                <option value="">Tous les statuts</option>
                <option value="TODO">À faire</option>
                <option value="IN_PROGRESS">En cours</option>
                <option value="DONE">Terminé</option>
              </select>
            </div>
            <div class="col-md-3">
              <label class="form-label">Assigné à</label>
              <select class="form-select" [(ngModel)]="filterAssigneId">
                <option value="">Tous les utilisateurs</option>
                <option *ngFor="let user of users" [value]="user.id">{{ user.nom }} {{ user.prenom }}</option>
              </select>
            </div>
            <div class="col-md-3 d-flex align-items-end gap-2">
              <button class="btn btn-primary flex-fill" (click)="applyFilters()">Filtrer</button>
              <button class="btn btn-outline-secondary" (click)="resetFilters()">Reset</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Tasks Table -->
      <div class="card">
        <div class="card-body">
          <table class="table table-hover">
            <thead>
              <tr>
                <th>Titre</th>
                <th>Projet</th>
                <th>Statut</th>
                <th>Priorité</th>
                <th>Assigné à</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let task of filteredTasks">
                <td>{{ task.titre }}</td>
                <td>{{ task.projet?.nom }}</td>
                <td>
                  <span class="badge" [ngClass]="getStatutClass(task.statut)">{{ task.statut }}</span>
                </td>
                <td>
                  <span class="badge" [ngClass]="getPrioriteClass(task.priorite)">{{ task.priorite }}</span>
                </td>
                <td>{{ task.assigne?.nom }} {{ task.assigne?.prenom || 'Non assigné' }}</td>
                <td>
                  <a [routerLink]="['/tasks', task.id]" class="btn btn-sm btn-outline-primary">Voir</a>
                  <select class="form-select form-select-sm d-inline-block w-auto ms-1"
                          [(ngModel)]="task.statut"
                          (ngModelChange)="updateTaskStatus(task)">
                    <option value="TODO">À faire</option>
                    <option value="IN_PROGRESS">En cours</option>
                    <option value="DONE">Terminé</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="alert alert-info" *ngIf="filteredTasks.length === 0">
        Aucune tâche trouvée avec les filtres sélectionnés.
      </div>
    </div>
  `,
  styles: []
})
export class TaskListComponent implements OnInit {
  private apiService = inject(ApiService);

  tasks: Task[] = [];
  projects: Project[] = [];
  users: User[] = [];
  filteredTasks: Task[] = [];

  filterProjetId = '';
  filterStatut = '';
  filterAssigneId = '';

  /**
   * Initializes the component and loads tasks, projects, and users.
   */
  ngOnInit(): void {
    this.loadTasks();
    this.loadProjects();
    this.loadUsers();
  }

  /**
   * Loads all tasks from the API.
   */
  loadTasks(): void {
    this.apiService.getTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.filteredTasks = tasks;
      }
    });
  }

  /**
   * Loads all projects from the API (for filtering).
   */
  loadProjects(): void {
    this.apiService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
      }
    });
  }

  /**
   * Loads all users from the API (for filtering).
   */
  loadUsers(): void {
    this.apiService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
      }
    });
  }

  /**
   * Applies the selected filters to the task list.
   * Calls the backend API with filter parameters.
   */
  applyFilters(): void {
    const params: any = {};
    if (this.filterProjetId) {
      params.projetId = this.filterProjetId;
    }
    if (this.filterStatut) {
      params.statut = this.filterStatut;
    }
    if (this.filterAssigneId) {
      params.assigneId = this.filterAssigneId;
    }

    this.apiService.filterTasks(params).subscribe({
      next: (tasks) => {
        this.filteredTasks = tasks;
      },
      error: (err) => {
        console.error('Error filtering tasks:', err);
        this.filteredTasks = this.tasks;
      }
    });
  }

  /**
   * Resets all filters to show all tasks.
   */
  resetFilters(): void {
    this.filterProjetId = '';
    this.filterStatut = '';
    this.filterAssigneId = '';
    this.loadTasks();
  }

  /**
   * Updates the status of a task.
   * @param task - The task whose status is being updated
   */
  updateTaskStatus(task: Task): void {
    this.apiService.updateTaskStatus(task.id, task.statut).subscribe({
      next: () => {
        console.log('Task status updated');
      }
    });
  }

  /**
   * Gets the CSS class for a task status badge.
   * @param statut - The task status
   * @returns CSS class name for styling the status badge
   */
  getStatutClass(statut: string): string {
    switch (statut) {
      case 'DONE': return 'badge-done';
      case 'IN_PROGRESS': return 'badge-in-progress';
      default: return 'badge-todo';
    }
  }

  /**
   * Gets the CSS class for a task priority badge.
   * @param priorite - The task priority
   * @returns CSS class name for styling the priority badge
   */
  getPrioriteClass(priorite: string): string {
    switch (priorite) {
      case 'HAUTE': return 'badge-haute';
      case 'MOYENNE': return 'badge-moyenne';
      default: return 'badge-basse';
    }
  }
}
