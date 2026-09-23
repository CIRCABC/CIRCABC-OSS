import { CommonModule, DatePipe } from '@angular/common';

import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  OnInit,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import {
  MatExpansionModule,
  MatExpansionPanel,
} from '@angular/material/expansion';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DomSanitizer } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AnalyticsService } from 'app/core/analytics.service';
import { assertDefined } from 'app/core/asserts';
import { HelpService } from 'app/core/generated/circabc/api/help.service';
import { SearchService } from 'app/core/generated/circabc/api/search.service';
import { HelpSearchResult } from 'app/core/generated/circabc/model/helpSearchResult';
import { SearchNode } from 'app/core/generated/circabc/model/searchNode';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { debounceTime } from 'rxjs/operators';
import { UserCardComponent } from '../../user-card/user-card.component';
import { AdvancedSearchComponent } from './advanced-search/advanced-search.component';

/**
 * Shape of the values collected by the search form, combining the free-text
 * query with the optional advanced-search criteria.
 *
 * All fields are optional because the basic search only populates
 * `searchString`, while the advanced search panel adds the remaining filters.
 */
interface SearchFormData {
  /** The free-text query typed by the user. */
  searchString?: string;
  /** ISO language code used to restrict results to a given language. */
  language?: string;
  /** Service scope the search should be limited to. */
  searchIn?: 'All' | 'Library' | 'Forums' | 'Information' | 'Agenda';
  /** Creator (author) filter. */
  creator?: string;
  /** Lower bound of the creation-date range filter. */
  creationDateFrom?: string;
  /** Upper bound of the creation-date range filter. */
  creationDateTo?: string;
  /** Lower bound of the modification-date range filter. */
  modifiedDateFrom?: string;
  /** Upper bound of the modification-date range filter. */
  modifiedDateTo?: string;
  /** Keyword filter. */
  keywords?: string;
  /** Status filter. */
  status?: string;
  /** Security ranking filter. */
  securityRanking?: string;
  /** Version filter. */
  version?: string;
}

/**
 * Search bar component (`cbc-search-bar`) rendered in the application header and
 * within the explorer view.
 *
 * It renders a debounced free-text search field together with expandable
 * Material panels that display categorised results. Depending on the current
 * route it performs one of two kinds of searches:
 * - a help-content search (when the current URL is under `/help`) using
 *   {@link HelpService};
 * - an interest-group search (when a {@link groupId} is provided) using
 *   {@link SearchService}, splitting the returned {@link SearchNode}s into
 *   files, folders, forums, topics, posts, events, information and other
 *   results.
 *
 * The component also hosts the advanced-search panel, tracks search metrics via
 * {@link AnalyticsService}, localises result titles through
 * {@link TranslocoService} and can navigate to help articles/categories via the
 * {@link Router}.
 */
@Component({
  selector: 'cbc-search-bar',
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SpinnerComponent,
    RouterLink,
    DatePipe,
    TranslocoModule,
    MatExpansionModule,
    MatCardModule,
    I18nPipe,
    MatTooltipModule,
    AdvancedSearchComponent,
    UserCardComponent,
  ],
})
export class SearchBarComponent implements OnInit {
  /** Reactive-forms builder used to construct the search form. */
  private readonly fb = inject(FormBuilder);
  /** Sanitiser used to trust URLs coming from help search results. */
  private readonly sanitizer = inject(DomSanitizer);
  /** Router used to detect the current route and to navigate to help pages. */
  private readonly router = inject(Router);
  /** Backend client used to run interest-group searches. */
  private readonly searchService = inject(SearchService);
  /** Service used to report search usage metrics. */
  private readonly analyticsService = inject(AnalyticsService);
  /** Backend client used to run help-content searches. */
  private readonly helpService = inject(HelpService);
  /** Translation service used to resolve localised result titles. */
  private readonly translateService = inject(TranslocoService);

  /**
   * Identifier of the interest group whose content should be searched. When
   * absent, interest-group searches are skipped.
   */
  readonly groupId = input<string>();

  /**
   * Whether the component is embedded in the explorer view. When `true`, each
   * search emits the query through {@link searchHit}.
   */
  readonly forExplorer = input(false);

