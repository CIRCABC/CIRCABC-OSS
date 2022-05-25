import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { ErrorHandler, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ServiceWorkerModule } from '@angular/service-worker';

import { TranslocoModule } from '@ngneat/transloco';

import { AccessDeniedComponent } from 'app/access-denied/access-denied.component';
import { appInfo } from 'app/app-info';
import { AppRoutingModule } from 'app/app-routing.module';
import { AppComponent } from 'app/app.component';
import { BASE_PATH as ARES_BRIDGE_BASE_PATH } from 'app/core/generated/ares-bridge';
import { BASE_PATH } from 'app/core/generated/circabc';
import { AuthInterceptor } from 'app/core/interceptors/auth.interceptor';
import { CacheInterceptor } from 'app/core/interceptors/cache.interceptor';
import { MessageInterceptor } from 'app/core/interceptors/message.interceptor';
import {
  RequestCache,
  RequestCacheWithMap,
} from 'app/core/interceptors/request-cache.service';
import { UnauthInterceptor } from 'app/core/interceptors/unauth.interceptor';
import { ErrorInterceptor } from 'app/core/interceptors/error.interceptor';
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
import { NoContentFoundComponent } from 'app/no-content-found/no-content-found.component';
import { PageNotFoundComponent } from 'app/page-not-found.component';
import { SharedModule } from 'app/shared/shared.module';
import { SwUpdatesModule } from 'app/sw-updates/sw-updates.module';
import { WelcomeModule } from 'app/welcome/welcome.module';
import { environment } from 'environments/environment';
import { MatomoModule } from 'ngx-matomo';
import { TranslocoRootModule } from 'app/transloco/transloco-root.module';
import { FooterComponent } from './footer/footer.component';
import { APP_BASE_HREF } from '@angular/common';
import { BrowseComponent } from 'app/browse/browse.component';
import { NoopInterceptor } from 'app/core/interceptors/noop-interceptor';

@NgModule({
  imports: [
    BrowserModule,
    MatomoModule,
    HttpClientModule,
    BrowserAnimationsModule,
    TranslocoModule,
    AppRoutingModule,
    SharedModule,
    WelcomeModule,
    UiMessageModule,
    SwUpdatesModule,
    ServiceWorkerModule.register(`${environment.baseHref}ngsw-worker.js`, {
      enabled: environment.production,
      registrationStrategy: 'registerImmediately',
    }),
    TranslocoRootModule,
  ],
  declarations: [
    AccessDeniedComponent,
    AppComponent,
    PageNotFoundComponent,
    NoContentFoundComponent,
    FooterComponent,
    BrowseComponent,
  ],
  providers: [
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
        environment.analyticsSiteId > 0 && environment.analyticsURL.length > 0
          ? AppErrorHandler
          : ErrorHandler,
    },
    { provide: BASE_PATH, useValue: environment.circabcURL },
    { provide: ARES_BRIDGE_BASE_PATH, useValue: environment.aresBridgeURL },
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
    {
      provide: HTTP_INTERCEPTORS,
      useClass:
        environment.analyticsSiteId > 0 && environment.analyticsURL.length > 0
          ? ErrorInterceptor
          : NoopInterceptor,
      multi: true,
    },
    { provide: APP_VERSION, useValue: appInfo.appVersion },
    { provide: APP_ALF_VERSION, useValue: appInfo.alfVersion },
    { provide: NODE_NAME, useValue: environment.nodeName },
    { provide: BUILD_DATE, useValue: appInfo.buildDate },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
