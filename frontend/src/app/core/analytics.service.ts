import { inject, Service } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
declare const $wt: any;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
declare const _paq: any[][];
/**
 * Shape of the JSON configuration consumed by the Europa Web Tools (`$wt`)
 * analytics script. It is serialized and injected into the page `<body>` so
 * the external tooling can pick up the correct site identity and mode.
 */
interface AnalyticsConfiguration {
  /** Web Tools utility identifier; always `'analytics'`. */
  utility: string;
  /** Analytics site identifier provided by the deployment environment. */
  siteID: string;
  /** List of analytics endpoint URLs the tracker should report to. */
  sitePath: string[];
  /** Analytics instance host (e.g. `'europa.eu'`). */
  instance: string;
  /** Tracking mode; `'manual'` means page views are tracked explicitly. */
  mode: string;
}

/**
 * Application-wide analytics facade.
 *
 * Provides a single entry point for reporting page views, errors, downloads,
 * site searches and custom events. It supports two mutually exclusive
 * back-ends selected via the build {@link environment}:
 * - Matomo (`_paq`) for the open-source (`oss`) release.
 * - Europa Web Analytics (`$wt`) for the European Commission releases.
 *
 * All tracking calls are guarded so that analytics failures never disrupt the
 * application, and every method becomes a no-op when analytics is disabled.
 *
 * Key collaborator: {@link LoginService}, used to attribute events to the
 * current user (falling back to `guest`).
 */
@Service()
export class AnalyticsService {
  /** Login service used to resolve the current user id for event attribution. */
  private readonly loginService = inject(LoginService);

  /**
   * Configuration serialized into the page for the Europa Web Tools tracker.
   * The `siteID`, `sitePath` and `instance` fields are populated from the
   * environment during {@link init}.
   */
  analyticsConfiguration: AnalyticsConfiguration = {
    utility: 'analytics',
    siteID: '',
    sitePath: [],
    instance: 'europa.eu',
    mode: 'manual',
  };

  /**
   * Whether analytics is enabled at all, derived from the presence of both
   * the analytics URL and site id in the current environment.
   */
  private readonly isAnalyticsEnabled =
    environment.analyticsURL !== '' && environment.analyticsSiteId !== '';

  /** Name of the current interest group, used to enrich tracked events. */
  public IGname = '';

  /**
   * Reports whether analytics is enabled for the current environment.
   *
   * @returns `true` if both the analytics URL and site id are configured.
   */
  public isEnabled() {
    return this.isAnalyticsEnabled;
  }

  /**
   * Reports whether the Matomo (`_paq`) back-end should be used.
   *
   * @returns `true` when analytics is enabled and the build is the `oss`
   * release.
   */
  isMatomoAnalyticsEnabled() {
    return this.isAnalyticsEnabled && environment.circabcRelease === 'oss';
  }

  /**
   * Reports whether the Europa Web Analytics (`$wt`) back-end should be used.
   *
   * @returns `true` when analytics is enabled, the build is not the `oss`
   * release and the Web Tools tracker currently reports the page as trackable.
   */
  isWebAnalyticsEnabled() {
    return (
      this.isAnalyticsEnabled &&
      environment.circabcRelease !== 'oss' &&
      $wt.analytics.isTrackable()
    );
  }

  /**
   * Initializes the Europa Web Analytics tracker by building the
   * {@link analyticsConfiguration} from the environment and injecting it as a
   * JSON `<script>` element into the document body.
   *
   * Does nothing when analytics or web analytics is disabled. Any error is
   * caught and logged so initialization never breaks bootstrap.
   */
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

  /**
   * Tracks a runtime error as a custom analytics event, attributing it to the
   * current user (or `guest`) and the current path.
   *
   * No-op unless web analytics is enabled. Any failure while tracking is
   * caught and logged.
   *
   * @param error - The error to report; its name, message and stack are sent.
   */
  public trackError(error: Error) {
    try {
      if (this.isWebAnalyticsEnabled()) {
        const user = this.loginService.getUser();
        const userId = user.userId ?? 'guest';

        _paq.push([
          'trackEvent',
          `error-${error.name}:path-${globalThis.location.pathname}:user-${userId}`,
          error.message,
          error.stack,
        ]);
      }
    } catch (innerError) {
      console.error(innerError);
    }
  }

  /**
   * Tracks a failed HTTP request as a custom analytics event, attributed to
   * the current user (or `guest`).
   *
   * No-op unless web analytics is enabled. Any failure while tracking is
   * caught and logged.
   *
   * @param url - The request URL that failed.
   * @param method - The HTTP method used (e.g. `GET`, `POST`).
   * @param statusCode - The HTTP status code returned.
   * @param response - The response body or error message to record.
   */
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

  /**
   * Reports a page view to the Europa Web Analytics tracker.
   *
   * No-op unless web analytics is enabled and the tracker is active. Any
   * failure while tracking is caught and logged.
   */
  public trackPageChange() {
    try {
      if (this.isWebAnalyticsEnabled() && $wt.analytics.isActive) {
        $wt.trackPageView();
      }
    } catch (error) {
      console.error(error);
    }
  }

  /**
   * Tracks a site search performed by the user.
   *
   * No-op unless web analytics is enabled. Any failure while tracking is
   * caught and logged.
   *
   * @param keyword - The search term entered by the user.
   * @param category - Optional category the search was scoped to.
   * @param resultsCount - Optional number of results returned.
   */
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

  /**
   * Tracks a file download (or outbound link) as a tracked link event.
   *
   * The authentication `ticket` query parameter is stripped from the URL and
   * replaced with the file name to avoid leaking session tickets and to
   * produce a clean, meaningful link. The tracker classifies the link as a
   * `download` or a `link` depending on whether the URL points to a document.
   *
   * No-op unless web analytics is enabled. Any failure while tracking is
   * caught and logged.
   *
   * @param url - The original download URL, possibly containing a ticket.
   * @param fileName - The file name used to rebuild a clean, ticket-free URL.
   */
  public trackDownload(url: string, fileName: string) {
    let localURL = url;
    if (localURL.includes('?ticket=')) {
      localURL = `${localURL.substring(
        0,
        localURL.lastIndexOf('?ticket=')
      )}/${fileName}`;
    } else if (localURL.includes('&ticket=')) {
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

  /**
   * Tracks an arbitrary custom event.
   *
   * No-op unless web analytics is enabled. Any failure while tracking is
   * caught and logged.
   *
   * @param group - The event category/group.
   * @param eventName - The name/action of the event.
   */
  public trackCustomEvent(group: string, eventName: string) {
    try {
      if (this.isWebAnalyticsEnabled()) {
        _paq.push(['trackEvent', group, eventName]);
      }
    } catch (error) {
      console.error(error);
    }
  }

  /**
   * Reads the user's cookie-consent preference from the Web Tools `cck1`
   * cookie.
   *
   * @returns The `cm` (cookie management) consent flag, or `null` if the
   * cookie is missing or cannot be parsed.
   */
  public getAgreeWithCookies(): boolean | null {
    try {
      return (JSON.parse($wt.cookie.get('cck1')) as { cm: boolean })['cm'];
    } catch (error) {
      console.error(error);
      return null;
    }
  }

  /**
   * Reads the user's first-party tracking-consent preference from the Web
   * Tools `cck1` cookie.
   *
   * @returns The `all1st` (all first-party) consent flag, or `null` if the
   * cookie is missing or cannot be parsed.
   */
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