  /**
   * Emits the current search string on every search when the component is used
   * inside the explorer (see {@link forExplorer}).
   */
  readonly searchHit = output<string>();

  /**
   * Emits the current search string when a refresh of the previously executed
   * search is requested (see {@link requestRefresh}).
   */
  readonly renewSearchHit = output<string>();

  /** Expansion panel that displays interest-group search results. */
  readonly panelResultIG = viewChild<MatExpansionPanel>('panelResultIG');

  /** Expansion panel that hosts the advanced-search form. */
  readonly advancedSearchIG = viewChild<MatExpansionPanel>('advancedSearchIG');

  /** Expansion panel that displays help search results. */
  readonly panelResultHelp = viewChild<MatExpansionPanel>('panelResultHelp');

  /** Search results of type `file`. */
  public readonly fileResults = signal<SearchNode[]>([]);
  /** Search results of type `folder`. */
  public readonly folderResults = signal<SearchNode[]>([]);
  /** Search results of type `post`. */
  public readonly postResults = signal<SearchNode[]>([]);
  /** Search results of type `forum`. */
  public readonly forumResults = signal<SearchNode[]>([]);
  /** Search results of type `topic`. */
  public readonly topicResults = signal<SearchNode[]>([]);
  /** Search results belonging to the `events` service. */
  public readonly eventResults = signal<SearchNode[]>([]);
  /** Search results of type `information`. */
  public readonly informationResults = signal<SearchNode[]>([]);
  /** Search results that do not match any of the other categories. */
  public readonly otherResults = signal<SearchNode[]>([]);
  /** Result of the most recent help-content search. */
  public readonly helpResult = signal<HelpSearchResult | undefined>(undefined);
  /** Whether a search request is currently in flight. */
  public readonly searching = signal(false);
  /** Whether the last search returned no results. */
  public readonly noResult = signal(false);
  /** Reactive form backing the search input. */
  public searchForm!: FormGroup;
  /** The search string in effect before opening the advanced search panel. */
  public lastSearchString = '';

  /**
   * Angular lifecycle hook. Builds the reactive search form and wires up the
   * debounced value-change subscription.
   */
  public ngOnInit(): void {
    this.buildForm();
  }

  /**
   * Creates the reactive search form and subscribes to its value changes with a
   * debounce so that a search is only triggered once the user stops typing.
   * Also clears any previously displayed results.
   */
  private buildForm(): void {
    this.searchForm = this.fb.group(
      {
        searchString: [''],
      },
      {
        updateOn: 'change',
      }
    );
    const debounceTimeInMiliSecond = 750;

    this.searchForm.valueChanges
      .pipe(debounceTime(debounceTimeInMiliSecond))
      .subscribe((data) => this.onValueChanged(data));
    this.clearResults();
  }

  /**
   * Clears all result collections and closes the result and advanced-search
   * expansion panels when they are available.
   */
  public clearResults() {
    this.fileResults.set([]);
    this.folderResults.set([]);
    this.postResults.set([]);
    this.forumResults.set([]);
    this.topicResults.set([]);
    this.eventResults.set([]);
    this.informationResults.set([]);
    this.otherResults.set([]);

    const panelResultIG = this.panelResultIG();
    const panelResultHelp = this.panelResultHelp();
    const advancedSearchIG = this.advancedSearchIG();
    if (panelResultIG && panelResultHelp && advancedSearchIG) {
      panelResultIG.close();
      panelResultHelp.close();
      advancedSearchIG.close();
    }
  }

  /**
   * Resets the search form and its results back to the initial empty state.
   */
  public resetForm(): void {
    this.clearResults();
    this.searchForm.controls.searchString.setValue('');
    this.noResult.set(false);
    this.searching.set(false);
  }

  /**
   * Handler invoked (debounced) whenever the search form value changes. Clears
   * previous results and dispatches either a help search or an interest-group
   * search depending on the current route, then emits {@link searchHit} when
   * used in the explorer.
   *
   * @param data The current form values, or `undefined` when the form is empty.
   */
  public async onValueChanged(data?: SearchFormData) {
    this.clearResults();
    this.noResult.set(false);

    if (this.isRouteHelp()) {
      await this.handleHelpSearch(data);
    } else {
      await this.handleGroupSearch(data);
    }

    if (this.forExplorer()) {
      this.searchHit.emit(data?.searchString ?? '');
    }
  }

