import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { SharedModule } from 'app/shared/shared.module';
import { AddDistributionEmailComponent } from 'app/support/distribution-list/add-distribution-email/add-distribution-email.component';
import { DistributionListComponent } from 'app/support/distribution-list/distribution-list.component';
import { RevocationJobsComponent } from 'app/support/revocation-jobs/revocation-jobs.component';
import { SupportRoutingModule } from 'app/support/support-routing.module';
import { SupportComponent } from 'app/support/support.component';
import { CreateTemplateComponent } from 'app/support/system-message/create-template/create-template.component';
import { OldUiConfigurationComponent } from 'app/support/system-message/old-ui-configuration/old-ui-configuration.component';
import { SystemMessageComponent } from 'app/support/system-message/system-message.component';
import { TemplateRendererComponent } from 'app/support/system-message/template-renderer/template-renderer.component';
import { ExpirationSchedulerComponent } from 'app/support/user-management/expiration-scheduler/expiration-scheduler.component';
import { FocusedUserBoxComponent } from 'app/support/user-management/focused-user-box/focused-user-box.component';
import { CategoryBoxComponent } from 'app/support/user-management/focused-user-membership-box/category-box/category-box.component';
// eslint-disable-next-line max-len
import { FocusedUserMembershipBoxComponent } from 'app/support/user-management/focused-user-membership-box/focused-user-membership-box.component';
import { GroupBoxComponent } from 'app/support/user-management/focused-user-membership-box/group-box/group-box.component';
import { RevocationSchedulerComponent } from 'app/support/user-management/revocation-scheduler/revocation-scheduler.component';
import { UserManagementComponent } from 'app/support/user-management/user-management.component';
import { UserResultBoxComponent } from 'app/support/user-management/user-result-box/user-result-box.component';

@NgModule({
  imports: [
    CommonModule,
    SupportRoutingModule,
    SharedModule,
    PrimengComponentsModule,
  ],
  declarations: [
    SupportComponent,
    SystemMessageComponent,
    TemplateRendererComponent,
    CreateTemplateComponent,
    OldUiConfigurationComponent,
    UserManagementComponent,
    UserResultBoxComponent,
    FocusedUserBoxComponent,
    FocusedUserMembershipBoxComponent,
    GroupBoxComponent,
    CategoryBoxComponent,
    RevocationSchedulerComponent,
    RevocationJobsComponent,
    ExpirationSchedulerComponent,
    DistributionListComponent,
    AddDistributionEmailComponent,
  ],
})
export class SupportModule {}
