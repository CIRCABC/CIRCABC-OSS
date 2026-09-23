import { Routes } from '@angular/router';

/**
 * Route configuration for the explorer feature area.
 *
 * Defines the lazy-loaded child routes used to browse categories and headers:
 * - `''` (default): renders the {@link ExplorerComponent} landing view.
 * - `'group-request'`: renders the {@link RequestGroupComponent} for
 *   requesting a new interest group.
 * - `'contact-category'`: renders the {@link ContactCategoryComponent} for
 *   contacting a category's administrators.
 *
 * Each route uses `loadComponent` so the associated standalone component is
 * loaded on demand.
 */
export const explorerRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/explorer/explorer.component').then(
        (m) => m.ExplorerComponent
      ),
  },
  {
    path: 'group-request',
    loadComponent: () =>
      import('app/explorer/request-group/request-group.component').then(
        (m) => m.RequestGroupComponent
      ),
  },
  {
    path: 'contact-category',
    loadComponent: () =>
      import('app/explorer/contact-category/contact-category.component').then(
        (m) => m.ContactCategoryComponent
      ),
  },
];
