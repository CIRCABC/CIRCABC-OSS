import { I18nSelectPipe, NgClass } from '@angular/common';
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
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
import { Subscription, firstValueFrom } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'cbc-navigator',
  templateUrl: './navigator.component.html',
  styleUrl: './navigator.component.scss',
  preserveWhitespaces: true,
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
  public circabcRelease = environment.circabcRelease;
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public currentIg!: InterestGroup;

  public location: 'default' | 'in-group' = 'default';

  public isGuest = false;

  public isMe = false;
  public isCalendar = false;
  public isRoles = false;
  public isExplore = false;
  public isHelp = false;

  public canSeeEvents = false;
  public canSeeInformation = false;
  public canSeeLibrary = true;
  public canSeeMembers = false;
  public canSeeNewsgroups = false;
  public isAdmin = false;
  public isAgenda = false;
  public isForum = false;
  public isGroupAdmin = false;
  public isGroupDashboard = false;
  public isInformation = false;
  public isInsideLibrary = false;
  public isInsideMembers = false;
  public subMenu = false;
  public sidebarActive = false;

  private routerEventsSubscription$!: Subscription;
  private interestGroupReloadsubscription$!: Subscription;

  public constructor(
    private router: Router,
    private loginService: LoginService,
    private interestGroupService: InterestGroupService,
    private groupReloadListenerService: GroupReloadListenerService,
    private analytics: AnalyticsService
  ) {
    this.listenGroupRefresh();
    this.listenToRouterEvents();
  }

  public ngOnDestroy(): void {
    this.routerEventsSubscription$.unsubscribe();
    this.interestGroupReloadsubscription$.unsubscribe();
  }
  public ngOnInit(): void {
    this.location = 'default';
    if (this.currentIg !== undefined) {
      this.location = 'in-group';
    }
    this.isGuest = this.loginService.isGuest();

    if (this.currentIg?.permissions) {
      this.setGroupAdmin();
      this.setPermissions();
      this.setGroupDashboard();
      this.setInsideLibrary();
      this.setInsideMembers();
      this.setInformation();
      this.setAgenda();
      this.setForum();
      this.setAdmin();

      if (this.analytics.IGname !== this.currentIg.name) {
        this.analytics.trackCustomEvent('IG', this.currentIg.name);
        this.analytics.IGname = this.currentIg.name;
      }
    } else {
      this.setMe();
      this.setCalendar();
      this.setRoles();
      this.setExplore();
      this.setHelp();
    }
  }

  public helpLink() {
    if (environment.environmentType === 'prod') {
      return 'https://circabc.europa.eu/ui/help/category/42bab9f5-6f98-4612-a957-677eb6967de6';
    }
    return 'https://circabc.acceptance.europa.eu/ui/help/category/81f68d80-9b69-4adc-9416-4c6c6b7d4ff7';
  }

  private listenGroupRefresh() {
    this.interestGroupReloadsubscription$ =
      this.groupReloadListenerService.refreshAnnounced$.subscribe(
        async (groupId: string) => {
          await this.reloadGroup(groupId);
        }
      );
  }

  private listenToRouterEvents() {
    this.routerEventsSubscription$ = this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe((e) => {
        if (e instanceof NavigationEnd) {
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

  private setMe() {
    if (this.router.url.endsWith('/me')) {
      this.isMe = true;
    } else {
      this.isMe = false;
    }
  }

  private setCalendar() {
    if (this.router.url.endsWith('/calendar')) {
      this.isCalendar = true;
    } else {
      this.isCalendar = false;
    }
  }

  private setRoles() {
    if (this.router.url.endsWith('/roles')) {
      this.isRoles = true;
    } else {
      this.isRoles = false;
    }
  }

  private setExplore() {
    if (this.router.url.endsWith('/explore')) {
      this.isExplore = true;
    } else {
      this.isExplore = false;
    }
  }

  private setHelp() {
    if (this.router.url.indexOf('/help') !== -1) {
      this.isHelp = true;
    } else {
      this.isHelp = false;
    }
  }

  private setPermissions() {
    const perms = this.currentIg.permissions;
    if (perms.information.indexOf('NoAccess') === -1) {
      this.canSeeInformation = true;
    }
    if (perms.event.indexOf('NoAccess') === -1) {
      this.canSeeEvents = true;
    }
    if (perms.newsgroup.indexOf('NoAccess') === -1) {
      this.canSeeNewsgroups = true;
    }
    if (perms.directory.indexOf('NoAccess') === -1) {
      this.canSeeMembers = true;
    }
  }

  private async reloadGroup(groupId: string) {
    this.currentIg = await firstValueFrom(
      this.interestGroupService.getInterestGroup(groupId)
    );
  }

  private checkCurrentRouteActive(routeName: string): boolean {
    return this.router.url.includes(routeName);
  }

  private setInsideMembers() {
    this.isInsideMembers =
      this.checkCurrentRouteActive('members') ||
      this.checkCurrentRouteActive('profiles') ||
      this.checkCurrentRouteActive('applicants');
  }

  private setInsideLibrary() {
    this.isInsideLibrary =
      this.checkCurrentRouteActive('library') ||
      this.checkCurrentRouteActive('keywords') ||
      this.checkCurrentRouteActive('dynamic-properties') ||
      (this.checkCurrentRouteActive('notification-status') &&
        this.checkCurrentRouteActive('from=library'));
  }

  private setInformation() {
    this.isInformation = this.checkCurrentRouteActive('information');
  }

  private setAgenda() {
    this.isAgenda = this.checkCurrentRouteActive('agenda');
  }

  private setForum() {
    this.isForum =
      this.checkCurrentRouteActive('forum') ||
      this.checkCurrentRouteActive('topic') ||
      (this.checkCurrentRouteActive('notification-status') &&
        (this.checkCurrentRouteActive('from=topic') ||
          this.checkCurrentRouteActive('from=forum')));
  }

  private setAdmin() {
    this.isAdmin = this.checkCurrentRouteActive('admin');
  }

  private setGroupDashboard() {
    this.isGroupDashboard =
      this.router.url.split('/').length === 3 &&
      this.router.url.includes('group');
  }

  private setGroupAdmin() {
    if (this.currentIg?.permissions) {
      const perms = this.currentIg.permissions;

      if (
        perms.directory.indexOf('Admin') !== -1 &&
        perms.event.indexOf('Admin') !== -1 &&
        perms.library.indexOf('Admin') !== -1 &&
        perms.information.indexOf('Admin') !== -1 &&
        perms.newsgroup.indexOf('Admin') !== -1
      ) {
        this.isGroupAdmin = true;
      }
    }
  }

  public environmentServerUrl() {
    return `${environment.serverURL}faces/jsp/extension/welcome.jsp`;
  }

  public isShareEnabled() {
    return environment.shareURL !== '';
  }

  public loadShareURL() {
    if (this.isShareEnabled()) {
      window.location.href = environment.shareURL;
    }
  }

  public uiSwitchEnabled() {
    return environment.showUiSwitch;
  }

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
