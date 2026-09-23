import { I18nSelectPipe, NgClass } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  model,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AnalyticsService } from 'app/core/analytics.service';
import {
  type InterestGroup,
  InterestGroupService,
  User,
} from 'app/core/generated/circabc';
import { GroupReloadListenerService } from 'app/core/group-reload-listener.service';
import { LoginService } from 'app/core/login.service';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { environment } from 'environments/environment';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

/**
 * Primary navigation component (`cbc-navigator`) that renders the application's
 * main sidebar / menu.
 *
 * It operates in two modes driven by {@link NavigatorComponent.location}:
 * - `default`: renders the top-level navigation (personal area, calendar,
 *   roles, explore, help) shown when the user is not inside an interest group.
 * - `in-group`: renders the interest-group navigation (dashboard, library,
 *   members, information, agenda, forum, admin) when a {@link currentIg} is set.
 *
 * The component reacts to Angular Router {@link NavigationEnd} events to keep
 * the highlighted/active menu entry in sync with the current URL, and it listens
 * to {@link GroupReloadListenerService} refresh notifications to reload the
 * current interest group when its data changes. Which entries are visible is
 * derived from the current group's permissions and the logged-in user's role.
 *
 * Key collaborators: {@link Router} (route detection), {@link LoginService}
 * (guest/admin state), {@link InterestGroupService} (group reload),
 * {@link GroupReloadListenerService} (refresh events) and
 * {@link AnalyticsService} (group tracking).
 */
@Component({
  selector: 'cbc-navigator',
  templateUrl: './navigator.component.html',
  styleUrl: './navigator.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NgClass,
    DataCyDirective,
    RouterLink,
    I18nSelectPipe,
    TranslocoModule,
    I18nPipe,
  ],
})
export class NavigatorComponent implements OnInit, OnDestroy {
  /** Router used to detect the active route and subscribe to navigation events. */
  private readonly router = inject(Router);
  /** Provides authentication state such as guest status and the current user. */
  private readonly loginService = inject(LoginService);
  /** Backend service used to (re)fetch interest group data on refresh events. */
  private readonly interestGroupService = inject(InterestGroupService);
  /** Emits events when the current interest group must be reloaded. */
  private readonly groupReloadListenerService = inject(
    GroupReloadListenerService
  );
  /** Tracks navigation-related analytics, notably the current interest group. */
  private readonly analytics = inject(AnalyticsService);

  /** Current CIRCABC release identifier, taken from the environment config. */
  public circabcRelease = environment.circabcRelease;

  /**
   * Two-way bindable model of the interest group currently being displayed.
   * When set, the component switches to the `in-group` navigation mode.
   */
  public currentIg = model<InterestGroup>();

  /**
   * Navigation mode: `default` for the top-level menu, `in-group` when an
   * interest group is active. Derived from {@link currentIg} in {@link ngOnInit}.
   */
  public location: 'default' | 'in-group' = 'default';

  /** Whether the current user is an anonymous/guest user. */
  public isGuest = signal(false);

  /** Whether the personal area (`/me`) route is active. */
  public isMe = signal(false);
  /** Whether the calendar route is active. */
  public isCalendar = signal(false);
  /** Whether the roles route is active. */
  public isRoles = signal(false);
  /** Whether the explore route is active. */
  public isExplore = signal(false);
  /** Whether a help route is active. */
  public isHelp = signal(false);

  /** Whether the current user may see the group's events/agenda entry. */
  public canSeeEvents = signal(false);
  /** Whether the current user may see the group's information entry. */
  public canSeeInformation = signal(false);
  /** Whether the current user may see the group's library entry. */
  public canSeeLibrary = signal(true);
  /** Whether the current user may see the group's members/directory entry. */
  public canSeeMembers = signal(false);
  /** Whether the current user may see the group's newsgroups/forum entry. */
  public canSeeNewsgroups = signal(false);
  /** Whether an admin route is active. */
  public isAdmin = signal(false);
  /** Whether an agenda route is active. */
  public isAgenda = signal(false);
  /** Whether a forum/topic route is active. */
  public isForum = signal(false);
  /** Whether the current user has full admin rights over the current group. */
  public isGroupAdmin = signal(false);
  /** Whether the group dashboard route is active. */
  public isGroupDashboard = signal(false);
  /** Whether an information route is active. */
  public isInformation = signal(false);
  /** Whether a route inside the library section is active. */
  public isInsideLibrary = signal(false);
  /** Whether a route inside the members section is active. */
  public isInsideMembers = signal(false);
  /** UI flag toggling display of a submenu. */
  public subMenu = false;
  /** UI flag toggling the active/expanded state of the sidebar. */
  public sidebarActive = false;

