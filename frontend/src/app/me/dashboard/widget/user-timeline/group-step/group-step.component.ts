import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import {
  type InterestGroupFeed,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';

/**
 * Renders a single interest-group section of the user timeline widget shown on
 * the personal dashboard.
 *
 * For a given {@link InterestGroupFeed} it displays the group's localized
 * name/title together with its recent activity entries. The feed is initially
 * capped at the first 15 entries; when more are available a "show all" action
 * lets the user expand the full list. Individual feed nodes are rendered with a
 * human-readable label derived from their (possibly multilingual) title or name,
 * with special handling for events/meetings and forum posts.
 *
 * Collaborates with {@link I18nPipe} to resolve multilingual text and with
 * {@link UserCardComponent} in its template to display author information.
 */
@Component({
  selector: 'cbc-group-step',
  templateUrl: './group-step.component.html',
  styleUrl: './group-step.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, UserCardComponent, DatePipe, TranslocoModule],
})
export class GroupStepComponent implements OnChanges {
  /** Pipe used to resolve multilingual (MLText) values into a display string. */
  private readonly i18nPipe = inject(I18nPipe);

  /**
   * Required input holding the interest group and its activity feed to render.
   */
  readonly groupFeed = input.required<InterestGroupFeed>();
  /**
   * Required input describing the type/category of this timeline step, used by
   * the template to tailor how the feed is presented.
   */
  readonly type = input.required<string>();

  /** Whether the full feed is displayed; when `false` only the first 15 entries show. */
  public showAll = false;
  /** Whether the feed contains more than 15 entries and can therefore be expanded. */
  public hasMoreThan15 = false;

  /**
   * Angular lifecycle hook reacting to input changes.
   *
   * Recomputes {@link hasMoreThan15} whenever the {@link groupFeed} input
   * changes so the template can decide whether to offer the "show all" action.
   *
   * @param changes - The set of changed input properties provided by Angular.
   */
  public ngOnChanges(changes: SimpleChanges) {
    if (changes.groupFeed?.currentValue.feed) {
      this.hasMoreThan15 = changes.groupFeed.currentValue.feed.length > 15;
    }
  }

  /**
   * Returns the group's display label, preferring its localized title and
   * falling back to its plain name.
   *
   * @returns The resolved title, the group name, or an empty string when
   * neither is available.
   */
  getGroupNameOrTitle(): string {
    const groupFeed = this.groupFeed();
    if (groupFeed.title && Object.keys(groupFeed.title).length > 0) {
      return this.i18nPipe.transform(groupFeed.title);
    }
    return groupFeed.name ? groupFeed.name : '';
  }

  /**
   * Computes a human-readable label for a feed node.
   *
   * Resolution rules, applied in order:
   * - When the node carries a multilingual title, it is used; for events and
   *   meetings (identified by `kindOfEvent`, `meetingType` or `eventType`
   *   properties) the meaningful value is extracted from the MLText payload.
   * - When the node is a forum post (name starting with `posted` and ending
   *   with `.html`) a `Post <date> <time>` label is built from the name.
   * - Otherwise the resolved title is used, falling back to the node name.
   *
   * @param node - The feed node to label, or `undefined`.
   * @returns The display label, or an empty string when `node` is `undefined`.
   */
  public getDisplayProperty(node: ModelNode | undefined) {
    if (node === undefined) {
      return '';
    }
    // check if the title is present, and if not, return the name
    let title = this.i18nPipe.transform(node.title);
    if (node?.title && Object.keys(node.title).length > 0 && node.properties) {
      if (
        node.properties.kindOfEvent !== undefined ||
        node.properties.meetingType !== undefined ||
        node.properties.eventType !== undefined
      ) {
        // case for events and meetings as the title is of type MLText, but not used as such
        const match = /=(.*?)}/.exec(title); // NOSONAR - lazy quantifier, no backtracking risk

        title = match === null ? title : match[1];
      }
      return title === '' ? node.name : title;
    }
    if (node.name?.startsWith('posted') && node.name.endsWith('.html')) {
      // case for the posts
      const postedLength = 'posted-'.length;
      const dateLength = 'dd-MM-yyyy'.length;
      const hourLength = 'HH:mm'.length;
      const firstPart = node.name.substring(
        postedLength,
        postedLength + dateLength
      );
      const secondPart = node.name
        .substring(
          postedLength + dateLength + 1,
          postedLength + dateLength + 1 + hourLength
        )
        .replace('-', ':');

      const name = `Post ${firstPart} ${secondPart}`;
      return title === '' ? name : title;
    }
    // just return the name (default)
    return title === '' ? node.name : title;
  }

  /**
   * Returns the feed entries to render, honoring the {@link showAll} toggle.
   *
   * @returns The full feed when expanded, the first 15 entries when collapsed,
   * or `undefined` when the group has no feed.
   */
  public getDisplayableFeed() {
    const groupFeed = this.groupFeed();
    if (this.showAll && groupFeed.feed) {
      return groupFeed.feed;
    }
    if (!this.showAll && groupFeed.feed) {
      return groupFeed.feed.slice(0, 15);
    }
    return undefined;
  }

  /**
   * Expands the feed so that all entries are displayed by setting
   * {@link showAll} to `true`.
   */
  public showAllFeed() {
    this.showAll = true;
  }
}
