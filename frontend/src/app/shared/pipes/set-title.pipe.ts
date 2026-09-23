import { inject, Pipe, PipeTransform } from '@angular/core';
import { Title } from '@angular/platform-browser';

/**
 * Impure pipe (`cbcSetTitle`) that sets the browser tab/document title as a
 * side effect while rendering an empty string in the template.
 *
 * Instead of transforming its input into visible output, this pipe uses the
 * Angular `Title` service to update the document title with the provided
 * value. It is declared `pure: false` so that it re-evaluates on each change
 * detection cycle, allowing the title to stay in sync with a dynamically
 * changing value bound in the template.
 *
 * Key collaborator: Angular's {@link Title} service from
 * `@angular/platform-browser`.
 *
 * @example
 * ```html
 * {{ pageTitle | cbcSetTitle }}
 * ```
 */
@Pipe({
  name: 'cbcSetTitle',

  pure: false,
})
export class SetTitlePipe implements PipeTransform {
  /** Angular service used to read and update the document (browser tab) title. */
  private readonly title = inject(Title);

  /**
   * Updates the document title with the given value (when defined) and returns
   * an empty string so the pipe produces no visible output in the template.
   *
   * @param value The title to apply to the document. When `undefined`, the
   * current document title is left unchanged.
   * @returns An empty string, so the pipe renders nothing where it is used.
   */
  public transform(value: string | undefined): string {
    if (value !== undefined) {
      this.title.setTitle(value);
    }
    return '';
  }
}
