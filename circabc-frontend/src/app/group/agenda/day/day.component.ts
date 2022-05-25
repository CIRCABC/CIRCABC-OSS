import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
} from '@angular/core';

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
import { firstValueFrom } from 'rxjs';

interface ShowableEventItemDefinition {
  event: EventItemDefinition;
  assignedColor: string;
  startTime: number;
  currentTime: number;
  endTime: number;
  show: boolean;
}
interface ViewRowElement {
  isHour: boolean;
  hour: number;
  minutes: number;
  multiplicity: Map<string, number>;
  value: string | ShowableEventItemDefinition[];
  showDetailsBox: boolean;
}

@Component({
  selector: 'cbc-day',
  templateUrl: './day.component.html',
  styleUrls: ['./day.component.scss'],
  preserveWhitespaces: true,
})
export class DayComponent implements OnChanges {
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
  @Output()
  public readonly processingEventEmitter = new EventEmitter<string>();

  public viewRowElements: ViewRowElement[] = [];

  public events!: EventItemDefinition[];

  public showRibbons = false;

  public get dayNames() {
    return this.localizationService.getDayNames('Monday', 'full', 5);
  }

  constructor(
    private userService: UserService,
    private eventsService: EventsService,
    private loginService: LoginService,
    private agendaHelperService: AgendaHelperService,
    private localizationService: LocalizationService,
    private timeZoneHelperService: TimeZoneHelperService
  ) {}

  async ngOnChanges() {
    await this.getEvents();
    this.showRibbons = this.agendaHelperService.isShowRibbons();
  }

  public async getEvents() {
    if (this.meMode) {
      this.events = await firstValueFrom(
        this.userService.getUserEventsPeriod(
          this.getUserId(),
          getFullDate(this.date),
          'Exact'
        )
      );
    } else {
      this.events = await firstValueFrom(
        this.eventsService.getInterestGroupEvents(
          this.id,
          getFullDate(this.date),
          getFullDate(this.date)
        )
      );
    }

    this.events = this.timeZoneHelperService.toLocalDateTime(this.events);

    this.getViewRows();

    this.processingEventEmitter.emit('day');
  }

  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }

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
  public padWithLeadingZero(value: number): string {
    return padWithLeadingZeroGlobal(value);
  }

  // event.appointmentType === 'Event'
  public isEvent(eid: EventItemDefinition): boolean {
    return eid.appointmentType === 'Event';
  }

  public isMeeting(eid: EventItemDefinition): boolean {
    return eid.appointmentType === 'Meeting';
  }

  private getViewRows(): void {
    this.viewRowElements = [];
    const multiplicity: Map<string, number> = new Map<string, number>();
    for (
      let hour: number = this.displayFromHour;
      hour < this.displayToHour + 1;
      hour += 1
    ) {
      this.viewRowElements.push({
        isHour: true,
        hour: hour,
        minutes: 0,
        value: `${this.padWithLeadingZero(hour)}:`,
        multiplicity: multiplicity,
        showDetailsBox: false,
      });
      for (let minutes = 0; minutes < 60; minutes += 15) {
        const showableEvents: ShowableEventItemDefinition[] = [];
        if (this.events !== undefined) {
          for (const event of this.events) {
            if (event.startTime !== undefined && event.endTime !== undefined) {
              const eventStartHour = Number(event.startTime.substring(0, 2));
              const eventStartMinutes = Number(event.startTime.substring(3, 5));

              const eventEndHour = Number(event.endTime.substring(0, 2));
              const eventEndMinutes = Number(event.endTime.substring(3, 5));

              const currentTime: number = hour * 60 + minutes;
              const eventStartTime: number =
                eventStartHour * 60 + eventStartMinutes;
              const eventEndTime: number = eventEndHour * 60 + eventEndMinutes;

              if (
                this.isEventAtThisTime(
                  currentTime,
                  eventStartTime,
                  eventEndTime
                )
              ) {
                let amount = multiplicity.get(event.id as string);
                amount = amount === undefined ? 1 : amount + 1;
                multiplicity.set(event.id as string, amount);
                if (amount === 1) {
                  let color: string | undefined =
                    this.agendaHelperService.getEventDisplayColor(
                      event.id as string
                    );
                  if (color === undefined) {
                    color = getRandomColor();
                  }
                  showableEvents.push({
                    event: event,
                    show: true,
                    assignedColor: color,
                    startTime: eventStartTime,
                    endTime: eventEndTime,
                    currentTime: currentTime,
                  });
                  this.agendaHelperService.setEventDisplayColor(
                    event.id as string,
                    color
                  );
                } else {
                  showableEvents.push({
                    event: event,
                    show: false,
                    assignedColor:
                      this.agendaHelperService.getEventDisplayColor(
                        event.id as string
                      ) as string,
                    startTime: eventStartTime,
                    endTime: eventEndTime,
                    currentTime: currentTime,
                  });
                }
              }
            }
          }
        }

        this.viewRowElements.push({
          isHour: false,
          hour: hour,
          minutes: minutes,
          value: showableEvents,
          multiplicity: multiplicity,
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
      item.minutes < 30 &&
      item.showDetailsBox &&
      item.value.length > 0
    );
  }

  public showLeftDetailsBox(item: ViewRowElement): boolean {
    return (
      !item.isHour &&
      item.minutes >= 30 &&
      item.showDetailsBox &&
      item.value.length > 0
    );
  }

  public getRepetition(occurrenceRate: string | undefined): string[] {
    return translateOccurrenceRate(occurrenceRate);
  }

  public getStyle(item: ShowableEventItemDefinition): object {
    if (item.startTime >= item.currentTime) {
      return {
        width: 0,
        height: 0,
        'border-top': '4px solid transparent',
        'border-bottom': '4px solid transparent',
        'border-left': `4px solid ${item.assignedColor}`,
      };
    } else {
      let portion = 0;
      if (item.endTime - item.currentTime > 14) {
        portion = 14;
      } else {
        portion = item.endTime - item.currentTime;
      }
      // x percentage of 14 (minutes) * (try and error scaling factor to adjust the size to the container)
      const width = `${(portion * 100) / 14}%`;
      return { 'background-color': item.assignedColor, width: width };
    }
  }

  public toggleRibbons() {
    this.showRibbons = this.agendaHelperService.toggleShowRibbons();
  }
  public getValueAsArray(itemValue: string | ShowableEventItemDefinition[]) {
    if (typeof itemValue === 'string') {
      return [];
    } else {
      return itemValue;
    }
  }
}
