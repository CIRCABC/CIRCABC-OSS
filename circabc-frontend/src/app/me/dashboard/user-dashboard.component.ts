import { Component, OnInit } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { InterestGroupProfile, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UserEventsComponent } from 'app/me/events/user-events.component';
import { UserMembershipsComponent } from 'app/me/memberships/user-memberships.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { firstValueFrom } from 'rxjs';
import { RecentConsultationComponent } from './widget/recent-consultation/recent-consultation.component';
import { UserFavouritesComponent } from './widget/user-favourites/user-favourites.component';
import { UserTimelineComponent } from './widget/user-timeline/user-timeline.component';
import { VisitedGroupsComponent } from './widget/visited-groups/visited-groups.component';

@Component({
  selector: 'cbc-user-dashboard',
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.scss',
  preserveWhitespaces: true,
  imports: [
    UserFavouritesComponent,
    RecentConsultationComponent,
    VisitedGroupsComponent,
    UserTimelineComponent,
    UserMembershipsComponent,
    UserEventsComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class UserDashboardComponent implements OnInit {
  public timeline!: string[];
  public memberships!: InterestGroupProfile[];
  public loadingMemberships = false;

  public constructor(
    private loginService: LoginService,
    private userService: UserService
  ) {}

  public async ngOnInit() {
    await this.loadMemberships(this.getUserId());
  }

  private async loadMemberships(userId: string) {
    this.loadingMemberships = true;
    this.memberships = await firstValueFrom(
      this.userService.getUserMembership(userId)
    );
    this.loadingMemberships = false;
  }

  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }
}
