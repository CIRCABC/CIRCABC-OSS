import { Routes } from '@angular/router';

/**
 * Lazy-loaded route definitions for the login feature area.
 *
 * These routes are mounted under the login feature path and provide
 * standalone-component based navigation for authentication flows:
 * - The empty path (`''`) lazily loads {@link LoginComponent} for signing in.
 * - The `logout` path lazily loads {@link LogoutComponent} for signing out.
 *
 * Each route uses `loadComponent` with a dynamic `import()` so the
 * corresponding component bundle is only fetched when the route is activated,
 * keeping the initial application payload small.
 */
export const loginRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'logout',
    loadComponent: () =>
      import('app/login/logout.component').then((m) => m.LogoutComponent),
  },
];
