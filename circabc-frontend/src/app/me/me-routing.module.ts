import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { canActivateAuth } from 'app/core/auth-guard.service';

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

@NgModule({
  imports: [RouterModule.forChild(meRoutes)],
  exports: [RouterModule],
})
export class MeRoutingModule {}
