import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChange,
} from '@angular/core';
import {
  EventItemDefinition,
  EventsService,
  UserService,
} from 'app/core/generated/circabc';
import { LocalizationService } from 'app/core/localization.service';
import { LoginService } from 'app/core/login.service';
import { TimeZoneHelperService } from 'app/core/timezone-helper.service';
import {
  eventsStartTimeComparator,
  getFullDate,
  padWithLeadingZero as padWithLeadingZeroGlobal,
  translateOccurrenceRate,
  convertDate,
} from 'app/core/util';
import { firstValueFrom } from 'rxjs';
interface CalendarDayType {
  date: Date;
  events: EventItemDefinition[];
  showDetailsBox: boolean;
}
interface ShowableEventItemDefinition {
  event: EventItemDefinition;
  show: boolean;
}
interface ViewRowElement {
  isHour: boolean;
  isToday: boolean;
  isWeekendDay: boolean;
  multiplicity: Map<string, number>;
  value: string | ShowableEventItemDefinition[];
  day: number;
  dayDate: number;
  dayName: string;
  showDetailsBox: boolean;
}

@Component({
  selector: 'cbc-week',
  templateUrl: './week.component.html',
  styleUrls: ['./week.component.scss'],
  preserveWhitespaces: true,
})
export class WeekComponent implements OnChanges {
  @Input()
  public date!: Date;
  @Input()
  public id!: string;
  @Input()
  public meMode = false;
  // property used to fire the ngOnChanges event when toggled for redisplay (new event/meeting has been added)
  // property used to fire the ngOnChanges event when toggled for redisplay (new event/meeting has been added)
  @Input()
  public redisplay!: boolean;
  @Input()
  public displayFromHour!: number;
  @Input()
  public displayToHour!: number;
  @Input()
  public workWeek!: boolean;
  @Output()
  public readonly processingEventEmitter = new EventEmitter<string>();

  private firstOfWeek!: Date;
  private lastOfWeek!: Date;
  private lastOfWorkWeek!: Date;

  public weekDates!: Date[];

  public get dayNames() {
    return this.localizationService.getDayNames('Monday', 'full');
  }

  public viewRowElements: ViewRowElement[] = [];

  public eventsPerWeekDays!: CalendarDayType[];

  constructor(
    private userService: UserService,
    private eventsService: EventsService,
    private loginService: LoginService,
    private localizationService: LocalizationService,
    private timeZoneHelperService: TimeZoneHelperService
  ) {}

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  public async ngOnChanges(_changes: { [propKey: string]: SimpleChange }) {
    await this.initDays();
  }

  private async initDays() {
    // monday
    this.firstOfWeek = new Date(this.date.getTime());
    this.firstOfWeek.setDate(
      this.firstOfWeek.getDate() - this.date.getDay() + 1
    );
    // sunday
    this.lastOfWeek = new Date(this.date.getTime());
    this.lastOfWeek.setDate(
      this.lastOfWeek.getDate() + (6 - this.date.getDay()) + 1
    );
    // friday
    this.lastOfWorkWeek = new Date(this.date.getTime());
    this.lastOfWorkWeek.setDate(
      this.lastOfWeek.getDate() + (6 - this.date.getDay()) - 1
    );

    // get the day numbers of the week
    this.weekDates = [];
    let weekDate = this.firstOfWeek;
    let dayIncrement = 0;
    while (dayIncrement < 7) {
      this.weekDates.push(weekDate);
      dayIncrement += 1;
      const newDate: Date = new Date(this.firstOfWeek.getTime());
      newDate.setDate(this.firstOfWeek.getDate() + dayIncrement);
      weekDate = newDate;
    }

    await this.getWeeklyEvents();
  }

