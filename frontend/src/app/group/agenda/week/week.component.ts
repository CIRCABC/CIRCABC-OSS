import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  output,
  SimpleChange,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  EventItemDefinition,
  EventsService,
  UserService,
} from 'app/core/generated/circabc';
import { LocalizationService } from 'app/core/localization.service';
import { LoginService } from 'app/core/login.service';
import { TimeZoneHelperService } from 'app/core/timezone-helper.service';
import {
  convertDate,
  eventsStartTimeComparator,
  getFullDate,
  padWithLeadingZero as padWithLeadingZeroGlobal,
  translateOccurrenceRate,
} from 'app/core/util';

/**
 * Groups the events that occur on a single day of the displayed week.
 */
interface CalendarDayType {
  /** The calendar date this bucket represents. */
  date: Date;
  /** The events scheduled on {@link CalendarDayType.date}. */
  events: EventItemDefinition[];
  /** Whether the day's details popover box should be shown. */
  showDetailsBox: boolean;
}
/**
 * Wraps an event together with a flag indicating whether it should be
 * rendered in the current cell. Used to avoid rendering the same
 * multi-hour event more than once within a day column.
 */
interface ShowableEventItemDefinition {
  /** The wrapped calendar event. */
  event: EventItemDefinition;
  /** Whether this occurrence of the event should be visibly rendered. */
  show: boolean;
}
/**
 * A single cell/row element of the week grid model consumed by the
 * template. Depending on {@link ViewRowElement.isHour} it either holds the
 * hour label (as a string) or the list of showable events for a day/hour
 * slot.
 */
interface ViewRowElement {
  /** True when this element represents the hour label column. */
  isHour: boolean;
  /** True when the element's day corresponds to the current day. */
  isToday: boolean;
  /** True when the element's day is a weekend day (Saturday/Sunday). */
  isWeekendDay: boolean;
  /** Per-event occurrence counter, keyed by event id, used to dedupe rendering. */
  multiplicity: Map<string, number>;
  /** The hour label (string) for hour rows, or the showable events for day rows. */
  value: string | ShowableEventItemDefinition[];
  /** Zero-based index of the day within the displayed week. */
  day: number;
  /** Day-of-month number for the element's day. */
  dayDate: number;
  /** Localized full name of the element's day. */
  dayName: string;
  /** Whether the details popover box should be shown for this element. */
  showDetailsBox: boolean;
}

/**
 * Renders the weekly view of the agenda/calendar as an hour-by-day grid.
 *
 * For a given anchor {@link WeekComponent.date} the component computes the
 * bounds of the (work) week, fetches the relevant events either for the
 * current user (`meMode`) or for an interest group, and lays them out into
 * a flat list of {@link ViewRowElement}s consumed by the template. Multi-hour
 * events are deduplicated so they render once per day column.
 *
 * Collaborates with {@link UserService} and {@link EventsService} to load
 * events, {@link LoginService} to resolve the current user, and
 * {@link LocalizationService} / {@link TimeZoneHelperService} for localized
 * day names and local-time conversion.
 */
@Component({
  selector: 'cbc-week',
  templateUrl: './week.component.html',
  styleUrl: './week.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, TranslocoModule],
})
export class WeekComponent implements OnChanges {
  private readonly userService = inject(UserService);
  private readonly eventsService = inject(EventsService);
  private readonly loginService = inject(LoginService);
  private readonly localizationService = inject(LocalizationService);
  private readonly timeZoneHelperService = inject(TimeZoneHelperService);

  /** Required anchor date; any date within the week to be displayed. */
  public readonly date = input.required<Date>();
  /** Optional interest group id whose events are loaded when not in `meMode`. */
  public readonly id = input<string>();
  /** When true, loads the current user's personal events instead of a group's. */
  public readonly meMode = input(false);
  // property used to fire the ngOnChanges event when toggled for redisplay (new event/meeting has been added)
  /**
   * Toggle input whose change triggers {@link WeekComponent.ngOnChanges} to
   * refresh the view (e.g. after a new event/meeting has been added).
   */
  public readonly redisplay = input<boolean>();
  /** Required first hour (0-23) rendered at the top of the grid. */
  public readonly displayFromHour = input.required<number>();
  /** Required last hour (0-23) rendered at the bottom of the grid. */
  public readonly displayToHour = input.required<number>();
  /** Required flag selecting a 5-day work week (true) or full 7-day week. */
  public readonly workWeek = input.required<boolean>();
  /** Emits after events have finished processing; payload is the string `'week'`. */
  public readonly processingEventEmitter = output<string>();

