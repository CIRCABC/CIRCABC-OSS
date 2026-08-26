import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { canActivateAdmin } from 'app/group/guards/admin-guard.service';

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

@NgModule({
  imports: [RouterModule.forChild(permissionsRoutes)],
  exports: [RouterModule],
})
export class PermissionsRoutingModule {}
