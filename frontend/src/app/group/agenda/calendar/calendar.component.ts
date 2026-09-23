import { DatePipe, I18nSelectPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnChanges,
  output,
  resource,
  SimpleChange,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  EventItemDefinition,
  EventsService,
  NodesService,
} from 'app/core/generated/circabc';
import { LocalizationService } from 'app/core/localization.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { TimeZoneHelperService } from 'app/core/timezone-helper.service';
import {
  convertDate,
  eventsStartTimeComparator,
  getFullDate,
  translateOccurrenceRate,
} from 'app/core/util';

/**
 * Describes a single cell (day) rendered in the calendar grid.
 */
interface CalendarDayType {
  /**
   * Where the day falls relative to the displayed month:
   * `'previousMonth'`, `'currentMonth'` or `'nextMonth'`.
   */
  type: string;
  /** The calendar date represented by this cell. */
  date: Date;
  /** Events occurring on this day, sorted by their start time. */
  events: EventItemDefinition[];
  /** Whether the hover/details popup for this day is currently shown. */
  showDetailsBox: boolean;
}

/**
 * Month-view calendar component for an interest group's agenda.
 *
 * Renders a full month grid (including the trailing days of the previous
 * month and the leading days of the next month needed to complete the
 * first and last weeks) and displays the events that occur on each day.
 * Events are fetched for the visible date range from {@link EventsService},
 * converted to the user's local time zone via {@link TimeZoneHelperService},
 * and sorted by start time.
 *
 * The component also exposes hooks for creating and deleting events and
 * signals its host when month processing starts/completes so surrounding
 * UI can react (e.g. show loading state or refresh).
 *
 * Key collaborators: {@link EventsService} (event retrieval),
 * {@link NodesService} (event root node lookup),
 * {@link PermissionEvaluatorService} (admin checks),
 * {@link LocalizationService} (day names/localization),
 * {@link TimeZoneHelperService} (time zone conversion) and
 * {@link UiMessageService} (error reporting).
 */
@Component({
  selector: 'cbc-calendar',
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, I18nSelectPipe, TranslocoModule],
})
export class CalendarComponent implements OnChanges {
  private readonly eventsService = inject(EventsService);
  private readonly nodesService = inject(NodesService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly uiMessageService = inject(UiMessageService);
  protected localizationService = inject(LocalizationService);
  protected timeZoneHelperService = inject(TimeZoneHelperService);

  /**
   * Localized short day names, ordered starting from Sunday, used to
   * render the calendar column headers.
   *
   * @returns The list of localized short weekday names.
   */
  public get dayNames() {
    return this.localizationService.getDayNames('Sunday', 'short');
  }

  /**
   * Two-way bound model holding the date whose month is currently
   * displayed. Changing it drives which month the grid renders.
   */
  public date = model<Date>(new Date());
  /**
   * Emits the day the user selected when requesting to create a new event,
   * carrying the target calendar date.
   */
  public readonly popupCreateEventEmitter = output<Date>();
  /** Emits the event the user requested to delete. */
  public readonly popupDeleteEventEmitter = output<EventItemDefinition>();
  /** Required identifier of the interest group whose events are shown. */
  public readonly igId = input.required<string>();
  /**
   * Optional identifier of the node acting as the events root, used for
   * permission checks and node lookup.
   */
  public readonly eventRootId = input<string>();
  // property used to fire the ngOnChanges event when toggled for redisplay (new event/meeting has been added)
  /**
   * Toggle input whose change triggers {@link ngOnChanges} so the calendar
   * re-displays (e.g. after a new event/meeting has been added).
   */
  public readonly redisplay = input.required<boolean>();

  /**
   * Resource that loads the events root node whenever {@link eventRootId}
   * changes. When no id is provided the loader does not run and the value
   * stays `undefined`.
   */
  private readonly eventRootResource = resource({
    params: () => this.eventRootId(),
    loader: ({ params }) => this.nodesService.getNodeAsync({ id: params }),
  });

  /**
   * The resolved events root node, loaded reactively from {@link eventRootId}.
   * Exposed as the resource's writable value signal, so it can also be set
   * locally (e.g. in tests or optimistic updates).
   */
  public readonly eventRootNode = this.eventRootResource.value;

  /** First calendar day of the currently displayed month. */
  private firstOfMonth!: Date;
  /** Last calendar day of the currently displayed month. */
  private lastOfMonth!: Date;

  /** The days rendered in the grid, including padding days from adjacent months. */
  public calendarDays = signal<CalendarDayType[]>([]);
  /** Today's date normalized to midnight, used to highlight "today". */
  private todaysDate!: Date;

  /** Whether the calendar is currently loading/building its data. */
  public processing = false;
  /** Emits a granularity tag (e.g. `'month'`) once processing completes. */
  public readonly processingEventEmitter = output<string>();

  /**
   * Angular lifecycle hook. Rebuilds the calendar days whenever a bound
   * input changes (including the {@link redisplay} toggle).
   *
   * @param _changes The set of changed input properties (unused).
   * @returns A promise that resolves once the days have been rebuilt.
   */
  public ngOnChanges(_changes: { [propKey: string]: SimpleChange }) {
    void this.handleChanges();
  }

  private async handleChanges() {
    await this.initDays();
  }

  /**
   * Initializes the month boundaries and today's date, then builds the
   * calendar grid for the currently selected {@link date}.
   *
   * @returns A promise that resolves once the calendar days are built.
   */
  public async initDays() {
    const todaysDateTime = new Date();
    this.todaysDate = new Date(
      todaysDateTime.getFullYear(),
      todaysDateTime.getMonth(),
      todaysDateTime.getDate()
    );
    this.firstOfMonth = new Date(
      this.date().getFullYear(),
      this.date().getMonth(),
      1
    );
    this.lastOfMonth = new Date(
      this.date().getFullYear(),
      this.date().getMonth() + 1,
      0
    );
    await this.getCalendarDays();
  }

  /**
   * Determines whether a calendar cell represents the current day.
   *
   * @param item The calendar day cell to test.
   * @returns `true` if the cell's date is today, otherwise `false`.
   */
  public isToday(item: CalendarDayType): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const givenDay = new Date(item.date);
    givenDay.setHours(0, 0, 0, 0);
    return today.getTime() === givenDay.getTime();
  }

