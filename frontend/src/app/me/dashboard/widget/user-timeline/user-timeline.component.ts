import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  resource,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { DashboardService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { TimelineStepComponent } from './timeline-step/timeline-step.component';

/**
 * Dashboard widget that renders the current user's activity timeline for a
 * given time window.
 *
 * On initialization it fetches the authenticated user's news feed from the
 * backend for the period selected via the {@link UserTimelineComponent.when}
 * input and displays each entry as a {@link TimelineStepComponent}. A
 * {@link SpinnerComponent} is shown while the feed is loading, and an error
 * state is exposed when the fetch fails.
 *
 * Key collaborators:
 * - {@link DashboardService} — retrieves the user news feed from the CIRCABC API.
 * - {@link LoginService} — provides the currently authenticated username.
 */
@Component({
  selector: 'cbc-user-timeline',
  templateUrl: './user-timeline.component.html',
  styleUrl: './user-timeline.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [SpinnerComponent, TimelineStepComponent, TranslocoModule],
})
export class UserTimelineComponent {
  /** Service used to fetch the user news feed from the CIRCABC backend. */
  private readonly dashboardService = inject(DashboardService);
  /** Service used to resolve the currently authenticated username. */
  private readonly loginService = inject(LoginService);

  /**
   * Required input selecting the time window for the timeline feed.
   *
   * Accepted values:
   * - `'today'` — activity from the current day.
   * - `'week'` — activity from the current week.
   * - `'previousWeek'` — activity from the previous week.
   */
  readonly when = input.required<'today' | 'week' | 'previousWeek'>();

  /**
   * The user news feed, loaded reactively for the currently authenticated user
   * and the selected {@link when} window. The request re-runs automatically
   * whenever {@link when} changes.
   */
  private readonly userFeedResource = resource({
    params: () => this.when(),
    loader: ({ params: when }) =>
      this.dashboardService.getUserNewsFeedAsync({
        userId: this.loginService.getCurrentUsername(),
        when,
      }),
  });

  /** The news feed data fetched for the current user and selected period. */
  public readonly userFeed = computed(() =>
    this.userFeedResource.hasValue() ? this.userFeedResource.value() : undefined
  );
  /** Whether the news feed is currently being loaded. */
  public readonly loading = this.userFeedResource.isLoading;
  /** Whether an error occurred while loading the news feed. */
  public readonly error = computed(
    () => this.userFeedResource.status() === 'error'
  );
}
