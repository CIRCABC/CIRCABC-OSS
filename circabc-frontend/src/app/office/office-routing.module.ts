import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

export const officeRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./office/office.component').then((m) => m.OfficeComponent),
  },
];

@NgModule({
  imports: [RouterModule.forChild(officeRoutes)],
  exports: [RouterModule],
})
export class OfficeRoutingModule {}
