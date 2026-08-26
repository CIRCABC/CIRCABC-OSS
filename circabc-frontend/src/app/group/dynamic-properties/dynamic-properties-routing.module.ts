import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

export const dynPropRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/group/dynamic-properties/dynamic-properties.component').then(
        (m) => m.DynamicPropertiesComponent
      ),
  },
  {
    path: ':dpId/edit',
    loadComponent: () =>
      import(
        'app/group/dynamic-properties/edit-dynamic-property/edit-dynamic-property.component'
      ).then((m) => m.EditDynamicPropertyComponent),
  },
];

@NgModule({
  imports: [RouterModule.forChild(dynPropRoutes)],
  exports: [RouterModule],
})
export class DynamicPropertiesRoutingModule {}
