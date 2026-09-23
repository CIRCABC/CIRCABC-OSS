import { inject, Service } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';

/**
 * Root-scoped service that provides locale-aware calendar labels
 * (weekday names, month names and the first day of the week).
 *
 * It resolves localized strings from the active language via the
 * {@link TranslocoService} `calendar` translation object and falls back to
 * hard-coded English names when translations are unavailable. Resolved
 * results are memoized per language/format so repeated lookups avoid
 * re-reading the translation store.
 *
 * Key collaborators:
 * - {@link TranslocoService}: supplies the active language and the
 *   `calendar` translation object used to build the localized labels.
 */
@Service()
export class LocalizationService {
  /** Transloco service used to read the active language and calendar translations. */
  private readonly translateService = inject(TranslocoService);

  /**
   * Memoization cache for day-name maps, keyed by
   * `firstDay-name-numberOfDays-activeLang`.
   */
  private readonly cacheDayNames = new Map<string, { [key: string]: string }>();
  /**
   * Memoization cache for month-name maps, keyed by the active language.
   */
  private readonly cacheMonthNames = new Map<
    string,
    { [key: string]: string }
  >();
  /**
   * Builds a map of localized weekday names indexed by position.
   *
   * The result is cached per language/format combination when it is derived
   * from actual translations (fallback English names are not cached).
   *
   * @param firstDay - The day the week starts on. When `'Monday'`, the array
   * is rotated so Monday becomes index `0`. Defaults to `'Sunday'`.
   * @param name - Whether to return `'full'` (e.g. "Monday") or `'short'`
   * (e.g. "Mon") names. Defaults to `'full'`.
   * @param numberOfDays - Number of days to include: `5` (work week) or `7`
   * (full week). Defaults to `7`.
   * @returns A record mapping zero-based day indices to their localized names.
   */
  getDayNames(
    firstDay: 'Sunday' | 'Monday' = 'Sunday',
    name: 'full' | 'short' = 'full',
    numberOfDays: 5 | 7 = 7
  ): { [key: string]: string } {
    const cacheKey = `${firstDay}-${name}-${numberOfDays}-${this.translateService.getActiveLang()}`;

    if (this.cacheDayNames.has(cacheKey)) {
      return this.cacheDayNames.get(cacheKey) as { [key: string]: string };
    }

    const dayNamesArray = this.getDayNamesArray(name);
    const dayNames = dayNamesArray.data;
    if (firstDay === 'Monday') {
      dayNames.push(dayNames.shift() as string);
    }
    let result: { [key: string]: string };
    if (numberOfDays === 5) {
      result = {
        0: dayNames[0],
        1: dayNames[1],
        2: dayNames[2],
        3: dayNames[3],
        4: dayNames[4],
      };
    } else {
      result = {
        0: dayNames[0],
        1: dayNames[1],
        2: dayNames[2],
        3: dayNames[3],
        4: dayNames[4],
        5: dayNames[5],
        6: dayNames[6],
      };
    }
    if (dayNamesArray.shouldCache) {
      this.cacheDayNames.set(cacheKey, result);
    }
    return result;
  }
  /**
   * Builds a map of localized month names indexed by month number (`1`–`12`).
   *
   * The result is cached per active language when it is derived from actual
   * translations (fallback English names are not cached).
   *
   * @returns A record mapping one-based month numbers to their localized names.
   */
  getMonthsNames(): { [key: string]: string } {
    const cacheKey = `${this.translateService.getActiveLang()}`;
    if (this.cacheMonthNames.has(cacheKey)) {
      return this.cacheMonthNames.get(cacheKey) as { [key: string]: string };
    }
    const monthsNamesArray = this.getMonthsNamesArray();
    const monthNames = monthsNamesArray.data;
    const result = {
      1: monthNames[0],
      2: monthNames[1],
      3: monthNames[2],
      4: monthNames[3],
      5: monthNames[4],
      6: monthNames[5],
      7: monthNames[6],
      8: monthNames[7],
      9: monthNames[8],
      10: monthNames[9],
      11: monthNames[10],
      12: monthNames[11],
    };
    if (monthsNamesArray.shouldCache) {
      this.cacheMonthNames.set(cacheKey, result);
    }
    return result;
  }
  /**
   * Resolves the ordered list of weekday names for the active language.
   *
   * @param name - Whether to return `'full'` or `'short'` weekday names.
   * @returns An object holding the seven-element name `data` array and a
   * `shouldCache` flag that is `true` only when the names came from
   * translations rather than the English fallback.
   */
  private getDayNamesArray(name: 'full' | 'short'): {
    data: string[];
    shouldCache: boolean;
  } {
    const calendar = this.getCalendarTranslations();
    const result =
      name === 'full' ? calendar?.['dayNames'] : calendar?.['dayNamesShort'];
    if (result !== undefined) {
      return { data: result as string[], shouldCache: true };
    }
    return name === 'full'
      ? {
          data: [
            'Sunday',
            'Monday',
            'Tuesday',
            'Wednesday',
            'Thursday',
            'Friday',
            'Saturday',
          ],
          shouldCache: false,
        }
      : {
          data: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
          shouldCache: false,
        };
  }
  /**
   * Resolves the ordered list of month names for the active language.
   *
   * @returns An object holding the twelve-element name `data` array and a
   * `shouldCache` flag that is `true` only when the names came from
   * translations rather than the English fallback.
   */
  private getMonthsNamesArray(): {
    data: readonly string[];
    shouldCache: boolean;
  } {
    const calendar = this.getCalendarTranslations();
    const result: undefined | readonly string[] = calendar?.['monthNames'] as
      string[] | undefined;

    if (result !== undefined) {
      return { data: result, shouldCache: true };
    }
    return {
      data: [
        'January',
        'February',
        'March',
        'April',
        'May',
        'June',
        'July',
        'August',
        'September',
        'October',
        'November',
        'December',
      ],
      shouldCache: false,
    };
  }
  /**
   * Reads the `calendar` translation object for the active language.
   *
   * @returns The calendar translation record, or `undefined` if the lookup
   * fails or the key is not defined.
   */
  private getCalendarTranslations(): Record<string, unknown> | undefined {
    try {
      return this.translateService.translateObject(
        'calendar'
      ) as unknown as Record<string, unknown>;
    } catch {
      return undefined;
    }
  }
  /**
   * Returns the index of the first day of the week for the active language.
   *
   * @returns `0` (Sunday) for Portuguese (`'pt'`) and `1` (Monday) otherwise.
   */
  getFirstDayOfWeek(): number {
    if (this.translateService.getActiveLang() === 'pt') {
      return 0;
    }
    return 1;
  }
}
