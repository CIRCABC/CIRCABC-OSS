import { Routes } from '@angular/router';

import { canActivateAdmin } from 'app/group/guards/admin-guard.service';

/**
 * Child route configuration for the category management area.
 *
 * All routes are nested under a `:id` parameter that identifies the target
 * category and lazy-load their respective standalone components. Access to the
 * parent route is protected by {@link canActivateAdmin}, restricting the entire
 * category section to administrators.
 *
 * The default child route redirects to `details`. Available sub-sections are:
 * - `details` — category details view
 * - `administrators` — manage category administrators
 * - `actions` — category-level actions
 * - `ig-statistics` — interest group statistics
 * - `customisation` — category customisation settings
 * - `support` — category support configuration
 * - `group-requests` — pending interest group requests
 * - `interest-groups` — interest groups belonging to the category
 */
export const categoryRoutes: Routes = [
  {
    path: ':id',
    loadComponent: () =>
      import('app/category/category.component').then(
        (m) => m.CategoryComponent
      ),
    canActivate: [canActivateAdmin],

    children: [
      { path: '', redirectTo: 'details', pathMatch: 'full' },
      {
        path: 'details',
        loadComponent: () =>
          import('app/category/category-details/category-details.component').then(
            (m) => m.CategoryDetailsComponent
          ),
      },
      {
        path: 'administrators',
        loadComponent: () =>
          import('app/category/category-administrators/category-administrators.component').then(
            (m) => m.CategoryAdministratorsComponent
          ),
      },
      {
        path: 'actions',
        loadComponent: () =>
          import('app/category/category-actions/category-actions.component').then(
            (m) => m.CategoryActionsComponent
          ),
      },
      {
        path: 'ig-statistics',
        loadComponent: () =>
          import('app/category/ig-statistics/ig-statistics.component').then(
            (m) => m.IgStatisticsComponent
          ),
      },
      {
        path: 'customisation',
        loadComponent: () =>
          import('app/category/category-customisation/category-customisation.component').then(
            (m) => m.CategoryCustomisationComponent
          ),
      },
      {
        path: 'support',
        loadComponent: () =>
          import('app/category/category-support/category-support.component').then(
            (m) => m.CategorySupportComponent
          ),
      },
      {
        path: 'group-requests',
        loadComponent: () =>
          import('./ig-requests/ig-requests.component').then(
            (m) => m.IgRequestsComponent
          ),
      },
      {
        path: 'interest-groups',
        loadComponent: () =>
          import('app/category/category-group/category-groups.component').then(
            (m) => m.CategoryGroupsComponent
          ),
      },
    ],
  },
];
