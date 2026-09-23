import {
  ChangeDetectionStrategy,
  Component,
  inject,
  resource,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { InterestGroupProfile, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UserEventsComponent } from 'app/me/events/user-events.component';
import { UserMembershipsComponent } from 'app/me/memberships/user-memberships.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { RecentConsultationComponent } from './widget/recent-consultation/recent-consultation.component';
import { UserFavouritesComponent } from './widget/user-favourites/user-favourites.component';
import { UserTimelineComponent } from './widget/user-timeline/user-timeline.component';
import { VisitedGroupsComponent } from './widget/visited-groups/visited-groups.component';

/**
 * Standalone dashboard component for the current user's personal area (`/me`).
 *
 * Rendered under the `cbc-user-dashboard` selector, it acts as the container
 * that composes the various personal dashboard widgets: favourites, recent
 * consultations, visited groups, an activity timeline, group memberships and
 * upcoming events. It resolves the current user's identity (via
 * {@link LoginService}) and loads that user's interest group memberships
 * (via {@link UserService}, through a `resource()`) so the child widgets can
 * display personalised data.
 *
 * Key collaborators:
 * - {@link LoginService} — provides the currently authenticated username.
 * - {@link UserService} — generated CIRCABC API client used to fetch the
 *   user's interest group memberships.
 */
@Component({
  selector: 'cbc-user-dashboard',
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
export class UserDashboardComponent {
  /** Service used to resolve the identity of the currently logged-in user. */
  private readonly loginService = inject(LoginService);
  /** Generated CIRCABC API client used to fetch user-specific data. */
  private readonly userService = inject(UserService);

  /** Activity timeline entries displayed for the current user. */
  public timeline!: string[];

  /**
   * Loads the current user's interest group memberships. Errors are caught
   * and logged so the resource never enters the error state, since the
   * template has no dedicated error UI for this widget.
   */
  private readonly membershipsResource = resource({
    loader: async () => {
      try {
        return await this.userService.getUserMembershipAsync({
          userId: this.getUserId(),
        });
      } catch (e) {
        console.error(e);
        return [];
      }
    },
    defaultValue: [] as InterestGroupProfile[],
  });

  /** Interest group profiles the current user is a member of. */
  public readonly memberships = this.membershipsResource.value;
  /** Whether the memberships request is currently in flight. */
  public readonly loadingMemberships = this.membershipsResource.isLoading;

  /**
   * Resolves the identifier (username) of the currently authenticated user.
   *
   * @returns The current user's username.
   */
  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }
}
