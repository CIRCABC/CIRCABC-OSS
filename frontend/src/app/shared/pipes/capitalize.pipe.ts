import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pure Angular pipe (used in templates as `cbcCapitalize`) that capitalizes
 * the first letter of each space-separated word in the given string.
 *
 * The input is split on spaces and each part has its first character
 * upper-cased while the remainder is left untouched. The result is trimmed
 * of surrounding whitespace before being returned. Being a pure pipe, it is
 * only re-evaluated when the input reference changes.
 */
@Pipe({
  name: 'cbcCapitalize',
  pure: true,
})
export class CapitalizePipe implements PipeTransform {
  /**
   * Capitalizes the first character of each whitespace-separated word in the
   * provided value.
   *
   * @param value The string to transform. Falsy values (e.g. `undefined`,
   * empty string) yield an empty string.
   * @returns The transformed, trimmed string with capitalized word initials.
   */
  public transform(value: string): string {
    let res = '';
    if (value) {
      const parts = value.split(' ');
      for (const part of parts) {
        res = ` ${part.charAt(0).toUpperCase()}${part.slice(1)}`;
      }
    }
    return res.trim();
  }
}
