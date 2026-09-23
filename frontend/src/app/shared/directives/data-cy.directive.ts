// directive like in article https://medium.com/agilix/angular-and-cypress-data-cy-attributes-d698c01df062
import { Directive, ElementRef, inject, Renderer2 } from '@angular/core';
import { environment } from 'environments/environment';
/**
 * Attribute directive that manages the `data-cy` attribute used by Cypress
 * end-to-end tests to reliably target DOM elements.
 *
 * The directive matches any element carrying a `data-cy` attribute. In the
 * production environment it strips the attribute at construction time so that
 * test-only selectors are not shipped to end users, while leaving it intact in
 * all other environments where automated tests may run.
 *
 * Implementation follows the pattern described in
 * {@link https://medium.com/agilix/angular-and-cypress-data-cy-attributes-d698c01df062}.
 *
 * @see environment.environmentType
 */
@Directive({
  selector: '[data-cy]',
})
export class DataCyDirective {
  /**
   * Creates the directive and, when running in the production environment,
   * removes the host element's `data-cy` attribute via {@link Renderer2}.
   *
   * Collaborators are resolved through Angular's `inject()` function:
   * - {@link ElementRef} — reference to the host DOM element.
   * - {@link Renderer2} — used to safely remove the attribute from the element.
   */
  constructor() {
    const el = inject(ElementRef);
    const renderer = inject(Renderer2);

    if (environment.environmentType === 'prod') {
      renderer.removeAttribute(el.nativeElement, 'data-cy');
    }
  }
}
