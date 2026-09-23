import { Routes } from '@angular/router';
import { canActivateAdmin } from 'app/group/guards/admin-guard.service';

/**
 * Route configuration for the notifications feature of an interest group.
 *
 * Declares a single child route matched by `nodeId`, which lazily loads the
 * standalone `NotificationsComponent`. Access is restricted by the
 * {@link canActivateAdmin} guard, so only group administrators can activate
 * the route.
 */
export const notificationsRoutes: Routes = [
  {
    path: ':nodeId',
    loadComponent: () =>
      import('app/group/notifications/notifications.component').then(
        (m) => m.NotificationsComponent
      ),
    canActivate: [canActivateAdmin],
  },
];
