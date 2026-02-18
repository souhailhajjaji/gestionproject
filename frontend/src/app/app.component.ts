import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { KeycloakService } from './core/service/keycloak.service';

/**
 * Root component of the Gestion Projet application.
 * Displays the navigation bar and renders routed components in the main content area.
 * Initializes Keycloak authentication on startup.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent],
  template: `
    <app-navbar></app-navbar>
    <main class="container-fluid py-4">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: []
})
export class AppComponent implements OnInit {
  title = 'Gestion Projet';
  private keycloakService = inject(KeycloakService);

  async ngOnInit(): Promise<void> {
    await this.keycloakService.init();
  }
}
