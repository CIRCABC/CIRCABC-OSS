import { Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { EULoginService } from 'app/core/eulogin.service';
import { LoginService } from 'app/core/login.service';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';
import { environment } from 'environments/environment';

/**
 * Standalone page component (`cbc-access-denied`) shown when the current user
 * is not authorized to view the requested resource.
 *
 * It renders the shared application {@link HeaderComponent} and
 * {@link NavigatorComponent} around an access-denied message and offers the
 * user recovery actions: navigating back to the previous page, going to the
 * internal login page, or authenticating through EU Login (ECAS).
 *
 * The exact login options presented depend on whether the user is currently
 * browsing as a guest ({@link AccessDeniedComponent.isGuest}) and on the
 * active CIRCABC release ({@link AccessDeniedComponent.useEULogin}).
 *
 * Key collaborators:
 * - {@link LoginService} — determines whether the user is a guest.
 * - {@link EULoginService} — triggers the EU Login authentication flow.
 * - `Location` — supports the "go back" navigation action.
 * - `Router` — navigates to the internal login route.
 */
@Component({
  selector: 'cbc-access-denied',
  templateUrl: './access-denied.component.html',
  styleUrl: './access-denied.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [HeaderComponent, NavigatorComponent, TranslocoModule],
})
export class AccessDeniedComponent implements OnInit {
  /** Service used to inspect the current authentication state. */
  private readonly loginService = inject(LoginService);
  /** Service used to initiate the EU Login (ECAS) authentication flow. */
  private readonly euLoginService = inject(EULoginService);
  /** Angular location service used to navigate back in browser history. */
  private readonly location = inject(Location);
  /** Angular router used to navigate to the internal login route. */
  private readonly router = inject(Router);

  /**
   * Whether the current user is browsing anonymously (as a guest).
   * Populated in {@link ngOnInit} and used by the template to decide which
   * login options to display.
   */
  public isGuest = false;

  /**
   * The URL the user attempted to access before being denied.
   * May be used by the template to display or restore the original target.
   */
  public requestedUrl!: string;

  /**
   * Angular lifecycle hook. Initializes {@link isGuest} from the
   * {@link LoginService} so the view reflects the current authentication
   * state.
   */
  ngOnInit(): void {
    this.isGuest = this.loginService.isGuest();
  }

  /**
   * Navigates back to the previous page in the browser history.
   */
  goBack() {
    this.location.back();
  }

  /**
   * Navigates to the internal login page (`/login`).
   *
   * @returns A promise that resolves once the navigation attempt completes.
   */
  async goToLogin() {
    this.router.navigate(['/login']);
  }

  /**
   * Starts the EU Login (ECAS) authentication flow via the
   * {@link EULoginService}.
   */
  public euLogin() {
    this.euLoginService.euLogin();
  }

  /**
   * Indicates whether EU Login should be offered as an authentication option.
   *
   * @returns `true` for all releases except the open-source (`'oss'`) build,
   * which does not use EU Login.
   */
  public get useEULogin(): boolean {
    return environment.circabcRelease !== 'oss';
  }
}
