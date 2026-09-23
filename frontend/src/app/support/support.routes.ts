import { Routes } from '@angular/router';
import { canActivateAlfrescoAdmin } from 'app/core/alfresco-admin-guard.service';
import { canActivateAppAdmin } from 'app/core/app-admin-guard.service';

/**
 * Lazy-loaded child route configuration for the CIRCABC support area.
 *
 * The root path renders the {@link SupportComponent} shell and is protected by
 * the {@link canActivateAppAdmin} guard, so only application administrators can
 * enter the support section. Nested child routes lazily load feature components
 * for the individual support tools:
 *
 * - `system-message` — view/manage system messages
 *   ({@link SystemMessageComponent}).
 * - `system-message/create` and `system-message/update/:id` — create or edit a
 *   system message template ({@link CreateTemplateComponent}).
 * - `user-management` — administer users ({@link UserManagementComponent}).
 * - `revocation-requests` — review revocation jobs
 *   ({@link RevocationJobsComponent}).
 * - `distribution-list` — manage distribution lists
 *   ({@link DistributionListComponent}).
 * - `ig-export` and `ig-import` — export/import interest groups
 *   ({@link IgExportComponent} / {@link IgImportComponent}); these are further
 *   restricted by the {@link canActivateAlfrescoAdmin} guard.
 *
 * The empty child path redirects to `system-message` as the default view.
 */
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
          import('app/support/system-message/create-template/create-template.component').then(
            (m) => m.CreateTemplateComponent
          ),
      },
      {
        path: 'system-message/update/:id',
        loadComponent: () =>
          import('app/support/system-message/create-template/create-template.component').then(
            (m) => m.CreateTemplateComponent
          ),
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
          import('app/support/distribution-list/distribution-list.component').then(
            (m) => m.DistributionListComponent
          ),
      },
      {
        path: 'ig-export',
        canActivate: [canActivateAlfrescoAdmin],
        loadComponent: () =>
          import('app/support/ig-export/ig-export.component').then(
            (m) => m.IgExportComponent
          ),
      },
      {
        path: 'ig-import',
        canActivate: [canActivateAlfrescoAdmin],
        loadComponent: () =>
          import('app/support/ig-import/ig-import.component').then(
            (m) => m.IgImportComponent
          ),
      },
    ],
  },
];
