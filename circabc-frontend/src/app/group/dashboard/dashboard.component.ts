import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { TranslocoService } from '@jsverse/transloco';

import {
  DashboardService,
  GroupDashboard,
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { ContactDescriptionComponent } from 'app/group/contact-description/contact-description.component';
import { EventsDashletComponent } from 'app/group/dashboard/events/events-dashlet.component';
import { MembersDashletComponent } from 'app/group/dashboard/members-dashlet/members-dashlet.component';
import { TimelineComponent } from 'app/group/dashboard/timeline/timeline.component';
import { DescriptorComponent } from 'app/group/descriptor/descriptor.component';
import { GroupLocationComponent } from 'app/group/group-location/group-location.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { firstValueFrom } from 'rxjs';
import { RecentDiscussionsComponent } from './recent-discussions/recent-discussions.component';

@Component({
  selector: 'cbc-group-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    GroupLocationComponent,
    DescriptorComponent,
    ContactDescriptionComponent,
    TimelineComponent,
    MembersDashletComponent,
    EventsDashletComponent,
    RecentDiscussionsComponent,
    SetTitlePipe,
  ],
})
export class DashboardComponent implements OnInit {
  public timeline!: GroupDashboard;
  public group?: InterestGroup;
  public igId!: string;
  public loading = false;

  public displayMembersBox = false;
  public displayEventsBox = false;
  public displayForumsBox = false;
  public displayWhatsnewBox = false;

  public constructor(
    private route: ActivatedRoute,
    private interestGroupService: InterestGroupService,
    private dashboardService: DashboardService,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService
  ) {}

  public ngOnInit() {
    this.route.params.subscribe(async (params) => await this.loadGroup(params));
  }

  private async loadGroup(params: { [key: string]: string }) {
    this.loading = true;
    this.igId = params.id;
    this.group = await firstValueFrom(
      this.interestGroupService.getInterestGroup(this.igId)
    );

    this.displayWhatsnewBox =
      this.group.permissions.information !== 'InfNoAccess' ||
      this.group.permissions.library !== 'LibNoAccess';
    this.displayMembersBox = this.group.permissions.directory !== 'DirNoAccess';
    this.displayForumsBox = this.group.permissions.newsgroup !== 'NwsNoAccess';
    this.displayEventsBox = this.group.permissions.event !== 'EveNoAccess';

    if (this.displayWhatsnewBox) {
      await this.loadDashboard(this.igId);
    }
    this.loading = false;
  }

  private async loadDashboard(groupId: string) {
    try {
      this.timeline = await firstValueFrom(
        this.dashboardService.getGroupDashboard(groupId)
      );
    } catch (_error) {
      this.timeline = {};
      const res = this.translateService.translate('error.dashboard.read');
      this.uiMessageService.addErrorMessage(res);
    }
  }
}
