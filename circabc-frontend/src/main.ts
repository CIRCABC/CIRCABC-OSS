import {
  NoPreloading,
  provideRouter,
  withInMemoryScrolling,
  withPreloading,
  withRouterConfig,
  withViewTransitions,
} from '@angular/router';

import {
  ApplicationRef,
  ErrorHandler,
  enableProdMode,
  importProvidersFrom,
  provideAppInitializer,
} from '@angular/core';
import {
  BrowserModule,
  bootstrapApplication,
  enableDebugTools,
} from '@angular/platform-browser';

import {
  HTTP_INTERCEPTORS,
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { ServiceWorkerModule } from '@angular/service-worker';
import { TranslocoModule } from '@jsverse/transloco';
import { appInfo } from 'app/app-info';
import { appRoutes } from 'app/app-routes';
import { AppComponent } from 'app/app.component';
import { BASE_PATH as ARES_BRIDGE_BASE_PATH } from 'app/core/generated/ares-bridge';
import { BASE_PATH } from 'app/core/generated/circabc';
import { BASE_PATH as EU_CAPTCHA_BASE_PATH } from 'app/core/generated/eu-captcha';
import { AuthInterceptor } from 'app/core/interceptors/auth.interceptor';
import { CacheInterceptor } from 'app/core/interceptors/cache.interceptor';
import { ErrorInterceptor } from 'app/core/interceptors/error.interceptor';
import { GroupGetInterceptor } from 'app/core/interceptors/group-get.interceptor';
import { MessageInterceptor } from 'app/core/interceptors/message.interceptor';
import { NoopInterceptor } from 'app/core/interceptors/noop-interceptor';
import {
  RequestCache,
  RequestCacheWithMap,
} from 'app/core/interceptors/request-cache.service';
import { UnauthInterceptor } from 'app/core/interceptors/unauth.interceptor';
import { UiMessageModule } from 'app/core/message/ui-message.module';
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
import { TranslocoRootModule } from 'app/transloco/transloco-root.module';
import { environment } from 'environments/environment';

import { APP_BASE_HREF, I18nSelectPipe } from '@angular/common';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { GroupResolver } from 'app/group/group.resolver';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { BulkDownloadPipe } from 'app/group/library/pipes/bulk-download.pipe';
import { LeaveFileUploadGuard } from 'app/group/guards/leave-file-upload-guard';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';

// Function to handle session storage transfer between tabs
function transferSessionStorage(): Promise<void> {
  return new Promise<void>((resolve) => {
    let dataReceived = false;

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const sessionStorage_transfer = (event: any) => {
      let localEvent = event;
      if (!localEvent) {
        // eslint-disable-next-line
        localEvent = window.event;
      } // ie suq
      if (!localEvent.newValue) {
        return;
      } // do nothing if no value to work with
      if (localEvent.key === 'getSessionStorage') {
        // another tab asked for the sessionStorage -> send it
        localStorage.setItem('sessionStorage', JSON.stringify(sessionStorage));
        // the other tab should now have it, so we're done with it.
        localStorage.removeItem('sessionStorage'); // <- could do short timeout as well.
      } else if (
        localEvent.key === 'sessionStorage' &&
        !sessionStorage.length
      ) {
        // another tab sent data <- get it
        const data = JSON.parse(localEvent.newValue) as Record<string, string>;
        for (const key in data) {
          sessionStorage.setItem(key, data[key]);
        }

        // Session data received, mark as done and resolve
        dataReceived = true;
        resolve();
      }
    };

    // listen for changes to localStorage
    if (window.addEventListener) {
      window.addEventListener('storage', sessionStorage_transfer, false);
    } else {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (window as any).attachEvent('onstorage', sessionStorage_transfer);
    }

    // Ask other tabs for session storage (this is ONLY to trigger event)
    if (sessionStorage.length) {
      // Already has session data, resolve immediately
      resolve();
    } else {
      localStorage.setItem('getSessionStorage', 'dummy');
      localStorage.removeItem('getSessionStorage');

      // The timeout is needed as a fallback in case no other tabs respond
      setTimeout(() => {
        if (!dataReceived) {
          resolve(); // Resolve anyway after timeout
        }
      }, 1000);
    }
  });
}

// Factory function for APP_INITIALIZER
function sessionStorageInitializer() {
  return () => {
    return transferSessionStorage();
  };
}

if (environment.production) {
  enableProdMode();
}
bootstrapApplication(AppComponent, {
  providers: [
    BulkDownloadPipe,
    ClipboardService,
    GroupResolver,
    I18nPipe,
    I18nSelectPipe,
    LeaveFileUploadGuard,
    importProvidersFrom(
      BrowserModule,
      ServiceWorkerModule.register(`${environment.baseHref}ngsw-worker.js`, {
        enabled: environment.production,
        registrationStrategy: 'registerWhenStable:30000',
      }),
      TranslocoModule,
      UiMessageModule,
      TranslocoRootModule
    ),

    provideRouter(
      appRoutes,
      withRouterConfig({ paramsInheritanceStrategy: 'always' }),
      withPreloading(NoPreloading),
      withViewTransitions(),
      withInMemoryScrolling({ scrollPositionRestoration: 'enabled' })
    ),

    {
      provide: APP_BASE_HREF,
      useValue: environment.baseHref.substring(
        0,
        environment.baseHref.length - 1
      ),
    },
    {
      provide: ErrorHandler,
      useClass:
        environment.analyticsSiteId !== '' && environment.analyticsURL !== ''
          ? AppErrorHandler
          : ErrorHandler,
    },
    { provide: BASE_PATH, useValue: environment.circabcURL },
    { provide: ARES_BRIDGE_BASE_PATH, useValue: environment.aresBridgeURL },
    { provide: EU_CAPTCHA_BASE_PATH, useValue: environment.captchaURL },
    { provide: ALF_BASE_PATH, useValue: environment.alfrescoURL },
    { provide: CBC_BASE_PATH, useValue: environment.circabcURL },
    { provide: SERVER_URL, useValue: environment.serverURL },
    { provide: RequestCache, useClass: RequestCacheWithMap },
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: UnauthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: MessageInterceptor, multi: true },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: CacheInterceptor,
      multi: true,
    },
    { provide: HTTP_INTERCEPTORS, useClass: GroupGetInterceptor, multi: true },
    {
      provide: HTTP_INTERCEPTORS,
      useClass:
        environment.analyticsSiteId !== '' && environment.analyticsURL !== ''
          ? ErrorInterceptor
          : NoopInterceptor,
      multi: true,
    },
    { provide: APP_VERSION, useValue: appInfo.appVersion },
    { provide: APP_ALF_VERSION, useValue: appInfo.alfVersion },
    { provide: NODE_NAME, useValue: environment.nodeName },
    { provide: BUILD_DATE, useValue: appInfo.buildDate },

    // Replace the APP_INITIALIZER provider with provideAppInitializer
    provideAppInitializer(sessionStorageInitializer()),

    provideHttpClient(withInterceptorsFromDi()),
    provideAnimations(),
    providePrimeNG({
      theme: {
        preset: Aura,
      },
    }),
  ],
}).then((module) => {
  if (!environment.production) {
    const applicationRef = module.injector.get(ApplicationRef);
    const appComponent = applicationRef.components[0];
    enableDebugTools(appComponent);
  }
});
