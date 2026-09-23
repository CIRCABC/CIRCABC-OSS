import { Routes } from '@angular/router';

/**
 * Route configuration for the platform administration area.
 *
 * The root path lazily loads {@link AdminComponent}, which acts as the shell
 * for the admin section. Child routes are lazily loaded as standalone
 * components:
 * - An empty child path redirects to `headers`.
 * - `headers` loads the `HeadersComponent` for managing headers.
 * - `circabc` loads the `CircabcComponent` for CIRCABC-level administration.
 */
export const adminRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/admin/admin.component').then((m) => m.AdminComponent),
    children: [
      { path: '', redirectTo: 'headers', pathMatch: 'full' },
      {
        path: 'headers',
        loadComponent: () =>
          import('app/admin/headers/headers.component').then(
            (m) => m.HeadersComponent
          ),
        canActivate: [],
      },
      {
        path: 'circabc',
        loadComponent: () =>
          import('app/admin/circabc/circabc.component').then(
            (m) => m.CircabcComponent
          ),
        canActivate: [],
      },
    ],
  },
];
