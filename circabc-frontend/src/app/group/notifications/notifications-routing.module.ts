import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminGuard } from 'app/group/guards/admin-guard.service';
import { NotificationsComponent } from 'app/group/notifications/notifications.component';

const notificationsRoutes: Routes = [
  {
    path: ':nodeId',
    component: NotificationsComponent,
    canActivate: [AdminGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(notificationsRoutes)],
  exports: [RouterModule],
})
export class NotificationsRoutingModule {}
