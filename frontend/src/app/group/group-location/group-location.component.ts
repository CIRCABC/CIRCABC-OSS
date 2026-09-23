import { I18nSelectPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  resource,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  type InterestGroup,
  InterestGroupService,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';

/**
 * Standalone Angular component that renders the hierarchical location
 * (breadcrumb path) of an interest group.
 *
 * Given an {@link InterestGroup}, the component resolves the group's full
 * path within the CIRCABC category/header hierarchy and displays it as a
 * navigable trail of {@link RouterLink} entries, using the i18n pipes to
 * localise the path segment labels.
 *
 * @remarks
 * Rendered via the `cbc-group-location` selector. It collaborates with
 * {@link InterestGroupService} to fetch the {@link GroupPath} for the
 * supplied group.
 */
@Component({
  selector: 'cbc-group-location',
  templateUrl: './group-location.component.html',
  styleUrl: './group-location.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, I18nSelectPipe, I18nPipe],
})
export class GroupLocationComponent {
  /**
   * Backend service used to resolve the group's location within the
   * category/header hierarchy.
   */
  private readonly groupService = inject(InterestGroupService);

  /**
   * Required input holding the interest group whose location should be
   * displayed. The group's `id` is used to fetch its path.
   */
  readonly group = input.required<InterestGroup>();

  /**
   * Resource resolving the hierarchical path of the {@link group}. Stays
   * idle while the group has no `id`, otherwise fetches the corresponding
   * {@link GroupPath} via {@link InterestGroupService}.
   */
  private readonly groupPathResource = resource({
    params: () => this.group()?.id || undefined,
    loader: async ({ params: id }) => {
      try {
        return await this.groupService.getGroupPathAsync({ id });
      } catch (e) {
        console.error(e);
        return undefined;
      }
    },
  });

  /**
   * The resolved hierarchical path of the group, used by the template to
   * render the breadcrumb trail.
   */
  readonly groupPath = this.groupPathResource.value;
}
