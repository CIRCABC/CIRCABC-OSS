import { Routes } from '@angular/router';

/**
 * Child route configuration for the group "profiles" (access profiles) feature area.
 *
 * Defines a single default (empty path) route that lazily loads the
 * {@link ProfilesComponent} via `loadComponent`, so the profiles view is only
 * fetched when this route branch is activated.
 */
export const profilesRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/profiles/profiles.component').then(
        (m) => m.ProfilesComponent
      ),
  },
];
