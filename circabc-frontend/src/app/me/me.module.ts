import { NgModule } from '@angular/core';
import { AgendaModule } from 'app/group/agenda/agenda.module';
import { AccountComponent } from 'app/me/account/account.component';
import { ChangeAvatarComponent } from 'app/me/account/change-avatar/change-avatar.component';
import { DashboardHelpLayerComponent } from 'app/me/dashboard/dashboard-help-layer/dashboard-help-layer.component';
import { UserDashboardComponent } from 'app/me/dashboard/user-dashboard.component';
import { RecentConsultationComponent } from 'app/me/dashboard/widget/recent-consultation/recent-consultation.component';
import { UserFavouritesComponent } from 'app/me/dashboard/widget/user-favourites/user-favourites.component';
import { GroupStepComponent } from 'app/me/dashboard/widget/user-timeline/group-step/group-step.component';
import { SummaryBoxComponent } from 'app/me/dashboard/widget/user-timeline/summary-box/summary-box.component';
import { TimelineStepComponent } from 'app/me/dashboard/widget/user-timeline/timeline-step/timeline-step.component';
import { UserTimelineComponent } from 'app/me/dashboard/widget/user-timeline/user-timeline.component';
import { VisitedGroupsComponent } from 'app/me/dashboard/widget/visited-groups/visited-groups.component';
import { UserEventsComponent } from 'app/me/events/user-events.component';
import { MeRoutingModule } from 'app/me/me-routing.module';
import { MeComponent } from 'app/me/me.component';
import { UserMembershipsComponent } from 'app/me/memberships/user-memberships.component';
import { MyCalendarComponent } from 'app/me/my-calendar/my-calendar.component';
import { QuitGroupComponent } from 'app/me/quit-group/quit-group.component';
import { RolesComponent } from 'app/me/roles/roles.component';
import { UserSearchesComponent } from 'app/me/searches/user-searches.component';
import { UserTasksComponent } from 'app/me/tasks/user-tasks.component';
import { PrimengComponentsModule } from 'app/primeng-components/primeng-components.module';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SharedModule } from 'app/shared/shared.module';

@NgModule({
  imports: [
    MeRoutingModule,
    SharedModule,
    AgendaModule,
    PrimengComponentsModule,
  ],
  exports: [],
  declarations: [
    AccountComponent,
    ChangeAvatarComponent,
    MeComponent,
    MyCalendarComponent,
    RecentConsultationComponent,
    UserDashboardComponent,
    UserEventsComponent,
    UserMembershipsComponent,
    UserSearchesComponent,
    UserTasksComponent,
    VisitedGroupsComponent,
    UserFavouritesComponent,
    UserTimelineComponent,
    TimelineStepComponent,
    SummaryBoxComponent,
    GroupStepComponent,
    RolesComponent,
    DashboardHelpLayerComponent,
    QuitGroupComponent,
  ],
  providers: [I18nPipe],
})
export class MeModule {}
