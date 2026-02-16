/**
 * User interface representing a user in the system.
 * Contains user profile information and assigned roles.
 */
export interface User {
  id: string;
  keycloakId: string;
  nom: string;
  prenom: string;
  dateNaissance?: string;
  email: string;
  telephone?: string;
  pieceIdentiteUrl?: string;
  roles: Role[];
}

/**
 * Role type representing user roles in the system.
 * ADMIN has full access, USER has standard access.
 */
export type Role = 'ADMIN' | 'USER';
