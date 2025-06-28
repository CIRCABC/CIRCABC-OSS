/* eslint-disable max-classes-per-file */
import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, NgModule } from '@angular/core';
import {
  Translation,
  TranslocoLoader,
  TranslocoModule,
  provideTransloco,
} from '@jsverse/transloco';
import { APP_VERSION } from 'app/core/variables';
import { environment } from 'environments/environment';

@Injectable({ providedIn: 'root' })
export class TranslocoHttpLoader implements TranslocoLoader {
  appVersion!: string;
  constructor(
    private http: HttpClient,
    @Inject(APP_VERSION) appVersion: string
  ) {
    if (appVersion) {
      this.appVersion = appVersion;
    }
  }

  getTranslation(lang: string) {
    return this.http.get<Translation>(
      `${environment.baseHref}assets/${this.appVersion}/i18n/${lang}.json`
    );
  }
}

@NgModule({
  exports: [TranslocoModule],
  providers: [
    provideTransloco({
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
    }),
  ],
})
export class TranslocoRootModule {}
