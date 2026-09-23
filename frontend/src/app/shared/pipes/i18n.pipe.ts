import { inject, Pipe, PipeTransform } from '@angular/core';
import { I18nService } from '../services/i18n.service';

/**
 * Impure Angular pipe (`cbcI18n`) that resolves a multilingual text map to a
 * single localized string for display.
 *
 * Multilingual (ML) text is represented as an object keyed by locale
 * (e.g. `{ 'en': 'Hello', 'fr': 'Bonjour' }`). Given such a map, the pipe
 * picks the most appropriate value using the following precedence:
 * 1. If only a single non-empty value exists, that value is returned.
 * 2. Otherwise, the value for the currently active language is returned when
 *    present.
 * 3. Otherwise, the value for the application default language is returned
 *    when present.
 * 4. As a fallback, the first non-empty value found is returned.
 *
 * It also tolerates legacy CIRCABC data where locales were stored with a
 * region suffix (e.g. `en-US`, `fr-FR`) by matching on a `xx-` prefix.
 *
 * The pipe is declared as `pure: false` so it re-evaluates when the active
 * language changes, keeping displayed text in sync with the current locale.
 *
 * Key collaborator: {@link I18nService}, used to obtain the active and default
 * languages.
 */
@Pipe({
  name: 'cbcI18n',

  pure: false,
})
export class I18nPipe implements PipeTransform {
  /** Service used to resolve the active and default application languages. */
  private readonly i18nService = inject(I18nService);

  /**
   * Resolves a multilingual text map into the best matching localized string.
   *
   * @param mltext Map of locale keys to their translated values, or
   *   `undefined`.
   * @returns The localized string according to the resolution precedence
   *   described on the pipe, or an empty string when `mltext` is `undefined`
   *   or contains no non-empty values.
   */
  transform(mltext: { [key: string]: string } | undefined): string {
    if (mltext === undefined) {
      return '';
    }
    let result: string;
    const lang = this.i18nService.getActiveLang();
    const defaultLang = this.i18nService.getDefaultLang();

    if (this.countNonEmptyValues(mltext) === 1) {
      result = this.getValue(mltext, this.getOnlyOneValueKey(mltext));
    } else if (
      this.countNonEmptyValues(mltext) > 1 &&
      this.hasLocale(mltext, lang)
    ) {
      result = this.getValue(mltext, lang);
    } else if (
      this.countNonEmptyValues(mltext) > 1 &&
      this.hasLocale(mltext, defaultLang)
    ) {
      result = this.getValue(mltext, defaultLang);
    } else {
      result = this.getFirstValidValue(mltext);
    }
    return result;
  }

  /**
   * Determines whether the multilingual map contains an entry for the given
   * locale, tolerating legacy region-suffixed keys.
   *
   * When the locale has no region part (no `-`) and is not present verbatim,
   * it also checks for a key beginning with `locale-` (e.g. `en` matches
   * `en-US`), accommodating older CIRCABC data.
   *
   * @param mltext Map of locale keys to translated values.
   * @param locale The locale to look for.
   * @returns `true` if a matching locale key exists, otherwise `false`.
   */
  private hasLocale(
    mltext: { [key: string]: string },
    locale: string
  ): boolean {
    const keys = Object.keys(mltext);
    const firstLook = keys.includes(locale);

    // fix, we search for 'xx-', because some old values from CIRCABC has title saved like { en-US: 'eeeee', fr-FR: 'ffff', es: 'erjlkg'}
    if (!(firstLook || locale.includes('-'))) {
      return keys.includes(`${locale}-`);
    }
    return keys.includes(locale);
  }

  /**
   * Counts how many locale entries in the map hold a non-empty value.
   *
   * @param mltext Map of locale keys to translated values.
   * @returns The number of entries whose resolved value is not an empty
   *   string.
   */
  private countNonEmptyValues(mltext: { [key: string]: string }): number {
    let i = 0;
    const keys = Object.keys(mltext);

    for (const k of keys) {
      if (this.getValue(mltext, k) !== '') {
        i += 1;
      }
    }

    return i;
  }

  /**
   * Returns the locale key of the single non-empty entry in the map.
   *
   * Intended to be called when exactly one non-empty value exists; if several
   * are present, the key of the last non-empty entry encountered is returned.
   *
   * @param mltext Map of locale keys to translated values.
   * @returns The matching locale key, or an empty string if none is found.
   */
  private getOnlyOneValueKey(mltext: { [key: string]: string }): string {
    let result = '';
    const keys = Object.keys(mltext);

    for (const k of keys) {
      if (this.getValue(mltext, k) !== '') {
        result = k;
      }
    }

    return result;
  }

  /**
   * Returns the first non-empty value found while iterating the map's keys.
   *
   * Used as a last-resort fallback when neither the active nor the default
   * language could be resolved.
   *
   * @param mltext Map of locale keys to translated values.
   * @returns The first non-empty translated value, or an empty string if all
   *   values are empty.
   */
  private getFirstValidValue(mltext: { [key: string]: string }): string {
    let result = '';
    const keys = Object.keys(mltext);

    for (const k of keys) {
      if (this.getValue(mltext, k) !== '') {
        result = this.getValue(mltext, k);
        break;
      }
    }

    return result;
  }

  /**
   * Resolves the translated value for a given locale, tolerating legacy
   * region-suffixed keys.
   *
   * If the locale is present verbatim or already carries a region part
   * (contains `-`), the first key containing the locale substring is used;
   * otherwise a key containing `locale-` is matched (e.g. `en` resolves
   * `en-US`).
   *
   * @param mltext Map of locale keys to translated values.
   * @param locale The locale whose value should be resolved.
   * @returns The matching translated value, or an empty string when no key
   *   matches or the matched value is nullish.
   */
  private getValue(mltext: { [key: string]: string }, locale: string): string {
    const keys = Object.keys(mltext);
    const firstLook = keys.includes(locale);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let resultKey: any;

    // fix, we search for 'xx-', because some old values from CIRCABC has title saved like { en-US: 'eeeee', fr-FR: 'ffff', es: 'erjlkg'}
    if (firstLook || locale.includes('-')) {
      resultKey = keys.find((key) => key.includes(locale));
    } else {
      resultKey = keys.find((key) => key.includes(`${locale}-`));
    }

    if (resultKey !== undefined) {
      return mltext[resultKey] ?? '';
    }
    return '';
  }
}
