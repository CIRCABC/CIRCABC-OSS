import { NgStyle } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  output,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AgendaHelperService } from 'app/core/agenda-helper.service';
import {
  EventItemDefinition,
  EventsService,
  UserService,
} from 'app/core/generated/circabc';
import { LocalizationService } from 'app/core/localization.service';
import { LoginService } from 'app/core/login.service';
import { TimeZoneHelperService } from 'app/core/timezone-helper.service';
import {
  getFullDate,
  getRandomColor,
  padWithLeadingZero as padWithLeadingZeroGlobal,
  translateOccurrenceRate,
} from 'app/core/util';

/**
 * Internal view-model describing a single event occurrence rendered within a
 * 15-minute time slot of the day grid.
 *
 * Times are expressed as an absolute number of minutes since midnight
 * (i.e. `hour * 60 + minutes`).
 */
interface ShowableEventItemDefinition {
  /** The underlying event/meeting definition being displayed. */
  event: EventItemDefinition;
  /** The colour assigned to this event, used to visually group its slots. */
  assignedColor: string;
  /** Event start time, in minutes since midnight. */
  startTime: number;
  /** The time slot (in minutes since midnight) this row represents. */
  currentTime: number;
  /** Event end time, in minutes since midnight. */
  endTime: number;
  /**
   * Whether this occurrence is the "primary" slot for the event and should
   * render the event details box (as opposed to a continuation slot).
   */
  show: boolean;
}

/**
 * Internal view-model for a single row of the day grid.
 *
 * A row is either an hour label row (`isHour === true`) or a 15-minute slot
 * row that may contain zero or more showable events.
 */
interface ViewRowElement {
  /** `true` when the row represents an hour header, `false` for a minute slot. */
  isHour: boolean;
  /** The hour (0-23) this row belongs to. */
  hour: number;
  /** The minute offset within the hour (0, 15, 30 or 45). */
  minutes: number;
  /** Count of slots occupied per event id, keyed by event id. */
  multiplicity: Map<string, number>;
  /**
   * The row content: a formatted hour label string for hour rows, or the list
   * of showable events for minute-slot rows.
   */
  value: string | ShowableEventItemDefinition[];
  /** Whether the details box should be shown for this row. */
  showDetailsBox: boolean;
}

/**
 * Renders the day view of the agenda: a vertical time grid split into hour
 * headers and 15-minute slots, laying out the events and meetings that occur
 * on a given date.
 *
 * The component fetches events either for the current user ("me" mode) or for
 * a specific interest group, converts them to the local time zone, assigns a
 * display colour per event and computes the grid rows to display. It reacts to
 * input changes via {@link ngOnChanges} and notifies parents once rendering
 * data has been processed through {@link processingEventEmitter}.
 *
 * Key collaborators: {@link UserService} and {@link EventsService} (event
 * retrieval), {@link AgendaHelperService} (colour assignment and ribbon
 * toggling), {@link LocalizationService} (localized day names),
 * {@link TimeZoneHelperService} (time-zone conversion) and
 * {@link LoginService} (current user resolution).
 */
@Component({
  selector: 'cbc-day',
  templateUrl: './day.component.html',
  styleUrl: './day.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, NgStyle, TranslocoModule],
})
export class DayComponent implements OnChanges {
  private readonly userService = inject(UserService);
  private readonly eventsService = inject(EventsService);
  private readonly loginService = inject(LoginService);
  private readonly agendaHelperService = inject(AgendaHelperService);
  private readonly localizationService = inject(LocalizationService);
  private readonly timeZoneHelperService = inject(TimeZoneHelperService);

  /** Required. The date whose events should be displayed. */
  public readonly date = input.required<Date>();
  /** The interest group id to load events for (ignored in "me" mode). */
  public readonly id = input<string>();
  /** When `true`, loads the current user's personal events instead of a group's. */
  public readonly meMode = input(false);
  // property used to fire the ngOnChanges event when toggled for redisplay (new event/meeting has been added)
  /**
   * Toggle input used solely to trigger {@link ngOnChanges} and force a
   * redisplay (e.g. after a new event/meeting has been added).
   */
  public readonly redisplay = input<boolean>();
  /** Required. The first hour (0-23) rendered in the day grid. */
  public readonly displayFromHour = input.required<number>();
  /** Required. The last hour (0-23) rendered in the day grid. */
  public readonly displayToHour = input.required<number>();
  /**
   * Emits once the day's events have been fetched and the grid rows computed.
   * Emits the string `'day'` to signal the parent that processing is complete.
   */
  public readonly processingEventEmitter = output<string>();

