import { inject, Service } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from 'environments/environment';

/**
 * Session storage key under which the URL to redirect to after login is
 * persisted.
 */
const MUST_REDIRECT = 'mustRedirect';

/**
 * Application-wide service that handles post-authentication redirection.
 *
 * It remembers the URL a user was trying to reach before being sent through
 * the login flow (persisted in `sessionStorage`) and, once the user is
 * authenticated, navigates back to that URL. When no target URL was stored it
 * falls back to the user's personal area (`/me`).
 *
 * Key collaborators:
 * - {@link Router} — performs the actual in-app navigation.
 * - `environment.baseHref` — used to strip the deployment base href from an
 *   absolute URL so that only the app-relative portion is navigated to.
 * - `sessionStorage` — stores the pending redirect URL across the login round trip.
 */
@Service()
export class RedirectionService {
  /** Angular router used to navigate to the resolved destination URL. */
  private readonly router = inject(Router);

  /**
   * Navigates to the URL previously stored by {@link mustRedirect}, if any.
   *
   * When a stored URL is present, any scheme/host prefix and the configured
   * `environment.baseHref` are stripped so that an app-relative URL remains,
   * the stored value is cleared from `sessionStorage`, and the router
   * navigates to it. When no URL was stored, the user is redirected to their
   * personal area (`/me`).
   *
   * @returns A promise that resolves once the navigation has completed.
   */
  public async redirect() {
    let url = sessionStorage.getItem(MUST_REDIRECT);
    if (url !== '' && url !== null) {
      let idx = url.indexOf('://');
      if (idx !== -1) {
        url = url.substring(idx + 3);
        idx = url.indexOf(environment.baseHref);
        url = url.substring(idx + environment.baseHref.length);
      }
      sessionStorage.removeItem(MUST_REDIRECT);
      await this.router.navigateByUrl(url);
    } else {
      await this.router.navigate(['/me']);
    }
  }
  /**
   * Records the current full browser URL as the destination to return to after
   * the login flow completes.
   *
   * The value is stored in `sessionStorage` and later consumed by
   * {@link redirect}.
   */
  public mustRedirect() {
    sessionStorage.setItem(MUST_REDIRECT, globalThis.location.href);
  }
}
