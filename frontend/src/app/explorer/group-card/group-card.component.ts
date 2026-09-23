import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
} from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { type InterestGroup } from 'app/core/generated/circabc';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { ShareComponent } from 'app/shared/share/share.component';

/**
 * Presentational card component that renders a summary of a single
 * {@link InterestGroup} within the explorer view.
 *
 * The card displays the group's localized name or title, an optional logo,
 * a router link to the group's workspace, and a share action (via
 * {@link ShareComponent}). It relies on {@link I18nPipe} to resolve the
 * multilingual title of the group.
 *
 * @remarks
 * This is a standalone component selected via `cbc-group-card`. It is purely
 * driven by its inputs and does not mutate any external state.
 */
@Component({
  selector: 'cbc-group-card',
  templateUrl: './group-card.component.html',
  styleUrl: './group-card.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    ShareComponent,
    DownloadPipe,
    SecurePipe,
    TranslocoModule,
  ],
})
export class GroupCardComponent {
  /**
   * Pipe used to resolve the localized value of the group's multilingual
   * title into the current UI language.
   */
  private readonly i18nPipe = inject(I18nPipe);

  /**
   * Required input holding the interest group to render in the card.
   */
  readonly group = input.required<InterestGroup>();

  /**
   * Optional input carrying a positional index/counter for the card, used by
   * the template (e.g. for ordering or display purposes).
   */
  readonly counter = input<number>();

  /**
   * Computes the display label for the group.
   *
   * If the group has a non-empty localized title, its translated value is
   * returned truncated to 40 characters (with an ellipsis appended when the
   * original title map contains more than 40 entries). Otherwise the group's
   * plain `name` is returned.
   *
   * @returns The localized title (possibly truncated) or the group name.
   */
  getGroupNameOrTitle(): string {
    const group = this.group();
    if (group.title && Object.keys(group.title).length > 0) {
      return (
        this.i18nPipe.transform(group.title).slice(0, 40) +
        (Object.keys(group.title).length > 40 ? '...' : '')
      );
    }
    return group.name;
  }

  /**
   * Builds an absolute URL pointing to the group's workspace.
   *
   * The link is derived from the current browser location by replacing the
   * `explore` segment of the URL with `group/{id}`.
   *
   * @returns The absolute URL to the group's workspace page.
   */
  getLink(): string {
    return `${globalThis.location.href.substring(
      0,
      globalThis.location.href.indexOf('explore')
    )}group/${this.group().id}`;
  }

  /**
   * Indicates whether the group has an associated logo to display.
   *
   * @returns `true` if the group exposes a `logoUrl`, otherwise `false`.
   */
  hasLogo(): boolean {
    if (this.group()?.logoUrl) {
      return true;
    }
    return false;
  }

  /**
   * Extracts the storage node identifier from the group's logo URL.
   *
   * When the `logoUrl` is an Alfresco workspace reference
   * (e.g. `workspace://SpacesStore/...`), the leading store prefix is
   * stripped so the remaining node reference can be used to fetch the logo.
   *
   * @returns The logo node reference without the workspace store prefix, or
   * an empty string when no usable logo URL is present.
   */
  getLogoUrl(): string {
    const group = this.group();
    if (group?.logoUrl) {
      // workspace:\/\/SpacesStore\/
      if (group.logoUrl.includes('workspace')) {
        return group.logoUrl.substring(24);
      }
    }

    return '';
  }
}
