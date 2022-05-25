import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DynamicPropertiesComponent } from 'app/group/dynamic-properties/dynamic-properties.component';
import { EditDynamicPropertyComponent } from 'app/group/dynamic-properties/edit-dynamic-property/edit-dynamic-property.component';

const dynPropRoutes: Routes = [
  { path: '', component: DynamicPropertiesComponent },
  { path: ':dpId/edit', component: EditDynamicPropertyComponent },
];

@NgModule({
  imports: [RouterModule.forChild(dynPropRoutes)],
  exports: [RouterModule],
})
export class DynamicPropertiesRoutingModule {}
