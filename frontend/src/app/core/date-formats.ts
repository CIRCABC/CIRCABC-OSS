import { inject, Service } from '@angular/core';
import { MatDateFormats, NativeDateAdapter } from '@angular/material/core';
import { TranslocoService } from '@jsverse/transloco';

/**
 * Default date format used across most of the application (dd/MM/yyyy).
 * Provide at root level via main.ts.
 */
export const CIRCABC_DATE_FORMATS: MatDateFormats = {
  parse: {
    dateInput: 'dd/MM/yyyy',
  },
  display: {
    dateInput: 'dd/MM/yyyy',
    monthYearLabel: 'MMM yyyy',
    dateA11yLabel: 'dd/MM/yyyy',
    monthYearA11yLabel: 'MMMM yyyy',
  },
};

/**
 * ISO date format used by agenda components (yyyy-MM-dd).
 * Provide at component level where needed.
 */
export const CIRCABC_ISO_DATE_FORMATS: MatDateFormats = {
  parse: {
    dateInput: 'yyyy-MM-dd',
  },
  display: {
    dateInput: 'yyyy-MM-dd',
    monthYearLabel: 'MMM yyyy',
    dateA11yLabel: 'yyyy-MM-dd',
    monthYearA11yLabel: 'MMMM yyyy',
  },
};

/**
 * Custom DateAdapter that:
 * 1. Parses dd/MM/yyyy and yyyy-MM-dd formats (NativeDateAdapter only handles ISO)
 * 2. Formats dates for display using the active MAT_DATE_FORMATS
 * 3. Reads localized day/month names from the 'calendar' i18n key
 * 4. Respects first day of week per locale (Monday for most, Sunday for Portuguese)
 */
@Service({ autoProvided: false })
export class CircabcDateAdapter extends NativeDateAdapter {
  /**
   * Transloco service used to resolve the active language and localized
   * calendar labels (day/month names) from the `calendar` i18n key.
   */
  private readonly translateService = inject(TranslocoService);

  /**
   * Parses a user- or code-supplied value into a {@link Date}.
   *
   * Supports numeric timestamps, the `dd/MM/yyyy` and `yyyy-MM-dd` string
   * formats (validated to reject overflowing values such as `31/02/2020`),
   * and falls back to native `Date` parsing for anything else.
   *
   * @param value The value to parse: a millisecond timestamp or a date string.
   * @returns The parsed {@link Date}, or `null` when the value is empty,
   * not a string/number, or cannot be parsed into a valid date.
   */
  override parse(value: string | number): Date | null {
    if (typeof value === 'number') {
      return new Date(value);
    }

    if (!value || typeof value !== 'string') {
      return null;
    }

    const trimmed = value.trim();

    // Try dd/MM/yyyy
    const dmy = /^(\d{1,2})\/(\d{1,2})\/(\d{4})$/.exec(trimmed);
    if (dmy) {
      const day = +dmy[1];
      const month = +dmy[2] - 1;
      const year = +dmy[3];
      const date = new Date(year, month, day);
      if (
        date.getFullYear() === year &&
        date.getMonth() === month &&
        date.getDate() === day
      ) {
        return date;
      }
      return null;
    }

    // Try yyyy-MM-dd
    const iso = /^(\d{4})-(\d{1,2})-(\d{1,2})$/.exec(trimmed);
    if (iso) {
      const year = +iso[1];
      const month = +iso[2] - 1;
      const day = +iso[3];
      const date = new Date(year, month, day);
      if (
        date.getFullYear() === year &&
        date.getMonth() === month &&
        date.getDate() === day
      ) {
        return date;
      }
      return null;
    }

    // Fallback to native parsing
    const fallback = new Date(trimmed);
    return Number.isNaN(fallback.getTime()) ? null : fallback;
  }

  /**
   * Formats a date for display according to the requested display format.
   *
   * Handles `dd/MM/yyyy`, `yyyy-MM-dd`, `MMM yyyy` (short month name) and
   * `MMMM yyyy` (long month name); any unrecognized format defaults to
   * `dd/MM/yyyy`. Month names are resolved via {@link getMonthNames} so they
   * respect the active locale.
   *
   * @param date The date to format.
   * @param displayFormat One of the display format tokens defined in the
   * active `MAT_DATE_FORMATS`.
   * @returns The formatted date string.
   */
  override format(date: Date, displayFormat: string): string {
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();

    if (displayFormat === 'dd/MM/yyyy') {
      return `${day}/${month}/${year}`;
    }

    if (displayFormat === 'yyyy-MM-dd') {
      return `${year}-${month}-${day}`;
    }

    if (displayFormat === 'MMM yyyy') {
      const monthNames = this.getMonthNames('short');
      return `${monthNames[date.getMonth()]} ${year}`;
    }

    if (displayFormat === 'MMMM yyyy') {
      const monthNames = this.getMonthNames('long');
      return `${monthNames[date.getMonth()]} ${year}`;
    }

    return `${day}/${month}/${year}`;
  }

  /**
   * Returns the locale-specific first day of the week for the calendar.
   *
   * @returns `0` (Sunday) for the Portuguese locale, otherwise `1` (Monday).
   */
  override getFirstDayOfWeek(): number {
    return this.translateService.getActiveLang() === 'pt' ? 0 : 1;
  }

  /**
   * Returns localized day-of-week names, falling back to the native adapter
   * when the requested style is not present in the `calendar` translations.
   *
   * @param style The label style: `long` (`dayNames`), `short`
   * (`dayNamesShort`) or `narrow` (`dayNamesMin`).
   * @returns The array of day names for the requested style.
   */
  override getDayOfWeekNames(style: 'long' | 'short' | 'narrow'): string[] {
    const calendar = this.getCalendarTranslations();
    if (!calendar) {
      return super.getDayOfWeekNames(style);
    }

    if (style === 'long' && calendar['dayNames']) {
      return calendar['dayNames'] as string[];
    }
    if (style === 'short' && calendar['dayNamesShort']) {
      return calendar['dayNamesShort'] as string[];
    }
    if (style === 'narrow' && calendar['dayNamesMin']) {
      return calendar['dayNamesMin'] as string[];
    }

    return super.getDayOfWeekNames(style);
  }

  /**
   * Returns localized month names from the `calendar` translations, falling
   * back to the native adapter when no translated names are available.
   *
   * Note: the same translated `monthNames` array is returned regardless of
   * the requested style.
   *
   * @param style The label style requested by Angular Material.
   * @returns The array of month names.
   */
  override getMonthNames(style: 'long' | 'short' | 'narrow'): string[] {
    const calendar = this.getCalendarTranslations();
    if (!calendar?.['monthNames']) {
      return super.getMonthNames(style);
    }

    return calendar['monthNames'] as string[];
  }

  /**
   * Reads the `calendar` i18n object from Transloco, returning it as a
   * generic record of localized calendar labels.
   *
   * @returns The calendar translations object, or `undefined` when the key
   * is missing or resolving the translations throws.
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
}
