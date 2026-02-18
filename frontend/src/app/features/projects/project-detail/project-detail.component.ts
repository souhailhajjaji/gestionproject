import { Component, inject, OnInit, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/service/api.service';
import { Project } from '../../../shared/models/project';
import { Task, TaskStatus } from '../../../shared/models/task';
import { User } from '../../../shared/models/user';

/**
 * Component for displaying project details and managing its tasks.
 * Shows project information and a Kanban-style task board grouped by status.
 */
@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container-fluid">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>Détails du projet</h2>
        <a routerLink="/projects" class="btn btn-secondary">Retour</a>
      </div>

      <div class="row" *ngIf="project">
        <div class="col-md-4">
          <div class="card mb-4">
            <div class="card-header">Informations</div>
            <div class="card-body">
              <h5>{{ project.nom }}</h5>
              <p>{{ project.description || 'Aucune description' }}</p>
              <hr>
              <p><strong>Début:</strong> {{ project.dateDebut }}</p>
              <p><strong>Fin:</strong> {{ project.dateFin || 'Non définie' }}</p>
              <p><strong>Responsable:</strong> {{ project.responsable?.nom }} {{ project.responsable?.prenom }}</p>
            </div>
          </div>
        </div>

        <div class="col-md-8">
          <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
              <span>Tâches du projet</span>
              <button class="btn btn-sm btn-primary" (click)="showCreateTask = true">
                <i class="bi bi-plus"></i> Nouvelle tâche
              </button>
            </div>
            <div class="card-body">
              <div class="row">
                <div class="col-md-4">
                  <h6>À faire ({{ tasksByStatut['TODO']?.length || 0 }})</h6>
                  <div class="list-group mb-3">
                    <a [routerLink]="['/tasks', task.id]"
                       class="list-group-item list-group-item-action"
                       *ngFor="let task of tasksByStatut['TODO']">
                      {{ task.titre }}
                      <span class="badge float-end" [ngClass]="getPrioriteClass(task.priorite)">{{ task.priorite }}</span>
                    </a>
                  </div>
                </div>
                <div class="col-md-4">
                  <h6>En cours ({{ tasksByStatut['IN_PROGRESS']?.length || 0 }})</h6>
                  <div class="list-group mb-3">
                    <a [routerLink]="['/tasks', task.id]"
                       class="list-group-item list-group-item-action"
                       *ngFor="let task of tasksByStatut['IN_PROGRESS']">
                      {{ task.titre }}
                      <span class="badge float-end" [ngClass]="getPrioriteClass(task.priorite)">{{ task.priorite }}</span>
                    </a>
                  </div>
                </div>
                <div class="col-md-4">
                  <h6>Terminé ({{ tasksByStatut['DONE']?.length || 0 }})</h6>
                  <div class="list-group mb-3">
                    <a [routerLink]="['/tasks', task.id]"
                       class="list-group-item list-group-item-action"
                       *ngFor="let task of tasksByStatut['DONE']">
                      {{ task.titre }}
                      <span class="badge float-end" [ngClass]="getPrioriteClass(task.priorite)">{{ task.priorite }}</span>
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Error Alert -->
      <div class="alert alert-danger alert-dismissible fade show mb-4" *ngIf="taskError" role="alert">
        {{ taskError }}
        <button type="button" class="btn-close" (click)="taskError = ''"></button>
      </div>

      <!-- Create Task Modal -->
      <div class="modal" [class.show]="showCreateTask" style="display: block;" *ngIf="showCreateTask">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">Nouvelle tâche</h5>
              <button type="button" class="btn-close" (click)="showCreateTask = false; taskError = ''"></button>
            </div>
            <div class="modal-body">
              <form (ngSubmit)="createTask()">
                <div class="mb-3">
                  <label class="form-label">Titre</label>
                  <input type="text" class="form-control" [(ngModel)]="newTask.titre" name="titre" required>
                </div>
                <div class="mb-3">
                  <label class="form-label">Description</label>
                  <textarea class="form-control" [(ngModel)]="newTask.description" name="description" rows="3"></textarea>
                </div>
                <div class="mb-3">
                  <label class="form-label">Priorité</label>
                  <select class="form-select" [(ngModel)]="newTask.priorite" name="priorite">
                    <option value="BASSE">Basse</option>
                    <option value="MOYENNE">Moyenne</option>
                    <option value="HAUTE">Haute</option>
                  </select>
                </div>
                <div class="mb-3">
                  <label class="form-label">Assigné à</label>
                  <select class="form-select" [(ngModel)]="newTask.assigneId" name="assigneId">
                    <option [ngValue]="undefined">Non assigné</option>
                    <option *ngFor="let user of users" [ngValue]="user.id">
                      {{ user.nom }} {{ user.prenom }}
                    </option>
                  </select>
                </div>
                <button type="submit" class="btn btn-primary">Créer</button>
              </form>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop show" *ngIf="showCreateTask"></div>
    </div>
  `,
  styles: []
})
export class ProjectDetailComponent implements OnInit {
  private apiService = inject(ApiService);

  id = input.required<string>();
  project?: Project;
  tasks: Task[] = [];
  users: User[] = [];
  showCreateTask = false;
  newTask: Partial<Task> = { statut: 'TODO', priorite: 'MOYENNE' };

  tasksByStatut: { [key: string]: Task[] } = {};

  /**
   * Initializes the component and loads project, tasks, and users.
   */
  ngOnInit(): void {
    this.loadProject();
    this.loadTasks();
    this.loadUsers();
  }

  /**
   * Loads the project details by ID.
   */
  loadProject(): void {
    this.apiService.getProject(this.id()).subscribe({
      next: (project) => {
        this.project = project;
      }
    });
  }

  /**
   * Loads all tasks for this project and groups them by status.
   */
  loadTasks(): void {
    this.apiService.getTasksByProjet(this.id()).subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.tasksByStatut = {
          TODO: tasks.filter(t => t.statut === 'TODO'),
          IN_PROGRESS: tasks.filter(t => t.statut === 'IN_PROGRESS'),
          DONE: tasks.filter(t => t.statut === 'DONE')
        };
      }
    });
  }

  /**
   * Loads all users from the API (for task assignment).
   */
  loadUsers(): void {
    this.apiService.getUsers().subscribe({
      next: (users) => {
        this.users = users;
      }
    });
  }

  taskError = '';

  /**
   * Creates a new task for this project with the current form data.
   * Closes the modal and refreshes the task list on success.
   */
  createTask(): void {
    this.taskError = '';
    this.newTask.projetId = this.id();
    this.apiService.createTask(this.newTask).subscribe({
      next: () => {
        this.showCreateTask = false;
        this.newTask = { statut: 'TODO', priorite: 'MOYENNE' };
        this.taskError = '';
        this.loadTasks();
      },
      error: (err) => {
        this.taskError = 'Erreur lors de la création de la tâche: ' + err.message;
        console.error('Error creating task:', err);
      }
    });
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
