import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { TranslocoService } from '@jsverse/transloco';
import { UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { EcLogoAppComponent } from 'app/shared/ec-logo-app/ec-logo-app.component';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { PersonalMenuComponent } from 'app/shared/menu/personal-menu.component';
import { SystemMessageIndicatorComponent } from 'app/shared/system-message-indicator/system-message-indicator.component';
import { environment } from 'environments/environment';
import { EnvironmentRibbonComponent } from './environment-ribbon/environment-ribbon.component';
import { SearchBarComponent } from './search/search-bar.component';

/**
 * Application header component (`cbc-header`).
 *
 * Renders the top-of-page banner shared across CIRCABC, including the EC logo
 * and application title, the environment ribbon, the system message indicator,
 * the personal (user) menu, the language selector and — when the current route
 * context allows it — the search bar.
 *
 * The component derives contextual behaviour from the active router state
 * (explorer, help or group context) to decide whether the search field should
 * be shown, and collaborates with several services:
 * - {@link LoginService} to determine the guest/authenticated state and the
 *   current username;
 * - {@link TranslocoService} to read and change the active UI language;
 * - {@link UserService} to persist the chosen UI language for logged-in users;
 * - {@link ActivatedRoute} to inspect the current routing context.
 */
@Component({
  selector: 'cbc-header',
  templateUrl: './header.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    EnvironmentRibbonComponent,
    RouterLink,
    EcLogoAppComponent,
    SystemMessageIndicatorComponent,
    SearchBarComponent,
    PersonalMenuComponent,
    LangSelectorComponent,
  ],
})
export class HeaderComponent {
  /** Service used to determine authentication state and the current username. */
  private readonly loginService = inject(LoginService);
  /** Active route, inspected to derive the current navigation context. */
  private readonly route = inject(ActivatedRoute);
  /** Transloco service used to read and change the active UI language. */
  private readonly translateService = inject(TranslocoService);
  /** Generated API client used to persist the user's UI language preference. */
  private readonly usersService = inject(UserService);

  /**
   * Emits the search term entered by the user so parent components can react
   * to (propagate) the search request.
   */
  readonly searchPropagated = output<string>();

  /**
   * Input flag controlling whether the search field may be displayed.
   * When `false`, the search bar is hidden even in explorer context.
   *
   * @defaultValue false
   */
  public readonly showSearchField = input(false);

  /**
   * Indicates whether the current user is an anonymous (guest) user.
   *
   * @returns `true` if the user is not authenticated, otherwise `false`.
   */
  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  /**
   * Determines whether the search bar should be visible for the current route.
   *
   * @returns `true` when the active context is explorer, help or group.
   */
  public isSearchVisible(): boolean {
    // temporary remove is group context -- disabled untilsearch is faster -- this.isGroupContext()
    return (
      this.isExplorerContext() || this.isHelpContext() || this.isGroupContext()
    );
  }

  /**
   * Checks whether the first path segment of the active route matches the
   * given context identifier.
   *
   * @param item - The context path segment to test against (`group`,
   * `explore` or `library`).
   * @returns `true` if the first route segment equals `item`, otherwise `false`.
   */
  private isContext(item: 'group' | 'explore' | 'library'): boolean {
    if (this.route.root.firstChild?.snapshot?.url[0]) {
      return this.route.root.firstChild.snapshot.url[0].path === item;
    }
    return false;
  }

  /**
   * Indicates whether the user is currently within a group context.
   *
   * @returns `true` when the active route is a group route.
   */
  public isGroupContext(): boolean {
    return this.isContext('group');
  }

  /**
   * Indicates whether the user is within the explorer context and the search
   * field is allowed to be shown.
   *
   * @returns `true` when the active route is an explorer route and
   * {@link showSearchField} is enabled.
   */
  public isExplorerContext(): boolean {
    return this.isContext('explore') && this.showSearchField();
  }

  /**
   * Indicates whether the user is currently within the help context.
   *
   * @returns `true` when the active route's first segment is `help`.
   */
  public isHelpContext(): boolean {
    if (this.route.root.firstChild?.snapshot?.url[0]) {
      return this.route.root.firstChild.snapshot.url[0].path === 'help';
    }
    return false;
  }

  /**
   * Extracts the group identifier from the active route when in group context.
   *
   * @returns The group id taken from the child route, or `undefined` when not
   * in a group context or when no child route is available.
   */
  public getGroupId(): string | undefined {
    if (this.isGroupContext() && this.route.root.firstChild?.firstChild) {
      return this.route.root.firstChild.firstChild.snapshot.url[0].path;
    }
    return undefined;
  }

  /**
   * The currently active UI language code.
   *
   * @returns The active language as reported by Transloco.
   */
  get currentLang(): string {
    return this.translateService.getActiveLang();
  }

  /**
   * Changes the active UI language and, for authenticated users, persists the
   * new preference to the backend.
   *
   * @param event - The language code to activate (e.g. `en`, `fr`).
   * @returns A promise that resolves once the language has been applied and,
   * where applicable, saved for the current user.
   */
  public async refreshUILang(event: string) {
    this.translateService.setActiveLang(event);
    if (!this.isGuest()) {
      await this.usersService.putUserAsync({
        userId: this.loginService.getCurrentUsername(),
        user: {
          uiLang: event,
        },
      });
    }
  }

  /**
   * Emits the given search value through the {@link searchPropagated} output.
   *
   * @param value - The search term to propagate to listeners.
   */
  public propagateSearch(value: string) {
    this.searchPropagated.emit(value);
  }

  /**
   * Builds the URL to the legacy/back-office server view corresponding to the
   * current page.
   *
   * If a node id (a 36-character UUID) is present in the current browser URL,
   * a browse URL for that node is returned; otherwise the default extension
   * index page URL is returned.
   *
   * @returns The absolute server URL to navigate to.
   */
  public environmentServerUrl() {
    let nodeId = null;
    const found = globalThis.location.href.match(/[a-f0-9-]{36}/g);

    if (found !== null) {
      nodeId = found.at(-1);
    }
    if (nodeId === null) {
      return `${environment.serverURL}jsp/extension/index.jsp`;
    }
    return `${environment.serverURL}w/browse/${nodeId}`;
  }
}