  /** Subscription to router navigation events; cleaned up in {@link ngOnDestroy}. */
  private routerEventsSubscription$!: Subscription;
  /** Subscription to group reload announcements; cleaned up in {@link ngOnDestroy}. */
  private interestGroupReloadsubscription$!: Subscription;

  /**
   * Sets up the subscriptions to group refresh notifications and router
   * navigation events so the navigation state stays in sync.
   */
  public constructor() {
    this.listenGroupRefresh();
    this.listenToRouterEvents();
  }

  /** Angular lifecycle hook: unsubscribes from router and group reload streams. */
  public ngOnDestroy(): void {
    this.routerEventsSubscription$.unsubscribe();
    this.interestGroupReloadsubscription$.unsubscribe();
  }
  /**
   * Angular lifecycle hook that determines the navigation {@link location} and
   * computes the initial set of visible/active menu entries. When an interest
   * group with permissions is present it configures the in-group menu and emits
   * an analytics event if the tracked group name changed; otherwise it
   * configures the default top-level menu.
   */
  public ngOnInit(): void {
    this.location = 'default';
    if (this.currentIg() !== undefined) {
      this.location = 'in-group';
    }
    this.isGuest.set(this.loginService.isGuest());

    if (this.currentIg()?.permissions) {
      this.setGroupAdmin();
      this.setPermissions();
      this.setGroupDashboard();
      this.setInsideLibrary();
      this.setInsideMembers();
      this.setInformation();
      this.setAgenda();
      this.setForum();
      this.setAdmin();

      const currentIgValue = this.currentIg();
      if (this.analytics.IGname !== currentIgValue?.name) {
        this.analytics.trackCustomEvent('IG', currentIgValue?.name || '');
        this.analytics.IGname = currentIgValue?.name || '';
      }
    } else {
      this.setMe();
      this.setCalendar();
      this.setRoles();
      this.setExplore();
      this.setHelp();
    }
  }

  /**
   * Subscribes to {@link GroupReloadListenerService} refresh announcements and
   * reloads the corresponding interest group when one is received.
   */
  private listenGroupRefresh() {
    this.interestGroupReloadsubscription$ =
      this.groupReloadListenerService.refreshAnnounced$.subscribe(
        async (groupId: string) => {
          await this.reloadGroup(groupId);
        }
      );
  }

  /**
   * Subscribes to router {@link NavigationEnd} events and recomputes the active
   * menu entries (and guest status) on every completed navigation, according to
   * the current {@link location}.
   */
  private listenToRouterEvents() {
    this.routerEventsSubscription$ = this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe((e) => {
        if (e instanceof NavigationEnd) {
          // Update isGuest status on navigation changes
          this.isGuest.set(this.loginService.isGuest());

          if (this.location === 'default') {
            this.setMe();
            this.setCalendar();
            this.setRoles();
            this.setExplore();
            this.setHelp();
          } else if (this.location === 'in-group') {
            this.setGroupAdmin();
            this.setPermissions();
            this.setGroupDashboard();
            this.setInsideLibrary();
            this.setInsideMembers();
            this.setInformation();
            this.setAgenda();
            this.setForum();
            this.setAdmin();
          }
        }
      });
  }

  /** Sets {@link isMe} to true when the current URL ends with `/me`. */
  private setMe() {
    this.isMe.set(this.router.url.endsWith('/me'));
  }

  /** Sets {@link isCalendar} to true when the current URL ends with `/calendar`. */
  private setCalendar() {
    this.isCalendar.set(this.router.url.endsWith('/calendar'));
  }

  /** Sets {@link isRoles} to true when the current URL ends with `/roles`. */
  private setRoles() {
    this.isRoles.set(this.router.url.endsWith('/roles'));
  }

  /** Sets {@link isExplore} to true when the current URL ends with `/explore`. */
  private setExplore() {
    this.isExplore.set(this.router.url.endsWith('/explore'));
  }

  /** Sets {@link isHelp} to true when the current URL contains `/help`. */
  private setHelp() {
    this.isHelp.set(this.router.url.includes('/help'));
  }

  /**
   * Derives the visibility flags ({@link canSeeInformation}, {@link canSeeEvents},
   * {@link canSeeNewsgroups}, {@link canSeeMembers}) from the current interest
   * group's permissions. Entries are shown when the corresponding permission is
   * not `NoAccess`. Does nothing when no permissions are available.
   */
  private setPermissions() {
    const perms = this.currentIg()?.permissions;
    if (!perms) {
      return;
    }
    if (!perms.information.includes('NoAccess')) {
      this.canSeeInformation.set(true);
    }
    if (!perms.event.includes('NoAccess')) {
      this.canSeeEvents.set(true);
    }
    if (!perms.newsgroup.includes('NoAccess')) {
      this.canSeeNewsgroups.set(true);
    }
    if (!perms.directory.includes('NoAccess')) {
      this.canSeeMembers.set(true);
    }
  }

