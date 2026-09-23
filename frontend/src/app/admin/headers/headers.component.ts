import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  resource,
} from '@angular/core';

import { TranslocoModule } from '@jsverse/transloco';
import { Header, HeaderService, User } from 'app/core/generated/circabc';
import { HeaderReloadListenerService } from 'app/core/header-reload-listener.service';
import { LoginService } from 'app/core/login.service';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { Subscription } from 'rxjs';

/**
 * Admin component (`cbc-headers`) that renders the list of platform headers
 * within the administration area.
 *
 * It fetches all headers from the backend on initialisation, displays each
 * header (including its localised description and whether it contains
 * categories), and allows administrators to delete a header via the inline
 * delete control. The component also listens for global header-reload
 * events so the list stays in sync when headers are modified elsewhere in
 * the application.
 *
 * Key collaborators:
 * - {@link HeaderService} — retrieves and deletes headers.
 * - {@link LoginService} — resolves the current user and admin status.
 * - {@link I18nPipe} — resolves localised header descriptions.
 * - {@link HeaderReloadListenerService} — signals when the list must refresh.
 */
@Component({
  selector: 'cbc-headers',
  templateUrl: './headers.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [InlineDeleteComponent, TranslocoModule],
})
export class HeadersComponent implements OnDestroy {
  private readonly headerService = inject(HeaderService);
  private readonly loginService = inject(LoginService);
  private readonly i18nPipe = inject(I18nPipe);
  private readonly headerReloadListenerService = inject(
    HeaderReloadListenerService
  );

  /** Loads all headers from the backend. Reloaded via {@link loadHeaders}. */
  private readonly headersResource = resource({
    loader: () => this.headerService.getHeadersAsync(),
    defaultValue: [],
  });

  /** Whether a header-loading request is currently in progress. */
  public readonly loading = this.headersResource.isLoading;
  /** The list of headers currently displayed by the component. */
  public readonly headers = this.headersResource.value;
  /** The current authenticated user. */
  public user!: User;
  /** Subscription to the global header-reload notifications. */
  private headerReloadSubscription$!: Subscription;

  /**
   * Creates the component and starts listening for header-reload events so
   * the displayed list is refreshed whenever a reload is announced.
   */
  constructor() {
    this.listenHeaderRefresh();
  }

  /**
   * Angular lifecycle hook. Unsubscribes from the header-reload
   * notifications to avoid memory leaks when the component is destroyed.
   */
  ngOnDestroy(): void {
    this.headerReloadSubscription$.unsubscribe();
  }

  /**
   * Reloads the headers from the backend.
   */
  public loadHeaders(): void {
    this.headersResource.reload();
  }

  /**
   * Subscribes to the {@link HeaderReloadListenerService} refresh stream and
   * reloads the headers whenever a refresh is announced.
   */
  private listenHeaderRefresh() {
    this.headerReloadSubscription$ =
      this.headerReloadListenerService.refreshAnnounced$.subscribe(() => {
        this.loadHeaders();
      });
  }

  /**
   * Determines whether the given header contains any categories.
   *
   * @param header The header to inspect.
   * @returns `true` if the header has at least one category, otherwise
   * `false`.
   */
  public hasCategories(header: Header) {
    if (header.categories === undefined) {
      return false;
    }
    return header.categories.length > 0;
  }

  /**
   * Deletes the given header via the backend and reloads the list to reflect
   * the change.
   *
   * @param header The header to delete; its identifier is used for the
   * delete request.
   * @returns A promise that resolves once the header has been deleted and the
   * list refreshed.
   */
  public async deleteHeader(header: Header) {
    await this.headerService.deleteHeaderAsync({ id: header.id as string });
    this.loadHeaders();
  }

  /**
   * Indicates whether the currently authenticated user has platform
   * administrator privileges.
   *
   * @returns `true` if the current user is an administrator, otherwise
   * `false`.
   */
  public isAdmin(): boolean {
    const user: User = this.loginService.getUser();
    return user.properties?.isAdmin === 'true';
  }

  /**
   * Resolves the localised description of the given header.
   *
   * @param header The header whose description should be resolved.
   * @returns The localised description, or `'-'` when the header has no
   * description.
   */
  public getDescription(header: Header): string {
    let result = '-';
    if (header.description !== undefined) {
      result = this.i18nPipe.transform(header.description);
    }
    return result;
  }
}
