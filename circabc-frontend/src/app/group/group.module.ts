import { NgModule } from '@angular/core';

import { GroupAdminModule } from 'app/group/admin/group-admin.module';
import { ContactDescriptionComponent } from 'app/group/contact-description/contact-description.component';
import { BigNumberComponent } from 'app/group/dashboard/big-number/big-number.component';
import { ClearRowComponent } from 'app/group/dashboard/clear-row/clear-row.component';
import { DashboardComponent } from 'app/group/dashboard/dashboard.component';
import { EventsDashletComponent } from 'app/group/dashboard/events/events-dashlet.component';
import { MembersDashletComponent } from 'app/group/dashboard/members-dashlet/members-dashlet.component';
import { RecentDiscussionsComponent } from 'app/group/dashboard/recent-discussions/recent-discussions.component';
import { SimpleNewsCardComponent } from 'app/group/dashboard/timeline/simple-news-card/simple-news-card.component';
import { TimelineComponent } from 'app/group/dashboard/timeline/timeline.component';
import { DescriptorComponent } from 'app/group/descriptor/descriptor.component';
import { GroupLocationComponent } from 'app/group/group-location/group-location.component';
import { GroupRoutingModule } from 'app/group/group-routing.module';
import { GroupComponent } from 'app/group/group.component';
import { AdminGuard } from 'app/group/guards/admin-guard.service';
import { GroupGuard } from 'app/group/guards/group-guard.service';
import { GroupMembersAdminGuard } from 'app/group/guards/group-members-admin-guard.service';
import { LeaderContactComponent } from 'app/group/leader-contact/leader-contact.component';
import { MembersModule } from 'app/group/members/members.module';
import { MembershipApplicationComponent } from 'app/group/membership-application/membership-application.component';
import { MaterialComponentsModule } from 'app/material-components/material-components.module';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [
    GroupAdminModule,
    GroupRoutingModule,
    MembersModule,
    PrimengComponentsModule,
    MaterialComponentsModule,
    SharedModule,
  ],
  exports: [],
  declarations: [
    BigNumberComponent,
    ClearRowComponent,
    ContactDescriptionComponent,
    DashboardComponent,
    DescriptorComponent,
    EventsDashletComponent,
    GroupComponent,
    LeaderContactComponent,
    MembersDashletComponent,
    MembershipApplicationComponent,
    RecentDiscussionsComponent,
    SimpleNewsCardComponent,
    TimelineComponent,
    GroupLocationComponent,
  ],
  providers: [AdminGuard, GroupGuard, GroupMembersAdminGuard, I18nPipe],
})
export class GroupModule {}
