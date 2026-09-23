import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  resource,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { CiracbcAdminReloadListenerService } from 'app/core/circabc-admin-reload-listener.service';
import { CircabcService, User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { Subscription } from 'rxjs';

/**
 * Admin panel component that manages the list of CIRCABC platform
 * administrators.
 *
 * Renders the current set of platform administrators and lets a privileged
 * user remove an administrator inline (via {@link InlineDeleteComponent}).
 * On initialisation, and whenever a refresh is announced through the
 * {@link CiracbcAdminReloadListenerService}, the administrator list is
 * (re)loaded from the backend through {@link CircabcService}.
 *
 * Key collaborators:
 * - {@link CircabcService} — backend API used to fetch and delete administrators.
 * - {@link LoginService} — provides the currently authenticated user for
 *   privilege checks.
 * - {@link CiracbcAdminReloadListenerService} — broadcasts reload events that
 *   trigger a refresh of the administrator list.
 */
@Component({
  selector: 'cbc-circabc',
  templateUrl: './circabc.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [InlineDeleteComponent, TranslocoModule],
})
export class CircabcComponent implements OnDestroy {
  /** Backend API client used to fetch and delete CIRCABC administrators. */
  private readonly circabcService = inject(CircabcService);
  /** Provides access to the currently authenticated user. */
  private readonly loginService = inject(LoginService);
  /** Emits events requesting a reload of the administrator list. */
  private readonly ciracbcAdminReloadListenerService = inject(
    CiracbcAdminReloadListenerService
  );

  /** Loads the current list of CIRCABC platform administrators. */
  private readonly administratorsResource = resource({
    defaultValue: [],
    loader: async () => {
      try {
        return await this.circabcService.getCircabAdministratorsAsync();
      } catch (e) {
        console.error(e);
        return [];
      }
    },
  });

  /** Whether an administrator load operation is currently in progress. */
  public readonly loading = this.administratorsResource.isLoading;
  /** The list of CIRCABC platform administrators currently displayed. */
  public readonly administrators = this.administratorsResource.value;
  /** Reference to a user, available for use by the template. */
  public user!: User;
  /** Subscription to admin-reload announcements; released on destroy. */
  private circabcAdminReloadSubscription$!: Subscription;

  /**
   * Creates the component and starts listening for administrator reload
   * announcements.
   */
  constructor() {
    this.listenCircabcAdminRefresh();
  }

  /**
   * Angular lifecycle hook. Unsubscribes from the admin-reload listener to
   * avoid memory leaks when the component is destroyed.
   */
  ngOnDestroy(): void {
    this.circabcAdminReloadSubscription$.unsubscribe();
  }

  /**
   * Subscribes to the {@link CiracbcAdminReloadListenerService} refresh stream
   * so that the administrator list is reloaded whenever a refresh is announced.
   */
  private listenCircabcAdminRefresh() {
    this.circabcAdminReloadSubscription$ =
      this.ciracbcAdminReloadListenerService.refreshAnnounced$.subscribe(() => {
        this.loadAdministrators();
      });
  }

  /**
   * Re-runs the administrator list loader, refreshing {@link administrators}
   * and toggling {@link loading} around the call.
   */
  public loadAdministrators(): void {
    this.administratorsResource.reload();
  }

  /**
   * Removes the given user from the set of CIRCABC administrators and then
   * reloads the administrator list to reflect the change.
   *
   * @param user The administrator to remove; its `userId` is sent to the backend.
   * @returns A promise that resolves once deletion and the subsequent reload
   * have completed.
   */
  public async deleteAdministrator(user: User) {
    await this.circabcService.deleteCircabcAdministratorsAsync({
      userId: user.userId as string,
    });
    this.loadAdministrators();
  }

  /**
   * Determines whether the currently authenticated user has platform
   * administrator privileges.
   *
   * @returns `true` if the current user's `isAdmin` property equals `'true'`,
   * otherwise `false`.
   */
  public isAdmin(): boolean {
    const user: User = this.loginService.getUser();
    return user.properties?.isAdmin === 'true';
  }
}
