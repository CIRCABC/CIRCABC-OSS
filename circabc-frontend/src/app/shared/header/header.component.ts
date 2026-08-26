import { Component, output, input } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { TranslocoService } from '@jsverse/transloco';
import { UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { EcLogoAppComponent } from 'app/shared/ec-logo-app/ec-logo-app.component';
import { LangSelectorComponent } from 'app/shared/lang/lang-selector.component';
import { PersonalMenuComponent } from 'app/shared/menu/personal-menu.component';
import { SystemMessageIndicatorComponent } from 'app/shared/system-message-indicator/system-message-indicator.component';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';
import { EnvironmentRibbonComponent } from './environment-ribbon/environment-ribbon.component';
import { SearchBarComponent } from './search/search-bar.component';

@Component({
  selector: 'cbc-header',
  templateUrl: './header.component.html',
  preserveWhitespaces: true,
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
  readonly searchPropagated = output<string>();

  public readonly showSearchField = input(false);

  constructor(
    private loginService: LoginService,
    private route: ActivatedRoute,
    private translateService: TranslocoService,
    private usersService: UserService
  ) {}

  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  public isSearchVisible(): boolean {
    // temporary remove is group context -- disabled untilsearch is faster -- this.isGroupContext()
    return (
      this.isExplorerContext() || this.isHelpContext() || this.isGroupContext()
    );
  }

  private isContext(item: 'group' | 'explore' | 'library'): boolean {
    if (this.route.root.firstChild?.snapshot?.url[0]) {
      return this.route.root.firstChild.snapshot.url[0].path === item;
    }
    return false;
  }

  public isGroupContext(): boolean {
    return this.isContext('group');
  }

  public isExplorerContext(): boolean {
    return this.isContext('explore') && this.showSearchField();
  }

  public isHelpContext(): boolean {
    if (this.route.root.firstChild?.snapshot?.url[0]) {
      return this.route.root.firstChild.snapshot.url[0].path === 'help';
    }
    return false;
  }

  public getGroupId(): string | undefined {
    if (
      this.isGroupContext() &&
      this.route.root.firstChild &&
      this.route.root.firstChild.firstChild
    ) {
      return this.route.root.firstChild.firstChild.snapshot.url[0].path;
    }
    return undefined;
  }

  get currentLang(): string {
    return this.translateService.getActiveLang();
  }

  public async refreshUILang(event: string) {
    this.translateService.setActiveLang(event);
    if (!this.isGuest()) {
      await firstValueFrom(
        this.usersService.putUser(this.loginService.getCurrentUsername(), {
          uiLang: event,
        })
      );
    }
  }

  public propagateSearch(value: string) {
    this.searchPropagated.emit(value);
  }

  public environmentServerUrl() {
    let nodeId = null;
    const found = window.location.href.match(/[a-f0-9-]{36}/g);

    if (found !== null) {
      nodeId = found[found.length - 1];
    }
    if (nodeId === null) {
      return `${environment.serverURL}jsp/extension/index.jsp`;
    }
    return `${environment.serverURL}w/browse/${nodeId}`;
  }
}
