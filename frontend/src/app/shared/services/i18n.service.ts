import { inject, Service } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';

/**
 * Application-wide internationalization (i18n) service.
 *
 * Acts as a thin facade over the `@jsverse/transloco` {@link TranslocoService},
 * exposing the parts of the Transloco API that the CIRCABC frontend needs for
 * language handling. Centralizing access here keeps components decoupled from
 * the underlying Transloco implementation and provides a single collaborator
 * for querying the active/default language and reacting to language changes.
 *
 * Registered as a root-level singleton via `providedIn: 'root'`.
 */
@Service()
export class I18nService {
  /**
   * Underlying Transloco service that performs the actual translation and
   * language management. Injected as a singleton and used to back all methods
   * of this facade.
   */
  private readonly translocoService = inject(TranslocoService);

  /**
   * Returns the currently active language.
   *
   * @returns The active language code (e.g. `'en'`).
   */
  getActiveLang(): string {
    return this.translocoService.getActiveLang();
  }

  /**
   * Returns the configured default (fallback) language.
   *
   * @returns The default language code (e.g. `'en'`).
   */
  getDefaultLang(): string {
    return this.translocoService.getDefaultLang();
  }

  /**
   * Exposes the Transloco `langChanges$` stream so that components can react
   * to runtime language switches.
   *
   * @returns An observable that emits the active language code whenever the
   * application language changes.
   */
  getLangChanges$() {
    // Expose langChanges$ observable for components to subscribe
    return this.translocoService.langChanges$;
  }

  // Add any other i18n-related methods here as needed
}
