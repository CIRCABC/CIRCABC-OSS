import { inject, Pipe, PipeTransform } from '@angular/core';

import { TranslocoService } from '@jsverse/transloco';
import { compensateDST } from 'app/core/util';
import { TimeZoneLookupKeys, timeZoneLookup } from './timezonehelper';

/**
 * Impure Angular pipe (`cbcTime`) that formats a date/time value into a
 * locale-aware time string.
 *
 * Given a date, a time and a CIRCABC time-zone key, it resolves the matching
 * IANA/offset time zone via {@link timeZoneLookup}, parses the combined value,
 * compensates for daylight saving time with {@link compensateDST} and renders
 * the time using the currently active Transloco language.
 *
 * The pipe is declared as `pure: false` so it re-evaluates when the active
 * language changes rather than only when its inputs change by reference.
 *
 * Key collaborators:
 * - {@link TranslocoService} — provides the active language used for locale
 *   formatting.
 * - {@link timeZoneLookup} — maps a CIRCABC time-zone key to a parseable
 *   time-zone offset/identifier.
 * - {@link compensateDST} — adjusts the parsed date for daylight saving time.
 */
@Pipe({
  name: 'cbcTime',

  pure: false,
})
export class TimePipe implements PipeTransform {
  /**
   * Transloco service used to resolve the active language, which drives the
   * locale used when formatting the resulting time string.
   */
  private readonly translateService = inject(TranslocoService);

  /**
   * Formats the supplied date/time into a locale-aware time string.
   *
   * @param date - The date part (e.g. `YYYY-MM-DD`). If `undefined`, an empty
   * string is returned.
   * @param time - The time part (e.g. `HH:mm`). If `undefined`, an empty
   * string is returned.
   * @param timeZone - A CIRCABC time-zone key resolved through
   * {@link timeZoneLookup}. If `undefined`, an empty string is returned.
   * @returns The time formatted for the active Transloco language, or an empty
   * string when any argument is `undefined`.
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

    const dateDate = compensateDST(new Date(dateNumber));

    return dateDate.toLocaleTimeString(this.translateService.getActiveLang());
  }
}
