import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pure Angular pipe (selector `cbcTaggedToPlainText`) that strips HTML/XML
 * markup from a string, returning only its plain-text content.
 *
 * It removes every tag matching `<...>` (for example rich-text or sanitized
 * markup) so the remaining text can be displayed in contexts where markup is
 * not desired, such as previews, tooltips or list summaries.
 *
 * @example
 * ```html
 * {{ '<p>Hello <b>world</b></p>' | cbcTaggedToPlainText }} <!-- "Hello world" -->
 * ```
 */
@Pipe({
  name: 'cbcTaggedToPlainText',
  pure: true,
})
export class TaggedToPlainTextPipe implements PipeTransform {
  /**
   * Removes all HTML/XML tags from the provided value.
   *
   * @param value - The (possibly `undefined`) tagged string to convert to
   * plain text.
   * @returns The input string with every `<...>` tag removed, or an empty
   * string when `value` is `undefined`, empty or otherwise falsy.
   */
  public transform(value: string | undefined): string {
    return value ? String(value).replaceAll(/<[^>]+>/gm, '') : ''; // NOSONAR - negated char class, no backtracking
  }
}
