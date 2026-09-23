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
  EntryEvent,
  GroupDashboardEntry,
  type InterestGroup,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SimpleNewsCardComponent } from './simple-news-card/simple-news-card.component';

/**
 * Dashboard timeline component (`cbc-timeline`).
 *
 * Renders the activity feed of an interest group's dashboard, splitting the
 * incoming dashboard entries into two switchable panels:
 * - a "library" feed (documents, folders and forum activity), and
 * - a "news" feed (information-service news items).
 *
 * The component consumes the raw {@link GroupDashboardEntry} data, filters and
 * categorizes each event into the appropriate feed, and builds router links so
 * that each entry navigates to its corresponding item (library, agenda,
 * information or forum). Individual news items are rendered via
 * {@link SimpleNewsCardComponent}.
 *
 * It reacts to changes of the `entries` input through {@link OnChanges} and
 * recomputes the categorized feeds accordingly.
 */
@Component({
  selector: 'cbc-timeline',
  templateUrl: './timeline.component.html',
  styleUrl: './timeline.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, SimpleNewsCardComponent, DatePipe, TranslocoModule],
})
export class TimelineComponent implements OnChanges {
  /** Pipe used to resolve multilingual ({@link MLText}) titles to a display string. */
  private readonly i18nPipe = inject(I18nPipe);

  /**
   * Optional input carrying the group's dashboard entries. The first element's
   * `news` collection provides the raw events that are categorized into the
   * library and information feeds.
   */
  readonly entries = input<GroupDashboardEntry[]>();

  /** Required input: the interest group the timeline belongs to. */
  group = input.required<InterestGroup>();

  /** Events categorized as library activity (documents, folders, forums). */
  public libraryEntries: EntryEvent[] = [];
  /** Events categorized as information-service news items (capped at 3). */
  public informationEntries: EntryEvent[] = [];

  /** Whether the news panel is currently visible. */
  public showNewsPanel = false;
  /** Whether the library panel is currently visible (default view). */
  public showLibraryPanel = true;
  /** True when there are more than 15 library entries, enabling "show all". */
  public hasMoreThan15 = false;
  /** Whether the full (unlimited) library feed is currently displayed. */
  public showAll = false;

  /**
   * Angular lifecycle hook. Recomputes the categorized feeds whenever the
   * `entries` input changes to a defined value.
   *
   * @param changes - The set of input changes reported by Angular.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes.entries) {
      if (changes.entries.currentValue !== undefined) {
        this.getValidNews();
      }
    }
  }

  /** Switches the view to the news panel, hiding the library panel. */
  public showNews() {
    this.showNewsPanel = true;
    this.showLibraryPanel = false;
  }

  /** Switches the view to the library panel, hiding the news panel. */
  public showLibrary() {
    this.showNewsPanel = false;
    this.showLibraryPanel = true;
  }