  /** The computed grid rows (hour headers and minute slots) to render. */
  public readonly viewRowElements = signal<ViewRowElement[]>([]);

  /** The events loaded for the current {@link date}, in local time. */
  public events!: EventItemDefinition[];

  /** Whether the coloured ribbons for multi-slot events are shown. */
  public readonly showRibbons = signal(false);

  /**
   * Localized full day names starting from Monday, for the header labels.
   *
   * @returns The list of localized day-name strings.
   */
  public get dayNames() {
    return this.localizationService.getDayNames('Monday', 'full', 5);
  }

  /**
   * Angular lifecycle hook. Reloads events on any input change and refreshes
   * the ribbon-display flag.
   *
   * @returns A promise that resolves once events are (re)loaded.
   */
  ngOnChanges() {
    void this.handleChanges();
  }

  private async handleChanges() {
    await this.getEvents();
    this.showRibbons.set(this.agendaHelperService.isShowRibbons());
  }

  /**
   * Fetches the events for {@link date} — either the current user's events
   * ("me" mode) or a specific interest group's events — converts them to the
   * local time zone, rebuilds the grid rows and emits {@link processingEventEmitter}.
   *
   * @returns A promise that resolves once events are loaded and rows computed.
   */
  public async getEvents() {
    if (this.meMode()) {
      this.events = await this.userService.getUserEventsPeriodAsync({
        userId: this.getUserId(),
        exactDate: getFullDate(this.date()),
        period: 'Exact',
      });
    } else {
      const id = this.id();
      if (id !== undefined) {
        this.events = await this.eventsService.getInterestGroupEventsAsync({
          id,
          startDate: getFullDate(this.date()),
          endDate: getFullDate(this.date()),
        });
      }
    }

    this.events = this.timeZoneHelperService.toLocalDateTime(this.events);

    this.getViewRows();

    this.processingEventEmitter.emit('day');
  }