  /**
   * Runs a help-content search for the given query and stores the result.
   * No-op when there is no query or a search is already running. Sets
   * {@link noResult} to `true` if the request fails.
   *
   * @param data The current form values containing the search string.
   */
  private async handleHelpSearch(data?: SearchFormData) {
    if (!data?.searchString || this.searching()) return;

    this.searching.set(true);
    this.panelResultHelp()?.open();

    try {
      const result = await this.helpService.helpSearchAsync({
        q: data.searchString,
      });
      this.helpResult.set(result);
      this.trackHelpSearchResults(data.searchString ?? '', result);
    } catch (error) {
      console.error(error);
      this.noResult.set(true);
    }

    this.searching.set(false);
  }

  /**
   * Reports analytics events for the number of help articles, categories and
   * links returned by a help search.
   *
   * @param searchString The query that produced the result.
   * @param result The help search result whose counts are tracked.
   */
  private trackHelpSearchResults(
    searchString: string,
    result: HelpSearchResult
  ) {
    this.analyticsService.trackSiteSearch(
      searchString,
      'help-articles',
      result.articles?.length ?? 0
    );
    this.analyticsService.trackSiteSearch(
      searchString,
      'help-categories',
      result.categories?.length ?? 0
    );
    this.analyticsService.trackSiteSearch(
      searchString,
      'help-links',
      result.links?.length ?? 0
    );
  }

  /**
   * Runs an interest-group search using the current form filters, splits the
   * returned nodes into their categories and reports the total to analytics.
   * No-op when there is no query, a search is already running, or no
   * {@link groupId} is set. Sets {@link noResult} to `true` if the request
   * fails or returns no results.
   *
   * @param data The current form values, including advanced-search filters.
   */
  private async handleGroupSearch(data?: SearchFormData) {
    const groupId = this.groupId();
    if (!data?.searchString || this.searching() || !groupId) return;

    this.searching.set(true);
    this.panelResultIG()?.open();

    try {
      const res = await this.searchService.getSearchAsync({
        q: data.searchString,
        node: groupId,
        language: data.language,
        searchIn: data.searchIn,
        creator: data.creator,
        creationDateFrom: data.creationDateFrom,
        creationDateTo: data.creationDateTo,
        modifiedDateFrom: data.modifiedDateFrom,
        modifiedDateTo: data.modifiedDateTo,
        keywords: data.keywords,
        status: data.status,
        securityRanking: data.securityRanking,
        version: data.version,
      });

      if (res.data) {
        this.splitResults(res.data);
        this.noResult.set(res.total === 0);
        this.analyticsService.trackSiteSearch(
          data.searchString,
          `search interest group -${groupId}`,
          res.total
        );
      }
    } catch (error) {
      console.error(error);
      this.noResult.set(true);
    }

    this.searching.set(false);
  }

  /**
   * Distributes the given search nodes into the per-type result collections
   * based on each node's `resultType` (and `service` for events/other).
   *
   * @param data The search nodes to categorise.
   */
  splitResults(data: SearchNode[]) {
    const fileResults: SearchNode[] = [];
    const folderResults: SearchNode[] = [];
    const postResults: SearchNode[] = [];
    const forumResults: SearchNode[] = [];
    const topicResults: SearchNode[] = [];
    const eventResults: SearchNode[] = [];
    const informationResults: SearchNode[] = [];
    const otherResults: SearchNode[] = [];

    for (const sn of data) {
      switch (sn.resultType) {
        case 'folder': {
          folderResults.push(sn);
          break;
        }
        case 'file': {
          fileResults.push(sn);
          break;
        }
        case 'topic': {
          topicResults.push(sn);
          break;
        }
        case 'post': {
          postResults.push(sn);
          break;
        }
        case 'forum': {
          forumResults.push(sn);
          break;
        }
        case 'information': {
          informationResults.push(sn);
          break;
        }
        default:
          if (sn.service === 'events') {
            eventResults.push(sn);
          } else {
            otherResults.push(sn);
          }
      }
    }

    this.fileResults.set(fileResults);
    this.folderResults.set(folderResults);
    this.postResults.set(postResults);
    this.forumResults.set(forumResults);
    this.topicResults.set(topicResults);
    this.eventResults.set(eventResults);
    this.informationResults.set(informationResults);
    this.otherResults.set(otherResults);
  }

