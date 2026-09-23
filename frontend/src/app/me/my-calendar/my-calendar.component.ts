import { DatePipe, I18nSelectPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnChanges,
  OnInit,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AgendaHelperService } from 'app/core/agenda-helper.service';
import { UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import {
  getFormattedDate as getFormattedDateGlobal,
  getFullDate,
} from 'app/core/util';
import { CalendarComponent } from 'app/group/agenda/calendar/calendar.component';
import { DayComponent } from 'app/group/agenda/day/day.component';
import { WeekComponent } from 'app/group/agenda/week/week.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';

/**
 * Personal calendar component for the current user's "Me" area.
 *
 * Renders the signed-in user's aggregated calendar of events, offering
 * day, week and month views. It extends {@link CalendarComponent} to reuse
 * the shared agenda rendering logic (day rows, week grid, month navigation)
 * while sourcing events from the current user's own events feed rather than
 * from a specific interest group agenda.
 *
 * Responsibilities:
 * - Initialise the calendar to today's date and restore the previously
 *   selected view from {@link AgendaHelperService}.
 * - Fetch the current user's future events via {@link UserService}.
 * - Provide view switching (day/week/month), work-week toggling, day-hour
 *   range selection and date navigation controls used by the template.
 *
 * Key collaborators:
 * - {@link UserService} — retrieves the current user's events for a period.
 * - {@link LoginService} — resolves the current username used as the user id.
 * - {@link AgendaHelperService} — persists/restores the selected view state.
 * - {@link FormBuilder} — builds the reactive date-picker form.
 */
@Component({
  selector: 'cbc-my-calendar',
  templateUrl: './my-calendar.component.html',
  styleUrl: './my-calendar.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatDatepickerModule,
    MatInputModule,
    MatFormFieldModule,
    SpinnerComponent,
    RouterLink,
    DayComponent,
    WeekComponent,
    DatePipe,
    I18nSelectPipe,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class MyCalendarComponent
  extends CalendarComponent
  implements OnInit, OnChanges
{
  /** API client used to fetch the current user's events for a given period. */
  private readonly userService = inject(UserService);
  /** Provides the current user's identity (username) for event lookups. */
  private readonly loginService = inject(LoginService);
  /** Persists and restores the selected calendar view (day/week/month). */
  private readonly agendaHelperService = inject(AgendaHelperService);
  /** Reactive forms builder used to create the date-picker form. */
  private readonly fb = inject(FormBuilder);
  /**
   * Change detector used to refresh the view after asynchronous data loads.
   *
   * Under OnPush this component's view is only re-checked when marked dirty.
   * The template reads `processing` and `calendarDays`, which the inherited
   * {@link CalendarComponent.initDays} populates from an async continuation
   * (after `await getEvents(...)`). Those plain fields live in the shared
   * parent component and cannot be converted to signals here, so we mark the
   * view for check once each awaited load completes.
   */
  private readonly cdr = inject(ChangeDetectorRef);

  /** Currently active calendar view: `'day'`, `'week'` or `'month'`. */
  public view!: string;
  /** Whether the week view shows only working days (`true`) or the full week. */
  public workWeek = true;
  /** Start hour (0–23) of the visible time range in the day view. */
  public from!: number;
  /** End hour (0–23) of the visible time range in the day view. */
  public to!: number;

  /**
   * Localised month names used to render month labels.
   *
   * @returns The list of month names for the active locale.
   */
  public get monthNames() {
    return this.localizationService.getMonthsNames();
  }

  /**
   * Localised full day names used to render day-of-week labels.
   *
   * @returns The list of day names for the active locale.
   */
  public get completeDayNames() {
    return this.localizationService.getDayNames();
  }

  /** Reactive form backing the date picker used to jump to a specific date. */
  public changeDateForm!: FormGroup;

  /**
   * Angular lifecycle hook that initialises the personal calendar.
   *
   * Sets the current date to today, enables the work-week mode, restores the
   * previously selected view, initialises the day-hour display range and builds
   * the date-picker form immediately. The initial set of days (and their
   * events) is loaded asynchronously via {@link loadDays} so the hook stays
   * synchronous.
   */
  ngOnInit(): void {
    this.date.set(new Date());
    this.workWeek = true;
    this.view = this.agendaHelperService.getMyCalendarViewState();
    this.initDayHourDisplayRange();

    this.changeDateForm = this.fb.group(
      {
        dateDayView: [this.date],
      },
      {
        updateOn: 'change',
      }
    );

    void this.loadDays();
  }

  /**
   * Loads the initial set of calendar days (and their events) and marks the
   * view for check once the asynchronous load completes.
   *
   * @returns A promise that resolves once the initial days have been loaded.
   */
  private async loadDays(): Promise<void> {
    await super.initDays();
    this.cdr.markForCheck();
  }

  /**
   * Sets the default visible time range for the day view (08:00–21:00).
   */
  private initDayHourDisplayRange() {
    this.from = 8;
    this.to = 21;
  }

  /**
   * Retrieves the current user's events for the requested period.
   *
   * Overrides {@link CalendarComponent.getEvents} to source events from the
   * current user's feed. Only future events starting from `startDate` are
   * requested; the end date is intentionally ignored.
   *
   * @param startDate The start of the period for which events are fetched.
   * @param _endDate The end of the period (unused; kept for signature parity).
   * @returns A promise resolving to the user's events for the period.
   */
  public override async getEvents(startDate: Date, _endDate: Date) {
    return await this.userService.getUserEventsPeriodAsync({
      userId: this.getUserId(),
      exactDate: getFullDate(startDate),
      period: 'Future',
    });
  }

  /**
   * Resolves the identifier of the current user.
   *
   * @returns The current user's username used as the event-lookup user id.
   */
  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }

  /**
   * Switches the active calendar view and persists the choice.
   *
   * When switching to the day view, the day-hour display range is reset to
   * its default bounds.
   *
   * @param value The view to activate (`'day'`, `'week'` or `'month'`).
   */
  public selectView(value: string) {
    this.view = value;
    this.agendaHelperService.saveMyCalendarViewState(value);
    if (this.view === 'day') {
      this.initDayHourDisplayRange();
    }
  }

  /**
   * Sets the start hour of the day view's visible time range.
   *
   * @param value The start hour as a string; parsed to a number.
   */
  public selectFrom(value: string) {
    this.from = Number(value);
  }

  /**
   * Sets the end hour of the day view's visible time range.
   *
   * @param value The end hour as a string; parsed to a number.
   */
  public selectTo(value: string) {
    this.to = Number(value);
  }

  /**
   * Toggles between showing only working days and the full week.
   */
  public toggleWorkWeek() {
    this.workWeek = !this.workWeek;
  }

  /**
   * Formats the currently selected date for display.
   *
   * @returns The current date formatted using the global date formatter.
   */
  public getFormattedDate() {
    return getFormattedDateGlobal(this.date());
  }

  /**
   * Applies the date chosen in the date-picker form to the calendar.
   *
   * Does nothing when the form's date value is undefined.
   */
  public applyToCurrentDate() {
    if (this.changeDateForm.value.dateDayView !== undefined) {
      this.date = this.changeDateForm.value.dateDayView;
    }
  }

  // navigate through days and months

  /**
   * Advances the calendar by one month and reloads the visible days.
   *
   * @returns A promise that resolves once the days have been reloaded.
   */
  public async nextMonth() {
    const tmp = new Date(this.date());
    tmp.setMonth(tmp.getMonth() + 1);
    this.date.set(new Date(tmp));
    await super.initDays();
    this.cdr.markForCheck();
  }

  /**
   * Moves the calendar back by one month and reloads the visible days.
   *
   * @returns A promise that resolves once the days have been reloaded.
   */
  public async previousMonth() {
    const tmp = new Date(this.date());
    tmp.setMonth(tmp.getMonth() - 1);
    this.date.set(new Date(tmp));
    await super.initDays();
    this.cdr.markForCheck();
  }

  /**
   * Advances the calendar by one day and reloads the visible days.
   *
   * @returns A promise that resolves once the days have been reloaded.
   */
  public async nextDay() {
    await this.getDays(1);
  }

  /**
   * Moves the calendar back by one day and reloads the visible days.
   *
   * @returns A promise that resolves once the days have been reloaded.
   */
  public async previousDay() {
    await this.getDays(-1);
  }

  /**
   * Moves the calendar back by one week and reloads the visible days.
   *
   * @returns A promise that resolves once the days have been reloaded.
   */
  public async previousWeek() {
    await this.getDays(-7);
  }

  /**
   * Advances the calendar by one week and reloads the visible days.
   *
   * @returns A promise that resolves once the days have been reloaded.
   */
  public async nextWeek() {
    await this.getDays(7);
  }
  /**
   * Shifts the current date by the given number of days and reloads the days.
   *
   * @param numberOfDays The offset to apply: `-7`, `-1`, `1` or `7` days.
   * @returns A promise that resolves once the days have been reloaded.
   */
  private async getDays(numberOfDays: -7 | -1 | 1 | 7) {
    const tmp = new Date(this.date());
    tmp.setDate(tmp.getDate() + numberOfDays);
    this.date.set(new Date(tmp));
    await super.initDays();
    this.cdr.markForCheck();
  }
}
