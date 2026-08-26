import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

export const explorerRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/explorer/explorer.component').then(
        (m) => m.ExplorerComponent
      ),
  },
  {
    path: 'group-request',
    loadComponent: () =>
      import('app/explorer/request-group/request-group.component').then(
        (m) => m.RequestGroupComponent
      ),
  },
  {
    path: 'contact-category',
    loadComponent: () =>
      import('app/explorer/contact-category/contact-category.component').then(
        (m) => m.ContactCategoryComponent
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(explorerRoutes)],
  exports: [RouterModule],
})
export class ExplorerRoutingModule {}
