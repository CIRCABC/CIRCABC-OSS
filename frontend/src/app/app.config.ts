import { I18nSelectPipe } from '@angular/common';
import {
  provideHttpClient,
  withInterceptors,
  withXhr,
} from '@angular/common/http';
import {
  ApplicationConfig,
  ErrorHandler,
  provideAppInitializer,
  provideZonelessChangeDetection,
} from '@angular/core';
import { DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import {
  NoPreloading,
  provideRouter,
  withInMemoryScrolling,
  withPreloading,
  withRouterConfig,
  withViewTransitions,
} from '@angular/router';
import { provideServiceWorker } from '@angular/service-worker';
import appInfo from 'app/app-info.json';
import { appRoutes } from 'app/app-routes';
import {
  CIRCABC_DATE_FORMATS,
  CircabcDateAdapter,
} from 'app/core/date-formats';
import { BASE_PATH as ARES_BRIDGE_BASE_PATH } from 'app/core/generated/ares-bridge';
import { BASE_PATH } from 'app/core/generated/circabc';
import { BASE_PATH as EU_CAPTCHA_BASE_PATH } from 'app/core/generated/eu-captcha';
import { sessionStorageInitializer } from 'app/core/init/session-storage.initializer';
import { authInterceptor } from 'app/core/interceptors/auth.interceptor';
import { cacheInterceptor } from 'app/core/interceptors/cache.interceptor';
import { errorInterceptor } from 'app/core/interceptors/error.interceptor';
import { groupGetInterceptor } from 'app/core/interceptors/group-get.interceptor';
import { messageInterceptor } from 'app/core/interceptors/message.interceptor';
import {
  RequestCache,
  RequestCacheWithMap,
} from 'app/core/interceptors/request-cache.service';
import { unauthInterceptor } from 'app/core/interceptors/unauth.interceptor';
import {
  ALF_BASE_PATH,
  APP_ALF_VERSION,
  APP_VERSION,
  BUILD_DATE,
  CBC_BASE_PATH,
  NODE_NAME,
  SERVER_URL,
} from 'app/core/variables';
import { AppErrorHandler } from 'app/error-handler/app-error-handler';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { BulkDownloadPipe } from 'app/group/library/pipes/bulk-download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { provideTranslocoRoot } from 'app/transloco/transloco.providers';
import { environment } from 'environments/environment';

/**
 * Builds the root {@link ApplicationConfig} for the CIRCABC application.
 *
 * This is intentionally a factory (rather than a top-level `const`) so it is
 * evaluated **after** `loadRuntimeConfig()` has merged `config.json` into
 * {@link environment}. Environment-derived injection tokens additionally use
 * `useFactory` so their values are read at DI resolution time — this keeps the
 * Docker runtime-configuration mechanism working regardless of module
 * evaluation order.
 *
 * HTTP interceptors are registered functionally via `withInterceptors(...)` in
 * their original chain order (auth, unauth, message, cache, group-get) with the
 * analytics `errorInterceptor` appended only when analytics is configured.
 */
export function createAppConfig(): ApplicationConfig {
  const analyticsEnabled =
    environment.analyticsSiteId !== '' && environment.analyticsURL !== '';

  return {
    providers: [
      provideZonelessChangeDetection(),
      BulkDownloadPipe,
      ClipboardService,
      I18nPipe,
      I18nSelectPipe,
      provideServiceWorker(`${environment.baseHref}ngsw-worker.js`, {
        enabled: environment.production,
        registrationStrategy: 'registerWhenStable:30000',
      }),
      provideTranslocoRoot(),

      provideRouter(
        appRoutes,
        withRouterConfig({ paramsInheritanceStrategy: 'always' }),
        withPreloading(NoPreloading),
        withViewTransitions(),
        withInMemoryScrolling({ scrollPositionRestoration: 'enabled' })
      ),

      {
        provide: ErrorHandler,
        useClass: analyticsEnabled ? AppErrorHandler : ErrorHandler,
      },
      { provide: BASE_PATH, useFactory: () => environment.circabcURL },
      {
        provide: ARES_BRIDGE_BASE_PATH,
        useFactory: () => environment.aresBridgeURL,
      },
      {
        provide: EU_CAPTCHA_BASE_PATH,
        useFactory: () => environment.captchaURL,
      },
      { provide: ALF_BASE_PATH, useFactory: () => environment.alfrescoURL },
      { provide: CBC_BASE_PATH, useFactory: () => environment.circabcURL },
      { provide: SERVER_URL, useFactory: () => environment.serverURL },
      { provide: RequestCache, useClass: RequestCacheWithMap },

      { provide: APP_VERSION, useValue: appInfo.appVersion },
      { provide: APP_ALF_VERSION, useValue: appInfo.alfVersion },
      { provide: NODE_NAME, useFactory: () => environment.nodeName },
      { provide: BUILD_DATE, useValue: appInfo.buildDate },

      // Replace the APP_INITIALIZER provider with provideAppInitializer
      provideAppInitializer(sessionStorageInitializer()),

      provideHttpClient(
        withXhr(),
        withInterceptors([
          authInterceptor,
          unauthInterceptor,
          messageInterceptor,
          cacheInterceptor,
          groupGetInterceptor,
          ...(analyticsEnabled ? [errorInterceptor] : []),
        ])
      ),

      // Angular Material Datepicker configuration
      { provide: DateAdapter, useClass: CircabcDateAdapter },
      { provide: MAT_DATE_FORMATS, useValue: CIRCABC_DATE_FORMATS },
    ],
  };
}
