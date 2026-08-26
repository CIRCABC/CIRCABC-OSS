import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

export const adminRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('app/admin/admin.component').then((m) => m.AdminComponent),
    children: [
      { path: '', redirectTo: 'headers', pathMatch: 'full' },
      {
        path: 'headers',
        loadComponent: () =>
          import('app/admin/headers/headers.component').then(
            (m) => m.HeadersComponent
          ),
        canActivate: [],
      },
      {
        path: 'circabc',
        loadComponent: () =>
          import('app/admin/circabc/circabc.component').then(
            (m) => m.CircabcComponent
          ),
        canActivate: [],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(adminRoutes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