  /**
   * Determines whether a calendar cell falls on a weekend.
   *
   * @param item The calendar day cell to test.
   * @returns `true` if the date is a Saturday or Sunday, otherwise `false`.
   */
  public isWeekendDay(item: CalendarDayType) {
    const weekDay: number = item.date.getDay();
    return weekDay === 0 || weekDay === 6;
  }

  /**
   * Fetches the interest group's events occurring within the given range.
   *
   * @param startDate The inclusive start of the range.
   * @param endDate The inclusive end of the range.
   * @returns A promise resolving to the events within the range.
   */
  public async getEvents(startDate: Date, endDate: Date) {
    return await this.eventsService.getInterestGroupEventsAsync({
      id: this.igId(),
      startDate: getFullDate(startDate),
      endDate: getFullDate(endDate),
    });
  }

  // collect the events that happen on the given day
  /**
   * Filters the supplied events down to those occurring on the given date.
   *
   * @param collectedEvents The pool of events to search.
   * @param date The day for which to collect events.
   * @returns The events whose appointment date matches the given day.
   */
  private getEventsForDate(collectedEvents: EventItemDefinition[], date: Date) {
    const events: EventItemDefinition[] = [];

    for (const event of collectedEvents) {
      if (
        event.appointmentDate?.toString() === convertDate(date.toDateString())
      ) {
        events.push(event);
      }
    }

    return events;
  }

  /**
   * Builds the full set of calendar day cells for the visible month,
   * including padding days from the previous and next months, fetches and
   * time-zone-adjusts the events for the whole range, assigns them to their
   * days and sorts them. Updates {@link processing} and emits on
   * {@link processingEventEmitter} when done. Any API error is surfaced via
   * {@link UiMessageService}.
   *
   * @returns A promise that resolves once {@link calendarDays} is populated.
   */
  private async getCalendarDays() {
    try {
      this.processing = true;

      const calDays: CalendarDayType[] = [];

      // get the last days of the previous month and events
      const previousMonthDaysForFirstWeek: Date[] =
        this.previousMonthDaysForFirstWeek();
      const nextMonthDaysForLastWeek: Date[] = this.nextMonthDaysForLastWeek();

      let firstCalendarDay: Date = previousMonthDaysForFirstWeek[0];

      firstCalendarDay ??= this.firstOfMonth;

      const lastCalendarDay: Date =
        nextMonthDaysForLastWeek.at(-1) ?? this.lastOfMonth;

      lastCalendarDay.setHours(23, 59, 59, 999);

      let collectedEvents: EventItemDefinition[] = await this.getEvents(
        firstCalendarDay,
        lastCalendarDay
      );
      collectedEvents =
        this.timeZoneHelperService.toLocalDateTime(collectedEvents);
      for (const d of previousMonthDaysForFirstWeek) {
        const dayEvents: EventItemDefinition[] = this.getEventsForDate(
          collectedEvents,
          d
        );
        calDays.push({
          type: 'previousMonth',
          date: d,
          events: dayEvents,
          showDetailsBox: false,
        });
      }

      // get the days of the month and events
      const tmpDate = new Date(this.firstOfMonth);
      while (tmpDate <= this.lastOfMonth) {
        const dayEvents2: EventItemDefinition[] = this.getEventsForDate(
          collectedEvents,
          tmpDate
        );
        calDays.push({
          type: 'currentMonth',
          date: new Date(tmpDate),
          events: dayEvents2,
          showDetailsBox: false,
        });
        tmpDate.setDate(tmpDate.getDate() + 1);
      }

      // get the first days of the next month and events
      for (const d of nextMonthDaysForLastWeek) {
        const dayEvents3: EventItemDefinition[] = this.getEventsForDate(
          collectedEvents,
          d
        );
        calDays.push({
          type: 'nextMonth',
          date: d,
          events: dayEvents3,
          showDetailsBox: false,
        });
      }

      this.sortCalDaysEvents(calDays);
      this.calendarDays.set(calDays);

      this.processing = false;
      this.processingEventEmitter.emit('month');
    } catch (error) {
      const jsonError = JSON.parse(error.error) as Record<string, unknown>;
      if (jsonError && 'message' in jsonError) {
        this.uiMessageService.addErrorMessage(jsonError.message as string);
      }
    }
  }