  /**
   * Fetches the interest group for the given id and updates {@link currentIg}.
   *
   * @param groupId - Identifier of the interest group to reload.
   * @returns A promise that resolves once {@link currentIg} has been updated.
   */
  private async reloadGroup(groupId: string) {
    this.currentIg.set(
      await this.interestGroupService.getInterestGroupAsync({ id: groupId })
    );
  }

  /**
   * Checks whether the current router URL contains the given route fragment.
   *
   * @param routeName - Route fragment to look for in the current URL.
   * @returns `true` if the current URL includes `routeName`, otherwise `false`.
   */
  private checkCurrentRouteActive(routeName: string): boolean {
    return this.router.url.includes(routeName);
  }

  /**
   * Sets {@link isInsideMembers} to true when the current route is within the
   * members, profiles or applicants sections.
   */
  private setInsideMembers() {
    this.isInsideMembers.set(
      this.checkCurrentRouteActive('members') ||
        this.checkCurrentRouteActive('profiles') ||
        this.checkCurrentRouteActive('applicants')
    );
  }

  /**
   * Sets {@link isInsideLibrary} to true when the current route is within the
   * library, keywords or dynamic-properties sections, or a notification-status
   * route originating from the library.
   */
  private setInsideLibrary() {
    this.isInsideLibrary.set(
      this.checkCurrentRouteActive('library') ||
        this.checkCurrentRouteActive('keywords') ||
        this.checkCurrentRouteActive('dynamic-properties') ||
        (this.checkCurrentRouteActive('notification-status') &&
          this.checkCurrentRouteActive('from=library'))
    );
  }

  /** Sets {@link isInformation} to true when the current route is an information route. */
  private setInformation() {
    this.isInformation.set(this.checkCurrentRouteActive('information'));
  }

  /** Sets {@link isAgenda} to true when the current route is an agenda route. */
  private setAgenda() {
    this.isAgenda.set(this.checkCurrentRouteActive('agenda'));
  }

  /**
   * Sets {@link isForum} to true when the current route is within the forum or
   * topic sections, or a notification-status route originating from a forum or
   * topic.
   */
  private setForum() {
    this.isForum.set(
      this.checkCurrentRouteActive('forum') ||
        this.checkCurrentRouteActive('topic') ||
        (this.checkCurrentRouteActive('notification-status') &&
          (this.checkCurrentRouteActive('from=topic') ||
            this.checkCurrentRouteActive('from=forum')))
    );
  }

  /** Sets {@link isAdmin} to true when the current route is an admin route. */
  private setAdmin() {
    this.isAdmin.set(this.checkCurrentRouteActive('admin'));
  }

  /**
   * Sets {@link isGroupDashboard} to true when the current URL is the group root
   * dashboard (a three-segment URL containing `group`).
   */
  private setGroupDashboard() {
    this.isGroupDashboard.set(
      this.router.url.split('/').length === 3 &&
        this.router.url.includes('group')
    );
  }

  /**
   * Sets {@link isGroupAdmin} to true when the current interest group's
   * permissions grant `Admin` rights across all areas (directory, event,
   * library, information and newsgroup).
   */
  private setGroupAdmin() {
    const perms = this.currentIg()?.permissions;
    if (perms) {
      if (
        perms.directory.includes('Admin') &&
        perms.event.includes('Admin') &&
        perms.library.includes('Admin') &&
        perms.information.includes('Admin') &&
        perms.newsgroup.includes('Admin')
      ) {
        this.isGroupAdmin.set(true);
      }
    }
  }

  /**
   * Builds the URL to the legacy JSF welcome page on the configured server.
   *
   * @returns The absolute URL to the legacy `welcome.jsp` extension page.
   */
  public environmentServerUrl() {
    return `${environment.serverURL}faces/jsp/extension/welcome.jsp`;
  }

  /**
   * Indicates whether the Alfresco Share integration is enabled.
   *
   * @returns `true` when a non-empty Share URL is configured, otherwise `false`.
   */
  public isShareEnabled() {
    return environment.shareURL !== '';
  }

  /**
   * Navigates the browser to the configured Alfresco Share URL when the Share
   * integration is enabled; otherwise does nothing.
   */
  public loadShareURL() {
    if (this.isShareEnabled()) {
      globalThis.location.href = environment.shareURL;
    }
  }

  /**
   * Indicates whether the UI switch control should be displayed.
   *
   * @returns `true` when the UI switch is enabled in the environment config.
   */
  public uiSwitchEnabled() {
    return environment.showUiSwitch;
  }

  /**
   * Determines whether the logged-in user has application-level admin rights.
   * Guests, anonymous users and users without properties are never admins.
   *
   * @returns `true` when the user is a CIRCABC or platform admin, otherwise `false`.
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
}
