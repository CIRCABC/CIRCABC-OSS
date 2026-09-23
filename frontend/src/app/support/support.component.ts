import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { LoginService } from 'app/core/login.service';
import { HeaderComponent } from 'app/shared/header/header.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';

/**
 * Top-level component for the support section of the application.
 *
 * Renders the shared page {@link HeaderComponent} and {@link NavigatorComponent}
 * chrome together with the support navigation links and a {@link RouterOutlet}
 * that hosts the currently selected support sub-page. It also exposes helpers
 * used by the template to highlight the active navigation entry and to
 * conditionally show administrator-only support options.
 *
 * @see LoginService for retrieval of the current user and admin status.
 * @see Router for the active-route inspection used by {@link SupportComponent.isRoute}.
 */
@Component({
  selector: 'cbc-support',
  templateUrl: './support.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HeaderComponent,
    NavigatorComponent,
    RouterLink,
    RouterOutlet,
    TranslocoModule,
  ],
})
export class SupportComponent {
  /** Angular router used to inspect the current URL for active-route checks. */
  private readonly router = inject(Router);
  /** Service providing access to the authenticated user and their properties. */
  private readonly loginService = inject(LoginService);

  /**
   * Indicates whether the currently authenticated user is an Alfresco
   * administrator, based on the `isAdmin` property of the logged-in user.
   *
   * @returns `true` when the current user's `isAdmin` property equals the
   * string `'true'`; otherwise `false`.
   */
  public get isAlfrescoAdmin(): boolean {
    const user = this.loginService.getUser();
    return user.properties?.isAdmin === 'true';
  }

  /**
   * Determines whether the given path fragment is part of the current router
   * URL, typically used to mark the corresponding navigation entry as active.
   *
   * @param part The URL fragment to look for within the current route.
   * @returns `true` if the current router URL contains `part`; otherwise `false`.
   */
  public isRoute(part: string) {
    return this.router.url.includes(part);
  }
}
