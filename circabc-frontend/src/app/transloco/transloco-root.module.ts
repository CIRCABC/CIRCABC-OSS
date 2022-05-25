/* eslint-disable max-classes-per-file */
import { HttpClient } from '@angular/common/http';
import {
  TRANSLOCO_LOADER,
  Translation,
  TranslocoLoader,
  TRANSLOCO_CONFIG,
  translocoConfig,
  TranslocoModule,
} from '@ngneat/transloco';
import { Injectable, NgModule } from '@angular/core';
import { environment } from 'environments/environment';

@Injectable({ providedIn: 'root' })
export class TranslocoHttpLoader implements TranslocoLoader {
  constructor(private http: HttpClient) {}

  getTranslation(lang: string) {
    return this.http.get<Translation>(
      `${environment.baseHref}assets/i18n/${lang}.json`
    );
  }
}

@NgModule({
  exports: [TranslocoModule],
  providers: [
    {
      provide: TRANSLOCO_CONFIG,
      useValue: translocoConfig({
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
      }),
    },
    { provide: TRANSLOCO_LOADER, useClass: TranslocoHttpLoader },
  ],
})
export class TranslocoRootModule {}
