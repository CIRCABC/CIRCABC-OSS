import { Component, OnChanges, OnDestroy, OnInit } from '@angular/core';

import { Header, HeaderService, User } from 'app/core/generated/circabc';
import { HeaderReloadListenerService } from 'app/core/header-reload-listener.service';
import { LoginService } from 'app/core/login.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { firstValueFrom, Subscription } from 'rxjs';

@Component({
  selector: 'cbc-headers',
  templateUrl: './headers.component.html',
  preserveWhitespaces: true,
})
export class HeadersComponent implements OnInit, OnChanges, OnDestroy {
  public loading = false;
  public headers!: Header[];
  public user!: User;
  private headerReloadSubscription$!: Subscription;

  constructor(
    private headerService: HeaderService,
    private loginService: LoginService,
    private i18nPipe: I18nPipe,
    private headerReloadListenerService: HeaderReloadListenerService
  ) {
    this.listenHeaderRefresh();
  }
  ngOnDestroy(): void {
    this.headerReloadSubscription$.unsubscribe();
  }

  async ngOnInit() {
    await this.loadHeaders();
  }

  async ngOnChanges() {
    await this.loadHeaders();
  }

  public async loadHeaders() {
    this.loading = true;
    this.headers = await firstValueFrom(this.headerService.getHeaders());
    this.loading = false;
  }

  private listenHeaderRefresh() {
    this.headerReloadSubscription$ =
      this.headerReloadListenerService.refreshAnnounced$.subscribe(async () => {
        await this.loadHeaders();
      });
  }
  public hasCategories(header: Header) {
    if (header.categories === undefined) {
      return false;
    }
    return header.categories.length > 0;
  }

  public async deleteHeader(header: Header) {
    await firstValueFrom(this.headerService.deleteHeader(header.id as string));
    await this.loadHeaders();
  }

  public isAdmin(): boolean {
    const user: User = this.loginService.getUser();
    return user.properties !== undefined && user.properties.isAdmin === 'true';
  }

  public getDescription(header: Header): string {
    let result = '-';
    if (header.description !== undefined) {
      result = this.i18nPipe.transform(header.description);
    }
    return result;
  }
}
