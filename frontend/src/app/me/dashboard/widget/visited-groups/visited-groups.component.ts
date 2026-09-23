import {
  ChangeDetectionStrategy,
  Component,
  inject,
  resource,
} from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Dashboard widget component that renders the list of interest groups the
 * current user has most recently visited.
 *
 * Displayed in the personal "me" dashboard, it fetches the visited interest
 * groups on initialization and shows them as router links so the user can
 * quickly navigate back to a group.
 *
 * Key collaborators:
 * - {@link InterestGroupService} to retrieve the visited interest groups.
 * - {@link I18nPipe} to resolve the localized title of a group.
 */
@Component({
  selector: 'cbc-visited-groups',
  templateUrl: './visited-groups.component.html',
  styleUrl: './visited-groups.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, TranslocoModule],
})
export class VisitedGroupsComponent {
  /** Service used to fetch the interest groups recently visited by the user. */
  private readonly interestGroupService = inject(InterestGroupService);
  /** Pipe used to resolve the localized (i18n) label of an interest group title. */
  private readonly i18nPipe = inject(I18nPipe);

  /**
   * The interest groups most recently visited by the user (up to a fixed
   * maximum), loaded reactively via {@link resource}. The value is `[]` until
   * the data has been fetched.
   */
  public readonly visitedIgs = resource<InterestGroup[], unknown>({
    defaultValue: [],
    // maximum amount of visited IGs to retrieve (change if more or less is desired)
    loader: async () => {
      try {
        return await this.interestGroupService.getVisitedInterestGroupsAsync({
          amount: 10,
        });
      } catch (error) {
        console.error(error);
        return [];
      }
    },
  }).value;

  /**
   * Resolves a human-readable label for an interest group.
   *
   * It prefers the localized title (resolved via {@link I18nPipe}) when one is
   * available, and falls back to the group's raw name otherwise.
   *
   * @param group The interest group whose display label should be resolved.
   * @returns The localized title if present, otherwise the group name, or an
   * empty string when neither is available.
   */
  getGroupNameOrTitle(group: InterestGroup): string {
    let result = '';

    if (group.title && Object.keys(group.title).length > 0) {
      result = this.i18nPipe.transform(group.title);
    }

    if (result === '' && group.name) {
      result = group.name;
    }

    return result;
  }
}