  /** Monday of the displayed week. */
  private firstOfWeek!: Date;
  /** Sunday of the displayed week. */
  private lastOfWeek!: Date;
  /** Friday of the displayed week (last day of the work week). */
  private lastOfWorkWeek!: Date;

  /** The seven consecutive dates of the displayed week, starting on Monday. */
  public weekDates!: Date[];

  /** Localized full day names starting from Monday. */
  public get dayNames() {
    return this.localizationService.getDayNames('Monday', 'full');
  }

  /** Flattened grid model rendered by the template. */
  public viewRowElements = signal<ViewRowElement[]>([]);

  /** Events grouped per day of the displayed week. */
  public eventsPerWeekDays!: CalendarDayType[];

  /**
   * Angular lifecycle hook. Rebuilds the week grid whenever a bound input
   * changes.
   *
   * @param _changes Map of changed input properties (unused).
   * @returns A promise that resolves once the days and events are initialized.
   */
  public ngOnChanges(_changes: { [propKey: string]: SimpleChange }) {
    void this.handleChanges();
  }

  private async handleChanges() {
    await this.initDays();
  }

  /**
   * Computes the week bounds (Monday/Sunday/Friday) from the anchor date,
   * builds the list of {@link WeekComponent.weekDates} and then loads the
   * corresponding events.
   *
   * @returns A promise that resolves once weekly events have been loaded.
   */
  private async initDays() {
    // monday
    this.firstOfWeek = new Date(this.date());
    const date = this.date();
    this.firstOfWeek.setDate(this.firstOfWeek.getDate() - date.getDay() + 1);
    // sunday
    this.lastOfWeek = new Date(date);
    this.lastOfWeek.setDate(
      this.lastOfWeek.getDate() + (6 - date.getDay()) + 1
    );
    // friday
    this.lastOfWorkWeek = new Date(date);
    this.lastOfWorkWeek.setDate(
      this.lastOfWeek.getDate() + (6 - date.getDay()) - 1
    );

    // get the day numbers of the week
    this.weekDates = [];
    let weekDate = this.firstOfWeek;
    let dayIncrement = 0;
    while (dayIncrement < 7) {
      this.weekDates.push(weekDate);
      dayIncrement += 1;
      const newDate: Date = new Date(this.firstOfWeek);
      newDate.setDate(this.firstOfWeek.getDate() + dayIncrement);
      weekDate = newDate;
    }

    await this.getWeeklyEvents();
  }

