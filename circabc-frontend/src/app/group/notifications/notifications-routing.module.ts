import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { canActivateAdmin } from 'app/group/guards/admin-guard.service';

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

@NgModule({
  imports: [RouterModule.forChild(notificationsRoutes)],
  exports: [RouterModule],
})
export class NotificationsRoutingModule {}
