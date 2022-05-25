import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { TranslocoService } from '@ngneat/transloco';
import { UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  preserveWhitespaces: true,
})
export class HeaderComponent {
  @Output()
  readonly searchPropagated = new EventEmitter<string>();

  @Input()
  public showSearchField = false;

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
    if (
      this.route.root.firstChild &&
      this.route.root.firstChild.snapshot &&
      this.route.root.firstChild.snapshot.url[0]
    ) {
      return this.route.root.firstChild.snapshot.url[0].path === item;
    } else {
      return false;
    }
  }

  public isGroupContext(): boolean {
    return this.isContext('group');
  }

  public isExplorerContext(): boolean {
    return this.isContext('explore') && this.showSearchField;
  }

  public isHelpContext(): boolean {
    if (
      this.route.root.firstChild &&
      this.route.root.firstChild.snapshot &&
      this.route.root.firstChild.snapshot.url[0]
    ) {
      return this.route.root.firstChild.snapshot.url[0].path === 'help';
    } else {
      return false;
    }
  }

  public getGroupId(): string | undefined {
    if (
      this.isGroupContext() &&
      this.route.root.firstChild &&
      this.route.root.firstChild.firstChild
    ) {
      return this.route.root.firstChild.firstChild.snapshot.url[0].path;
    } else {
      return undefined;
    }
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
    } else {
      return `${environment.serverURL}w/browse/${nodeId}`;
    }
  }
}
