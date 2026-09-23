import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  resource,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { EventItemDefinition, EventsService } from 'app/core/generated/circabc';
import { getFullDate } from 'app/core/util';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { DatePipe } from 'app/shared/pipes/date.pipe';
import { TimePipe } from 'app/shared/pipes/time.pipe';

/**
 * Dashboard dashlet that displays the upcoming events (agenda items) of an
 * interest group.
 *
 * On initialization it fetches the events occurring within the next year for
 * the group identified by {@link EventsDashletComponent.igId} and renders them
 * as a list, showing a loader while the request is in flight and an error
 * state if the REST call fails. By default only the first eight events are
 * shown; the {@link EventsDashletComponent.more} flag controls whether the
 * full list is displayed. Each entry links to the relevant agenda route.
 *
 * Key collaborator: {@link EventsService} (generated CIRCABC API client) used
 * to retrieve the group's events.
 */
@Component({
  selector: 'cbc-events-dashlet',
  templateUrl: './events-dashlet.component.html',
  styleUrl: './events-dashlet.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    DatePipe,
    TimePipe,
    TranslocoModule,
  ],
})
export class EventsDashletComponent {
  /** Generated CIRCABC API client used to fetch the interest group's events. */
  private readonly eventsService = inject(EventsService);

  /**
   * Required input holding the identifier of the interest group whose events
   * should be displayed.
   */
  public readonly igId = input.required<string>();

  /**
   * Events for the interest group occurring between now and one year from
   * today, loaded reactively. Stays idle while {@link igId} is empty and
   * re-runs whenever {@link igId} changes.
   */
  private readonly eventsResource = resource({
    params: () => this.igId() || undefined,
    loader: ({ params: id }) => {
      const startDate: Date = new Date();
      const endDate: Date = new Date();
      const intervalInDays = 365;
      endDate.setDate(endDate.getDate() + intervalInDays);
      return this.eventsService.getInterestGroupEventsAsync({
        id,
        startDate: getFullDate(startDate),
        endDate: getFullDate(endDate),
      });
    },
  });

  /** List of events fetched for the interest group. */
  public readonly events = computed<EventItemDefinition[]>(() =>
    this.eventsResource.hasValue() ? this.eventsResource.value() : []
  );
  /** Whether the full list of events is shown (`true`) or only the first eight (`false`). */
  public more = false;
  /** Whether an event fetch is currently in progress (drives the loader). */
  public readonly loading = this.eventsResource.isLoading;
  /** Whether the last REST call to fetch events failed. */
  public readonly restCallError = computed(
    () => this.eventsResource.status() === 'error'
  );

  /**
   * Returns the events to render in the dashlet.
   *
   * When {@link EventsDashletComponent.more} is `true` the complete list of
   * events is returned; otherwise the result is capped at the first eight
   * events.
   *
   * @returns The (possibly truncated) list of events to display.
   */
  public getListOfEvents(): EventItemDefinition[] {
    let result: EventItemDefinition[] = [];

    if (this.more) {
      result = this.events();
    } else {
      const events = this.events();
      for (let i = 0; i < events.length && i < 8; i += 1) {
        result.push(events[i]);
      }
    }

    return result;
  }
}
