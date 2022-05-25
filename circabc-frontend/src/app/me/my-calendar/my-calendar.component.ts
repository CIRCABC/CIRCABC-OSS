import { Component, OnChanges, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

import { AgendaHelperService } from 'app/core/agenda-helper.service';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  EventsService,
  NodesService,
  UserService,
} from 'app/core/generated/circabc';
import { LocalizationService } from 'app/core/localization.service';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { TimeZoneHelperService } from 'app/core/timezone-helper.service';
import {
  getFormattedDate as getFormattedDateGlobal,
  getFullDate,
} from 'app/core/util';
import { CalendarComponent } from 'app/group/agenda/calendar/calendar.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-my-calendar',
  templateUrl: './my-calendar.component.html',
  styleUrls: ['./my-calendar.component.scss'],
  preserveWhitespaces: true,
})
export class MyCalendarComponent
  extends CalendarComponent
  implements OnInit, OnChanges
{
  public view!: string;
  public workWeek = true;
  public from!: number;
  public to!: number;

  public get monthNames() {
    return this.localizationService.getMonthsNames();
  }

  public get completeDayNames() {
    return this.localizationService.getDayNames();
  }

  public changeDateForm!: FormGroup;

  // eslint-disable-next-line
  public constructor(
    private userService: UserService,
    private loginService: LoginService,
    private agendaHelperService: AgendaHelperService,
    eventsService: EventsService,
    nodesService: NodesService,
    permEvalService: PermissionEvaluatorService,
    uiMessageService: UiMessageService,
    localizationService: LocalizationService,
    timeZoneHelperService: TimeZoneHelperService,
    private fb: FormBuilder
  ) {
    super(
      eventsService,
      nodesService,
      permEvalService,
      uiMessageService,
      localizationService,
      timeZoneHelperService
    );
  }

  override async ngOnInit() {
    this.date = new Date();
    this.workWeek = true;
    this.view = this.agendaHelperService.getMyCalendarViewState();
    this.initDayHourDisplayRange();
    await super.initDays();

    this.changeDateForm = this.fb.group(
      {
        dateDayView: [this.date],
      },
      {
        updateOn: 'change',
      }
    );
  }

  private initDayHourDisplayRange() {
    this.from = 8;
    this.to = 21;
  }

  public override async getEvents(startDate: Date, _endDate: Date) {
    return await firstValueFrom(
      this.userService.getUserEventsPeriod(
        this.getUserId(),
        getFullDate(startDate),
        'Future'
      )
    );
  }

  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }

  public selectView(value: string) {
    this.view = value;
    this.agendaHelperService.saveMyCalendarViewState(value);
    if (this.view === 'day') {
      this.initDayHourDisplayRange();
    }
  }

  public selectFrom(value: string) {
    this.from = Number(value);
  }

  public selectTo(value: string) {
    this.to = Number(value);
  }

  public toggleWorkWeek() {
    this.workWeek = !this.workWeek;
  }

  public getFormattedDate() {
    return getFormattedDateGlobal(this.date);
  }

  public applyToCurrentDate() {
    if (this.changeDateForm.value.dateDayView !== undefined) {
      this.date = this.changeDateForm.value.dateDayView;
    }
  }

  // navigate through days and months

  public async nextMonth() {
    const tmp = new Date(this.date.getTime());
    tmp.setMonth(tmp.getMonth() + 1);
    this.date = new Date(tmp.getTime());
    await super.initDays();
  }

  public async previousMonth() {
    const tmp = new Date(this.date.getTime());
    tmp.setMonth(tmp.getMonth() - 1);
    this.date = new Date(tmp.getTime());
    await super.initDays();
  }

  public async nextDay() {
    await this.getDays(1);
  }

  public async previousDay() {
    await this.getDays(-1);
  }

  public async previousWeek() {
    await this.getDays(-7);
  }

  public async nextWeek() {
    await this.getDays(7);
  }
  private async getDays(numberOfDays: -7 | -1 | 1 | 7) {
    const tmp = new Date(this.date.getTime());
    tmp.setDate(tmp.getDate() + numberOfDays);
    this.date = new Date(tmp.getTime());
    await super.initDays();
  }
}
