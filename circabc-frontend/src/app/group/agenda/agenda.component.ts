import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { AgendaHelperService } from 'app/core/agenda-helper.service';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  EventItemDefinition,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { LocalizationService } from 'app/core/localization.service';
import { getFormattedDate as getFormattedDateGlobal } from 'app/core/util';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-agenda',
  templateUrl: './agenda.component.html',
  styleUrls: ['./agenda.component.scss'],
  preserveWhitespaces: true,
})
export class AgendaComponent implements OnInit {
  public createEventShowModal = false;
  public deleteEventShowModal = false;
  public currentDate!: Date;
  public igId!: string;
  public ig!: InterestGroup;
  public eventRootNode!: ModelNode;
  public eventToDelete!: EventItemDefinition;
  public redisplayCalendar = false;
  public igLoaded = false;
  public loading = false;
  public isEvent = true;

  public view!: string;
  public workWeek = true;
  public from!: number;
  public to!: number;

  public processing = false;

  public get monthNames() {
    return this.localizationService.getMonthsNames();
  }
  /*
  public monthNames: { [key: string]: string } = {
    1: 'January',
    2: 'February',
    3: 'March',
    4: 'April',
    5: 'May',
    6: 'June',
    7: 'July',
    8: 'August',
    9: 'September',
    10: 'October',
    11: 'November',
    12: 'December',
  };
  */
  public get dayNames() {
    return this.localizationService.getDayNames();
  }

  /*
  public dayNames: { [key: string]: string } = {
    0: 'Sunday',
    1: 'Monday',
    2: 'Tuesday',
    3: 'Wednesday',
    4: 'Thursday',
    5: 'Friday',
    6: 'Saturday',
  };
  */
  public changeDateForm!: FormGroup;

  public constructor(
    private route: ActivatedRoute,
    private agendaHelperService: AgendaHelperService,
    private interestGroupService: InterestGroupService,
    private nodesService: NodesService,
    private permEvalService: PermissionEvaluatorService,
    private localizationService: LocalizationService,
    private fb: FormBuilder
  ) {}

  public ngOnInit() {
    this.currentDate = new Date();
    this.route.params.subscribe(
      async (params) => await this.getInterestGroup(params)
    );
    this.workWeek = true;
    this.view = this.agendaHelperService.getAgendaViewState();
    this.initDayHourDisplayRange();

    this.changeDateForm = this.fb.group(
      {
        dateDayView: [this.currentDate],
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

  public async getInterestGroup(params: { [key: string]: string }) {
    this.igId = params.id;
    this.ig = await firstValueFrom(
      this.interestGroupService.getInterestGroup(this.igId)
    );
    this.eventRootNode = await firstValueFrom(
      this.nodesService.getNode(this.ig.eventId as string)
    );
    this.igLoaded = true;
  }

  public isEveAdmin(): boolean {
    return this.permEvalService.isEveAdmin(this.eventRootNode);
  }

  public popupCreateEventWithCalendarDay(calendarSheetDate: Date): void {
    this.currentDate = calendarSheetDate;
    this.createEventShowModal = true;
  }

  public popupDeleteEvent(event: EventItemDefinition) {
    if (event !== undefined) {
      this.eventToDelete = event;
      this.deleteEventShowModal = true;
    }
  }

  public createEventWithToday(isEvent: boolean): void {
    this.currentDate = new Date();
    this.createEventShowModal = true;
    this.isEvent = isEvent;
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  public redisplayCalendarAfterSave() {
    this.redisplayCalendar = !this.redisplayCalendar;
  }

  public finishProcessing(view: string) {
    this.view = view;
    this.agendaHelperService.saveAgendaViewState(view);
    this.processing = false;
  }

  public selectView(value: string) {
    this.view = value;
    this.agendaHelperService.saveAgendaViewState(value);
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
    return getFormattedDateGlobal(this.currentDate);
  }

  public applyToCurrentDate() {
    if (
      this.changeDateForm.value.dateDayView !== undefined &&
      this.changeDateForm.value.dateDayView !== null
    ) {
      this.currentDate = this.changeDateForm.value.dateDayView;
    }
  }

  // navigate through days and months

  public nextMonth(): void {
    const nextMonthDate = new Date(this.currentDate.getTime());
    nextMonthDate.setMonth(nextMonthDate.getMonth() + 1);
    this.currentDate = new Date(nextMonthDate.getTime());
    this.processing = true;
  }

  public previousMonth(): void {
    const previousMonthDate = new Date(this.currentDate.getTime());
    previousMonthDate.setMonth(previousMonthDate.getMonth() - 1);
    this.currentDate = new Date(previousMonthDate.getTime());
    this.processing = true;
  }

  public previousDay() {
    const previousDayDate = new Date(this.currentDate.getTime());
    previousDayDate.setDate(previousDayDate.getDate() - 1);
    this.currentDate = new Date(previousDayDate.getTime());
    this.processing = true;
  }

  public nextDay() {
    const nextDayDate = new Date(this.currentDate.getTime());
    nextDayDate.setDate(nextDayDate.getDate() + 1);
    this.currentDate = new Date(nextDayDate.getTime());
    this.processing = true;
  }

  public previousWeek() {
    const previousWeekDate = new Date(this.currentDate.getTime());
    previousWeekDate.setDate(previousWeekDate.getDate() - 7);
    this.currentDate = new Date(previousWeekDate.getTime());
    this.processing = true;
  }

  public nextWeek() {
    const nextWeekDate = new Date(this.currentDate.getTime());
    nextWeekDate.setDate(nextWeekDate.getDate() + 7);
    this.currentDate = new Date(nextWeekDate.getTime());
    this.processing = true;
  }
}
