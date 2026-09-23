import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { EULoginService } from 'app/core/eulogin.service';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { SystemMessageIndicatorComponent } from 'app/shared/system-message-indicator/system-message-indicator.component';
import { environment } from 'environments/environment';
import { CookieService } from 'ngx-cookie-service';

/**
 * Landing/welcome page component (`cbc-welcome`).
 *
 * Renders the CIRCABC entry page, showing the release banner, a language
 * selector, system message indicator and EU Login entry points. Beyond
 * rendering, it acts as the post-authentication landing point: on
 * initialization it consumes any login credentials that were handed off via
 * cookies (typically after an EU Login / ECAS round-trip), completes the login
 * with {@link LoginService} and routes the user to the appropriate
 * destination. When credentials are absent but the user is already
 * authenticated, it delegates to {@link RedirectionService} to send them to
 * their default landing location.
 *
 * Key collaborators:
 * - {@link CookieService} – reads and clears the transient login cookies.
 * - {@link LoginService} – loads the user session and reports guest status.
 * - {@link EULoginService} – triggers the EU Login (ECAS) authentication flow.
 * - {@link RedirectionService} – computes the default post-login redirect.
 * - {@link TranslocoService} – reads and switches the active UI language.
 * - {@link Router} – performs post-login navigation.
 */
@Component({
  selector: 'cbc-welcome',
  templateUrl: './welcome.component.html',
  styleUrl: './welcome.component.scss',
  preserveWhitespaces: true,
  providers: [CookieService],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    SystemMessageIndicatorComponent,
    RouterLink,
    LangSelectorComponent,
    DataCyDirective,
    TranslocoModule,
  ],
})
export class WelcomeComponent implements OnInit {
  /** Reads and deletes the transient login hand-off cookies. */
  private readonly cookieService = inject(CookieService);
  /** Provides access to and control over the active UI language. */
  private readonly translateService = inject(TranslocoService);
  /** Initiates the EU Login (ECAS) authentication flow. */
  private readonly euLoginService = inject(EULoginService);
  /** Loads the user session and exposes authentication/guest state. */
  private readonly loginService = inject(LoginService);
  /** Computes the default landing destination after authentication. */
  private readonly redirectionService = inject(RedirectionService);
  /** Angular router used for post-login navigation. */
  private readonly router = inject(Router);

  /** Current CIRCABC release identifier (e.g. `'oss'`), from the environment. */
  public circabcRelease = environment.circabcRelease;
  /** Whether a login is in progress; used to display a waiting state in the template. */
  public readonly waitingAfterLogin = signal(false);
  /** Whether this is an open-source (OSS) deployment, which skips the login flow. */
  public readonly isOSS = environment.circabcRelease === 'oss';

  /**
   * Angular lifecycle hook run on component initialization.
   *
   * For OSS deployments it does nothing (the login flow is skipped). Otherwise
   * it delegates to {@link initializeSession} without blocking the hook, so the
   * lifecycle method stays synchronous.
   */
  ngOnInit(): void {
    if (this.isOSS) {
      return;
    }
    void this.initializeSession();
  }

  /**
   * Consumes any hand-off login cookies: if a username and ticket are present
   * it completes the login and navigates the user; if the user is already
   * authenticated (not a guest) it triggers the default redirection.
   *
   * @returns A promise that resolves once login and any resulting navigation
   * have completed.
   */
  public async initializeSession(): Promise<void> {
    const credentials = this.getAndClearCredentials();

    if (credentials.username && credentials.ticket) {
      await this.handleLoginWithCredentials(credentials);
    } else if (!this.loginService.isGuest()) {
      await this.redirectionService.redirect();
    }
  }

  /**
   * Reads the `username`, `ticket` and `route` login hand-off cookies and then
   * deletes them so they are consumed only once.
   *
   * @returns An object containing the `username`, `ticket` and `route` values
   * read from the cookies (empty strings when a cookie is absent).
   */
  private getAndClearCredentials() {
    const username = this.cookieService.get('username');
    const ticket = this.cookieService.get('ticket');
    const route = this.cookieService.get('route');

    this.cookieService.delete('username', '/');
    this.cookieService.delete('ticket', '/');
    this.cookieService.delete('route', '/');

    return { username, ticket, route };
  }

  /**
   * Completes the login using the supplied credentials and, on success,
   * navigates the user to their intended destination.
   *
   * Sets {@link waitingAfterLogin} to `true` while the session loads and resets
   * it to `false` if authentication fails.
   *
   * @param credentials The hand-off login data.
   * @param credentials.username The user's login name.
   * @param credentials.ticket The authentication ticket to validate.
   * @param credentials.route A hint indicating the target route after login.
   * @returns A promise that resolves once login and any navigation complete.
   */
  private async handleLoginWithCredentials(credentials: {
    username: string;
    ticket: string;
    route: string;
  }) {
    this.waitingAfterLogin.set(true);
    const result = await this.loginService.loadUser(
      credentials.username,
      credentials.ticket
    );

    if (result) {
      await this.navigateAfterLogin(credentials.route);
    } else {
      this.waitingAfterLogin.set(false);
    }
  }

  /**
   * Navigates the user after a successful login based on the route hint.
   *
   * Known hints map to fixed destinations (`calendar` → `/me/calendar`,
   * `roles` → `/me/roles`, `explore` → `explore`); any other value falls back
   * to `/me`. When no route hint is provided, the default redirection is used
   * instead.
   *
   * @param route The route hint captured before authentication.
   * @returns A promise that resolves once navigation completes.
   */
  private async navigateAfterLogin(route: string) {
    if (!route) {
      await this.redirectionService.redirect();
      return;
    }

    const routeMap: { [key: string]: string[] } = {
      calendar: ['/me/calendar'],
      roles: ['/me/roles'],
      explore: ['explore'],
    };

    const targetRoute = routeMap[route] || ['/me'];
    await this.router.navigate(targetRoute);
  }

  /**
   * Handles the EU Login action from the template.
   *
   * For guest users it starts the EU Login (ECAS) authentication flow; for
   * already-authenticated users it navigates directly to `/me`.
   *
   * @returns A promise that resolves once the flow is triggered or navigation
   * completes.
   */
  public async euLogin() {
    if (this.loginService.isGuest()) {
      this.euLoginService.euLogin();
    } else {
      await this.router.navigate(['/me']);
    }
  }

  /**
   * Redirects the browser to the external EU Login (ECAS) account registration
   * page so a new user can create an account.
   */
  public euLoginCreate() {
    globalThis.location.href =
      'https://ecas.cc.cec.eu.int:7002/cas/eim/external/register.cgi';
  }

  /**
   * Whether EU Login is the active authentication method.
   *
   * @returns Always `true` in the current deployment configuration.
   */
  public get useEULogin(): boolean {
    return true;
  }

  /**
   * The currently active UI language code.
   *
   * @returns The active language identifier reported by Transloco.
   */
  get currentLang(): string {
    return this.translateService.getActiveLang();
  }

  /**
   * Switches the active UI language.
   *
   * @param event The language code to activate (e.g. emitted by the language
   * selector).
   */
  public refreshUILang(event: string): void {
    this.translateService.setActiveLang(event);
  }
}
