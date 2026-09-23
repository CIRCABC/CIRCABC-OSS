import { Routes } from '@angular/router';

/**
 * Route configuration for the keywords feature area.
 *
 * Declares a single child route bound to the empty path (`''`) that lazily
 * loads the standalone {@link KeywordsComponent} via `loadComponent`. This
 * deferred import keeps the keywords view out of the initial bundle until the
 * route is activated.
 */
export const keywordsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/keywords/keywords.component').then(
        (m) => m.KeywordsComponent
      ),
  },
];
