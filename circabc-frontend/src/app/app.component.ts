import { filter } from 'rxjs/operators';
import { firstValueFrom, Observable, Subscription } from 'rxjs';

import { Component, OnInit, OnDestroy, Inject } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';

import { getBrowserLang, TranslocoService } from '@ngneat/transloco';

import { AnalyticsService } from 'app/core//analytics.service';

import { PrimeNGConfig } from 'primeng/api';
import { DOCUMENT } from '@angular/common';
@Component({
  selector: 'cbc-app',
  templateUrl: './app.component.html',
  preserveWhitespaces: true,
})
export class AppComponent implements OnInit, OnDestroy {
  navEnd$: Observable<NavigationEnd>;
  navigationSubscription!: Subscription;
  languageChangeSubscription!: Subscription;

  public constructor(
    private translateService: TranslocoService,
    router: Router,
    private analyticsService: AnalyticsService,
    private primeNGConfig: PrimeNGConfig,
    @Inject(DOCUMENT) private document: Document
  ) {
    this.navEnd$ = router.events.pipe(
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      filter((evt: any) => evt instanceof NavigationEnd)
    ) as Observable<NavigationEnd>;

    this.languageChangeSubscription =
      this.translateService.langChanges$.subscribe(async (lang: string) => {
        const translation = await firstValueFrom(
          this.translateService.selectTranslateObject('primeng')
        );
        try {
          this.primeNGConfig.setTranslation(translation);
          this.document.documentElement.lang = lang;
        } catch (error) {
          console.error(`can not load translation for ${lang}`);
          console.error(error);
        }
      });
    // this language will be used as a fallback when a translation isn't found in the current language
    this.translateService.setDefaultLang('en');

    // the lang to use, if the lang isn't available, it will use the language from the browser
    // if the browser language could not be read, it will use english as language
    const browserLang = getBrowserLang();
    if (browserLang !== undefined) {
      this.translateService.setActiveLang(browserLang);
    } else {
      this.translateService.setActiveLang('en');
    }

    // reset the system message information display
    localStorage.setItem('systemMessageAlreadyShown', '-1');
  }
  ngOnDestroy(): void {
    this.navigationSubscription.unsubscribe();
    this.languageChangeSubscription.unsubscribe();
  }
  ngOnInit(): void {
    this.analyticsService.init();
    this.trackAnalytics();

    // tab sessionStorage replication
    //  https://stackoverflow.com/questions/20325763/browser-sessionstorage-share-between-tabs

    // transfers sessionStorage from one tab to another
    this.transferSessionStorage();
  }

  private trackAnalytics() {
    this.navigationSubscription = this.navEnd$.subscribe(
      (_evt: NavigationEnd) => {
        setTimeout(() => {
          this.analyticsService.trackPageChange();
        }, 1000);
      }
    );
  }

  private transferSessionStorage() {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const sessionStorage_transfer = (event: any) => {
      if (!event) {
        // eslint-disable-next-line
        event = window.event;
      } // ie suq
      if (!event.newValue) {
        return;
      } // do nothing if no value to work with
      if (event.key === 'getSessionStorage') {
        // another tab asked for the sessionStorage -> send it
        localStorage.setItem('sessionStorage', JSON.stringify(sessionStorage));
        // the other tab should now have it, so we're done with it.
        localStorage.removeItem('sessionStorage'); // <- could do short timeout as well.
      } else if (event.key === 'sessionStorage' && !sessionStorage.length) {
        // another tab sent data <- get it
        const data = JSON.parse(event.newValue);
        // eslint-disable-next-line guard-for-in
        for (const key in data) {
          // eslint-disable-next-line no-restricted-syntax
          sessionStorage.setItem(key, data[key]);
        }
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
    if (!sessionStorage.length) {
      localStorage.setItem('getSessionStorage', 'dummy');
      localStorage.removeItem('getSessionStorage');
    }
  }
}