  /**
   * @returns `true` if any result category contains at least one result.
   */
  hasResults(): boolean {
    return (
      this.hasFolderResults() ||
      this.hasFileResults() ||
      this.hasTopicResults() ||
      this.hasForumResults() ||
      this.hasPostResults() ||
      this.hasEventResults() ||
      this.hasInformationResults() ||
      this.hasOtherResults()
    );
  }

  /** @returns `true` if there is at least one file result. */
  hasFileResults(): boolean {
    return this.fileResults().length > 0;
  }

  /** @returns `true` if there is at least one folder result. */
  hasFolderResults(): boolean {
    return this.folderResults().length > 0;
  }

  /** @returns `true` if there is at least one forum result. */
  hasForumResults(): boolean {
    return this.forumResults().length > 0;
  }

  /** @returns `true` if there is at least one topic result. */
  hasTopicResults(): boolean {
    return this.topicResults().length > 0;
  }

  /** @returns `true` if there is at least one post result. */
  hasPostResults(): boolean {
    return this.postResults().length > 0;
  }

  /** @returns `true` if there is at least one event result. */
  hasEventResults(): boolean {
    return this.eventResults().length > 0;
  }

  /** @returns `true` if there is at least one information result. */
  hasInformationResults(): boolean {
    return this.informationResults().length > 0;
  }

  /** @returns `true` if there is at least one uncategorised result. */
  hasOtherResults(): boolean {
    return this.otherResults().length > 0;
  }

  /**
   * Resolves the routing context for a node: library nodes route through the
   * library, other nodes use the supplied forum-based context.
   *
   * @param node The search node whose context is resolved.
   * @param item The forum-based context to use for non-library nodes.
   * @returns The context segment to use when building the node's link.
   */
  private getContext(node: SearchNode, item: 'forum' | 'forum/topic'): string {
    if (node.service === 'library') {
      return node.service;
    }
    return item;
  }

  /**
   * @param node The topic search node.
   * @returns The routing context for a topic node.
   */
  getTopicContext(node: SearchNode): string {
    return this.getContext(node, 'forum');
  }

  /**
   * @param node The post search node.
   * @returns The routing context for a post node.
   */
  getPostContext(node: SearchNode): string {
    return this.getContext(node, 'forum/topic');
  }

  /**
   * @param node The post search node.
   * @returns The trailing route segment for a post link (`/details` for
   * library nodes, empty otherwise).
   */
  getPostEndContext(node: SearchNode): string {
    if (node.service === 'library') {
      return '/details';
    }
    return '';
  }

  /**
   * @param searchNode The node whose modification date is requested.
   * @returns The node's modified date, or `null` when no properties exist.
   */
  public getModified(searchNode: SearchNode): string | null {
    if (searchNode.properties) {
      return searchNode.properties.modified;
    }
    return null;
  }

  /**
   * @returns `true` when the search input currently contains a non-empty value.
   */
  public hasText(): boolean {
    return (
      this.searchForm.value.searchString !== '' &&
      this.searchForm.value.searchString !== undefined
    );
  }

  /**
   * Emits {@link renewSearchHit} with the current query when the search input
   * is non-empty, requesting a refresh of the previously run search.
   */
  public requestRefresh() {
    if (this.hasText()) {
      this.renewSearchHit.emit(this.searchForm.value.searchString);
    }
  }

  /**
   * @returns `true` when the last help search returned at least one article,
   * category or link; `false` otherwise or when no help result exists.
   */
  public hasResult() {
    const helpResult = this.helpResult();
    if (helpResult) {
      if (
        (helpResult.articles && helpResult.articles.length > 0) ||
        (helpResult.categories && helpResult.categories.length > 0) ||
        (helpResult.links && helpResult.links.length > 0)
      ) {
        return true;
      }
      return false;
    }
    return false;
  }

