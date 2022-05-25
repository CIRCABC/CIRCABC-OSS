import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminGuard } from 'app/group/guards/admin-guard.service';
import { PermissionsComponent } from 'app/group/permissions/permissions.component';

const permissionsRoutes: Routes = [
  {
    path: ':nodeId',
    component: PermissionsComponent,
    canActivate: [AdminGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(permissionsRoutes)],
  exports: [RouterModule],
})
export class PermissionsRoutingModule {}
