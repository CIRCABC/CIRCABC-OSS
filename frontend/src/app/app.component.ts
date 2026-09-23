import { DOCUMENT } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { SwUpdate, VersionReadyEvent } from '@angular/service-worker';

import {
  getBrowserLang,
  TranslocoModule,
  TranslocoService,
} from '@jsverse/transloco';

import { AnalyticsService } from 'app/core/analytics.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UiMessageSystemComponent } from 'app/core/message/ui-message.system.component';
import { FooterComponent } from 'app/footer/footer.component';
import { TicketValidatorComponent } from 'app/shared/ticket-validator/ticket-validator.component';
import { Observable, Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
/**
 * Root component of the CIRCABC single-page application.
 *
 * Rendered under the `cbc-app` selector, it defines the application shell and
 * hosts the primary layout: the global UI message system
 * ({@link UiMessageSystemComponent}), the router outlet that renders the
 * currently routed feature ({@link RouterOutlet}), the EU Login ticket
 * validator ({@link TicketValidatorComponent}) and the application footer
 * ({@link FooterComponent}).
 *
 * Beyond composition, it performs several application-wide bootstrap concerns:
 * - Listens for service-worker version updates and prompts an automatic reload
 *   when a new version is ready.
 * - Configures internationalization defaults through {@link TranslocoService}
 *   (default language, active language derived from the browser) and keeps the
 *   `<html lang>` attribute in sync with language changes.
 * - Applies the default document theme and initializes analytics, tracking a
 *   page change on every {@link NavigationEnd} router event.
 *
 * Key collaborators: {@link TranslocoService}, {@link AnalyticsService},
 * {@link UiMessageService}, {@link Router} and {@link SwUpdate}.
 */
@Component({
  selector: 'cbc-app',
  templateUrl: './app.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    UiMessageSystemComponent,
    RouterOutlet,
    FooterComponent,
    TicketValidatorComponent,
    TranslocoModule,
  ],
})
export class AppComponent implements OnInit, OnDestroy {
  private readonly translateService = inject(TranslocoService);
  private readonly analyticsService = inject(AnalyticsService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly document = inject<Document>(DOCUMENT);

  /**
   * Stream of {@link NavigationEnd} events emitted by the router, filtered
   * from the full router event stream. Used to trigger analytics page-change
   * tracking after each completed navigation.
   */
  navEnd$: Observable<NavigationEnd>;
  /**
   * Subscription to {@link navEnd$} that drives analytics page-change
   * tracking. Established in {@link trackAnalytics} and torn down in
   * {@link ngOnDestroy}. Non-null assertion: assigned during initialization.
   */
  navigationSubscription!: Subscription;
  /**
   * Subscription to the Transloco language-change stream that mirrors the
   * active language onto the `<html lang>` attribute. Torn down in
   * {@link ngOnDestroy}. Non-null assertion: assigned in the constructor.
   */
  languageChangeSubscription!: Subscription;

  /**
   * Wires up application-wide behavior at construction time:
   * - Subscribes to service-worker version updates (when enabled) to notify
   *   the user and reload the page once a new version is ready.
   * - Builds the {@link navEnd$} navigation-end event stream.
   * - Subscribes to Transloco language changes to keep `<html lang>` current.
   * - Sets the fallback (`en`) and active language (browser language, falling
   *   back to `en` when it cannot be determined).
   * - Resets the persisted system-message display flag in `localStorage`.
   */
  public constructor() {
    const router = inject(Router);
    const swUpdate = inject(SwUpdate);
    const translateService = this.translateService;

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
            globalThis.location.reload();
          }, 5000);
        });
    }

    this.navEnd$ = router.events.pipe(
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      filter((evt: any) => evt instanceof NavigationEnd)
    );

    this.languageChangeSubscription =
      this.translateService.langChanges$.subscribe(async (lang: string) => {
        try {
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
    if (browserLang === undefined) {
      this.translateService.setActiveLang('en');
    } else {
      this.translateService.setActiveLang(browserLang);
    }

    // reset the system message information display
    localStorage.setItem('systemMessageAlreadyShown', '-1');
  }
  /**
   * Angular lifecycle hook invoked when the component is destroyed.
   * Unsubscribes from the navigation and language-change subscriptions to
   * prevent memory leaks.
   */
  ngOnDestroy(): void {
    this.navigationSubscription.unsubscribe();
    this.languageChangeSubscription.unsubscribe();
  }
  /**
   * Angular lifecycle hook invoked after component initialization.
   * Applies the default `white` document theme, initializes the analytics
   * service and starts tracking navigation-driven page changes.
   */
  ngOnInit(): void {
    document.documentElement.setAttribute('theme', 'white');

    this.analyticsService.init();
    this.trackAnalytics();

    // tab sessionStorage replication is now handled in APP_INITIALIZER
  }

  /**
   * Subscribes to the {@link navEnd$} navigation-end stream and reports a page
   * change to the analytics service shortly after each completed navigation
   * (a small delay lets the destination view settle before tracking).
   */
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
