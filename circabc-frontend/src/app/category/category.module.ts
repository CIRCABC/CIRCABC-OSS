import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { CategoryActionsComponent } from 'app/category/category-actions/category-actions.component';
// eslint-disable-next-line max-len
import { AddCategoryAdministratorComponent } from 'app/category/category-administrators/add-category-administrator/add-category-administrator.component';
import { CategoryAdministratorsComponent } from 'app/category/category-administrators/category-administrators.component';
import { AddCategoryLogoComponent } from 'app/category/category-customisation/add-category-logo/add-category-logo.component';
import { CategoryCustomisationComponent } from 'app/category/category-customisation/category-customisation.component';
import { CategoryDescriptorComponent } from 'app/category/category-descriptor/category-descriptor.component';
import { CategoryDetailsComponent } from 'app/category/category-details/category-details.component';
import { CategoryGroupsComponent } from 'app/category/category-group/category-groups.component';
import { CategoryRoutingModule } from 'app/category/category-routing.module';
import { CategorySupportComponent } from 'app/category/category-support/category-support.component';
import { CategoryComponent } from 'app/category/category.component';
import { CreateGroupComponent } from 'app/category/create-group/create-group.component';
import { GroupRequestElementComponent } from 'app/category/group-requests/group-request-element/group-request-element.component';
import { GroupRequestsComponent } from 'app/category/group-requests/group-requests.component';
import { SimpleLeaderCardComponent } from 'app/category/group-requests/simple-leader-card/simple-leader-card.component';
import { IgStatisticsComponent } from 'app/category/ig-statistics/ig-statistics.component';
import { AdminGuard } from 'app/group/guards/admin-guard.service';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    CategoryRoutingModule,
    SharedModule,
    PrimengComponentsModule,
  ],
  declarations: [
    CategoryComponent,
    CategoryDetailsComponent,
    CategoryAdministratorsComponent,
    CategoryActionsComponent,
    CreateGroupComponent,
    IgStatisticsComponent,
    CategoryCustomisationComponent,
    AddCategoryLogoComponent,
    AddCategoryAdministratorComponent,
    CategoryDescriptorComponent,
    CategorySupportComponent,
    GroupRequestsComponent,
    GroupRequestElementComponent,
    SimpleLeaderCardComponent,
    CategoryGroupsComponent,
  ],
  providers: [I18nPipe, AdminGuard],
})
export class CategoryModule {}
