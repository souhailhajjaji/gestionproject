import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { User } from '../../shared/models/user';
import { Project } from '../../shared/models/project';
import { Task } from '../../shared/models/task';

/**
 * Service for making HTTP requests to the backend API.
 * Provides methods for CRUD operations on users, projects, and tasks.
 */
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);
  private apiUrl = '/api';

  /**
   * Handles HTTP errors and returns a formatted error observable.
   * @param error - The HTTP error response
   * @returns Observable throwing the formatted error
   */
  private handleError(error: any): Observable<never> {
    const errorMessage = error.error?.message || error.message || 'Une erreur inconnue est survenue';
    console.error('API Error:', error);
    return throwError(() => new Error(errorMessage));
  }

  // ==================== User endpoints ====================

  /**
   * Retrieves all users from the system.
   * @returns Observable of an array of users
   */
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`).pipe(catchError(this.handleError));
  }

  /**
   * Retrieves a specific user by their ID.
   * @param id - The UUID of the user to retrieve
   * @returns Observable of the user
   */
  getUser(id: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/users/${id}`).pipe(catchError(this.handleError));
  }

  /**
   * Creates a new user with the given data and password.
   * @param user - The user data to create
   * @param password - The initial password for the user
   * @returns Observable of the created user
   */
  createUser(user: Partial<User>, password: string): Observable<User> {
    const params = new HttpParams().set('password', password);
    return this.http.post<User>(`${this.apiUrl}/users`, user, { params }).pipe(catchError(this.handleError));
  }

  /**
   * Updates an existing user with new data.
   * @param id - The UUID of the user to update
   * @param user - The updated user data
   * @returns Observable of the updated user
   */
  updateUser(id: string, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/users/${id}`, user).pipe(catchError(this.handleError));
  }

  /**
   * Deletes a user by their ID.
   * @param id - The UUID of the user to delete
   * @returns Observable of the delete response
   */
  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${id}`).pipe(catchError(this.handleError));
  }

  /**
   * Uploads an identity document for a user.
   * @param id - The UUID of the user
   * @param file - The identity document file to upload
   * @returns Observable of the updated user with document URL
   */
  uploadIdentityDocument(id: string, file: File): Observable<User> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<User>(`${this.apiUrl}/users/${id}/document`, formData).pipe(catchError(this.handleError));
  }

  /**
   * Assigns a role to a user.
   * @param id - The UUID of the user
   * @param role - The role to assign (e.g., 'ADMIN', 'USER')
   * @returns Observable of the updated user
   */
  assignRole(id: string, role: string): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/users/${id}/roles/${role}`, {}).pipe(catchError(this.handleError));
  }

  /**
   * Removes a role from a user.
   * @param id - The UUID of the user
   * @param role - The role to remove
   * @returns Observable of the updated user
   */
  removeRole(id: string, role: string): Observable<User> {
    return this.http.delete<User>(`${this.apiUrl}/users/${id}/roles/${role}`).pipe(catchError(this.handleError));
  }

  // ==================== Project endpoints ====================

  /**
   * Retrieves all projects from the system.
   * @returns Observable of an array of projects
   */
  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/projects`).pipe(catchError(this.handleError));
  }

  /**
   * Retrieves a specific project by its ID.
   * @param id - The UUID of the project to retrieve
   * @returns Observable of the project
   */
  getProject(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/projects/${id}`).pipe(catchError(this.handleError));
  }

  /**
   * Retrieves all projects managed by a specific responsible user.
   * @param responsableId - The UUID of the responsible user
   * @returns Observable of an array of projects
   */
  getProjectsByResponsable(responsableId: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/projects/responsable/${responsableId}`).pipe(catchError(this.handleError));
  }

  /**
   * Creates a new project.
   * @param project - The project data to create
   * @returns Observable of the created project
   */
  createProject(project: Partial<Project>): Observable<Project> {
    return this.http.post<Project>(`${this.apiUrl}/projects`, project).pipe(catchError(this.handleError));
  }

  /**
   * Updates an existing project with new data.
   * @param id - The UUID of the project to update
   * @param project - The updated project data
   * @returns Observable of the updated project
   */
  updateProject(id: string, project: Partial<Project>): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/projects/${id}`, project).pipe(catchError(this.handleError));
  }

  /**
   * Deletes a project by its ID.
   * @param id - The UUID of the project to delete
   * @returns Observable of the delete response
   */
  deleteProject(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/projects/${id}`).pipe(catchError(this.handleError));
  }

  // ==================== Task endpoints ====================

  /**
   * Retrieves all tasks from the system.
   * @returns Observable of an array of tasks
   */
  getTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks`).pipe(catchError(this.handleError));
  }

  /**
   * Retrieves a specific task by its ID.
   * @param id - The UUID of the task to retrieve
   * @returns Observable of the task
   */
  getTask(id: string): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/tasks/${id}`).pipe(catchError(this.handleError));
  }

  /**
   * Retrieves all tasks belonging to a specific project.
   * @param projetId - The UUID of the project
   * @returns Observable of an array of tasks
   */
  getTasksByProjet(projetId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/projet/${projetId}`).pipe(catchError(this.handleError));
  }

  /**
   * Retrieves all tasks assigned to a specific user.
   * @param assigneId - The UUID of the assignee
   * @returns Observable of an array of tasks
   */
  getTasksByAssigne(assgneId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/assigne/${assgneId}`).pipe(catchError(this.handleError));
  }

  /**
   * Filters tasks based on optional criteria: assignee, status, and project.
   * @param params - Filter parameters (assigneId, statut, projetId)
   * @returns Observable of an array of matching tasks
   */
  filterTasks(params: { assigneId?: string; statut?: string; projetId?: string }): Observable<Task[]> {
    let httpParams = new HttpParams();
    if (params.assigneId) {
      httpParams = httpParams.set('assigneId', params.assigneId);
    }
    if (params.statut) {
      httpParams = httpParams.set('statut', params.statut);
    }
    if (params.projetId) {
      httpParams = httpParams.set('projetId', params.projetId);
    }
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/filter`, { params: httpParams }).pipe(catchError(this.handleError));
  }

  /**
   * Retrieves task count grouped by status for a specific project.
   * @param projetId - The UUID of the project
   * @returns Observable of a map containing status counts
   */
  getTaskStatsByProjet(projetId: string): Observable<{ [key: string]: number }> {
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/tasks/projet/${projetId}/stats`).pipe(catchError(this.handleError));
  }

  /**
   * Creates a new task.
   * @param task - The task data to create
   * @returns Observable of the created task
   */
  createTask(task: Partial<Task>): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/tasks`, task).pipe(catchError(this.handleError));
  }

  /**
   * Updates an existing task with new data.
   * @param id - The UUID of the task to update
   * @param task - The updated task data
   * @returns Observable of the updated task
   */
  updateTask(id: string, task: Partial<Task>): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/tasks/${id}`, task).pipe(catchError(this.handleError));
  }

  /**
   * Updates only the status of a task.
   * @param id - The UUID of the task
   * @param statut - The new status to set
   * @returns Observable of the updated task
   */
  updateTaskStatus(id: string, statut: string): Observable<Task> {
    const params = new HttpParams().set('statut', statut);
    return this.http.patch<Task>(`${this.apiUrl}/tasks/${id}/status`, {}, { params }).pipe(catchError(this.handleError));
  }

  /**
   * Deletes a task by its ID.
   * @param id - The UUID of the task to delete
   * @returns Observable of the delete response
   */
  deleteTask(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/tasks/${id}`).pipe(catchError(this.handleError));
  }
}
