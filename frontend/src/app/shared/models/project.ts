import { User } from './user';

/**
 * Project interface representing a project in the system.
 * Contains project details and the responsible user.
 */
export interface Project {
  id: string;
  nom: string;
  description?: string;
  dateDebut: string;
  dateFin?: string;
  responsableId: string;
  responsable?: User;
}
