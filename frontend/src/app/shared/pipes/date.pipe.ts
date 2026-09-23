import { inject, Pipe, PipeTransform } from '@angular/core';

import { TranslocoService } from '@jsverse/transloco';
import { TimeZoneLookupKeys, timeZoneLookup } from './timezonehelper';

/**
 * Impure pipe (`cbcDate`) that combines a separate date part, time part and
 * time-zone identifier into a single locale-aware, human-readable date string.
 *
 * The pipe resolves the supplied time-zone key to its UTC offset via
 * {@link timeZoneLookup}, builds an ISO-8601 timestamp from the date, time and
 * offset, and then formats the resulting `Date` according to the currently
 * active Transloco language. English (`en`) is rendered using the `en-GB`
 * locale; all other languages use their own active locale.
 *
 * Declared as `pure: false` so it re-evaluates when the active language
 * changes, keeping the formatted output in sync with the current locale.
 *
 * Key collaborator: {@link TranslocoService} (provides the active language).
 */
@Pipe({
  name: 'cbcDate',

  pure: false,
})
export class DatePipe implements PipeTransform {
  /** Transloco service used to read the currently active UI language. */
  private readonly translateService = inject(TranslocoService);

  /**
   * Formats the given date/time/time-zone triple into a localized date string.
   *
   * @param date - The calendar date part in `YYYY-MM-DD` format, or `undefined`.
   * @param time - The time-of-day part in `HH:mm` format, or `undefined`.
   * @param timeZone - A {@link TimeZoneLookupKeys} identifier used to resolve
   * the UTC offset applied to the timestamp, or `undefined`.
   * @returns The date formatted for the active locale (`en-GB` when the active
   * language is `en`), or an empty string when any argument is `undefined`.
   */
  transform(
    date: string | undefined,
    time: string | undefined,
    timeZone: string | undefined
  ): string {
    if (date === undefined || time === undefined || timeZone === undefined) {
      return '';
    }

    const timezone: string = timeZoneLookup[timeZone as TimeZoneLookupKeys];

    const dateNumber = Date.parse(`${date}T${time}:00.000${timezone}`);

    const dateDate = new Date(dateNumber);

    return dateDate.toLocaleDateString(
      this.translateService.getActiveLang() === 'en'
        ? 'en-GB'
        : this.translateService.getActiveLang()
    );
  }
}