  /**
   * Sorts each day's events in place by their start time.
   *
   * @param calDays The calendar day cells whose events should be sorted.
   */
  private sortCalDaysEvents(calDays: CalendarDayType[]) {
    for (const calDay of calDays) {
      calDay.events.sort(eventsStartTimeComparator);
    }
  }

  /**
   * Computes the trailing days of the previous month needed to fill the
   * first week of the grid (so the month always starts on a Monday).
   *
   * @returns The previous-month dates that pad the first week; empty when
   * the month already starts on a Monday.
   */
  private previousMonthDaysForFirstWeek(): Date[] {
    const lastMonthDays: Date[] = [];

    if (this.firstOfMonth.getDay() !== 1) {
      let mondayOfWeek = new Date(
        this.firstOfMonth.getFullYear(),
        this.firstOfMonth.getMonth(),
        this.firstOfMonth.getDate() - this.firstOfMonth.getDay() + 1
      );

      if (this.firstOfMonth.getDay() === 0) {
        const sixDays = 6;
        mondayOfWeek = new Date(
          this.firstOfMonth.getFullYear(),
          this.firstOfMonth.getMonth(),
          this.firstOfMonth.getDate() - sixDays
        );
      }

      const tmpDate = mondayOfWeek;
      while (tmpDate < this.firstOfMonth) {
        lastMonthDays.push(new Date(tmpDate));
        tmpDate.setDate(tmpDate.getDate() + 1);
      }
    }

    return lastMonthDays;
  }

  /**
   * Computes the leading days of the next month needed to fill the last
   * week of the grid (so the month always ends on a Sunday).
   *
   * @returns The next-month dates that pad the last week; empty when the
   * month already ends on a Sunday.
   */
  private nextMonthDaysForLastWeek(): Date[] {
    const nextMonthDays: Date[] = [];

    if (this.lastOfMonth.getDay() !== 0) {
      const tmpDate = new Date(this.lastOfMonth);
      tmpDate.setDate(tmpDate.getDate() + 1);
      while (tmpDate.getDay() !== 0) {
        nextMonthDays.push(new Date(tmpDate));
        tmpDate.setDate(tmpDate.getDate() + 1);
      }
      nextMonthDays.push(new Date(tmpDate));
    }

    return nextMonthDays;
  }

  /**
   * Used to check if we should display the + sign on the date for the
   * events to avoid to allow creating an event before in time.
   *
   * @param calendarSheetDate The date of the calendar cell to test.
   * @returns `true` if the date is today or in the future, otherwise `false`.
   */
  public checkIfDateAfterToday(calendarSheetDate: Date): boolean {
    return this.todaysDate <= calendarSheetDate;
  }

  /**
   * Requests creation of a new event on the given day by emitting on
   * {@link popupCreateEventEmitter}.
   *
   * @param calendarSheetDate The date on which to create the event.
   */
  public onPopupCreateEventWithCalendarDate(calendarSheetDate: Date): void {
    this.popupCreateEventEmitter.emit(calendarSheetDate);
  }

  /**
   * Requests deletion of the given event by emitting on
   * {@link popupDeleteEventEmitter}.
   *
   * @param event The event to delete.
   */
  public deleteEvent(event: EventItemDefinition) {
    this.popupDeleteEventEmitter.emit(event);
  }

  /**
   * Shows the details popup for the calendar cell matching the given date,
   * but only when that day has events.
   *
   * @param date The date identifying the calendar cell.
   */
  public enableDetailsBox(date: Date) {
    for (const calendarDay of this.calendarDays()) {
      if (calendarDay.date === date && calendarDay.events.length > 0) {
        calendarDay.showDetailsBox = true;
        return;
      }
    }
  }

  /**
   * Hides the details popup for the calendar cell matching the given date.
   *
   * @param date The date identifying the calendar cell.
   */
  public disableDetailsBox(date: Date) {
    for (const calendarDay of this.calendarDays()) {
      if (calendarDay.date === date) {
        calendarDay.showDetailsBox = false;
        return;
      }
    }
  }

  /**
   * Translates a recurrence/occurrence rate code into human-readable,
   * localized labels.
   *
   * @param occurrenceRate The occurrence rate code, if any.
   * @returns The translated occurrence description parts.
   */
  public getRepetition(occurrenceRate: string | undefined): string[] {
    return translateOccurrenceRate(occurrenceRate);
  }

  /**
   * Determines whether the current user has event administration rights on
   * the events root node.
   *
   * @returns `true` if the user is an event administrator, otherwise `false`.
   */
  public isEveAdmin(): boolean {
    const eventRootNode = this.eventRootNode();
    if (eventRootNode === undefined) {
      return false;
    }
    return this.permEvalService.isEveAdmin(eventRootNode);
  }
}