  /**
   * Marks a help-result URL as trusted so it can be safely bound in the
   * template. The URL originates from backend-stored search results.
   *
   * @param href The raw URL to sanitise.
   * @returns A sanitised (trusted) URL, or `undefined` when no href is given.
   */
  public sanitizeLinkRef(href: string | undefined) {
    if (href) {
      // NOSONAR: Safe - URL comes from search results stored in backend database
      return this.sanitizer.bypassSecurityTrustUrl(href); // NOSONAR
    }
    return undefined;
  }

  /**
   * Resets the search and navigates to the given help category page.
   *
   * @param id The help category identifier.
   * @throws If `id` is `undefined` (via {@link assertDefined}).
   */
  public goToCategoryLink(id: string | undefined) {
    assertDefined(id);
    this.resetForm();
    this.router.navigate(['/help/category', id]);
  }

  /**
   * Resets the search and navigates to the given help article page.
   *
   * @param categoryId The identifier of the article's category.
   * @param articleId The article identifier.
   */
  public goToArticleLink(
    categoryId: string | undefined,
    articleId: string | undefined
  ) {
    this.resetForm();
    this.router.navigate(['/help/category', categoryId, 'article', articleId]);
  }

  /**
   * Toggles the advanced-search panel. When closing, the panel is collapsed;
   * when opening, the current query is remembered in {@link lastSearchString},
   * the search input is cleared and the panel is opened after a short delay.
   */
  public openCloseCustonSearch() {
    this.lastSearchString = this.searchForm.value.searchString;
    const advancedSearchIG = this.advancedSearchIG();
    if (advancedSearchIG?.expanded) {
      advancedSearchIG.close();
    } else {
      this.searchForm.controls.searchString.setValue('');
      setTimeout(() => {
        this.advancedSearchIG()?.open();
      }, 900);
    }
  }

  /**
   * Resolves the best display name for a node, preferring the title in the
   * active language, then the English title, then the `title` property, then a
   * `news_`-prefixed name, and finally the raw name. The result is truncated to
   * fit the current screen width.
   *
   * @param searchNode The node whose display name is resolved.
   * @returns The (possibly truncated) display name.
   */
  public getName(searchNode: SearchNode) {
    const lang = this.translateService.getActiveLang();

    if (searchNode.title?.[lang] && searchNode.title[lang].trim().length > 0) {
      return this.cutBigText(searchNode.title[lang].trim());
    }

    if (searchNode.title?.en && searchNode.title.en.trim().length > 0) {
      return this.cutBigText(searchNode.title.en.trim());
    }

    if (searchNode.properties?.title) {
      const title = searchNode.properties.title.slice(4, -1);
      if (title.trim().length > 0) {
        return this.cutBigText(title);
      }
    }

    if (searchNode.name?.includes('news_')) {
      return this.cutBigText(searchNode.name?.slice(5));
    }
    return this.cutBigText(searchNode.name);
  }

  /**
   * Truncates text based on the current screen width, appending an ellipsis
   * when the original text exceeds the threshold for that width.
   *
   * @param text The text to truncate.
   * @returns The truncated text, or an empty string when `text` is falsy.
   */
  cutBigText(text: string | undefined) {
    if (!text) return '';

    const screenWidth = window.screen.width;
    const limits = [
      { width: 400, length: 20, checkLength: 30 },
      { width: 600, length: 40, checkLength: 40 },
      { width: 700, length: 50, checkLength: 50 },
      { width: Number.POSITIVE_INFINITY, length: 60, checkLength: 60 },
    ];

    const limit = limits.find((l) => screenWidth < l.width);
    if (!limit) return text;

    return (
      text.slice(0, limit.length) +
      (text.length > limit.checkLength ? '...' : '')
    );
  }

  /**
   * @returns `true` when the current route is a help route (URL contains
   * `/help`).
   */
  public isRouteHelp(): boolean {
    return this.router.url.includes('/help');
  }

  /**
   * @param date The date string to evaluate.
   * @returns `true` when the given date is in the past.
   */
  public isExpired(date: string) {
    return new Date(date) < new Date();
  }
}
