import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  linkedSignal,
  resource,
  signal,
} from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import { AnalyticsService } from 'app/core/analytics.service';
import {
  Category,
  CategoryService,
  Header,
  HeaderService,
  InterestGroup,
  Node as ModelNode,
  NodesService,
  User,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { IndexedInterestGroup } from 'app/core/ui-model/index';
import { sortI18nProperty } from 'app/core/util';
import { HeaderComponent } from 'app/shared/header/header.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { NavigatorComponent } from 'app/shared/navigator/navigator.component';
import { DownloadPipe } from 'app/shared/pipes/download.pipe';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { ExplorerDropdownComponent } from './explorer-dropdown/explorer-dropdown.component';
import { GroupCardComponent } from './group-card/group-card.component';

/**
 * Standalone page component (`<cbc-explorer>`) that lets users browse the
 * CIRCABC content tree in three sequential steps: institutional
 * **headers**, then their **categories**, and finally the **interest
 * groups** contained in a selected category.
 *
 * The component renders a header/navigator, a responsive sub-menu, a set of
 * cards (group cards) and a search box. It drives navigation through an
 * internal {@link ExplorerComponent.step} state machine (`'headers'`,
 * `'categories'`, `'groups'`) and remembers the current header/category in
 * `sessionStorage` so the selection survives reloads. Deep-linking is
 * supported via the `headerId`, `categoryId` and `start` query parameters.
 *
 * Key collaborators: {@link HeaderService}, {@link CategoryService} and
 * {@link NodesService} for fetching content, {@link LoginService} for
 * permission checks, {@link TranslocoService}/{@link I18nPipe} for
 * localisation, {@link AnalyticsService} for search tracking and
 * {@link UiMessageService} for surfacing errors.
 */
@Component({
  selector: 'cbc-explorer',
  templateUrl: './explorer.component.html',
  styleUrl: './explorer.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HeaderComponent,
    NavigatorComponent,
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    ExplorerDropdownComponent,
    GroupCardComponent,
    DownloadPipe,
    SecurePipe,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class ExplorerComponent {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly headerService = inject(HeaderService);
  private readonly categoryService = inject(CategoryService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translateService = inject(TranslocoService);
  private readonly loginService = inject(LoginService);
  private readonly i18nPipe = inject(I18nPipe);
  private readonly nodesService = inject(NodesService);
  private readonly analyticsService = inject(AnalyticsService);

  /**
   * One-shot bootstrap: loads and sorts the top-level headers, reads the
   * deep-link query parameters (`headerId`, `categoryId`, `start`) and
   * restores/initialises the current header and category (from the query or
   * from `sessionStorage`), then loads the interest groups when the flow
   * lands on the `'groups'` step. Errors are logged and result in an empty
   * bootstrap (headers step, no header/category selected).
   */
  private readonly bootstrapResource = resource({
    loader: async () => {
      try {
        const headers = await this.loadAndSortHeaders();

        const { headerFromQuery, categoryFromQuery, reset } =
          this.getQueryParams();

        const headerResult = await this.initializeHeader(
          headerFromQuery,
          reset
        );
        const categoryResult = await this.initializeCategory(
          categoryFromQuery,
          reset,
          headerResult.step
        );

        return {
          headers,
          currentHeader: headerResult.currentHeader,
          currentCategory: categoryResult.currentCategory,
          step: categoryResult.step,
        };
      } catch (error) {
        console.error(error);
        return {
          headers: [] as Header[],
          currentHeader: undefined as Header | undefined,
          currentCategory: undefined as ModelNode | undefined,
          step: 'headers',
        };
      }
    },
  });

  /**
   * Once the bootstrap has resolved, loads the interest groups for the
   * restored/deep-linked category when the flow lands on the `'groups'`
   * step. Mirrors the tail of the former `ngOnInit`, which called
   * {@link listInterestGroups} after initialising the header/category.
   */
  private readonly groupsBootstrapResource = resource({
    params: () =>
      this.bootstrapResource.hasValue()
        ? this.bootstrapResource.value()
        : undefined,
    loader: async ({ params: bootstrap }) => {
      const currentCategory = bootstrap.currentCategory;
      if (
        bootstrap.step === 'groups' &&
        currentCategory?.id &&
        currentCategory?.name
      ) {
        const categ: Category = {
          id: currentCategory.id,
          name: currentCategory.name,
        };
        await this.listInterestGroups(categ);
      }
      return true;
    },
  });

  /** Whether the component is performing its initial bootstrap. */
  public readonly preparing = computed(
    () =>
      this.bootstrapResource.isLoading() ||
      this.groupsBootstrapResource.isLoading()
  );

  /** Institutional headers available at the top level, sorted for display. */
  public readonly headers = linkedSignal(() =>
    this.bootstrapResource.hasValue()
      ? this.bootstrapResource.value().headers
      : []
  );
  /** Categories belonging to the {@link currentHeader}, sorted for display. */
  public readonly categories = signal<Category[]>([]);
  /** Interest groups belonging to the {@link currentCategory}, sorted for display. */
  public interestGroups: InterestGroup[] = [];
  /**
   * Interest groups grouped by their first (upper-cased) letter, used to
   * render an alphabetical index. Populated by {@link filterGroups} and
   * {@link listInterestGroups}.
   */
  public readonly groupsWithIndex = signal<{
    [key: string]: IndexedInterestGroup[];
  }>({});

  /** Current informational/error message shown to the user, if any. */
  public uiMessage: string | undefined;

  /** Whether the headers list is currently being loaded. */
  public loadingHeaders!: boolean;
  /** Whether the categories list is currently being loaded. */
  public readonly loadingCategories = signal(false);
  /** Whether the interest groups list (IGs) is currently being loaded. */
  public readonly loadingIGs = signal(false);
  /** Reactive form backing the group search input. */
  public searchForm!: FormGroup;
  /** Currently selected header, or `undefined` when at the headers step. */
  public readonly currentHeader = linkedSignal(() =>
    this.bootstrapResource.hasValue()
      ? this.bootstrapResource.value().currentHeader
      : undefined
  );
  /** Currently selected category node, or `undefined` when not yet chosen. */
  public readonly currentCategory = linkedSignal(() =>
    this.bootstrapResource.hasValue()
      ? this.bootstrapResource.value().currentCategory
      : undefined
  );
  /** Whether the "request access" modal is visible. */
  public showRequestModal = false;

  /**
   * Current step of the browsing flow: `'headers'`, `'categories'` or
   * `'groups'`. Drives which section of the template is rendered.
   */
  public readonly step = linkedSignal(() =>
    this.bootstrapResource.hasValue()
      ? this.bootstrapResource.value().step
      : 'headers'
  );

  /**
   * Type guard validating that a string is a well-formed RFC 4122 UUID
   * (versions 1–5). Used to sanitise `headerId`/`categoryId` query params
   * before they are used in API calls.
   *
   * @param value The candidate string (may be `null`).
   * @returns `true` if `value` is a valid UUID, narrowing its type to `string`.
   */
  private isValidUuid(value: string | null): value is string {
    if (!value) {
      return false;
    }
    // RFC 4122 UUID v1-v5
    return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(
      value
    );
  }

  /**
   * Fetches all headers from the backend and sorts them by their localised
   * description (falling back to name).
   *
   * @returns A promise resolving to the sorted headers.
   */
  private async loadAndSortHeaders(): Promise<Header[]> {
    const unsortedHeaders = await this.headerService.getHeadersAsync({
      guest: true,
    });
    unsortedHeaders.sort((a: Header, b: Header) =>
      sortI18nProperty(
        a.description,
        b.description,
        this.getCurrentLang(),
        this.getDefaultLang(),
        a.name,
        b.name
      )
    );
    return unsortedHeaders;
  }

  /**
   * Reads the deep-link query parameters from the current route. Only
   * valid UUIDs are accepted for `headerId`/`categoryId`; the presence of a
   * `start` parameter requests a reset of any restored session state.
   *
   * @returns An object with the validated `headerFromQuery` and
   *   `categoryFromQuery` values (or `null`) and the `reset` flag.
   */
  private getQueryParams() {
    let headerFromQuery: string | null = null;
    let categoryFromQuery: string | null = null;
    let reset = false;

    this.route.queryParamMap.subscribe((queryParams) => {
      reset = queryParams.has('start');

      if (queryParams.has('headerId')) {
        const candidate = queryParams.get('headerId');
        headerFromQuery = this.isValidUuid(candidate) ? candidate : null;
      }

      if (queryParams.has('categoryId')) {
        const candidate = queryParams.get('categoryId');
        categoryFromQuery = this.isValidUuid(candidate) ? candidate : null;
      }
    });

    return { headerFromQuery, categoryFromQuery, reset };
  }

  /**
   * Establishes the current header during initialisation. If a header id
   * came from the query it is fetched and the flow advances to the
   * `'categories'` step; otherwise a previously stored header is restored
   * from `sessionStorage` unless a reset was requested.
   *
   * @param headerFromQuery The validated header id from the query, or `null`.
   * @param reset When `true`, ignores any header stored in the session.
   * @returns A promise resolving to the current header and step.
   */
  private async initializeHeader(
    headerFromQuery: string | null,
    reset: boolean
  ): Promise<{ currentHeader: Header | undefined; step: string }> {
    if (headerFromQuery) {
      const currentHeader = await this.headerService.getHeaderAsync({
        id: headerFromQuery,
      });
      sessionStorage.removeItem('currentCategory');
      return { currentHeader, step: 'categories' };
    }

    const headerFromSession = sessionStorage.getItem('currentHeader');
    if (headerFromSession && !reset) {
      return {
        currentHeader: JSON.parse(headerFromSession) as Header,
        step: 'categories',
      };
    }

    return { currentHeader: undefined, step: 'headers' };
  }

  /**
   * Establishes the current category during initialisation. If a category
   * id came from the query it is fetched and the flow advances to the
   * `'groups'` step; otherwise a previously stored category is restored from
   * `sessionStorage` unless a reset was requested.
   *
   * @param categoryFromQuery The validated category id from the query, or `null`.
   * @param reset When `true`, ignores any category stored in the session.
   * @param currentStep The step established so far (from {@link initializeHeader}).
   * @returns A promise resolving to the current category and step.
   */
  private async initializeCategory(
    categoryFromQuery: string | null,
    reset: boolean,
    currentStep: string
  ): Promise<{ currentCategory: ModelNode | undefined; step: string }> {
    if (categoryFromQuery) {
      const currentCategory = await this.categoryService.getCategoryAsync({
        id: categoryFromQuery,
      });
      return { currentCategory, step: 'groups' };
    }

    const categoryFromSession = sessionStorage.getItem('currentCategory');
    if (categoryFromSession && !reset) {
      return {
        currentCategory: JSON.parse(categoryFromSession) as Category,
        step: 'groups',
      };
    }

    return { currentCategory: undefined, step: currentStep };
  }

  /**
   * Filters the loaded interest groups against a free-text search and
   * rebuilds the alphabetical {@link groupsWithIndex} map. Matching is
   * performed case-insensitively against a group's name, its localised
   * titles and its descriptions. When a search term is supplied, the
   * matching count is reported to {@link AnalyticsService}. An empty search
   * includes every group.
   *
   * @param search The search text; falsy/empty shows all groups.
   */
  public filterGroups(search: string) {
    let i = 1;
    const groupsWithIndex: { [key: string]: IndexedInterestGroup[] } = {};

    if (search) {
      for (const ig of this.interestGroups) {
        const indexGroup = { ...ig, index: i };

        if (
          ig.name?.toLowerCase().includes(search.toLowerCase()) ||
          (ig.title && this.containsValueInMap(ig.title, search)) ||
          (ig.description && this.containsValueInMap(ig.description, search))
        ) {
          this.addGroupToMap(groupsWithIndex, indexGroup);
          i += 1;
        }
      }
      this.analyticsService.trackSiteSearch(search, 'group-explorer', i);
    } else {
      for (const ig of this.interestGroups) {
        const indexGroup = { ...ig, index: i };
        this.addGroupToMap(groupsWithIndex, indexGroup);
        i += 1;
      }
    }
    this.groupsWithIndex.set(groupsWithIndex);
  }

  /**
   * Adds an interest group to the given index map under the bucket of its
   * first (upper-cased) letter, creating the bucket if necessary.
   *
   * @param groupsWithIndex The index map being built.
   * @param group The interest group (optionally already indexed) to add.
   */
  private addGroupToMap(
    groupsWithIndex: { [key: string]: IndexedInterestGroup[] },
    group: InterestGroup | IndexedInterestGroup
  ) {
    const letter = this.getGroupFirstLetter(group).toUpperCase();
    if (letter in groupsWithIndex) {
      groupsWithIndex[letter].push(group);
    } else {
      groupsWithIndex[letter] = [group];
    }
  }

  /**
   * Returns the alphabetical index letters present in
   * {@link groupsWithIndex}, sorted ascending.
   *
   * @returns The sorted list of letter keys, or an empty array if no groups.
   */
  public getKeysOfGroups(): string[] {
    const groupsWithIndex = this.groupsWithIndex();
    if (groupsWithIndex) {
      return Object.keys(groupsWithIndex).sort((a, b) => a.localeCompare(b));
    }
    return [];
  }

  /**
   * Returns the interest groups filed under a given index letter.
   *
   * @param letterKey The upper-cased index letter to look up.
   * @returns The groups for that letter, or an empty array if none.
   */
  public getGroupInIndex(letterKey: string): IndexedInterestGroup[] {
    const groupsWithIndex = this.groupsWithIndex();
    if (groupsWithIndex) {
      return groupsWithIndex[letterKey];
    }
    return [];
  }

  /**
   * Checks whether any value of a localisation map contains the search text
   * (case-insensitive).
   *
   * @param map A language-keyed map of localised strings.
   * @param search The text to look for.
   * @returns `true` if at least one value contains the search text.
   */
  private containsValueInMap(
    map: { [key: string]: string },
    search: string
  ): boolean {
    return Object.values(map).some((value) =>
      value.toLowerCase().includes(search.toLowerCase())
    );
  }

  /**
   * Selects a header and loads its categories. Advances the flow to the
   * `'categories'` step, persists the selection in `sessionStorage`, clears
   * any stored category, and sorts the fetched categories by localised
   * title (falling back to name). Guards against concurrent loads. Errors
   * are surfaced via {@link UiMessageService}.
   *
   * @param header The header to open; when `undefined` the method returns immediately.
   * @returns A promise that resolves once the categories have been loaded.
   */
  public async listCategories(header: Header | undefined) {
    if (header === undefined) {
      return;
    }
    this.step.set('categories');
    this.currentHeader.set(header);
    sessionStorage.setItem('currentHeader', JSON.stringify(header));
    sessionStorage.removeItem('currentCategory');

    if (this.loadingIGs() || this.loadingCategories()) {
      return;
    }
    this.loadingCategories.set(true);
    this.uiMessage = undefined;

    try {
      if (header.id) {
        const unsortedCategories =
          await this.headerService.getCategoriesByHeaderIdAsync({
            id: header.id,
            language: this.getCurrentLang(),
          });
        unsortedCategories.sort((a: Category, b: Category) =>
          sortI18nProperty(
            a.title,
            b.title,
            this.getCurrentLang(),
            this.getDefaultLang(),
            a.name,
            b.name
          )
        );
        this.categories.set(unsortedCategories);

        this.interestGroups = [];
      }
    } catch (err) {
      this.uiMessageService.addErrorMessage(err);
    }
    this.loadingCategories.set(false);
  }

  /**
   * Selects a category and loads its interest groups. Advances the flow to
   * the `'groups'` step, persists the selection in `sessionStorage`,
   * refreshes the full category node, sorts the groups by localised title
   * (falling back to name) and rebuilds the alphabetical
   * {@link groupsWithIndex} map. Guards against concurrent loads. Errors are
   * surfaced via {@link UiMessageService}.
   *
   * @param category The category node to open; when `undefined` the method returns immediately.
   * @returns A promise that resolves once the interest groups have been loaded.
   */
  public async listInterestGroups(category: ModelNode | undefined) {
    if (category === undefined) {
      return;
    }

    this.step.set('groups');
    this.currentCategory.set(category);
    sessionStorage.setItem('currentCategory', JSON.stringify(category));

    if (this.loadingIGs()) {
      return;
    }
    this.uiMessage = undefined;
    this.loadingIGs.set(true);

    try {
      if (category.id) {
        this.currentCategory.set(
          await this.nodesService.getNodeAsync({ id: category.id })
        );

        const unsortedInterestGroups =
          await this.categoryService.getInterestGroupsByCategoryIdAsync({
            id: category.id,
            language: this.getCurrentLang(),
          });
        unsortedInterestGroups.sort((a: InterestGroup, b: InterestGroup) =>
          sortI18nProperty(
            a.title,
            b.title,
            this.getCurrentLang(),
            this.getDefaultLang(),
            a.name,
            b.name
          )
        );
        this.interestGroups = unsortedInterestGroups;

        let i = 1;
        const groupsWithIndex: { [key: string]: IndexedInterestGroup[] } = {};
        for (const ig of this.interestGroups) {
          const indexGroup = { ...ig, index: i };
          this.addGroupToMap(groupsWithIndex, indexGroup);
          i += 1;
        }
        this.groupsWithIndex.set(groupsWithIndex);
      }
    } catch (err) {
      this.uiMessageService.addErrorMessage(err);
    }
    this.loadingIGs.set(false);
  }

  /**
   * Navigates to the dashboard of the given interest group.
   *
   * @param ig The interest group to open.
   */
  public openInterestGroup(ig: InterestGroup): void {
    this.router.navigate(['/group', ig.id]);
  }

  /**
   * @returns The currently active Transloco language code.
   */
  private getCurrentLang(): string {
    return this.translateService.getActiveLang();
  }

  /**
   * @returns The default Transloco language code.
   */
  private getDefaultLang(): string {
    return this.translateService.getDefaultLang();
  }

  /**
   * Determines whether the logged-in user is a platform administrator or a
   * CIRCABC administrator. Guests and users without properties are excluded.
   *
   * @returns `true` if the user has admin or CIRCABC-admin privileges.
   */
  public isAdminOrCircabcAdmin(): boolean {
    const user: User = this.loginService.getUser();
    if (
      user.properties === null ||
      user.userId === '' ||
      user.userId === 'guest'
    ) {
      return false;
    }
    return (
      user.properties !== undefined &&
      (user.properties.isAdmin === 'true' ||
        user.properties.isCircabcAdmin === 'true')
    );
  }

  /**
   * @returns `true` if a user is authenticated (i.e. not a guest).
   */
  isLoggedIn(): boolean {
    return !this.loginService.isGuest();
  }

  /**
   * Resolves a human-readable label for a category node, preferring its
   * localised title and falling back to its name.
   *
   * @param category The category node to describe.
   * @returns The localised title, or the name, or an empty string.
   */
  public getCategoryGroupDescription(category: ModelNode): string {
    if (category.name === undefined) {
      return '';
    }

    let result = category.name;

    if (category.title) {
      const title = this.i18nPipe.transform(category.title);
      if (title !== '' && title !== undefined) {
        result = title;
      }
    }

    return result;
  }
  /**
   * Resolves a human-readable label for an interest group, preferring its
   * localised title and falling back to its name.
   *
   * @param interestGroup The interest group to describe.
   * @returns The localised title, or the name.
   */
  public getInterestGroupDescription(interestGroup: InterestGroup): string {
    let result = '';
    if (interestGroup.title !== undefined) {
      result = this.i18nPipe.transform(interestGroup.title);
    }
    if (result === '') {
      result = interestGroup.name;
    }
    return result;
  }

  /**
   * Determines whether the user is a category administrator for the
   * currently selected category, based on its permissions.
   *
   * @returns `true` if the current category grants `CircaCategoryAdmin`.
   */
  isCategoryAdmin(): boolean {
    if (this.currentCategory()?.permissions?.CircaCategoryAdmin === 'ALLOWED') {
      return true;
    }
    return false;
  }

  /** Opens the "request access" modal by setting {@link showRequestModal}. */
  startRequestModal() {
    this.showRequestModal = true;
  }

  /**
   * @returns `true` when the browsing flow is on the headers step.
   */
  public isStepHeaders(): boolean {
    return this.step() === 'headers';
  }

  /**
   * @returns `true` when the browsing flow is on the categories step.
   */
  public isStepCategories(): boolean {
    return this.step() === 'categories';
  }

  /**
   * @returns `true` when the browsing flow is on the groups step.
   */
  public isStepGroups(): boolean {
    return this.step() === 'groups';
  }

  /**
   * Resets the explorer back to the headers step, clearing the stored
   * header/category from both the component state and `sessionStorage`.
   */
  public reset() {
    this.step.set('headers');
    sessionStorage.removeItem('currentCategory');
    sessionStorage.removeItem('currentHeader');
    this.currentCategory.set(undefined);
    this.currentHeader.set(undefined);
  }

  /**
   * Chooses the logo image to display for a header based on well-known
   * institution names contained in the header name, falling back to the
   * generic CIRCABC logo.
   *
   * @param header The header whose logo image path is requested.
   * @returns The relative path to the appropriate logo image.
   */
  public getHeaderImgName(header: Header) {
    if (header.name.includes('Justice') && header.name.includes('European')) {
      return 'img/court-of-justice-128.png';
    }
    if (
      header.name.includes('Commission') &&
      header.name.includes('European')
    ) {
      return 'img/LOGO-CE_Vertical_EN_128.png';
    }
    if (
      header.name.includes('Parliament') &&
      header.name.includes('European')
    ) {
      return 'img/EP-logo-128.png';
    }
    if (
      header.name.includes('Programmes') &&
      header.name.includes('European')
    ) {
      return 'img/euro-institutions-128.png';
    }

    return 'img/LOGO_circabc_header-128.png';
  }

  /**
   * Determines whether a category carries a custom logo reference.
   *
   * @param category The category to inspect.
   * @returns `true` if `logoRef` is set and non-empty.
   */
  public hasCategoryLogo(category: Category): boolean {
    return !(category.logoRef === undefined || category.logoRef === '');
  }

  /**
   * Determines whether a model node carries a custom logo reference in its
   * properties.
   *
   * @param category The model node to inspect.
   * @returns `true` if `properties.logoRef` is set and non-empty.
   */
  public hasModelNodeLogo(category: ModelNode): boolean {
    if (category?.properties) {
      return !(
        category.properties.logoRef === undefined ||
        category.properties.logoRef === ''
      );
    }
    return false;
  }

  /**
   * Extracts the final path segment (the file/reference name) from a model
   * node's `logoRef` property.
   *
   * @param category The model node whose logo reference is read.
   * @returns The last segment of the logo reference, or an empty string.
   */
  public getLogoRef(category: ModelNode): string {
    if (category?.properties) {
      const parts = category.properties.logoRef.split('/');
      return parts.at(-1) ?? '';
    }
    return '';
  }

  /**
   * Returns the leading character used to bucket an interest group in the
   * alphabetical index, derived from its localised title when available or
   * otherwise from its name.
   *
   * @param group The interest group (optionally already indexed).
   * @returns A single-character string.
   */
  getGroupFirstLetter(group: InterestGroup | IndexedInterestGroup): string {
    if (group.title && Object.keys(group.title).length > 0) {
      return this.i18nPipe.transform(group.title).substring(0, 1);
    }
    return group.name.substring(0, 1);
  }

  /**
   * Determines whether the administrative sub-menu should be displayed:
   * either for platform/CIRCABC admins, or for category admins when a
   * category is currently selected.
   *
   * @returns `true` if the admin menu should be shown.
   */
  public shouldDisplayMenu() {
    return (
      this.isAdminOrCircabcAdmin() ||
      (this.isCategoryAdmin() && this.currentCategory())
    );
  }
}
