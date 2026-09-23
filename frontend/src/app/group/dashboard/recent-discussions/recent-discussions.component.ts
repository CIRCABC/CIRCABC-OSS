import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  resource,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  InterestGroupService,
  Node as ModelNode,
  RecentDiscussion,
} from 'app/core/generated/circabc';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';

/**
 * Dashboard widget that renders the list of the most recent forum discussions
 * for a given interest group.
 *
 * On initialization it fetches the recent discussions from the
 * {@link InterestGroupService} and displays them. While loading it shows a
 * horizontal loader; on failure it flags a REST error state. By default the
 * view is capped to the first eight discussions unless the {@link more} flag
 * is enabled to reveal the full list. Each entry links to its discussion via
 * {@link RouterLink} and shows the author through {@link UserCardComponent}.
 *
 * Rendered on the group dashboard as `<cbc-recent-discussions>`.
 */
@Component({
  selector: 'cbc-recent-discussions',
  templateUrl: './recent-discussions.component.html',
  styleUrl: './recent-discussions.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    RouterLink,
    UserCardComponent,
    DatePipe,
    TranslocoModule,
  ],
})
export class RecentDiscussionsComponent {
  /** Service used to fetch the recent discussions for the interest group. */
  private readonly interestGroupService = inject(InterestGroupService);
  /** Pipe used to resolve localized multilingual titles to a display string. */
  private readonly i18nPipe = inject(I18nPipe);

  /**
   * Required input holding the identifier of the interest group whose recent
   * discussions should be loaded and displayed.
   */
  readonly igId = input.required<string>();

  /**
   * Recent discussions for the configured {@link igId}, loaded reactively.
   * The request stays idle when the group identifier is missing or empty and
   * re-runs whenever {@link igId} changes.
   */
  private readonly discussionsResource = resource({
    params: () => this.igId() || undefined,
    loader: ({ params: id }) =>
      this.interestGroupService.getGroupRecentDiscussionsAsync({ id }),
  });

  /** Recent discussions retrieved from the backend for the interest group. */
  public readonly discussions = computed<RecentDiscussion[]>(() =>
    this.discussionsResource.hasValue() ? this.discussionsResource.value() : []
  );
  /** Whether the discussions are currently being fetched. */
  public readonly loading = this.discussionsResource.isLoading;
  /**
   * Whether the full list of discussions should be shown. When `false` the
   * view is limited to the first eight entries.
   */
  public more = false;
  /** Whether the last REST call to fetch discussions failed. */
  public readonly restCallError = computed(
    () => this.discussionsResource.status() === 'error'
  );

  /**
   * Resolves the display label for a node, preferring its localized title when
   * available and falling back to its name otherwise.
   *
   * @param node The node whose title or name should be resolved.
   * @returns The localized title if present, otherwise the node name, or
   * `undefined` when neither is available.
   */
  public getTitleOrName(node: ModelNode): string | undefined {
    if (node.title && Object.keys(node.title).length > 0) {
      return this.i18nPipe.transform(node.title);
    }
    return node.name;
  }

  /**
   * Returns the discussions to display, honoring the {@link more} flag. When
   * {@link more} is `true` the full list is returned; otherwise the result is
   * limited to at most the first eight discussions.
   *
   * @returns The discussions to render in the view.
   */
  getRecentDiscussions(): RecentDiscussion[] {
    let result: RecentDiscussion[] = [];
    const discussions = this.discussions();

    if (this.more) {
      result = discussions;
    } else {
      for (let i = 0; i < discussions.length && i < 8; i += 1) {
        result.push(discussions[i]);
      }
    }

    return result;
  }
}
