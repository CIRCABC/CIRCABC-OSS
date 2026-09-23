import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  output,
} from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { assertDefined } from 'app/core/asserts';
import { HelpLink, HelpService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Displays a list of help links (`cbc-help-links`) and, for privileged
 * users, exposes inline management controls for those links.
 *
 * The component renders each provided {@link HelpLink} as a navigable anchor
 * (using {@link RouterLink} and the {@link I18nPipe} for localized labels).
 * When the current user is an administrator or CIRCABC administrator, it also
 * renders inline edit and delete affordances (via
 * {@link InlineDeleteComponent}) so links can be maintained directly from the
 * list.
 *
 * Key collaborators:
 * - {@link DomSanitizer} to trust admin-managed link URLs.
 * - {@link HelpService} to delete help links on the backend.
 * - {@link LoginService} to determine the current user's privileges.
 */
@Component({
  selector: 'cbc-help-links',
  templateUrl: './help-links.component.html',
  styleUrl: './help-links.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, InlineDeleteComponent, I18nPipe, TranslocoModule],
})
export class HelpLinksComponent {
  /** Sanitizer used to mark admin-managed help link URLs as trusted. */
  private readonly sanitizer = inject(DomSanitizer);
  /** Backend service used to remove help links. */
  private readonly helpService = inject(HelpService);
  /** Service exposing the current user and their permission properties. */
  private readonly loginService = inject(LoginService);

  /** The list of help links to render. Defaults to an empty array. */
  readonly links = input<HelpLink[]>([]);
  /** Emitted after a help link has been successfully deleted. */
  readonly linkDeleted = output();
  /**
   * Emitted when a link is selected for editing, carrying the id of the
   * {@link HelpLink} to edit.
   */
  readonly clickedForEdition = output<string>();

  /**
   * Marks a help link URL as trusted so it can be safely bound in the
   * template.
   *
   * The URL originates from admin-managed help links stored in the backend
   * database and is therefore considered safe to bypass Angular's built-in
   * URL sanitization.
   *
   * @param href The link URL to sanitize. Must be defined.
   * @returns A `SafeUrl` value that can be bound in the template.
   * @throws Error if `href` is `undefined` (via {@link assertDefined}).
   */
  public sanitizeLinkRef(href: string | undefined) {
    assertDefined(href);
    // NOSONAR: Safe - URL comes from admin-managed help links stored in backend database
    return this.sanitizer.bypassSecurityTrustUrl(href); // NOSONAR
  }

  /**
   * Deletes the given help link via {@link HelpService} and notifies
   * listeners on success.
   *
   * If the link has no id the call is a no-op. Backend errors are caught and
   * logged to the console rather than propagated.
   *
   * @param link The help link to delete.
   * @returns A promise that resolves once the deletion attempt completes.
   */
  public async deleteLink(link: HelpLink) {
    if (link.id) {
      try {
        await this.helpService.removeHelpLinkAsync({ id: link.id });
        this.linkDeleted.emit();
      } catch (error) {
        console.error(error);
      }
    }
  }

  /**
   * Determines whether the current user may manage help links.
   *
   * @returns `true` if the user is authenticated (not a guest) and has the
   * `isAdmin` or `isCircabcAdmin` property set to `'true'`; otherwise
   * `false`.
   */
  public isAdminOrSupport(): boolean {
    if (!this.loginService.isGuest()) {
      const user = this.loginService.getUser();
      return (
        user.properties !== undefined &&
        (user.properties.isAdmin === 'true' ||
          user.properties.isCircabcAdmin === 'true')
      );
    }

    return false;
  }

  /**
   * Requests editing of the given help link by emitting its id through
   * {@link clickedForEdition}.
   *
   * @param link The help link selected for editing.
   */
  public edit(link: HelpLink) {
    this.clickedForEdition.emit(link.id as string);
  }

  /**
   * Determines the anchor `target` attribute for a link based on whether it
   * points to the current host.
   *
   * @param link The link URL to inspect.
   * @returns `'_self'` for links on the current host, otherwise `'_blank'`
   * so external links open in a new tab.
   */
  public getTarget(link: string | undefined) {
    if (link?.includes(globalThis.location.hostname)) {
      return '_self';
    }
    return '_blank';
  }
}