  /**
   * Resolves the display title for a node, applying special handling for
   * events/meetings (whose title is stored as {@link MLText} but not used as
   * such) and for forum posts (whose name encodes a date and time). Falls back
   * to the node name when no title is available.
   *
   * @param node - The node whose display property should be computed; may be
   * `undefined`.
   * @returns The resolved display string, or `undefined` when `node` is
   * `undefined`.
   */
  public getDisplayProperty(node: ModelNode | undefined) {
    // check if the title is present, and if not, return the name

    if (node === undefined) {
      return;
    }
    let title = this.i18nPipe.transform(node.title);
    if (node.title && Object.keys(node.title).length > 0 && node.properties) {
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
   * Determines the logical service destination for a node, used to categorize
   * where the entry should route to.
   *
   * @param node - The node to inspect.
   * @returns One of `'agenda'` (events/meetings), `'forum/topic'` (posts),
   * `'library'` (default), or `'UNDEFINED'` when the node has no properties.
   */
  public getDestination(node: ModelNode) {
    if (node.properties === undefined) {
      return 'UNDEFINED';
    }

    if (
      node.properties.kindOfEvent !== undefined ||
      node.properties.meetingType !== undefined ||
      node.properties.eventType !== undefined
    ) {
      // for events/meetings
      return 'agenda';
    }
    if (node.name?.startsWith('posted') && node.name.endsWith('.html')) {
      // for posts
      return 'forum/topic';
    }
    // for library
    return 'library';
  }

  /**
   * Checks whether a node type represents a file (content that is not a folder).
   *
   * @param type - The node type string.
   * @returns `true` if the type denotes a file, otherwise `false`.
   */
  public isFile(type: string) {
    return !type.includes('folder') && type.includes('content');
  }

  /**
   * Checks whether a node type represents a forum topic.
   *
   * @param type - The node type string.
   * @returns `true` if the type denotes a topic, otherwise `false`.
   */
  public isTopic(type: string) {
    return type.includes('topic');
  }

  /**
   * Checks whether a node type represents a forum (as opposed to a post or topic).
   *
   * @param type - The node type string.
   * @returns `true` if the type denotes a forum, otherwise `false`.
   */
  public isForum(type: string) {
    return type.includes('forum') && !this.isPost(type) && !this.isTopic(type);
  }

  /**
   * Checks whether a node type represents a forum post.
   *
   * @param type - The node type string.
   * @returns `true` if the type denotes a post, otherwise `false`.
   */
  public isPost(type: string) {
    return type.includes('post');
  }

  /**
   * Checks whether a node type represents a news item.
   *
   * @param type - The node type string.
   * @returns `true` if the type denotes news, otherwise `false`.
   */
  public isNews(type: string) {
    return type.includes('news');
  }

  /**
   * Checks whether a node type represents a folder.
   *
   * @param type - The node type string; may be `undefined`.
   * @returns `true` if the type denotes a folder, otherwise `false`/`undefined`.
   */
  public isFolder(type: string | undefined) {
    return type?.includes('folder');
  }

  /**
   * Iterates over the news events of the first dashboard entry, filtering and
   * categorizing each valid event into the library or information feed. Also
   * updates {@link hasMoreThan15} and decides which panel is initially shown.
   */
  private getValidNews() {
    const entries = this.entries();
    if (!(entries?.length && entries[0].news)) return;

    for (const entry of entries[0].news) {
      if (this.shouldIncludeEntry(entry)) {
        this.categorizeEntry(entry);
      }
    }

    this.hasMoreThan15 = this.libraryEntries.length > 15;
    this.showNewsPanel =
      this.libraryEntries.length === 0 && this.informationEntries.length > 0;
  }

  /**
   * Determines whether an entry should be included in the feeds, excluding
   * library comments (posts/topics under the library service).
   *
   * @param entry - The event to evaluate.
   * @returns `true` if the entry should be kept, otherwise `false`.
   */
  private shouldIncludeEntry(entry: EntryEvent): boolean {
    if (!entry.node?.type) return false;

    const isLibraryComment =
      entry.node.service === 'library' &&
      (this.isPost(entry.node.type) || this.isTopic(entry.node.type));

    return !isLibraryComment;
  }

  /**
   * Pushes an entry into the appropriate feed: the library feed for library
   * nodes (other than the root "Library" node) or the information feed for
   * valid information news entries.
   *
   * @param entry - The event to categorize.
   */
  private categorizeEntry(entry: EntryEvent) {
    const node = entry.node;
    if (!node) return;

    if (node.service === 'library' && node.name !== 'Library') {
      this.libraryEntries.push(entry);
    } else if (this.isValidInformationEntry(node)) {
      this.informationEntries.push(entry);
    }
  }

  /**
   * Checks whether a node qualifies as an information feed entry: a news item
   * from the information service (other than the root "Information" node) while
   * the information feed still has fewer than 3 entries.
   *
   * @param node - The node to validate.
   * @returns `true` if the node should be added to the information feed.
   */
  private isValidInformationEntry(node: ModelNode): boolean {
    if (!node.type) return false;
    return (
      node.service === 'information' &&
      node.name !== 'Information' &&
      this.isNews(node.type) &&
      this.informationEntries.length < 3
    );
  }

  /**
   * Builds the Angular router link segments used to navigate to the item
   * represented by a node, dispatching on the node's service (library,
   * information or newsgroups).
   *
   * @param groupId - The interest group identifier.
   * @param node - The node to link to; may be `undefined`.
   * @returns The array of route segments, or an empty array when the required
   * data is missing or the service is unsupported.
   */
  getRouterLinkParts(groupId: string, node: ModelNode | undefined): string[] {
    if (!(groupId && node?.type && node.id && node.parentId)) {
      return [];
    }

    if (node.service === 'library') {
      return this.getLibraryRoute(groupId, node);
    }

    if (node.service === 'information') {
      return ['/group', groupId, 'information'];
    }

    if (node.service === 'newsgroups') {
      return this.getNewsgroupsRoute(groupId, node);
    }

    return [];
  }

  /**
   * Builds the router link segments for a library node (file, folder or forum).
   *
   * @param groupId - The interest group identifier.
   * @param node - The library node to link to.
   * @returns The route segments, or an empty array when the node type is
   * unsupported or required data is missing.
   */
  private getLibraryRoute(groupId: string, node: ModelNode): string[] {
    if (!(node.type && node.id)) return [];

    if (this.isFile(node.type)) {
      return ['/group', groupId, 'library', node.id, 'details'];
    }
    if (this.isFolder(node.type)) {
      return ['/group', groupId, 'library', node.id];
    }
    if (this.isForum(node.type) && node.parentId) {
      return ['/group', groupId, 'library', node.parentId, 'details'];
    }
    return [];
  }

  /**
   * Builds the router link segments for a newsgroups node (forum, topic or post).
   *
   * @param groupId - The interest group identifier.
   * @param node - The newsgroups node to link to.
   * @returns The route segments, or an empty array when the node type is
   * unsupported or required data is missing.
   */
  private getNewsgroupsRoute(groupId: string, node: ModelNode): string[] {
    if (!(node.type && node.id)) return [];

    if (this.isForum(node.type)) {
      return ['/group', groupId, 'forum', node.id];
    }
    if (this.isTopic(node.type)) {
      return ['/group', groupId, 'forum', 'topic', node.id];
    }
    if (this.isPost(node.type) && node.parentId) {
      return ['/group', groupId, 'forum', 'topic', node.parentId];
    }
    return [];
  }

  /**
   * Returns the library entries to display: all of them when {@link showAll} is
   * enabled, otherwise the first 15.
   *
   * @returns The (possibly truncated) list of library entries.
   */
  public getLibraryEntries() {
    if (this.showAll) {
      return this.libraryEntries;
    }
    return this.libraryEntries.slice(0, 15);
  }

  /** Enables display of the complete library feed (removes the 15-item cap). */
  public showAllFeed() {
    this.showAll = true;
  }
}
