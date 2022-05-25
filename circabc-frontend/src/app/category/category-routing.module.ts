import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CategoryActionsComponent } from 'app/category/category-actions/category-actions.component';
import { CategoryAdministratorsComponent } from 'app/category/category-administrators/category-administrators.component';
import { CategoryCustomisationComponent } from 'app/category/category-customisation/category-customisation.component';
import { CategoryDetailsComponent } from 'app/category/category-details/category-details.component';
import { CategoryGroupsComponent } from 'app/category/category-group/category-groups.component';
import { CategorySupportComponent } from 'app/category/category-support/category-support.component';
import { CategoryComponent } from 'app/category/category.component';
import { GroupRequestsComponent } from 'app/category/group-requests/group-requests.component';
import { IgStatisticsComponent } from 'app/category/ig-statistics/ig-statistics.component';
import { AdminGuard } from 'app/group/guards/admin-guard.service';

const routes: Routes = [
  {
    path: ':id',
    component: CategoryComponent,
    canActivate: [AdminGuard],

    children: [
      { path: '', redirectTo: 'details', pathMatch: 'full' },
      { path: 'details', component: CategoryDetailsComponent },
      { path: 'administrators', component: CategoryAdministratorsComponent },
      { path: 'actions', component: CategoryActionsComponent },
      { path: 'ig-statistics', component: IgStatisticsComponent },
      { path: 'customisation', component: CategoryCustomisationComponent },
      { path: 'support', component: CategorySupportComponent },
      { path: 'group-requests', component: GroupRequestsComponent },
      { path: 'interest-groups', component: CategoryGroupsComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CategoryRoutingModule {}
