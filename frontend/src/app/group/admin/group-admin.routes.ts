import { Routes } from '@angular/router';

import { canActivateGroupDelete } from 'app/group/guards/group-delete-guard.service';

/**
 * Route configuration for the group administration feature area.
 *
 * The root route lazily loads {@link GroupAdminComponent}, which acts as the
 * shell/layout for all group admin screens. Its child routes lazily load the
 * individual administration panels and are rendered inside the shell's router
 * outlet:
 * - `general` – general group settings ({@link AdminGeneralComponent})
 * - `security` – security/permission settings ({@link AdminSecurityComponent})
 * - `summary` – group summary ({@link AdminSummaryComponent})
 * - `documents` – document lifecycle configuration ({@link DocumentLifecycleComponent})
 * - `auto-upload` – automatic upload settings ({@link AutoUploadComponent})
 * - `paste-notifications` – paste notification settings ({@link PasteNotificationsComponent})
 * - `notification-status` – notification status overview ({@link NotificationStatusComponent})
 * - `log` – activity log ({@link LogComponent})
 * - `logos` – group logo management ({@link LogosComponent})
 * - `external-repository` – external repository configuration ({@link ExternalRepositoryComponent})
 * - `delete` – group deletion ({@link DeleteGroupComponent}), protected by the
 *   {@link canActivateGroupDelete} guard which restricts access to users
 *   allowed to delete the group.
 *
 * The empty child path redirects to `general` so the admin area opens on the
 * general settings panel by default.
 */
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
          import('app/group/admin/paste-notifications/paste-notifications.component').then(
            (m) => m.PasteNotificationsComponent
          ),
      },
      {
        path: 'notification-status',
        loadComponent: () =>
          import('app/group/admin/notification-status/notification-status.component').then(
            (m) => m.NotificationStatusComponent
          ),
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
          import('app/group/admin/external-repository/external-repository.component').then(
            (m) => m.ExternalRepositoryComponent
          ),
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
