import {
  ChangeDetectionStrategy,
  Component,
  inject,
  model,
} from '@angular/core';
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

/**
 * Root component for the platform administration area (selector `cbc-admin`).
 *
 * Renders the admin shell: the shared header and navigator, a horizontal
 * loader, and a `<router-outlet>` that hosts the admin sub-routes (headers,
 * categories and CIRCABC administration). It also owns the modal dialogs for
 * adding a header, creating a category and creating a CIRCABC node, toggling
 * their visibility through signal-based models.
 *
 * Collaborators:
 * - {@link Router} to inspect the active route and highlight the current section.
 * - {@link LoginService} to resolve the current user and determine admin rights.
 * - {@link HeaderReloadListenerService} to notify listeners when headers change.
 */
@Component({
  selector: 'cbc-admin',
  templateUrl: './admin.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  /** Angular router, used to inspect the current URL for route matching. */
  private readonly router = inject(Router);
  /** Provides access to the authenticated user and their properties. */
  private readonly loginService = inject(LoginService);
  /** Broadcasts header refresh events to interested components. */
  private readonly headerReloadListenerService = inject(
    HeaderReloadListenerService
  );

  /** Whether a loading indicator should be displayed in the admin shell. */
  public loading = false;
  /** Two-way model controlling the visibility of the "add header" modal. */
  public showAddHeaderModal = model(false);
  /** Two-way model controlling the visibility of the "create category" modal. */
  public showCreateCategory = model(false);
  /** Two-way model controlling the visibility of the "create CIRCABC" modal. */
  public showCreateCircabc = model(false);

  /**
   * Checks whether the current router URL contains the given route fragment.
   *
   * @param routeName The route fragment to look for in the current URL.
   * @returns `true` if the current URL includes `routeName`, otherwise `false`.
   */
  public checkCurrentRouteActive(routeName: string): boolean {
    return this.router.url.includes(routeName);
  }

  /**
   * Indicates whether the "headers" admin section is currently active.
   *
   * @returns `true` when the current route is the headers section.
   */
  public isHeadersRoute(): boolean {
    return this.checkCurrentRouteActive('headers');
  }

  /**
   * Indicates whether the "circabc" admin section is currently active.
   *
   * @returns `true` when the current route is the CIRCABC section.
   */
  public isCircabcRoute(): boolean {
    return this.checkCurrentRouteActive('circabc');
  }

  /**
   * Determines whether the current user has general administrator rights.
   *
   * @returns `true` when the user's `isAdmin` property equals `'true'`.
   */
  public isAdmin(): boolean {
    const user: User = this.loginService.getUser();
    return user.properties?.isAdmin === 'true';
  }

  /**
   * Determines whether the current user has CIRCABC administrator rights.
   *
   * @returns `true` when the user's `isCircabcAdmin` property equals `'true'`.
   */
  public isCircabcAdmin(): boolean {
    const user: User = this.loginService.getUser();
    return user.properties?.isCircabcAdmin === 'true';
  }

  /**
   * Opens the "add header" modal by setting its visibility model to `true`.
   */
  public addHeader() {
    this.showAddHeaderModal.set(true);
  }

  /**
   * Triggers a refresh of the headers by propagating a reload event through
   * {@link HeaderReloadListenerService}.
   */
  public loadHeaders() {
    this.headerReloadListenerService.propagateHeaderRefresh();
  }
}
