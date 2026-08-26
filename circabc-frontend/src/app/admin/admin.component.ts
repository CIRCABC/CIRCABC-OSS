import { Component, model } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import { CreateCircabcComponent } from 'app/admin/circabc/create-circabc/create-circabc.component';
import { CreateCategoryComponent } from 'app/admin/create-category/create-category.component';
import { AddHeaderComponent } from 'app/admin/headers/add-header/add-header.component';
import { User } from 'app/core/generated/circabc';
import { HeaderReloadListenerService } from 'app/core/header-reload-listener.service';
import { LoginService } from 'app/core/login.service';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HeaderComponent } from 'app/shared/header/header.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

@Component({
  selector: 'cbc-admin',
  templateUrl: './admin.component.html',
  preserveWhitespaces: true,
  imports: [
    HeaderComponent,
    NavigatorComponent,
    HorizontalLoaderComponent,
    DataCyDirective,
    RouterLink,
    AddHeaderComponent,
    CreateCategoryComponent,
    CreateCircabcComponent,
    RouterOutlet,
    TranslocoModule,
  ],
})
export class AdminComponent {
  public loading = false;
  public showAddHeaderModal = model(false);
  public showCreateCategory = model(false);
  public showCreateCircabc = model(false);

  constructor(
    private router: Router,
    private loginService: LoginService,
    private headerReloadListenerService: HeaderReloadListenerService
  ) {}

  public checkCurrentRouteActive(routeName: string): boolean {
    return this.router.url.includes(routeName);
  }

  public isHeadersRoute(): boolean {
    return this.checkCurrentRouteActive('headers');
  }

  public isCircabcRoute(): boolean {
    return this.checkCurrentRouteActive('circabc');
  }

  public isAdmin(): boolean {
    const user: User = this.loginService.getUser();
    return user.properties !== undefined && user.properties.isAdmin === 'true';
  }

  public isCircabcAdmin(): boolean {
    const user: User = this.loginService.getUser();
    return (
      user.properties !== undefined && user.properties.isCircabcAdmin === 'true'
    );
  }

  public addHeader() {
    this.showAddHeaderModal.set(true);
  }

  public loadHeaders() {
    this.headerReloadListenerService.propagateHeaderRefresh();
  }
}
