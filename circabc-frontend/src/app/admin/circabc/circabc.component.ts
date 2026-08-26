import { Component, OnDestroy, OnInit } from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { CiracbcAdminReloadListenerService } from 'app/core/circabc-admin-reload-listener.service';
import { CircabcService, User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { Subscription, firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-circabc',
  templateUrl: './circabc.component.html',
  preserveWhitespaces: true,
  imports: [InlineDeleteComponent, TranslocoModule],
})
export class CircabcComponent implements OnInit, OnDestroy {
  public loading = false;
  public administrators!: User[];
  public user!: User;
  private circabcAdminReloadSubscription$!: Subscription;

  constructor(
    private circabcService: CircabcService,
    private loginService: LoginService,
    private ciracbcAdminReloadListenerService: CiracbcAdminReloadListenerService
  ) {
    this.listenCircabcAdminRefresh();
  }
  ngOnDestroy(): void {
    this.circabcAdminReloadSubscription$.unsubscribe();
  }

  private listenCircabcAdminRefresh() {
    this.circabcAdminReloadSubscription$ =
      this.ciracbcAdminReloadListenerService.refreshAnnounced$.subscribe(
        async () => {
          await this.loadAdministrators();
        }
      );
  }

  async ngOnInit() {
    await this.loadAdministrators();
  }

  public async loadAdministrators() {
    this.loading = true;
    this.administrators = await firstValueFrom(
      this.circabcService.getCircabAdministrators()
    );
    this.loading = false;
  }

  public async deleteAdministrator(user: User) {
    await firstValueFrom(
      this.circabcService.deleteCircabcAdministrators(user.userId as string)
    );
    await this.loadAdministrators();
  }

  public isAdmin(): boolean {
    const user: User = this.loginService.getUser();
    return user.properties !== undefined && user.properties.isAdmin === 'true';
  }
}
