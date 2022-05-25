import { Injectable } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';
import { MatomoInjector, MatomoTracker } from 'ngx-matomo';
import { Title } from '@angular/platform-browser';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
declare const $wt: any;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
declare const _paq: any[][];
interface AnalyticsConfiguration {
  utility: string;
  siteID: number;
  sitePath: string[];
  instance: string;
  mode: string;
}

@Injectable({
  providedIn: 'root',
})
export class AnalyticsService {
  analyticsConfiguration: AnalyticsConfiguration = {
    utility: 'analytics',
    siteID: 0,
    sitePath: [],
    instance: 'europa.eu',
    mode: 'manual',
  };

  public constructor(
    private loginService: LoginService,
    private matomoInjector: MatomoInjector,
    private matomoTracker: MatomoTracker,
    private title: Title
  ) {}

  private readonly isAnalyticsEnabled =
    environment.analyticsURL !== '' && environment.analyticsSiteId > 0;

  private isUserEnableCookies = true;

  public isEnabled() {
    return this.isAnalyticsEnabled;
  }

  isMatomoAnalyticsEnabled() {
    return this.isAnalyticsEnabled && environment.circabcRelease === 'oss';
  }

  isWebAnalyticsEnabled() {
    return (
      this.isAnalyticsEnabled &&
      environment.circabcRelease !== 'oss' &&
      $wt.analytics.isTrackable()
    );
  }

  public init() {
    try {
      if (this.isEnabled()) {
        if (this.isMatomoAnalyticsEnabled()) {
          this.matomoInjector.init(
            environment.analyticsURL,
            environment.analyticsSiteId
          );
        }
        if (this.isWebAnalyticsEnabled()) {
          // add script analytics configuration
          this.analyticsConfiguration.siteID = environment.analyticsSiteId;
          this.analyticsConfiguration.sitePath = [environment.analyticsURL];
          const analyticsConfigJson = document.createElement('script');
          analyticsConfigJson.type = 'application/json';
          analyticsConfigJson.innerHTML = JSON.stringify(
            this.analyticsConfiguration
          );
          document
            .getElementsByTagName('body')[0]
            .appendChild(analyticsConfigJson);
        }
      }
    } catch (error) {
      console.error(error);
    }
  }

  public trackError(error: Error) {
    try {
      if (this.isMatomoAnalyticsEnabled() && this.isUserEnableCookies) {
        this.setUser();
        this.matomoTracker.trackEvent(
          `error-${error.name}`,
          error.message,
          error.stack
        );
        this.matomoTracker.setUserId('');
      }
      if (this.isWebAnalyticsEnabled()) {
        const user = this.loginService.getUser();
        const userId = user.userId ?? 'guest';

        _paq.push([
          'trackEvent',
          `error-${error.name}:path-${window.location.pathname}:user-${userId}`,
          error.message,
          error.stack,
        ]);
      }
    } catch (innerError) {
      console.error(innerError);
    }
  }

  public trackHTTPError(
    url: string,
    method: string,
    statusCode: number,
    response: string
  ) {
    try {
      if (this.isMatomoAnalyticsEnabled() && this.isUserEnableCookies) {
        this.setUser();
        this.matomoTracker.trackEvent(
          `${url}`,
          `${url}:${method}:${statusCode}`,
          `${response}`
        );
        this.matomoTracker.setUserId('');
      }
      if (this.isWebAnalyticsEnabled()) {
        const user = this.loginService.getUser();
        const userId = user.userId ?? 'guest';

        _paq.push([
          'trackEvent',
          `${url}:user-${userId}`,
          `${url}:${method}:${statusCode}`,
          `${response}`,
        ]);
      }
    } catch (error) {
      console.error(error);
    }
  }

  public trackPageChange() {
    try {
      if (this.isMatomoAnalyticsEnabled() && this.isUserEnableCookies) {
        const title = this.title.getTitle();
        this.matomoTracker.setDocumentTitle(title);
        this.matomoTracker.trackPageView(title);
      }
      if (this.isWebAnalyticsEnabled() && $wt.analytics.isActive) {
        $wt.trackPageView();
      }
    } catch (error) {
      console.error(error);
    }
  }

  public trackSiteSearch(
    keyword: string,
    category?: string,
    resultsCount?: number
  ) {
    try {
      if (this.isMatomoAnalyticsEnabled() && this.isUserEnableCookies) {
        this.matomoTracker.trackSiteSearch(keyword, category, resultsCount);
      }
      if (this.isWebAnalyticsEnabled()) {
        _paq.push(['trackSiteSearch', keyword, category, resultsCount]);
      }
    } catch (error) {
      console.error(error);
    }
  }

  public trackDownload(url: string) {
    try {
      if (this.isMatomoAnalyticsEnabled() && this.isUserEnableCookies) {
        const queryStringIndex = url.indexOf('?');
        if (queryStringIndex > -1) {
          this.matomoTracker.trackLink(
            url.substring(0, queryStringIndex),
            'download'
          );
        } else {
          this.matomoTracker.trackLink(url, 'download');
        }
      }

      if (this.isWebAnalyticsEnabled()) {
        const linkType = $wt.isDocument(url) ? 'download' : 'link';
        _paq.push(['trackLink', url, linkType]);
      }
    } catch (error) {
      console.error(error);
    }
  }

  private setUser() {
    const user = this.loginService.getUser();
    const userId = user.userId ?? 'guest';
    const email = user.email ?? 'unknown';
    this.matomoTracker.setUserId(`${userId}-${email}`);
  }

  public setAgreeWithCookies(value: boolean) {
    this.isUserEnableCookies = value;
  }

  public getAgreeWithCookies(): boolean | null {
    return JSON.parse($wt.cookie.get('cck1'))['cm'];
  }

  public getAgreeWithTrack(): boolean | null {
    return JSON.parse($wt.cookie.get('cck1'))['all1st'];
  }
}
