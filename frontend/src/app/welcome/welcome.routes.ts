import { Routes } from '@angular/router';

/**
 * Route definitions for the welcome feature.
 *
 * Declares the `welcome` path and lazily loads the standalone
 * {@link WelcomeComponent} via `loadComponent`, so the component's code is
 * only fetched when the route is activated.
 */
export const welcomeRoutes: Routes = [
  {
    path: 'welcome',
    loadComponent: () =>
      import('app/welcome/welcome.component').then((m) => m.WelcomeComponent),
  },
];
