import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Translation, TranslocoLoader } from '@jsverse/transloco';
import { APP_VERSION } from 'app/core/variables';
import { environment } from 'environments/environment';

/**
 * Transloco loader responsible for fetching translation files over HTTP.
 *
 * Implements the {@link TranslocoLoader} contract so the Transloco runtime can
 * lazily load per-language translation JSON on demand. Translation assets are
 * version-scoped (using {@link APP_VERSION}) to guarantee cache busting between
 * deployments.
 *
 * Key collaborators:
 * - {@link HttpClient} — performs the GET request for the translation file.
 * - {@link APP_VERSION} injection token — provides the current application
 *   version used to build the versioned asset path.
 * - `environment.baseHref` — base URL prefix for the built application.
 */
@Service()
export class TranslocoHttpLoader implements TranslocoLoader {
  /** Angular HTTP client used to retrieve translation JSON files. */
  private readonly http = inject(HttpClient);

  /**
   * Current application version injected from {@link APP_VERSION}. It is used
   * to build a version-scoped path to translation assets so that clients fetch
   * the translations matching the deployed build.
   */
  appVersion!: string;

  /**
   * Initializes the loader by reading the {@link APP_VERSION} injection token
   * and, when a value is present, storing it in {@link appVersion} for use when
   * constructing translation asset URLs.
   */
  constructor() {
    const appVersion = inject(APP_VERSION);

    if (appVersion) {
      this.appVersion = appVersion;
    }
  }

  /**
   * Loads the translation set for the requested language.
   *
   * @param lang - The language code (e.g. `en`, `fr`) whose translation file
   * should be fetched.
   * @returns An observable emitting the {@link Translation} object parsed from
   * the version-scoped `assets/{version}/i18n/{lang}.json` file.
   */
  getTranslation(lang: string) {
    return this.http.get<Translation>(
      `${environment.baseHref}assets/${this.appVersion}/i18n/${lang}.json`
    );
  }
}