  // collect the events that happen on the given day
  /**
   * Filters the supplied events down to those whose appointment date matches
   * the given day.
   *
   * @param collectedEvents The pool of events to filter.
   * @param date The day to match events against.
   * @returns The events that occur on `date`.
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
   * @returns The username of the currently logged-in user.
   */
  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }

  /**
   * Loads the events for the displayed week (personal events in `meMode`, or
   * the interest group's events otherwise), converts them to local time,
   * groups them per day, builds the view rows and finally notifies listeners
   * via {@link WeekComponent.processingEventEmitter}.
   *
   * @returns A promise that resolves once the weekly events are processed.
   */
  private async getWeeklyEvents() {
    let events: EventItemDefinition[] = [];

    if (this.meMode()) {
      events = await this.userService.getUserEventsPeriodAsync({
        userId: this.getUserId(),
        exactDate: getFullDate(this.firstOfWeek),
        period: 'Future',
      });
    } else {
      const id = this.id();
      if (id !== undefined) {
        events = await this.eventsService.getInterestGroupEventsAsync({
          id,
          startDate: getFullDate(this.firstOfWeek),
          endDate: getFullDate(this.lastOfWeek),
        });
      }
    }

    events = this.timeZoneHelperService.toLocalDateTime(events);

    this.eventsPerWeekDays = [];

    for (const weekDate of this.weekDates) {
      const dayEvents: EventItemDefinition[] = this.getEventsForDate(
        events,
        weekDate
      );
      this.eventsPerWeekDays.push({
        date: weekDate,
        events: dayEvents,
        showDetailsBox: false,
      });
    }

    this.getViewRows();

    this.processingEventEmitter.emit('week');
  }

  /**
   * Determines whether the given day index of the week is today.
   *
   * @param weekDay Zero-based index into {@link WeekComponent.weekDates}.
   * @returns True if the referenced day is the current day.
   */
  public isToday(weekDay: number) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const givenDay = this.weekDates[weekDay];
    givenDay.setHours(0, 0, 0, 0);
    return today === givenDay;
  }

  /**
   * @param weekDay Zero-based index into {@link WeekComponent.weekDates}.
   * @returns True if the referenced day is a weekend day (Saturday or Sunday).
   */
  public isWeekendDay(weekDay: number) {
    return weekDay === 5 || weekDay === 6;
  }

  // pad values with '0' to conform to the date and time formats
  /**
   * Left-pads a number with a leading zero to match date/time formatting.
   *
   * @param value The number to pad.
   * @returns The zero-padded string representation.
   */
  public padWithLeadingZero(value: number): string {
    return padWithLeadingZeroGlobal(value);
  }

  /**
   * Determines whether an event overlaps a specific day/hour slot of the grid.
   *
   * @param event The event to test.
   * @param weekDay Zero-based index into {@link WeekComponent.weekDates}.
   * @param hour The hour (0-23) of the slot.
   * @returns True if the event spans the given day and hour.
   */
  public isOnDate(event: EventItemDefinition, weekDay: number, hour: number) {
    if (
      event.startTime === undefined ||
      event.endTime === undefined ||
      event.appointmentDate === undefined
    ) {
      return false;
    }
    const eventDay = Number(event.appointmentDate.substring(8, 10));
    const eventStartHour = Number(event.startTime.substring(0, 2));
    const eventEndHour = Number(event.endTime.substring(0, 2));
    const eventEndMinutes = Number(event.endTime.substring(3, 5));
    return (
      eventStartHour <= hour &&
      (eventEndHour > hour || (eventEndHour === hour && eventEndMinutes > 0)) &&
      eventDay === this.weekDates[weekDay].getDate()
    );
  }

  // event.appointmentType === 'Event'
  /**
   * @param eid The event to test.
   * @returns True if the appointment type is `'Event'`.
   */
  public isEvent(eid: EventItemDefinition): boolean {
    return eid.appointmentType === 'Event';
  }

  /**
   * @param eid The event to test.
   * @returns True if the appointment type is `'Meeting'`.
   */
  public isMeeting(eid: EventItemDefinition): boolean {
    return eid.appointmentType === 'Meeting';
  }

  /**
   * @returns The zero-based day indices to display: `[0..4]` for a work week
   * or `[0..6]` for a full week.
   */
  public getDisplayableDays() {
    if (this.workWeek()) {
      return [0, 1, 2, 3, 4];
    }
    return [0, 1, 2, 3, 4, 5, 6];
  }

  /**
   * Builds {@link WeekComponent.viewRowElements} by iterating over the
   * configured hour range and appending the hour and day rows for each hour.
   */
  private getViewRows(): void {
    this.viewRowElements.set([]);
    const multiplicity = new Map<string, number>();
    const numberOfDays = this.workWeek() ? 5 : 7;

    for (
      let hour = this.displayFromHour();
      hour < this.displayToHour() + 1;
      hour++
    ) {
      this.addWeekRows(hour, numberOfDays, multiplicity);
    }
  }

  /**
   * Appends, for a single hour, the hour label row followed by one day row
   * per displayed day.
   *
   * @param hour The hour (0-23) being laid out.
   * @param numberOfDays The number of day columns (5 or 7).
   * @param multiplicity Shared per-event occurrence counter used for dedup.
   */
  private addWeekRows(
    hour: number,
    numberOfDays: number,
    multiplicity: Map<string, number>
  ) {
    let firstPass = true;

    for (let day = 0; day < numberOfDays; day++) {
      if (firstPass) {
        this.addHourRow(hour, day, multiplicity);
        firstPass = false;
      }

      const showableEvents = this.getShowableEventsForDay(
        day,
        hour,
        multiplicity
      );
      this.addDayRow(day, showableEvents, multiplicity);
    }
  }

  /**
   * Appends an hour-label {@link ViewRowElement} to the grid model.
   *
   * @param hour The hour (0-23) to display as the row label.
   * @param day Zero-based day index used to derive day metadata.
   * @param multiplicity Shared per-event occurrence counter.
   */
  private addHourRow(
    hour: number,
    day: number,
    multiplicity: Map<string, number>
  ) {
    this.viewRowElements.update((rows) => [
      ...rows,
      {
        isHour: true,
        multiplicity,
        value: this.padWithLeadingZero(hour),
        isToday: this.isToday(day),
        isWeekendDay: this.isWeekendDay(day),
        day,
        dayDate: this.weekDates[day].getDate(),
        dayName: this.dayNames[day],
        showDetailsBox: false,
      },
    ]);
  }

  /**
   * Appends a day {@link ViewRowElement} holding the showable events for a
   * day/hour slot.
   *
   * @param day Zero-based day index used to derive day metadata.
   * @param showableEvents The events to render in this slot.
   * @param multiplicity Shared per-event occurrence counter.
   */
  private addDayRow(
    day: number,
    showableEvents: ShowableEventItemDefinition[],
    multiplicity: Map<string, number>
  ) {
    this.viewRowElements.update((rows) => [
      ...rows,
      {
        isHour: false,
        multiplicity,
        value: showableEvents,
        isToday: this.isToday(day),
        isWeekendDay: this.isWeekendDay(day),
        day,
        dayDate: this.weekDates[day].getDate(),
        dayName: this.dayNames[day],
        showDetailsBox: false,
      },
    ]);
  }

  /**
   * Collects the events overlapping a given day/hour slot, flagging the first
   * occurrence of each event as visible (`show === true`) and subsequent
   * occurrences as hidden, then sorts them by start time.
   *
   * @param day Zero-based day index of the slot.
   * @param hour The hour (0-23) of the slot.
   * @param multiplicity Shared per-event occurrence counter, mutated here.
   * @returns The showable events for the slot, sorted by start time.
   */
  private getShowableEventsForDay(
    day: number,
    hour: number,
    multiplicity: Map<string, number>
  ): ShowableEventItemDefinition[] {
    if (!this.eventsPerWeekDays) return [];

    const showableEvents: ShowableEventItemDefinition[] = [];

    for (const eventPerWeekDay of this.eventsPerWeekDays) {
      for (const event of eventPerWeekDay.events) {
        if (this.isOnDate(event, day, hour)) {
          const eventId = event.id as string;
          const amount = (multiplicity.get(eventId) ?? 0) + 1;
          multiplicity.set(eventId, amount);
          showableEvents.push({ event, show: amount === 1 });
        }
      }
    }

    showableEvents.sort((e1, e2) =>
      eventsStartTimeComparator(e1.event, e2.event)
    );
    return showableEvents;
  }

  /**
   * Determines whether a day row contains an event that spans multiple
   * consecutive minute/hour ranges (i.e. a hidden occurrence whose event
   * appears more than once).
   *
   * @param item The grid element to inspect.
   * @returns True if the element covers multiple minute ranges.
   */
  public coversMultipleMinuteRanges(item: ViewRowElement): boolean {
    if (typeof item.value === 'string') {
      return false;
    }
    let result = false;
    for (const showableEvent of item.value) {
      const show: boolean = showableEvent.show;
      let multResult = true;
      if (!show) {
        const multiple: number = item.multiplicity.get(
          showableEvent.event.id as string
        ) as number;
        multResult = multiple > 1;
      }
      result = result || multResult;
    }
    return result;
  }

  /**
   * Determines whether the details popover box should be shown on the right
   * side for the given element (used for the first two day columns).
   *
   * @param item The grid element to inspect.
   * @returns True if the right-side details box should be shown.
   */
  public showRightDetailsBox(item: ViewRowElement): boolean {
    return (
      !item.isHour &&
      item.showDetailsBox &&
      item.value.length > 0 &&
      item.day <= 1
    );
  }

  /**
   * Determines whether the details popover box should be shown on the left
   * side for the given element (used for later day columns).
   *
   * @param item The grid element to inspect.
   * @returns True if the left-side details box should be shown.
   */
  public showLeftDetailsBox(item: ViewRowElement): boolean {
    return (
      !item.isHour &&
      item.showDetailsBox &&
      item.value.length > 0 &&
      item.day > 1
    );
  }

  /**
   * Resolves the localized labels describing an event's recurrence.
   *
   * @param occurrenceRate The occurrence rate code, or undefined.
   * @returns The translated recurrence description parts.
   */
  public getRepetition(occurrenceRate: string | undefined): string[] {
    return translateOccurrenceRate(occurrenceRate);
  }
  /**
   * Narrows a {@link ViewRowElement} value to its array form for template
   * iteration.
   *
   * @param itemValue The element value (string label or showable events).
   * @returns The showable events array, or an empty array for string values.
   */
  public getValueAsArray(itemValue: string | ShowableEventItemDefinition[]) {
    if (typeof itemValue === 'string') {
      return [];
    }
    return itemValue;
  }
}
