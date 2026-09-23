import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { EULoginService } from 'app/core/eulogin.service';
import { User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UserCacheService } from 'app/core/user-cache.service';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { environment } from 'environments/environment';

/**
 * Header/navigation menu component that displays the current user's personal
 * options (avatar, profile links, sign-in/sign-out actions).
 *
 * Rendered via the `cbc-personal-menu` selector, the component decides what to
 * show based on the authentication state resolved through {@link LoginService}:
 * a guest is offered a login entry point (EU Login when running a non-OSS
 * release), while an authenticated user sees their profile details. On init it
 * refreshes the user record from the backend (through {@link UserCacheService})
 * so up-to-date data such as the avatar is reflected.
 *
 * Key collaborators:
 * - {@link LoginService} — provides the current session/user and guest status.
 * - {@link UserCacheService} — fetches the latest user data from the backend.
 * - {@link EULoginService} — triggers the EU Login (ECAS) authentication flow.
 */
@Component({
  selector: 'cbc-personal-menu',
  templateUrl: './personal-menu.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, TranslocoModule, DownloadPipe, SecurePipe],
})
export class PersonalMenuComponent implements OnInit {
  /** Service exposing the current authentication session and user. */
  private readonly loginService = inject(LoginService);
  /** Cache-backed service used to retrieve fresh user data from the backend. */
  private readonly userCacheService = inject(UserCacheService);
  /** Service that initiates the EU Login (ECAS) authentication flow. */
  private readonly euLoginService = inject(EULoginService);

  /**
   * The currently authenticated user. Populated by {@link setUser} and only
   * defined when the visitor is not a guest. Signal-backed so the view updates
   * under OnPush after the async refresh in {@link setUser}.
   */
  public readonly user = signal<User | undefined>(undefined);
  /** Whether the personal sub-menu (dropdown) is currently expanded. */
  public subMenu = false;

  /**
   * Angular lifecycle hook. When the visitor is authenticated it loads the
   * user record so the menu can display personalised information.
   */
  public ngOnInit(): void {
    if (!this.isGuest()) {
      this.setUser();
    }
  }

  /**
   * Indicates whether the current visitor is an anonymous (guest) user.
   *
   * @returns `true` if the visitor is not authenticated, otherwise `false`.
   */
  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  /**
   * Indicates whether an authenticated user is available for display.
   *
   * @returns `true` when the visitor is not a guest and the {@link user}
   * record has been loaded, otherwise `false`.
   */
  public isUser(): boolean {
    return !this.loginService.isGuest() && this.user() !== undefined;
  }

  /**
   * Returns the login name of the current user.
   *
   * @returns The current user's username.
   */
  public userName(): string {
    return this.loginService.getCurrentUsername();
  }

  /**
   * Loads the current user into {@link user}. The record is first taken from
   * the session and then refreshed from the backend so recent changes (such as
   * an updated avatar) are reflected.
   *
   * @returns A promise that resolves once the user has been (re)loaded.
   */
  public async setUser() {
    const currentUser = this.loginService.getUser();
    this.user.set(currentUser);
    // load user data from the backend to reflect
    // changes/updates, like for example the avatar
    this.user.set(
      await this.userCacheService.getUser(currentUser.userId as string)
    );
  }

  /**
   * Determines whether the current user holds application administrator
   * privileges (either a general admin or a CIRCABC admin).
   *
   * @returns `true` if the user has admin or CIRCABC-admin properties and is a
   * valid, non-guest account, otherwise `false`.
   */
  public isAppAdmin(): boolean {
    const user: User = this.loginService.getUser();
    if (
      user.properties === null ||
      user.userId === '' ||
      user.userId === 'guest'
    ) {
      return false;
    }
    return (
      user.properties !== undefined &&
      (user.properties.isAdmin === 'true' ||
        user.properties.isCircabcAdmin === 'true')
    );
  }

  /**
   * Whether EU Login (ECAS) should be used as the authentication mechanism.
   *
   * @returns `true` for any release other than the open-source (`oss`) build.
   */
  public get useEULogin(): boolean {
    return environment.circabcRelease !== 'oss';
  }

  /**
   * Starts the EU Login (ECAS) authentication flow by delegating to
   * {@link EULoginService}.
   */
  public euLogin() {
    this.euLoginService.euLogin();
  }
}
