import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { canActivateAppAdmin } from 'app/core/app-admin-guard.service';

export const supportRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/support/support.component').then((m) => m.SupportComponent),
    canActivate: [canActivateAppAdmin],
    children: [
      { path: '', redirectTo: 'system-message', pathMatch: 'full' },
      {
        path: 'system-message',
        loadComponent: () =>
          import('app/support/system-message/system-message.component').then(
            (m) => m.SystemMessageComponent
          ),
      },
      {
        path: 'system-message/create',
        loadComponent: () =>
          import(
            'app/support/system-message/create-template/create-template.component'
          ).then((m) => m.CreateTemplateComponent),
      },
      {
        path: 'system-message/update/:id',
        loadComponent: () =>
          import(
            'app/support/system-message/create-template/create-template.component'
          ).then((m) => m.CreateTemplateComponent),
      },
      {
        path: 'user-management',
        loadComponent: () =>
          import('app/support/user-management/user-management.component').then(
            (m) => m.UserManagementComponent
          ),
      },
      {
        path: 'revocation-requests',
        loadComponent: () =>
          import('app/support/revocation-jobs/revocation-jobs.component').then(
            (m) => m.RevocationJobsComponent
          ),
      },
      {
        path: 'distribution-list',
        loadComponent: () =>
          import(
            'app/support/distribution-list/distribution-list.component'
          ).then((m) => m.DistributionListComponent),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(supportRoutes)],
  exports: [RouterModule],
})
export class SupportRoutingModule {}
