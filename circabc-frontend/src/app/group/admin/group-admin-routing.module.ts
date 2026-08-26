import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { canActivateGroupDelete } from 'app/group/guards/group-delete-guard.service';

export const groupAdminRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/admin/group-admin.component').then(
        (m) => m.GroupAdminComponent
      ),
    children: [
      { path: '', redirectTo: 'general', pathMatch: 'full' },
      {
        path: 'general',
        loadComponent: () =>
          import('app/group/admin/general/admin-general.component').then(
            (m) => m.AdminGeneralComponent
          ),
      },
      {
        path: 'security',
        loadComponent: () =>
          import('app/group/admin/security/admin-security.component').then(
            (m) => m.AdminSecurityComponent
          ),
      },
      {
        path: 'summary',
        loadComponent: () =>
          import('app/group/admin/summary/admin-summary.component').then(
            (m) => m.AdminSummaryComponent
          ),
      },
      {
        path: 'documents',
        loadComponent: () =>
          import('app/group/admin/documents/document-lifecycle.component').then(
            (m) => m.DocumentLifecycleComponent
          ),
      },
      {
        path: 'auto-upload',
        loadComponent: () =>
          import('app/group/admin/auto-upload/auto-upload.component').then(
            (m) => m.AutoUploadComponent
          ),
      },
      {
        path: 'paste-notifications',
        loadComponent: () =>
          import(
            'app/group/admin/paste-notifications/paste-notifications.component'
          ).then((m) => m.PasteNotificationsComponent),
      },
      {
        path: 'notification-status',
        loadComponent: () =>
          import(
            'app/group/admin/notification-status/notification-status.component'
          ).then((m) => m.NotificationStatusComponent),
      },
      {
        path: 'log',
        loadComponent: () =>
          import('app/group/admin/log/log.component').then(
            (m) => m.LogComponent
          ),
      },
      {
        path: 'logos',
        loadComponent: () =>
          import('app/group/admin/logos/logos.component').then(
            (m) => m.LogosComponent
          ),
      },
      {
        path: 'external-repository',
        loadComponent: () =>
          import(
            'app/group/admin/external-repository/external-repository.component'
          ).then((m) => m.ExternalRepositoryComponent),
      },
      {
        path: 'delete',
        loadComponent: () =>
          import('app/group/admin/delete-group/delete-group.component').then(
            (m) => m.DeleteGroupComponent
          ),
        canActivate: [canActivateGroupDelete],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(groupAdminRoutes)],
  exports: [RouterModule],
})
export class GroupAdminRoutingModule {}
