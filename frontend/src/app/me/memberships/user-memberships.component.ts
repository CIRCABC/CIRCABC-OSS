import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupProfile,
  Profile,
} from 'app/core/generated/circabc';
import { HintComponent } from 'app/shared/hint/hint.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Standalone component that renders the list of interest groups a user
 * belongs to, together with the access profile the user holds in each
 * group.
 *
 * It presents each membership as a row linking to the corresponding
 * interest group, resolving human-readable, localized labels for both the
 * group and the profile. While the membership data is being fetched a
 * horizontal loader is displayed.
 *
 * Key collaborators:
 * - `TranslocoService` to resolve the active and default UI languages.
 * - `I18nPipe` to translate multilingual `title` values into the current
 *   language.
 */
@Component({
  selector: 'cbc-user-memberships',
  templateUrl: './user-memberships.component.html',
  styleUrl: './user-memberships.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HintComponent,
    HorizontalLoaderComponent,
    RouterLink,
    TranslocoModule,
  ],
})
export class UserMembershipsComponent {
  /** Transloco service used to read the active and default UI languages. */
  private readonly translateService = inject(TranslocoService);
  /** Pipe used to resolve multilingual `title` values into the active language. */
  private readonly i18nPipe = inject(I18nPipe);

  /**
   * Required input holding the memberships to render. Each entry pairs an
   * interest group with the profile the user has within that group.
   */
  readonly memberships = input.required<InterestGroupProfile[]>();
  /**
   * Input flag indicating whether the memberships are still loading. When
   * `true` the component shows a loading indicator instead of the list.
   * Defaults to `false`.
   */
  readonly loading = input(false);

  /**
   * Computes the display label for an interest group, preferring its
   * localized `title` when available and falling back to its `name`.
   *
   * @param interestGroup - The interest group to build a label for, or
   * `undefined`.
   * @returns The localized title if present, otherwise the group name, or
   * `'unknown'` when no group is provided.
   */
  public getGroupDisplay(interestGroup: InterestGroup | undefined): string {
    if (interestGroup === undefined) {
      return 'unknown';
    }
    let result = interestGroup.name;

    if (interestGroup.title) {
      const title = this.i18nPipe.transform(interestGroup.title);
      if (title !== '' && title !== undefined) {
        result = title;
      }
    }

    return result;
  }

  /**
   * Computes the display label for an access profile, preferring its
   * localized `title` when available and falling back to its `name`.
   *
   * @param profile - The profile to build a label for, or `undefined`.
   * @returns The localized title if present, otherwise the profile name, or
   * an empty string when no profile is provided.
   */
  public getProfileDisplay(profile: Profile | undefined): string {
    if (profile === undefined) {
      return '';
    }
    let result = profile.name ?? '';

    if (profile.title) {
      const title = this.i18nPipe.transform(profile.title);
      if (title !== '' && title !== undefined) {
        result = title;
      }
    }

    return result;
  }

  /**
   * Returns the currently active UI language.
   *
   * @returns The active language code (e.g. `'en'`).
   */
  public getCurrentLang(): string {
    return this.translateService.getActiveLang();
  }

  /**
   * Returns the default UI language configured for the application.
   *
   * @returns The default language code (e.g. `'en'`).
   */
  public getDefaultLang(): string {
    return this.translateService.getDefaultLang();
  }
}
