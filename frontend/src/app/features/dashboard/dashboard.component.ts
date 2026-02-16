import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/service/api.service';
import { Project } from '../../shared/models/project';
import { Task, TaskStatus } from '../../shared/models/task';

/**
 * Dashboard component displaying an overview of projects and tasks.
 * Shows summary statistics and recent items for quick access.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container-fluid">
      <h2 class="mb-4">Tableau de bord</h2>

      <div class="alert alert-info" *ngIf="!backendAvailable">
        <strong>Backend non disponible:</strong> Le serveur backend n'est pas démarré.
        Démarrez le backend Spring Boot pour voir les données.
      </div>

      <div class="row mb-4" *ngIf="backendAvailable">
        <div class="col-md-4">
          <div class="card bg-primary text-white">
            <div class="card-body">
              <h5 class="card-title">Projets</h5>
              <h2 class="display-4">{{ projects.length }}</h2>
              <a routerLink="/projects" class="text-white">Voir tous les projets →</a>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="card bg-success text-white">
            <div class="card-body">
              <h5 class="card-title">Tâches terminées</h5>
              <h2 class="display-4">{{ tasksDone }}</h2>
              <a routerLink="/tasks" class="text-white">Voir toutes les tâches →</a>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="card bg-warning text-dark">
            <div class="card-body">
              <h5 class="card-title">Tâches en cours</h5>
              <h2 class="display-4">{{ tasksInProgress }}</h2>
              <a routerLink="/tasks" class="text-dark">Voir toutes les tâches →</a>
            </div>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="col-md-6">
          <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
              <span>Derniers projets</span>
              <a routerLink="/projects" class="btn btn-sm btn-primary">Voir tout</a>
            </div>
            <div class="card-body">
              <ul class="list-group list-group-flush">
                <li class="list-group-item" *ngFor="let project of recentProjects">
                  <div class="d-flex justify-content-between align-items-center">
                    <div>
                      <h6 class="mb-0">{{ project.nom }}</h6>
                      <small class="text-muted">Responsable: {{ project.responsable?.nom }} {{ project.responsable?.prenom }}</small>
                    </div>
                    <a [routerLink]="['/projects', project.id]" class="btn btn-sm btn-outline-primary">Voir</a>
                  </div>
                </li>
                <li class="list-group-item text-center" *ngIf="projects.length === 0">
                  <p class="text-muted mb-0">Aucun projet</p>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div class="col-md-6">
          <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
              <span>Tâches récentes</span>
              <a routerLink="/tasks" class="btn btn-sm btn-primary">Voir tout</a>
            </div>
            <div class="card-body">
              <ul class="list-group list-group-flush">
                <li class="list-group-item" *ngFor="let task of recentTasks">
                  <div class="d-flex justify-content-between align-items-center">
                    <div>
                      <h6 class="mb-0">{{ task.titre }}</h6>
                      <small class="text-muted">
                        <span class="badge" [ngClass]="getStatutClass(task.statut)">{{ task.statut }}</span>
                        <span class="badge" [ngClass]="getPrioriteClass(task.priorite)">{{ task.priorite }}</span>
                      </small>
                    </div>
                    <a [routerLink]="['/tasks', task.id]" class="btn btn-sm btn-outline-primary">Voir</a>
                  </div>
                </li>
                <li class="list-group-item text-center" *ngIf="allTasks.length === 0">
                  <p class="text-muted mb-0">Aucune tâche</p>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class DashboardComponent implements OnInit {
  private apiService = inject(ApiService);

  projects: Project[] = [];
  allTasks: Task[] = [];
  tasksDone = 0;
  tasksInProgress = 0;
  backendAvailable = true;

  /**
   * Gets the 5 most recent projects for display.
   * @returns Array of up to 5 recent projects
   */
  get recentProjects(): Project[] {
    return this.projects.slice(0, 5);
  }

  /**
   * Gets the 5 most recent tasks for display.
   * @returns Array of up to 5 recent tasks
   */
  get recentTasks(): Task[] {
    return this.allTasks.slice(0, 5);
  }

  /**
   * Initializes the component and loads dashboard data.
   */
  ngOnInit(): void {
    this.loadData();
  }

  /**
   * Loads projects and tasks from the API.
   * Calculates task statistics and handles backend availability.
   */
  loadData(): void {
    this.apiService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.backendAvailable = true;
      },
      error: () => {
        this.backendAvailable = false;
        this.projects = [];
        this.allTasks = [];
        this.tasksDone = 0;
        this.tasksInProgress = 0;
      }
    });

    this.apiService.getTasks().subscribe({
      next: (tasks) => {
        this.allTasks = tasks;
        this.tasksDone = tasks.filter(t => t.statut === 'DONE').length;
        this.tasksInProgress = tasks.filter(t => t.statut === 'IN_PROGRESS').length;
      },
      error: () => {
        this.backendAvailable = false;
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
