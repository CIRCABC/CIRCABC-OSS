import { Injectable } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
declare const $wt: any;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
declare const _paq: any[][];
interface AnalyticsConfiguration {
  utility: string;
  siteID: string;
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
    siteID: '',
    sitePath: [],
    instance: 'europa.eu',
    mode: 'manual',
  };

  public constructor(private loginService: LoginService) {}

  private readonly isAnalyticsEnabled =
    environment.analyticsURL !== '' && environment.analyticsSiteId !== '';

  public IGname = '';

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
        if (this.isWebAnalyticsEnabled()) {
          // add script analytics configuration
          this.analyticsConfiguration.siteID = environment.analyticsSiteId;
          this.analyticsConfiguration.sitePath = [environment.analyticsURL];
          this.analyticsConfiguration.instance = environment.analyticsInstance;
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
      if (this.isWebAnalyticsEnabled()) {
        _paq.push(['trackSiteSearch', keyword, category, resultsCount]);
      }
    } catch (error) {
      console.error(error);
    }
  }

  public trackDownload(url: string, fileName: string) {
    let localURL = url;
    if (localURL.lastIndexOf('?ticket=') > -1) {
      localURL = `${localURL.substring(
        0,
        localURL.lastIndexOf('?ticket=')
      )}/${fileName}`;
    } else if (localURL.lastIndexOf('&ticket=') > -1) {
      localURL = `${localURL.substring(
        0,
        localURL.lastIndexOf('&ticket=')
      )}/${fileName}`;
    }

    try {
      if (this.isWebAnalyticsEnabled()) {
        const linkType = $wt.isDocument(localURL) ? 'download' : 'link';
        _paq.push(['trackLink', localURL, linkType]);
      }
    } catch (error) {
      console.error(error);
    }
  }

  public trackCustomEvent(group: string, eventName: string) {
    try {
      if (this.isWebAnalyticsEnabled()) {
        _paq.push(['trackEvent', group, eventName]);
      }
    } catch (error) {
      console.error(error);
    }
  }

  public getAgreeWithCookies(): boolean | null {
    try {
      return (JSON.parse($wt.cookie.get('cck1')) as { cm: boolean })['cm'];
    } catch (error) {
      console.error(error);
      return null;
    }
  }

  public getAgreeWithTrack(): boolean | null {
    try {
      return (JSON.parse($wt.cookie.get('cck1')) as { all1st: boolean })[
        'all1st'
      ];
    } catch (error) {
      console.error(error);
      return null;
    }
  }
}
