import { Routes } from '@angular/router';
import { canActivateAuth } from 'app/core/auth-guard.service';

/**
 * Route configuration for the "me" (personal user) feature area.
 *
 * Declares a parent route rendered by {@link MeComponent} that is protected by
 * the {@link canActivateAuth} guard, ensuring only authenticated users can
 * access it. The parent hosts a set of lazily loaded child routes for the
 * personal sections of the application:
 *
 * - `''` (default): the user dashboard ({@link UserDashboardComponent}).
 * - `calendar`: the personal calendar view ({@link MyCalendarComponent}).
 * - `account`: the account settings view ({@link AccountComponent}).
 * - `roles`: the user roles view ({@link RolesComponent}).
 *
 * All components are loaded on demand via `loadComponent` to keep the initial
 * bundle small.
 */
export const meRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/me/me.component').then((m) => m.MeComponent),
    canActivate: [canActivateAuth],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('app/me/dashboard/user-dashboard.component').then(
            (m) => m.UserDashboardComponent
          ),
      },
      {
        path: 'calendar',
        loadComponent: () =>
          import('app/me/my-calendar/my-calendar.component').then(
            (m) => m.MyCalendarComponent
          ),
      },
      {
        path: 'account',
        loadComponent: () =>
          import('app/me/account/account.component').then(
            (m) => m.AccountComponent
          ),
      },
      {
        path: 'roles',
        loadComponent: () =>
          import('app/me/roles/roles.component').then((m) => m.RolesComponent),
      },
    ],
  },
];
