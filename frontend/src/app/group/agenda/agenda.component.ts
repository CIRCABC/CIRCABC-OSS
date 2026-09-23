import { I18nSelectPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
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
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { CalendarComponent } from './calendar/calendar.component';
import { CreateEventComponent } from './create-event/create-event.component';
import { DayComponent } from './day/day.component';
import { DeleteEventComponent } from './delete-event/delete-event.component';
import { WeekComponent } from './week/week.component';

/**
 * Standalone Angular component (`cbc-agenda`) that renders the agenda/calendar
 * area of an interest group.
 *
 * It hosts the month {@link CalendarComponent}, {@link WeekComponent} and
 * {@link DayComponent} views, wiring together navigation between days, weeks and
 * months, the current display date, and the visible hour range for the day view.
 * It also drives the modals used to create ({@link CreateEventComponent}) and
 * delete ({@link DeleteEventComponent}) events.
 *
 * On initialization it resolves the interest group and its event root node from
 * the route, determines the user's event administration permissions, and
 * restores the previously selected view from {@link AgendaHelperService}.
 *
 * Key collaborators:
 * - {@link InterestGroupService} / {@link NodesService} to load the interest
 *   group and its event root node.
 * - {@link AgendaHelperService} to persist and restore the selected view state.
 * - {@link PermissionEvaluatorService} to evaluate event administration rights.
 * - {@link LocalizationService} to obtain localized month and day names.
 */
@Component({
  selector: 'cbc-agenda',
  templateUrl: './agenda.component.html',
  styleUrl: './agenda.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    ReactiveFormsModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    SpinnerComponent,
    CalendarComponent,
    DayComponent,
    WeekComponent,
    CreateEventComponent,
    DeleteEventComponent,
    I18nSelectPipe,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class AgendaComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly agendaHelperService = inject(AgendaHelperService);
  private readonly interestGroupService = inject(InterestGroupService);
  private readonly nodesService = inject(NodesService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly localizationService = inject(LocalizationService);
  private readonly fb = inject(FormBuilder);

  /** Whether the create-event modal is currently displayed. */
  public createEventShowModal = false;
  /** Whether the delete-event confirmation modal is currently displayed. */
  public deleteEventShowModal = false;
  /** The date currently focused by the active view (day/week/month). */
  public currentDate!: Date;
  /** Identifier of the interest group whose agenda is being displayed. */
  public readonly igId = signal('');
  /** The loaded interest group, populated once resolved from the route. */
  public readonly ig = signal<InterestGroup | undefined>(undefined);
  /** Root node under which the group's events are stored. */
  public readonly eventRootNode = signal<ModelNode | undefined>(undefined);
  /** The event selected for deletion, bound to the delete-event modal. */
  public eventToDelete!: EventItemDefinition;
  /** Toggle flag whose change forces the calendar views to re-render. */
  public redisplayCalendar = false;
  /** True once the interest group and its event root node have been loaded. */
  public readonly igLoaded = signal(false);
  /** Generic loading indicator flag. */
  public loading = false;
  /** Whether the item being created is an event (as opposed to another type). */
  public isEvent = true;

  /** Currently selected view: one of `'calendar'`, `'week'` or `'day'`. */
  public view!: string;
  /** Whether the week view shows only working days (work week) or the full week. */
  public workWeek = true;
  /** Start hour (inclusive) of the visible range in the day view. */
  public from!: number;
  /** End hour (inclusive) of the visible range in the day view. */
  public to!: number;

  /** True while a navigation-triggered re-render is in progress. */
  public processing = false;

  /**
   * Localized month names keyed by month, taken from {@link LocalizationService}.
   *
   * @returns A map of month identifiers to their localized display names.
   */
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
  /**
   * Localized day-of-week names keyed by day index, taken from
   * {@link LocalizationService}.
   *
   * @returns A map of day identifiers to their localized display names.
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
  /** Reactive form backing the day-view date picker. */
  public changeDateForm!: FormGroup;

  /**
   * Angular lifecycle hook. Initializes the current date, subscribes to route
   * parameters to load the interest group, restores the persisted view state,
   * sets the default day-view hour range and builds the date-picker form.
   */
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

  /**
   * Resets the day-view visible hour range to its default bounds (08:00–21:00).
   */
  private initDayHourDisplayRange() {
    this.from = 8;
    this.to = 21;
  }

  /**
   * Loads the interest group identified by the route parameters together with
   * its event root node, and marks the group as loaded.
   *
   * @param params Route parameters; the `id` entry identifies the interest group.
   * @returns A promise that resolves once the group and event root node are loaded.
   */
  public async getInterestGroup(params: { [key: string]: string }) {
    this.igId.set(params.id);
    const ig = await this.interestGroupService.getInterestGroupAsync({
      id: this.igId(),
    });
    this.ig.set(ig);
    this.eventRootNode.set(
      await this.nodesService.getNodeAsync({ id: ig.eventId as string })
    );
    this.igLoaded.set(true);
  }

  /**
   * Indicates whether the current user has event administration rights on the
   * group's event root node.
   *
   * @returns `true` if the user is an event administrator, otherwise `false`.
   */
  public isEveAdmin(): boolean {
    const node = this.eventRootNode();
    return node !== undefined && this.permEvalService.isEveAdmin(node);
  }

  /**
   * Opens the create-event modal pre-set to the given calendar day.
   *
   * @param calendarSheetDate The date selected in a calendar sheet.
   */
  public popupCreateEventWithCalendarDay(calendarSheetDate: Date): void {
    this.currentDate = calendarSheetDate;
    this.createEventShowModal = true;
  }

  /**
   * Opens the delete-event confirmation modal for the given event.
   *
   * @param event The event to delete; ignored if `undefined`.
   */
  public popupDeleteEvent(event: EventItemDefinition) {
    if (event !== undefined) {
      this.eventToDelete = event;
      this.deleteEventShowModal = true;
    }
  }

  /**
   * Opens the create-event modal for today's date.
   *
   * @param isEvent Whether the item being created is an event.
   */
  public createEventWithToday(isEvent: boolean): void {
    this.currentDate = new Date();
    this.createEventShowModal = true;
    this.isEvent = isEvent;
  }

  /**
   * Toggles the {@link redisplayCalendar} flag to force the calendar views to
   * re-render after an event has been saved.
   */
  public redisplayCalendarAfterSave() {
    this.redisplayCalendar = !this.redisplayCalendar;
  }

  /**
   * Finalizes a view transition: adopts the given view, persists it and clears
   * the processing indicator.
   *
   * @param view The view that has finished processing.
   */
  public finishProcessing(view: string) {
    this.view = view;
    this.agendaHelperService.saveAgendaViewState(view);
    this.processing = false;
  }

  /**
   * Selects and persists the active view, resetting the day-view hour range
   * when switching to the day view.
   *
   * @param value The view to activate (e.g. `'calendar'`, `'week'`, `'day'`).
   */
  public selectView(value: string) {
    this.view = value;
    this.agendaHelperService.saveAgendaViewState(value);
    if (this.view === 'day') {
      this.initDayHourDisplayRange();
    }
  }

  /**
   * Sets the start hour of the day-view visible range.
   *
   * @param value The start hour as a string; parsed to a number.
   */
  public selectFrom(value: string) {
    this.from = Number(value);
  }

  /**
   * Sets the end hour of the day-view visible range.
   *
   * @param value The end hour as a string; parsed to a number.
   */
  public selectTo(value: string) {
    this.to = Number(value);
  }

  /**
   * Toggles the week view between the work week and the full week.
   */
  public toggleWorkWeek() {
    this.workWeek = !this.workWeek;
  }

  /**
   * Formats the current date using the shared date formatting utility.
   *
   * @returns The formatted representation of {@link currentDate}.
   */
  public getFormattedDate() {
    return getFormattedDateGlobal(this.currentDate);
  }

  /**
   * Applies the date selected in the day-view date picker to
   * {@link currentDate}, ignoring empty selections.
   */
  public applyToCurrentDate() {
    if (
      this.changeDateForm.value.dateDayView !== undefined &&
      this.changeDateForm.value.dateDayView !== null
    ) {
      this.currentDate = this.changeDateForm.value.dateDayView;
    }
  }

  // navigate through days and months

  /**
   * Advances {@link currentDate} by one month and flags a re-render.
   */
  public nextMonth(): void {
    const nextMonthDate = new Date(this.currentDate);
    nextMonthDate.setMonth(nextMonthDate.getMonth() + 1);
    this.currentDate = new Date(nextMonthDate);
    this.processing = true;
  }

  /**
   * Moves {@link currentDate} back by one month and flags a re-render.
   */
  public previousMonth(): void {
    const previousMonthDate = new Date(this.currentDate);
    previousMonthDate.setMonth(previousMonthDate.getMonth() - 1);
    this.currentDate = new Date(previousMonthDate);
    this.processing = true;
  }

  /**
   * Moves {@link currentDate} back by one day and flags a re-render.
   */
  public previousDay() {
    const previousDayDate = new Date(this.currentDate);
    previousDayDate.setDate(previousDayDate.getDate() - 1);
    this.currentDate = new Date(previousDayDate);
    this.processing = true;
  }

  /**
   * Advances {@link currentDate} by one day and flags a re-render.
   */
  public nextDay() {
    const nextDayDate = new Date(this.currentDate);
    nextDayDate.setDate(nextDayDate.getDate() + 1);
    this.currentDate = new Date(nextDayDate);
    this.processing = true;
  }

  /**
   * Moves {@link currentDate} back by one week and flags a re-render.
   */
  public previousWeek() {
    const previousWeekDate = new Date(this.currentDate);
    previousWeekDate.setDate(previousWeekDate.getDate() - 7);
    this.currentDate = new Date(previousWeekDate);
    this.processing = true;
  }

  /**
   * Advances {@link currentDate} by one week and flags a re-render.
   */
  public nextWeek() {
    const nextWeekDate = new Date(this.currentDate);
    nextWeekDate.setDate(nextWeekDate.getDate() + 7);
    this.currentDate = new Date(nextWeekDate);
    this.processing = true;
  }
}
