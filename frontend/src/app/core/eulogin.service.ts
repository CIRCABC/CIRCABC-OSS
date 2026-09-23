import { inject, Service } from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';

/**
 * Root-provided service that handles redirection to and from the EU Login
 * (ECAS) authentication provider.
 *
 * It bridges the application's local session state with the external EU Login
 * flow: when an unauthenticated (guest) user needs to sign in, the service
 * navigates the browser to the configured EU Login URL, and it navigates to
 * the EU Login logout URL when the user signs out.
 *
 * Key collaborators:
 * - {@link LoginService}: consulted to determine whether the current user is
 *   still a guest before triggering the login redirect.
 * - `environment`: supplies the `euloginUrl` and `eulogoutUrl` endpoints used
 *   for the redirects.
 */
@Service()
export class EULoginService {
  /**
   * Local session service used to check the current authentication state
   * (specifically whether the user is a guest) before initiating the EU Login
   * redirect.
   */
  private readonly loginService = inject(LoginService);

  /**
   * Initiates the EU Login (ECAS) sign-in flow.
   *
   * If the current user is a guest, the browser is redirected to the
   * configured EU Login URL (`environment.euloginUrl`). If the user is already
   * authenticated, no redirect occurs.
   *
   * @returns Nothing; side effect is a browser navigation when the user is a guest.
   */
  public euLogin() {
    if (this.loginService.isGuest()) {
      globalThis.location.href = environment.euloginUrl;
    }
  }

  /**
   * Logs the user out by redirecting the browser to the configured EU Login
   * logout URL (`environment.eulogoutUrl`).
   *
   * @returns Nothing; side effect is a browser navigation to the logout endpoint.
   */
  public logout() {
    globalThis.location.href = environment.eulogoutUrl;
  }
}
