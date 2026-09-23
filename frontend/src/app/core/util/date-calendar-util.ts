import { AbstractControl } from '@angular/forms';
import { pairwise, startWith } from 'rxjs';

/**
 * Utility functions for reconciling calendar date changes emitted by an
 * Angular reactive form control.
 *
 * When a date/time picker steps a single field (minutes, hours, day) across a
 * boundary — for example minutes going from 59 to 0 — the neighbouring fields
 * must be carried over (the hour incremented, the day advanced, the month or
 * year rolled over, etc.). This module inspects consecutive values emitted by
 * a control's `valueChanges` stream and applies those carry-over adjustments so
 * the resulting {@link Date} stays consistent.
 *
 * The primary entry point is {@link setupCalendarDateHandling}; the remaining
 * functions are private helpers.
 */

/**
 * Decomposed view of the individual fields of a {@link Date} used when
 * comparing a previous value against the current one.
 */
interface DateComponents {
  /** Minute of the hour, 0–59. */
  minutes: number;
  /** Hour of the day, 0–23. */
  hours: number;
  /** Day of the month, 1–31. */
  day: number;
  /** Zero-based month, 0 (January) – 11 (December). */
  month: number;
  /** Full four-digit year. */
  year: number;
}

/**
 * Extracts the individual date/time fields from a {@link Date} instance.
 *
 * @param date The date to decompose.
 * @returns The {@link DateComponents} for the given date.
 */
function getDateComponents(date: Date): DateComponents {
  return {
    minutes: date.getMinutes(),
    hours: date.getHours(),
    day: date.getDate(),
    month: date.getMonth(),
    year: date.getFullYear(),
  };
}

/**
 * Computes the last calendar day of the given month, accounting for leap years.
 *
 * @param year The full four-digit year.
 * @param month The zero-based month (0 = January, 11 = December).
 * @returns The last day number of that month (28, 29, 30 or 31).
 */
function getLastDayOfMonth(year: number, month: number): number {
  return new Date(year, month + 1, 0).getDate();
}

/**
 * Adjusts the month (and year where necessary) of a date when a day change has
 * crossed a month boundary.
 *
 * Detects two transitions: moving forward from the last day of the previous
 * month to day 1 (advancing the month, and the year when December rolls into
 * January), and moving backward from day 1 to the last day of the preceding
 * month (retreating the month, and the year when January rolls back into
 * December). The supplied `date` is mutated in place.
 *
 * @param date The date to adjust in place.
 * @param prevDay The day-of-month of the previous value.
 * @param currDay The day-of-month of the current value.
 * @param prevMonth The zero-based month of the previous value.
 * @param currMonth The zero-based month of the current value.
 * @param prevYear The year of the previous value.
 */
function adjustForMonthRollover(
  date: Date,
  prevDay: number,
  currDay: number,
  prevMonth: number,
  currMonth: number,
  prevYear: number
) {
  const isEndOfMonth =
    prevDay === getLastDayOfMonth(prevYear, prevMonth) && currDay === 1;
  const isStartOfMonth =
    prevDay === 1 &&
    currDay === getLastDayOfMonth(date.getFullYear(), date.getMonth());

  if (isEndOfMonth) {
    date.setMonth(date.getMonth() + 1);
    if (prevMonth === 11 && currMonth === 0) {
      date.setFullYear(date.getFullYear() + 1);
    }
  } else if (isStartOfMonth) {
    date.setMonth(date.getMonth() - 1);
    if (prevMonth === 0 && currMonth === 11) {
      date.setFullYear(date.getFullYear() - 1);
    }
  }
}

/**
 * Carries a minute-boundary change over into the higher-order fields.
 *
 * When minutes step from 59 to 0 the hour is incremented (and, when the hour
 * also wraps from 23 to 0, the day is advanced and any month/year rollover
 * applied). The symmetric case where minutes step from 0 to 59 decrements the
 * hour and, on a 0-to-23 hour wrap, retreats the day. The supplied
 * `currentDate` is mutated in place.
 *
 * @param currentDate The date to adjust in place.
 * @param prev The decomposed components of the previous value.
 * @param curr The decomposed components of the current value.
 */
function handleMinuteRollover(
  currentDate: Date,
  prev: DateComponents,
  curr: DateComponents
) {
  if (prev.minutes === 59 && curr.minutes === 0) {
    currentDate.setHours(currentDate.getHours() + 1);
    if (prev.hours === 23 && curr.hours === 0) {
      currentDate.setDate(currentDate.getDate() + 1);
      adjustForMonthRollover(
        currentDate,
        prev.day,
        curr.day,
        prev.month,
        curr.month,
        prev.year
      );
    }
  } else if (prev.minutes === 0 && curr.minutes === 59) {
    currentDate.setHours(currentDate.getHours() - 1);
    if (prev.hours === 0 && curr.hours === 23) {
      currentDate.setDate(currentDate.getDate() - 1);
      adjustForMonthRollover(
        currentDate,
        prev.day,
        curr.day,
        prev.month,
        curr.month,
        prev.year
      );
    }
  }
}

/**
 * Carries an hour-boundary change over into the day (and month/year) fields.
 *
 * Only applies when the minutes are unchanged, so it handles direct hour steps
 * rather than hour changes already accounted for by
 * {@link handleMinuteRollover}. An hour wrap from 23 to 0 advances the day while
 * a wrap from 0 to 23 retreats it, applying any month/year rollover in either
 * case. The supplied `currentDate` is mutated in place.
 *
 * @param currentDate The date to adjust in place.
 * @param prev The decomposed components of the previous value.
 * @param curr The decomposed components of the current value.
 */
function handleHourRollover(
  currentDate: Date,
  prev: DateComponents,
  curr: DateComponents
) {
  if (prev.minutes !== curr.minutes) return;

  if (prev.hours === 23 && curr.hours === 0) {
    currentDate.setDate(currentDate.getDate() + 1);
    adjustForMonthRollover(
      currentDate,
      prev.day,
      curr.day,
      prev.month,
      curr.month,
      prev.year
    );
  } else if (prev.hours === 0 && curr.hours === 23) {
    currentDate.setDate(currentDate.getDate() - 1);
    adjustForMonthRollover(
      currentDate,
      prev.day,
      curr.day,
      prev.month,
      curr.month,
      prev.year
    );
  }
}

/**
 * Subscribes to a form control's value changes and keeps the resulting date
 * consistent when a single field is stepped across a boundary.
 *
 * Each time the control emits, the previous and current values are compared and
 * carry-over adjustments are applied via {@link handleMinuteRollover} and
 * {@link handleHourRollover} (which in turn cascade into day, month and year
 * rollovers). The reconciled date is written back to the control with
 * `emitEvent: false` so the correction does not re-trigger the subscription.
 *
 * @param control The {@link AbstractControl} to monitor for date changes.
 * @returns The RxJS subscription, which the caller should unsubscribe from when
 *   no longer needed to avoid leaks.
 */
export function setupCalendarDateHandling(control: AbstractControl) {
  return control.valueChanges
    .pipe(startWith(control.value), pairwise())
    .subscribe(([previousValue, currentValue]) => {
      if (!(previousValue && currentValue)) return;

      const previousDate = new Date(previousValue);
      const currentDate = new Date(currentValue);

      const prev = getDateComponents(previousDate);
      const curr = getDateComponents(currentDate);

      handleMinuteRollover(currentDate, prev, curr);
      handleHourRollover(currentDate, prev, curr);

      control.setValue(currentDate, { emitEvent: false });
    });
}
