import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { TranslocoService } from '@jsverse/transloco';

import {
  DashboardService,
  GroupDashboard,
  InterestGroup,
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
import { RecentDiscussionsComponent } from './recent-discussions/recent-discussions.component';

/**
 * Dashboard view for an interest group.
 *
 * Renders the landing page of a group workspace, composing several dashlets
 * (timeline / what's-new, members, events, recent forum discussions) together
 * with the group location breadcrumb, descriptor and contact description.
 *
 * On initialization it reads the resolved {@link InterestGroup} from the parent
 * route data and, based on the current user's per-service permissions, decides
 * which dashlet boxes to display. When the "what's new" box is enabled it
 * fetches the aggregated {@link GroupDashboard} timeline from the backend.
 *
 * Key collaborators:
 * - {@link ActivatedRoute}: supplies the resolved group via parent route data.
 * - {@link DashboardService}: generated API client used to load the dashboard timeline.
 * - {@link UiMessageService}: surfaces error notifications to the user.
 * - {@link TranslocoService}: resolves i18n message keys.
 */
@Component({
  selector: 'cbc-group-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  private readonly route = inject(ActivatedRoute);
  private readonly dashboardService = inject(DashboardService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translateService = inject(TranslocoService);

  /** Aggregated dashboard data (timeline / what's-new) loaded from the backend. */
  public readonly timeline = signal<GroupDashboard | undefined>(undefined);
  /** The interest group currently being displayed, resolved from parent route data. */
  public readonly group = signal<InterestGroup | undefined>(undefined);
  /** Identifier of the current interest group. */
  public readonly igId = signal('');
  /** Whether an asynchronous group/dashboard load is in progress (drives the loader UI). */
  public readonly loading = signal(false);

  /** Whether the members dashlet is shown (true when the user has directory access). */
  public readonly displayMembersBox = signal(false);
  /** Whether the events dashlet is shown (true when the user has event access). */
  public readonly displayEventsBox = signal(false);
  /** Whether the recent-discussions/forums dashlet is shown (true when the user has newsgroup access). */
  public readonly displayForumsBox = signal(false);
  /** Whether the "what's new" timeline dashlet is shown (true when the user has information or library access). */
  public readonly displayWhatsnewBox = signal(false);

  /**
   * Angular lifecycle hook. Subscribes to the parent route data and loads the
   * resolved interest group when it becomes available.
   */
  public ngOnInit() {
    this.route.parent?.data.subscribe(async (data) => {
      await this.loadGroup(data.group);
    });
  }

  /**
   * Stores the given group, derives which dashlet boxes should be visible from
   * the group's per-service permissions and, when applicable, loads the
   * dashboard timeline. Toggles {@link loading} around the operation.
   *
   * @param group - The interest group to display on the dashboard.
   * @returns A promise that resolves once the group state and dashboard data have been loaded.
   */
  private async loadGroup(group: InterestGroup) {
    this.loading.set(true);
    this.group.set(group);
    this.igId.set(group.id as string);

    this.displayWhatsnewBox.set(
      group.permissions.information !== 'InfNoAccess' ||
        group.permissions.library !== 'LibNoAccess'
    );
    this.displayMembersBox.set(group.permissions.directory !== 'DirNoAccess');
    this.displayForumsBox.set(group.permissions.newsgroup !== 'NwsNoAccess');
    this.displayEventsBox.set(group.permissions.event !== 'EveNoAccess');

    if (this.displayWhatsnewBox()) {
      await this.loadDashboard(this.igId());
    }
    this.loading.set(false);
  }

  /**
   * Fetches the aggregated dashboard timeline for the given group from the
   * backend and stores it in {@link timeline}. On failure it falls back to an
   * empty timeline and displays a localized error message.
   *
   * @param groupId - Identifier of the interest group whose dashboard is loaded.
   * @returns A promise that resolves once the dashboard has been loaded or the error handled.
   */
  private async loadDashboard(groupId: string) {
    try {
      this.timeline.set(
        await this.dashboardService.getGroupDashboardAsync({ id: groupId })
      );
    } catch (_error) {
      this.timeline.set({});
      const res = this.translateService.translate('error.dashboard.read');
      this.uiMessageService.addErrorMessage(res);
    }
  }
}