  // collect the events that happen on the given day
  private getEventsForDate(collectedEvents: EventItemDefinition[], date: Date) {
    const events: EventItemDefinition[] = [];

    for (const event of collectedEvents) {
      if (
        event.appointmentDate !== undefined &&
        event.appointmentDate.toString() === convertDate(date.toDateString())
      ) {
        events.push(event);
      }
    }

    return events;
  }

  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }

  private async getWeeklyEvents() {
    let events: EventItemDefinition[] = [];

    if (this.meMode) {
      events = await firstValueFrom(
        this.userService.getUserEventsPeriod(
          this.getUserId(),
          getFullDate(this.firstOfWeek),
          'Future'
        )
      );
    } else {
      events = await firstValueFrom(
        this.eventsService.getInterestGroupEvents(
          this.id,
          getFullDate(this.firstOfWeek),
          getFullDate(this.lastOfWeek)
        )
      );
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

  public isToday(weekDay: number) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const givenDay = this.weekDates[weekDay];
    givenDay.setHours(0, 0, 0, 0);
    return today.getTime() === givenDay.getTime();
  }

  public isWeekendDay(weekDay: number) {
    return weekDay === 5 || weekDay === 6;
  }

  // pad values with '0' to conform to the date and time formats
  public padWithLeadingZero(value: number): string {
    return padWithLeadingZeroGlobal(value);
  }

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
  public isEvent(eid: EventItemDefinition): boolean {
    return eid.appointmentType === 'Event';
  }

  public isMeeting(eid: EventItemDefinition): boolean {
    return eid.appointmentType === 'Meeting';
  }

  public getDisplayableDays() {
    if (this.workWeek) {
      return [0, 1, 2, 3, 4];
    } else {
      return [0, 1, 2, 3, 4, 5, 6];
    }
  }

  private getViewRows(): void {
    this.viewRowElements = [];
    const multiplicity: Map<string, number> = new Map<string, number>();
    for (
      let hour: number = this.displayFromHour;
      hour < this.displayToHour + 1;
      hour += 1
    ) {
      let firstPass = true;
      const numberOfDays: number = this.workWeek ? 5 : 7;
      for (let day = 0; day < numberOfDays; day += 1) {
        if (firstPass) {
          this.viewRowElements.push({
            isHour: true,
            multiplicity: multiplicity,
            value: this.padWithLeadingZero(hour),
            isToday: this.isToday(day),
            isWeekendDay: this.isWeekendDay(day),
            day: day,
            dayDate: this.weekDates[day].getDate(),
            dayName: this.dayNames[day],
            showDetailsBox: false,
          });
          firstPass = false;
        }

        const showableEvents: ShowableEventItemDefinition[] = [];
        if (this.eventsPerWeekDays !== undefined) {
          for (const eventPerWeekDay of this.eventsPerWeekDays) {
            for (const event of eventPerWeekDay.events) {
              if (this.isOnDate(event, day, hour)) {
                let amount = multiplicity.get(event.id as string);
                amount = amount === undefined ? 1 : amount + 1;
                multiplicity.set(event.id as string, amount);
                showableEvents.push({ event: event, show: amount === 1 });
              }
            }
          }
        }
        showableEvents.sort(
          (
            event1: ShowableEventItemDefinition,
            event2: ShowableEventItemDefinition
          ): number => eventsStartTimeComparator(event1.event, event2.event)
        );
        this.viewRowElements.push({
          isHour: false,
          multiplicity: multiplicity,
          value: showableEvents,
          isToday: this.isToday(day),
          isWeekendDay: this.isWeekendDay(day),
          day: day,
          dayDate: this.weekDates[day].getDate(),
          dayName: this.dayNames[day],
          showDetailsBox: false,
        });
      }
    }
  }

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

  public showRightDetailsBox(item: ViewRowElement): boolean {
    return (
      !item.isHour &&
      item.showDetailsBox &&
      item.value.length > 0 &&
      item.day <= 1
    );
  }

  public showLeftDetailsBox(item: ViewRowElement): boolean {
    return (
      !item.isHour &&
      item.showDetailsBox &&
      item.value.length > 0 &&
      item.day > 1
    );
  }

  public getRepetition(occurrenceRate: string | undefined): string[] {
    return translateOccurrenceRate(occurrenceRate);
  }
  public getValueAsArray(itemValue: string | ShowableEventItemDefinition[]) {
    if (typeof itemValue === 'string') {
      return [];
    } else {
      return itemValue;
    }
  }
}
