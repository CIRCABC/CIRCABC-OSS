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
import { environment } from 'environments/environment';

/**
 * Standalone component (`cbc-logout`) that performs the user logout flow.
 *
 * The component renders a minimal logout view (a translated logout message)
 * and drives the sign-out process on initialization. It first terminates the
 * local CIRCABC session via {@link LoginService}, then either redirects to the
 * welcome page (for the open-source `oss` release) or delegates to the EU Login
 * (ECAS) logout via {@link EULoginService} for EU-hosted deployments.
 *
 * Key collaborators:
 * - {@link LoginService}: clears the local CIRCABC session/ticket.
 * - {@link EULoginService}: performs the external EU Login (ECAS) logout.
 * - {@link Router}: navigates to the welcome page for the `oss` release.
 */
@Component({
  selector: 'cbc-logout',
  templateUrl: './logout.component.html',
  styleUrl: './logout.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslocoModule],
})
export class LogoutComponent implements OnInit {
  /** Service used to clear the local CIRCABC session during logout. */
  private readonly loginService = inject(LoginService);
  /** Service used to perform the external EU Login (ECAS) logout. */
  private readonly euloginservice = inject(EULoginService);
  /** Angular router used to redirect to the welcome page for the `oss` release. */
  private readonly router = inject(Router);

  /**
   * The current CIRCABC release variant (e.g. `'oss'`), taken from the
   * environment configuration. Determines whether logout redirects locally
   * or delegates to EU Login.
   */
  public circabcRelease = environment.circabcRelease;

  /**
   * Angular lifecycle hook that starts the logout flow when the component
   * initializes. Delegates to {@link performLogout} without blocking the hook,
   * so the lifecycle method stays synchronous.
   */
  public ngOnInit(): void {
    void this.performLogout();
  }

  /**
   * Performs the logout flow: awaits the local CIRCABC logout, then branches on
   * the release variant. For the `oss` release it navigates to `/welcome`,
   * otherwise it triggers the EU Login (ECAS) logout.
   *
   * @returns A promise that resolves once the local logout has completed and
   * the appropriate redirect/logout branch has been initiated.
   */
  public async performLogout(): Promise<void> {
    await this.loginService.logout();
    if (environment.circabcRelease === 'oss') {
      await this.router.navigate(['/welcome']);
    } else {
      this.euloginservice.logout();
    }
  }
}
