import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { canActivateAdmin } from 'app/group/guards/admin-guard.service';

export const categoryRoutes: Routes = [
  {
    path: ':id',
    loadComponent: () =>
      import('app/category/category.component').then(
        (m) => m.CategoryComponent
      ),
    canActivate: [canActivateAdmin],

    children: [
      { path: '', redirectTo: 'details', pathMatch: 'full' },
      {
        path: 'details',
        loadComponent: () =>
          import(
            'app/category/category-details/category-details.component'
          ).then((m) => m.CategoryDetailsComponent),
      },
      {
        path: 'administrators',
        loadComponent: () =>
          import(
            'app/category/category-administrators/category-administrators.component'
          ).then((m) => m.CategoryAdministratorsComponent),
      },
      {
        path: 'actions',
        loadComponent: () =>
          import(
            'app/category/category-actions/category-actions.component'
          ).then((m) => m.CategoryActionsComponent),
      },
      {
        path: 'ig-statistics',
        loadComponent: () =>
          import('app/category/ig-statistics/ig-statistics.component').then(
            (m) => m.IgStatisticsComponent
          ),
      },
      {
        path: 'customisation',
        loadComponent: () =>
          import(
            'app/category/category-customisation/category-customisation.component'
          ).then((m) => m.CategoryCustomisationComponent),
      },
      {
        path: 'support',
        loadComponent: () =>
          import(
            'app/category/category-support/category-support.component'
          ).then((m) => m.CategorySupportComponent),
      },
      {
        path: 'group-requests',
        loadComponent: () =>
          import('./ig-requests/ig-requests.component').then(
            (m) => m.IgRequestsComponent
          ),
      },
      {
        path: 'interest-groups',
        loadComponent: () =>
          import('app/category/category-group/category-groups.component').then(
            (m) => m.CategoryGroupsComponent
          ),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(categoryRoutes)],
  exports: [RouterModule],
})
export class CategoryRoutingModule {}
