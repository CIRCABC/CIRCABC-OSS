import { Observable, Subscription, firstValueFrom } from 'rxjs';
import { filter } from 'rxjs/operators';

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';

import { TranslocoService, getBrowserLang } from '@jsverse/transloco';

import { AnalyticsService } from 'app/core/analytics.service';

import { DOCUMENT } from '@angular/common';
import { SwUpdate, VersionReadyEvent } from '@angular/service-worker';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UiMessageSystemComponent } from 'app/core/message/ui-message.system.component';
import { FooterComponent } from 'app/footer/footer.component';
import { TicketValidatorComponent } from 'app/shared/ticket-validator/ticket-validator.component';
import { PrimeNG } from 'primeng/config';
@Component({
  selector: 'cbc-app',
  templateUrl: './app.component.html',
  preserveWhitespaces: true,
  imports: [
    UiMessageSystemComponent,
    RouterOutlet,
    FooterComponent,
    TicketValidatorComponent,
  ],
})
export class AppComponent implements OnInit, OnDestroy {
  navEnd$: Observable<NavigationEnd>;
  navigationSubscription!: Subscription;
  languageChangeSubscription!: Subscription;

  public constructor(
    router: Router,
    swUpdate: SwUpdate,
    private translateService: TranslocoService,
    private analyticsService: AnalyticsService,
    private primeNGConfig: PrimeNG,
    private uiMessageService: UiMessageService,
    @Inject(DOCUMENT) private document: Document
  ) {
    if (swUpdate.isEnabled) {
      swUpdate.versionUpdates
        .pipe(
          filter(
            (evt): evt is VersionReadyEvent => evt.type === 'VERSION_READY'
          )
        )
        .subscribe((_evt) => {
          const infoMessage = translateService.translate('automatic.update');
          this.uiMessageService.addInfoMessage(infoMessage, true, 4);
          setTimeout(() => {
            window.location.reload();
          }, 5000);
        });
    }

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
    const themeStored = localStorage.getItem('theme');
    if (themeStored === 'dark') {
      document.documentElement.setAttribute('theme', themeStored);
    }

    this.analyticsService.init();
    this.trackAnalytics();

    // tab sessionStorage replication is now handled in APP_INITIALIZER
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
}
