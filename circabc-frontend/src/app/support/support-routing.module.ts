import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AppAdminGuard } from 'app/core/app-admin-guard.service';
import { DistributionListComponent } from 'app/support/distribution-list/distribution-list.component';
import { RevocationJobsComponent } from 'app/support/revocation-jobs/revocation-jobs.component';
import { SupportComponent } from 'app/support/support.component';
import { CreateTemplateComponent } from 'app/support/system-message/create-template/create-template.component';
import { SystemMessageComponent } from 'app/support/system-message/system-message.component';
import { UserManagementComponent } from 'app/support/user-management/user-management.component';

const routes: Routes = [
  {
    path: '',
    component: SupportComponent,
    canActivate: [AppAdminGuard],
    children: [
      { path: '', redirectTo: 'system-message', pathMatch: 'full' },
      { path: 'system-message', component: SystemMessageComponent },
      { path: 'system-message/create', component: CreateTemplateComponent },
      { path: 'system-message/update/:id', component: CreateTemplateComponent },
      { path: 'user-management', component: UserManagementComponent },
      { path: 'revocation-requests', component: RevocationJobsComponent },
      { path: 'distribution-list', component: DistributionListComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class SupportRoutingModule {}
