import { provideTransloco } from '@jsverse/transloco';
import { TranslocoHttpLoader } from 'app/transloco/transloco-http-loader';
import { environment } from 'environments/environment';

/**
 * Configures and provides Transloco internationalization for the application
 * in a standalone (NgModule-free) bootstrap.
 *
 * Responsibilities:
 * - Declares the full set of available EU languages and sets English (`en`) as
 *   both the default and fallback language.
 * - Enables re-rendering on language change and production mode based on the
 *   current environment.
 * - Configures retry and missing-key handling (logging missing keys and using
 *   the fallback translation).
 * - Registers {@link TranslocoHttpLoader} as the loader used to fetch
 *   translation files at runtime.
 *
 * Standalone components consume Transloco directives/pipes by importing
 * `TranslocoModule` directly, so no module re-export is required here.
 *
 * @returns The Transloco providers to include in the application config.
 */
export function provideTranslocoRoot() {
  return provideTransloco({
    config: {
      availableLangs: [
        'bg',
        'cs',
        'da',
        'de',
        'el',
        'en',
        'es',
        'et',
        'fi',
        'fr',
        'ga',
        'hr',
        'it',
        'lv',
        'lt',
        'hu',
        'mt',
        'nl',
        'pl',
        'pt',
        'ro',
        'sk',
        'sl',
        'sv',
      ],
      defaultLang: 'en',
      reRenderOnLangChange: true,
      prodMode: environment.production,
      fallbackLang: 'en',
      failedRetries: 1,
      missingHandler: {
        logMissingKey: true,
        useFallbackTranslation: true,
      },
    },
    loader: TranslocoHttpLoader,
  });
}
