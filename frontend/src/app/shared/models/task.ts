import { User } from './user';
import { Project } from './project';

/**
 * Task interface representing a task within a project.
 * Contains task details, status, priority, and assignment information.
 */
export interface Task {
  id: string;
  titre: string;
  description?: string;
  statut: TaskStatus;
  priorite: Priority;
  projetId: string;
  projet?: Project;
  assigneId?: string;
  assigne?: User;
}

/**
 * TaskStatus type representing the possible states of a task.
 * TODO: Not started, IN_PROGRESS: Currently being worked on, DONE: Completed
 */
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

/**
 * Priority type representing task priority levels.
 * BASSE: Low, MOYENNE: Medium, HAUTE: High
 */
export type Priority = 'BASSE' | 'MOYENNE' | 'HAUTE';
