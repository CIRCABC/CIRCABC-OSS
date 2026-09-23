import { Routes } from '@angular/router';
import { canActivateAdmin } from 'app/group/guards/admin-guard.service';

/**
 * Child route configuration for the group permissions feature.
 *
 * Declares a single parameterized route (`:nodeId`) that lazily loads the
 * standalone `PermissionsComponent` to manage permissions for the identified
 * node. Access is protected by the {@link canActivateAdmin} guard, so only
 * users with administrative rights on the group can activate the route.
 */
export const permissionsRoutes: Routes = [
  {
    path: ':nodeId',
    loadComponent: () =>
      import('app/group/permissions/permissions.component').then(
        (m) => m.PermissionsComponent
      ),
    canActivate: [canActivateAdmin],
  },
];