  /**
   * Resolves the id of the currently logged-in user.
   *
   * @returns The current username used as the user id.
   */
  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }

  /**
   * Determines whether an event overlaps a given 14-minute time slot, matching
   * its start point, an intermediate point, or its end point.
   *
   * @param currentTime The slot start time, in minutes since midnight.
   * @param eventStartTime The event start time, in minutes since midnight.
   * @param eventEndTime The event end time, in minutes since midnight.
   * @returns `true` if the event is present during the slot.
   */
  public isEventAtThisTime(
    currentTime: number,
    eventStartTime: number,
    eventEndTime: number
  ): boolean {
    return (
      (eventStartTime >= currentTime && eventStartTime <= currentTime + 14) || // startpoint
      (eventStartTime < currentTime && eventEndTime > currentTime) || // middlepoint(s)
      (eventEndTime - 1 >= currentTime && eventEndTime - 1 <= currentTime + 14) // endpoint
    );
  }

  // pad values with '0' to conform to the date and time formats
  /**
   * Left-pads a numeric value with a leading zero to conform to date/time
   * display formats.
   *
   * @param value The number to pad.
   * @returns The value as a zero-padded string.
   */
  public padWithLeadingZero(value: number): string {
    return padWithLeadingZeroGlobal(value);
  }

  // event.appointmentType === 'Event'
  /**
   * @param eid The event item definition to test.
   * @returns `true` if the item is an event (appointment type `'Event'`).
   */
  public isEvent(eid: EventItemDefinition): boolean {
    return eid.appointmentType === 'Event';
  }

  /**
   * @param eid The event item definition to test.
   * @returns `true` if the item is a meeting (appointment type `'Meeting'`).
   */
  public isMeeting(eid: EventItemDefinition): boolean {
    return eid.appointmentType === 'Meeting';
  }

  /**
   * Rebuilds {@link viewRowElements} for the configured hour range, adding an
   * hour header row and four 15-minute slot rows per hour.
   */
  private getViewRows(): void {
    const rows: ViewRowElement[] = [];
    const multiplicity = new Map<string, number>();

    for (
      let hour = this.displayFromHour();
      hour < this.displayToHour() + 1;
      hour++
    ) {
      this.addHourRow(rows, hour, multiplicity);
      this.addMinuteRows(rows, hour, multiplicity);
    }

    this.viewRowElements.set(rows);
  }

  /**
   * Appends an hour header row to {@link viewRowElements}.
   *
   * @param rows The row list being built, mutated in place before the signal
   * is set.
   * @param hour The hour (0-23) for the header.
   * @param multiplicity The shared per-event slot-count map for the day.
   */
  private addHourRow(
    rows: ViewRowElement[],
    hour: number,
    multiplicity: Map<string, number>
  ) {
    rows.push({
      isHour: true,
      hour,
      minutes: 0,
      value: `${this.padWithLeadingZero(hour)}:`,
      multiplicity,
      showDetailsBox: false,
    });
  }

  /**
   * Appends the four 15-minute slot rows for a given hour to
   * {@link viewRowElements}, each populated with its showable events.
   *
   * @param rows The row list being built, mutated in place before the signal
   * is set.
   * @param hour The hour (0-23) whose minute rows are added.
   * @param multiplicity The shared per-event slot-count map for the day.
   */
  private addMinuteRows(
    rows: ViewRowElement[],
    hour: number,
    multiplicity: Map<string, number>
  ) {
    for (let minutes = 0; minutes < 60; minutes += 15) {
      const showableEvents = this.getShowableEventsForTime(
        hour,
        minutes,
        multiplicity
      );

      rows.push({
        isHour: false,
        hour,
        minutes,
        value: showableEvents,
        multiplicity,
        showDetailsBox: false,
      });
    }
  }

  /**
   * Builds the list of events that occur within a specific 15-minute slot,
   * incrementing their slot multiplicity as it goes.
   *
   * @param hour The slot hour (0-23).
   * @param minutes The slot minute offset (0, 15, 30 or 45).
   * @param multiplicity The shared per-event slot-count map for the day.
   * @returns The showable events present during the slot; empty when no events
   * are loaded.
   */
  private getShowableEventsForTime(
    hour: number,
    minutes: number,
    multiplicity: Map<string, number>
  ): ShowableEventItemDefinition[] {
    if (!this.events) return [];

    const currentTime = hour * 60 + minutes;
    const showableEvents: ShowableEventItemDefinition[] = [];

    for (const event of this.events) {
      if (!(event.startTime && event.endTime)) continue;

      const eventTimes = this.parseEventTimes(event);

      if (
        this.isEventAtThisTime(currentTime, eventTimes.start, eventTimes.end)
      ) {
        const showableEvent = this.createShowableEvent(
          event,
          eventTimes,
          currentTime,
          multiplicity
        );
        showableEvents.push(showableEvent);
      }
    }

    return showableEvents;
  }

  /**
   * Parses an event's `startTime`/`endTime` string fields (formatted `HH:mm`)
   * into absolute minutes since midnight.
   *
   * @param event The event whose times are parsed.
   * @returns The start and end times, in minutes since midnight.
   */
  private parseEventTimes(event: EventItemDefinition): {
    start: number;
    end: number;
  } {
    const startHour = Number(event.startTime?.substring(0, 2) ?? 0);
    const startMinutes = Number(event.startTime?.substring(3, 5) ?? 0);
    const endHour = Number(event.endTime?.substring(0, 2) ?? 0);
    const endMinutes = Number(event.endTime?.substring(3, 5) ?? 0);

    return {
      start: startHour * 60 + startMinutes,
      end: endHour * 60 + endMinutes,
    };
  }

  /**
   * Creates a {@link ShowableEventItemDefinition} for an event within a slot.
   * The first slot occupied by an event is marked as the primary
   * (`show === true`) and receives a freshly assigned colour; subsequent slots
   * are continuation rows (`show === false`) reusing the same colour.
   *
   * @param event The event being rendered.
   * @param eventTimes The event's start/end in minutes since midnight.
   * @param currentTime The slot start time, in minutes since midnight.
   * @param multiplicity The shared per-event slot-count map for the day.
   * @returns The view-model describing the event within the slot.
   */
  private createShowableEvent(
    event: EventItemDefinition,
    eventTimes: { start: number; end: number },
    currentTime: number,
    multiplicity: Map<string, number>
  ): ShowableEventItemDefinition {
    const eventId = event.id as string;
    const amount = (multiplicity.get(eventId) ?? 0) + 1;
    multiplicity.set(eventId, amount);

    if (amount === 1) {
      const color = this.getOrAssignColor(eventId);
      return {
        event,
        show: true,
        assignedColor: color,
        startTime: eventTimes.start,
        endTime: eventTimes.end,
        currentTime,
      };
    }

    return {
      event,
      show: false,
      assignedColor: this.agendaHelperService.getEventDisplayColor(
        eventId
      ) as string,
      startTime: eventTimes.start,
      endTime: eventTimes.end,
      currentTime,
    };
  }

  /**
   * Returns the display colour for an event, assigning and persisting a random
   * colour via {@link AgendaHelperService} if none exists yet.
   *
   * @param eventId The event id to look up or assign a colour for.
   * @returns The event's display colour.
   */
  private getOrAssignColor(eventId: string): string {
    let color = this.agendaHelperService.getEventDisplayColor(eventId);
    if (!color) {
      color = getRandomColor();
      this.agendaHelperService.setEventDisplayColor(eventId, color);
    }
    return color;
  }

  /**
   * Determines whether a row contains at least one event that spans more than a
   * single 15-minute slot (used to decide whether to draw connecting ribbons).
   *
   * @param item The grid row to inspect.
   * @returns `true` if any event in the row covers multiple minute ranges;
   * `false` for hour label rows.
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
   * Whether the event details box should be shown on the right side, i.e. for
   * non-hour rows in the first half of the hour that have events and are
   * flagged to display details.
   *
   * @param item The grid row to inspect.
   * @returns `true` if the right-side details box should render.
   */
  public showRightDetailsBox(item: ViewRowElement): boolean {
    return (
      !item.isHour &&
      item.minutes < 30 &&
      item.showDetailsBox &&
      item.value.length > 0
    );
  }

  /**
   * Whether the event details box should be shown on the left side, i.e. for
   * non-hour rows in the second half of the hour that have events and are
   * flagged to display details.
   *
   * @param item The grid row to inspect.
   * @returns `true` if the left-side details box should render.
   */
  public showLeftDetailsBox(item: ViewRowElement): boolean {
    return (
      !item.isHour &&
      item.minutes >= 30 &&
      item.showDetailsBox &&
      item.value.length > 0
    );
  }

  /**
   * Translates an event's occurrence/recurrence rate into localized display
   * strings.
   *
   * @param occurrenceRate The raw occurrence rate, or `undefined`.
   * @returns The localized repetition description parts.
   */
  public getRepetition(occurrenceRate: string | undefined): string[] {
    return translateOccurrenceRate(occurrenceRate);
  }

  /**
   * Computes the inline style for an event marker within a slot. Renders a
   * small coloured triangle at the event's start slot, or a proportionally
   * sized coloured bar for continuation slots based on how much of the
   * 14-minute window the event occupies.
   *
   * @param item The showable event view-model for the slot.
   * @returns An `NgStyle`-compatible style object.
   */
  public getStyle(item: ShowableEventItemDefinition): object {
    if (item.startTime >= item.currentTime) {
      return {
        width: 0,
        height: 0,
        'border-top': '4px solid transparent',
        'border-bottom': '4px solid transparent',
        'border-left': `4px solid ${item.assignedColor}`,
      };
    }
    let portion: number;
    if (item.endTime - item.currentTime > 14) {
      portion = 14;
    } else {
      portion = item.endTime - item.currentTime;
    }
    // x percentage of 14 (minutes) * (try and error scaling factor to adjust the size to the container)
    const width = `${(portion * 100) / 14}%`;
    return { 'background-color': item.assignedColor, width: width };
  }

  /**
   * Toggles the visibility of the multi-slot event ribbons and persists the
   * preference via {@link AgendaHelperService}.
   */
  public toggleRibbons() {
    this.showRibbons.set(this.agendaHelperService.toggleShowRibbons());
  }
  /**
   * Narrows a row's `value` to the event array for template iteration.
   *
   * @param itemValue The row value (either an hour label string or events).
   * @returns The events array, or an empty array when the value is a string.
   */
  public getValueAsArray(itemValue: string | ShowableEventItemDefinition[]) {
    if (typeof itemValue === 'string') {
      return [];
    }
    return itemValue;
  }
}
