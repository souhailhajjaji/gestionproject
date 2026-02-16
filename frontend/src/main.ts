import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

/**
 * Main entry point for the Angular application.
 * Bootstraps the root AppComponent with the application configuration.
 */
bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
