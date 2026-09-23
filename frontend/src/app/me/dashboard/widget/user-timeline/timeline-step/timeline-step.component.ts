import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { UserNewsFeed } from 'app/core/generated/circabc';
import { GroupStepComponent } from 'app/me/dashboard/widget/user-timeline/group-step/group-step.component';
import { SummaryBoxComponent } from 'app/me/dashboard/widget/user-timeline/summary-box/summary-box.component';

/**
 * Renders a single step within the user timeline widget on the personal
 * dashboard.
 *
 * A timeline step represents the aggregated activity for a given point in
 * time ({@link TimelineStepComponent.when}). It displays a summary box
 * (`cbc-summary-box`) followed by one `cbc-group-step` per group that has
 * feed activity, derived from the supplied {@link UserNewsFeed}.
 */
@Component({
  selector: 'cbc-timeline-step',
  templateUrl: './timeline-step.component.html',
  styleUrl: './timeline-step.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SummaryBoxComponent, GroupStepComponent, TranslocoModule],
})
export class TimelineStepComponent {
  /**
   * Required input holding the news feed for this timeline step, grouping
   * the per-group activity to render.
   */
  readonly userFeed = input.required<UserNewsFeed>();

  /**
   * Required input describing the point in time this step represents,
   * used as the timeline label (e.g. a date or relative time string).
   */
  readonly when = input.required<string>();

  /**
   * Determines whether the current feed contains group activity to display.
   *
   * @returns `true` when the {@link userFeed} has at least one group feed;
   *          `false` when there are no group feeds or the feed is absent.
   */
  isEmptyFeed(): boolean {
    const userFeed = this.userFeed();
    if (userFeed?.groupFeeds) {
      return userFeed.groupFeeds.length > 0;
    }

    return false;
  }
}
