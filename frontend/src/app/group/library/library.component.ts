import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  linkedSignal,
  resource,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ColumnOptions,
  InterestGroup,
  Node as ModelNode,
  NodesService,
  PagedNodes,
  PreferenceConfiguration,
  SpaceService,
  UserService,
} from 'app/core/generated/circabc';
import { LibraryIdService } from 'app/core/libraryId.service';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UserPreferencesService } from 'app/core/user-preferences.service';
import { getSuccessTranslation } from 'app/core/util';
import { BreadcrumbComponent } from 'app/group/breadcrumb/breadcrumb.component';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { FlatMessageComponent } from 'app/shared/flat-message/flat-message.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { NotificationMessageComponent } from 'app/shared/notification-message/notification-message.component';
import { IfOrRolesPipe } from 'app/shared/pipes/if-or-roles.pipe';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { CookieService } from 'ngx-cookie-service';
import { LibraryBrowserComponent } from './browser/library-browser.component';
import { ClipboardComponent } from './clipboard/clipboard.component';
import { AddDropdownComponent } from './dropdown/add-dropdown.component';
import { FolderTreeViewComponent } from './folder-tree-view/folder-tree-view.component';

/**
 * Top-level view component for an interest group's document library.
 *
 * Rendered at the `cbc-library` selector, this component orchestrates the
 * whole library browsing experience for a given node (folder or file):
 * - a breadcrumb and responsive sub-menu for navigation,
 * - an "add" dropdown for creating/uploading content,
 * - an optional folder tree view (toggled via `localStorage`),
 * - the main {@link LibraryBrowserComponent} listing of the current folder's
 *   children (with paging, sorting and configurable columns), and
 * - a clipboard side panel for copy/cut/paste operations.
 *
 * Responsibilities include resolving the current node from the route,
 * determining whether the user has full or restricted (no-access) library
 * permissions, loading the node's contents from the backend, persisting and
 * restoring listing/column preferences (via cookies, session storage and the
 * user's saved preferences), and exposing permission helpers used by the
 * template to gate administrative actions.
 *
 * Key collaborators: {@link SpaceService} and {@link NodesService} for content
 * retrieval, {@link PermissionEvaluatorService} for permission checks,
 * {@link UserService}/{@link UserPreferencesService} for preference
 * persistence, {@link LoginService} for the current user, and
 * {@link UiMessageService}/{@link TranslocoService} for user feedback.
 */
@Component({
  selector: 'cbc-library',
  templateUrl: './library.component.html',
  styleUrl: './library.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    FlatMessageComponent,
    BreadcrumbComponent,
    NotificationMessageComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    AddDropdownComponent,
    FolderTreeViewComponent,
    LibraryBrowserComponent,
    ClipboardComponent,
    IfOrRolesPipe,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class LibraryComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly spaceService = inject(SpaceService);
  private readonly nodesService = inject(NodesService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translateService = inject(TranslocoService);
  private readonly loginService = inject(LoginService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly userService = inject(UserService);
  private readonly cookieService = inject(CookieService);
  private readonly libraryIdService = inject(LibraryIdService);
  private readonly userPreferencesService = inject(UserPreferencesService);

  /** The current route params, as a signal. */
  private readonly routeParams = toSignal(this.route.params);
  /** The current route query params, as a signal. */
  private readonly routeQueryParams = toSignal(this.route.queryParams);
  /** The current route data, as a signal (the `group` resolved by {@link resolveGroup}). */
  private readonly routeData = toSignal(this.route.data);

  /** Identifier of the current node, taken from the `nodeId` route parameter. */
  public readonly nodeId = computed(() => this.routeParams()?.nodeId ?? '');

  /** Default set of columns shown in the listing, keyed by column visibility. */
  public readonly columnOptions: ColumnOptions = {
    name: true,
    title: true,
    version: true,
    modification: true,
    creation: false,
    size: true,
    expiration: true,
    status: false,
    description: false,
    author: false,
    securityRanking: false,
  };

  /** Listing/column preferences read from cookies at construction time, if present. */
  private readonly cookieListing = this.loadListingConfiguration();
  private readonly cookieColumn = this.loadColumnConfiguration();

  /**
   * Resource resolving the user's saved preferences via
   * {@link UserPreferencesService}. Only runs when neither cookie provides a
   * complete listing/column configuration (mirrors the previous
   * `ngOnInit` guard); otherwise stays idle and cookie/default values are used.
   * Errors are logged and otherwise ignored, matching the previous behaviour.
   */
  private readonly userPreferencesResource = resource({
    params: () =>
      !(this.cookieColumn || this.cookieListing) ||
      this.cookieColumn?.title === undefined ||
      this.cookieColumn?.securityRanking === undefined
        ? true
        : undefined,
    loader: async () => {
      try {
        return await this.userPreferencesService.waitForPreferences();
      } catch (error) {
        console.error(error);
        return undefined;
      }
    },
  });

  /**
   * Base preference configuration combining the account preferences (if
   * loaded) with cookie overrides for listing and column options.
   */
  private readonly basePreferences = computed<PreferenceConfiguration>(() => {
    const loaded = this.userPreferencesResource.value();
    const prefs: PreferenceConfiguration = loaded ?? {
      library: {
        column: this.columnOptions,
        listing: { page: 1, limit: 10, sort: 'modified_DESC' },
      },
      search: [],
    };
    if (this.cookieListing) {
      prefs.library.listing = this.cookieListing;
    }
    if (this.cookieColumn) {
      prefs.library.column = this.cookieColumn;
    }
    return prefs;
  });

  /**
   * Aggregated user preference configuration for the library, combining
   * {@link basePreferences} with pagination/sorting overrides coming from the
   * current route query params (`p`, `n`, `sort`). Recomputed whenever the
   * route query params or the base preferences change; still directly
   * writable so {@link changePage} and {@link changeListing} can reflect
   * their own updates before the resulting navigation completes.
   */
  public readonly preferences = linkedSignal<PreferenceConfiguration>(() =>
    this.applyQueryParams(this.basePreferences(), this.routeQueryParams())
  );

  /** Available page numbers for the pager (populated from the listing). */
  public pages: number[] = [];
  /** Whether the folder tree view is displayed (persisted in `localStorage`). */
  public treeView = localStorage.getItem('showTreeView') === 'true';

  /** Whether the clipboard side panel is open. */
  public clipboardOpen = false;
  /** Number of items currently held in the clipboard. */
  public amountOfItemsInClipboard = 0;
  /** Flag used to signal that the listing should reset to the first page. */
  public resetPage = false;

  /** The currently authenticated user, resolved from {@link LoginService}. */
  private readonly user = this.loginService.getUser();

  /**
   * Shows an error message when the route has no `nodeId` param. This is a
   * genuine imperative side effect (a one-off toast, not data to be stored in
   * state), so it is kept as an `effect()` rather than folded into a
   * resource. In practice `nodeId` is a mandatory path segment, so this only
   * guards against an unexpected/malformed route.
   */
  constructor() {
    effect(() => {
      if (this.routeParams() !== undefined && !this.nodeId()) {
        const res = this.translateService.translate('error.library.read');
        this.uiMessageService.addErrorMessage(res);
      }
    });
  }

  /**
   * Resource resolving the current node and restricted-mode state for
   * {@link nodeId}.
   *
   * When the route-resolved group is not a full {@link InterestGroup} it
   * attempts to fetch the owning group for the node; on failure the view is
   * marked restricted. Restricted mode is also enabled when the group's
   * library permission is `LibNoAccess`. The node itself is then either
   * fetched directly, or synthesised as a virtual "Library" root node when
   * access is restricted to the library root.
   */
  private readonly nodeResource = resource({
    params: () => {
      const nodeId = this.nodeId();
      return nodeId ? { nodeId, group: this.routeData()?.group } : undefined;
    },
    loader: async ({ params: { nodeId, group } }) => {
      let restrictedMode = false;
      let resolvedGroup = group;

      if (resolvedGroup && !this.isInterestGroup(resolvedGroup)) {
        try {
          resolvedGroup = await this.nodesService.getGroupAsync({
            id: nodeId,
          });
          if (resolvedGroup.permissions?.library === 'LibNoAccess') {
            restrictedMode = true;
          }
        } catch (error) {
          console.error(error);
          resolvedGroup = { ...resolvedGroup, libraryId: nodeId };
          restrictedMode = true;
        }
      } else if (resolvedGroup?.permissions?.library === 'LibNoAccess') {
        restrictedMode = true;
      }

      let node: ModelNode;
      if (!restrictedMode || nodeId !== resolvedGroup?.libraryId) {
        node = await this.nodesService.getNodeAsync({ id: nodeId });
      } else {
        this.libraryIdService.updateLibraryId(resolvedGroup.libraryId);
        node = {
          id: resolvedGroup.libraryId,
          parentId: resolvedGroup.id,
          name: 'Library',
          permissions: {
            LibNoAccess: 'ALLOWED',
          },
        };
      }

      return { node, group: resolvedGroup, restrictedMode };
    },
  });

  /** The interest group whose library is being displayed. */
  public readonly group = computed(() => this.nodeResource.value()?.group);
  /** The currently selected library node (folder or file) resolved from the route. */
  public readonly node = computed(
    () => this.nodeResource.value()?.node as ModelNode
  );
  /** True when the user only has restricted (no-access) permission on the library. */
  public readonly restrictedMode = computed(
    () => this.nodeResource.value()?.restrictedMode ?? false
  );
  /** Whether {@link node} is a folder. */
  public readonly nodeIsFolder = true;
  /** Whether {@link node} is a file. */
  public readonly nodeIsFile = false;

  /**
   * Resource fetching the children of the current node according to the
   * active paging and sorting preferences.
   *
   * In restricted mode it retrieves only the accessible (top-level) folders
   * and files via {@link SpaceService.getRestrictedChildrenAsync} and filters
   * out items nested under inaccessible folders; otherwise it uses
   * {@link SpaceService.getChildrenAsync}. Any error results in an empty
   * listing (no dedicated error UI exists for this section).
   */
  private readonly contentResource = resource({
    params: () => {
      const nodeId = this.nodeId();
      if (!nodeId || this.nodeResource.isLoading()) {
        return undefined;
      }
      const listing = this.preferences().library.listing;
      return {
        nodeId,
        restrictedMode: this.restrictedMode(),
        page: listing.page,
        limit: listing.limit,
        sort: listing.sort,
      };
    },
    loader: async ({ params }) => {
      const { nodeId, restrictedMode, page, limit, sort } = params;
      sessionStorage.setItem('libraryPage', `${page}`);
      sessionStorage.setItem('libraryLimit', `${limit}`);
      sessionStorage.setItem('librarySort', `${sort}`);

      try {
        // get only folders
        if (restrictedMode) {
          const contents = await this.spaceService.getRestrictedChildrenAsync({
            id: nodeId,
            language: this.translateService.getActiveLang(),
            guest: false,
            limit: -1,
            page,
            order: sort,
            folderOnly: false,
            fileOnly: false,
          });

          const listOfFolderIds = [];
          for (const content of contents.data) {
            if (this.isFolder(content)) {
              listOfFolderIds.push(content.id);
            }
          }

          const currentContents: PagedNodes = { data: [], total: 0 };
          for (const content of contents.data) {
            if (
              (this.isFile(content) &&
                (content.parentId === nodeId ||
                  !listOfFolderIds.includes(content.parentId))) ||
              (this.isFolder(content) &&
                !listOfFolderIds.includes(content.parentId))
            ) {
              currentContents.data.push(content);
              currentContents.total = currentContents.total + 1;
            }
          }
          return currentContents;
        }
        return await this.spaceService.getChildrenAsync({
          id: nodeId,
          language: this.translateService.getActiveLang(),
          guest: false,
          limit,
          page,
          order: sort,
          folderOnly: false,
          fileOnly: false,
          skipExpiredItems: true,
        });
      } catch (error) {
        console.error(error);
        return { data: [], total: 0 };
      }
    },
  });

  /** The paged children of the current node currently rendered in the listing. */
  public readonly currentContents = computed(() =>
    this.contentResource.hasValue() ? this.contentResource.value() : undefined
  );

  /** True while the node or its content is being fetched from the backend. */
  public readonly loading = computed(
    () => this.nodeResource.isLoading() || this.contentResource.isLoading()
  );
  /** True once the component has completed its initial load. */
  public readonly ready = computed(() => this.nodeResource.hasValue());

  /**
   * Applies pagination and sorting values coming from the route query params
   * onto a copy of the given preferences. Invalid or non-positive values fall
   * back to sensible defaults (page 1, page size 10). A page size of `-1` is
   * treated as a valid "show all" value.
   *
   * @param prefs The base preferences to apply overrides onto.
   * @param params The raw query parameters (`p` page, `n` page size, `sort`).
   * @returns A new {@link PreferenceConfiguration} with query param overrides applied.
   */
  private applyQueryParams(
    prefs: PreferenceConfiguration,
    params: { [key: string]: string } | undefined
  ): PreferenceConfiguration {
    const listing: ListingOptions = { ...prefs.library.listing };

    if (params?.p) {
      try {
        listing.page = Number(params.p) > 0 ? Number(params.p) : 1;
      } catch (error) {
        console.error(error);
        listing.page = 1;
      }
    }

    if (params?.n) {
      try {
        listing.limit =
          Number(params.n) > 0 || Number(params.n) === -1
            ? Number(params.n)
            : 10;
      } catch (error) {
        console.error(error);
        listing.limit = 10;
      }
    }

    if (params?.sort) {
      listing.sort = params.sort;
    }

    return {
      ...prefs,
      library: { ...prefs.library, listing },
    };
  }

  /**
   * Handles the result of a child action (e.g. an upload) and reloads content.
   *
   * Shows a success notification when a file upload succeeded, then refreshes
   * the current listing regardless of the action type.
   *
   * @param result The outcome emitted by a child component action.
   */
  public refresh(result: ActionEmitterResult) {
    if (result.result === ActionResult.SUCCEED) {
      if (result.type === ActionType.UPLOAD_FILE) {
        const res = this.translateService.translate(
          getSuccessTranslation(ActionType.UPLOAD_FILE)
        );
        this.uiMessageService.addSuccessMessage(res, true);
      }
    }
    this.contentResource.reload();
  }

  /**
   * Navigates to the requested listing page, updating the route query params
   * to reflect the new page (the content resource reloads reactively once the
   * params change).
   *
   * @param page The 1-based page number to display.
   * @returns A promise that resolves once navigation completes.
   */
  public async changePage(page: number) {
    const listing = { ...this.preferences().library.listing, page };
    this.preferences.set({
      ...this.preferences(),
      library: { ...this.preferences().library, listing },
    });
    await this.navigateWithListing(listing);
  }

  /**
   * Updates the route query params to reflect the given paging/sorting state,
   * keeping the URL shareable and bookmarkable. The content resource reloads
   * reactively once the resulting query param change is reflected in
   * {@link preferences}.
   *
   * @param listing The listing options to reflect in the URL.
   * @returns A promise that resolves once navigation completes.
   */
  private async navigateWithListing(listing: ListingOptions) {
    await this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        p: listing.page,
        n: listing.limit,
        sort: listing.sort,
      },
    });
  }

  /**
   * Applies new listing options, persists them to the user account, and
   * refreshes the view. Persistence errors are logged but do not prevent the
   * refresh.
   *
   * @param listingOptions The new paging/sorting options to apply.
   * @returns A promise that resolves once the listing has been refreshed.
   */
  public async changeListing(listingOptions: ListingOptions) {
    this.preferences.set({
      ...this.preferences(),
      library: { ...this.preferences().library, listing: listingOptions },
    });
    try {
      await this.saveListing(listingOptions);
    } catch (error) {
      console.error("Can't save listing options", error);
    }
    await this.navigateWithListing(listingOptions);
  }

  /**
   * Determines whether the current user is a full administrator of the group,
   * i.e. holds admin permission across all group service areas (library,
   * directory, information, newsgroup and event).
   *
   * @returns `true` if the user is a group-wide administrator, otherwise `false`.
   */
  public isGroupAdmin(): boolean {
    const group = this.group();
    if (group?.permissions === undefined) {
      return false;
    }
    return (
      group.permissions.library === 'LibAdmin' &&
      group.permissions.directory === 'DirAdmin' &&
      group.permissions.information === 'InfAdmin' &&
      group.permissions.newsgroup === 'NwsAdmin' &&
      group.permissions.event === 'EveAdmin'
    );
  }

  /**
   * Determines whether the current user administers the library, either through
   * the group's `LibAdmin` permission or through node-level permissions
   * evaluated by {@link PermissionEvaluatorService}.
   *
   * @returns `true` if the user is a library administrator, otherwise `false`.
   */
  public isLibAdmin(): boolean {
    const group = this.group();
    if (group?.permissions === undefined) {
      return false;
    }
    if (group.permissions.library === 'LibAdmin') {
      return true;
    }
    return this.permEvalService.isLibAdmin(this.node());
  }

  /**
   * Determines whether the current user may manage their own content (or has a
   * higher privilege) on the current node.
   *
   * @returns `true` if the user has "manage own" or higher permission,
   * otherwise `false`.
   */
  public isLibManageOwn(): boolean {
    if (this.group()?.permissions === undefined) {
      return false;
    }
    return this.permEvalService.isLibManageOwnOrHigher(this.node());
  }

  /** Closes the clipboard side panel. */
  public clipboardSidebarClosed() {
    this.clipboardOpen = false;
  }

  /**
   * Updates the displayed clipboard item count. The update is deferred with a
   * zero-delay timeout to avoid Angular change-detection conflicts.
   *
   * @param amount The number of items currently in the clipboard.
   */
  public itemsInClipboard(amount: number) {
    setTimeout(() => {
      this.amountOfItemsInClipboard = amount;
    }, 0);
  }

  /**
   * Determines whether the given node is a file (i.e. its type does not include
   * "folder").
   *
   * @param node The node to test.
   * @returns `true` if the node is a file, otherwise `false`.
   */
  isFile(node: ModelNode): boolean {
    if (node.type) {
      return !node.type.includes('folder');
    }
    return false;
  }

  /**
   * Determines whether the given node is a folder (i.e. its type includes
   * "folder").
   *
   * @param node The node to test.
   * @returns `true` if the node is a folder, otherwise `false`.
   */
  isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.includes('folder');
    }
    return false;
  }

  /**
   * Determines whether the currently displayed node is the library root.
   *
   * @returns `true` if the current node is the "Library" root node.
   */
  isLibraryRoot(): boolean {
    return this.node().name === 'Library';
  }

  /**
   * Persists the given listing options into the user's preferences on the
   * backend, when a user id is available.
   *
   * @param listingOptions The listing options to save.
   * @returns A promise that resolves once the preferences have been saved.
   */
  private async saveListing(listingOptions: ListingOptions) {
    const prefs = this.preferences();
    if (prefs.library) {
      if (this.user?.userId) {
        await this.userService.saveUserPreferencesAsync({
          userId: this.user.userId,
          preferenceConfiguration: {
            ...prefs,
            library: { ...prefs.library, listing: listingOptions },
          },
        });
      }
    }
  }

  /**
   * Reads the column configuration previously stored in the `columnsOptions`
   * cookie, if present.
   *
   * @returns The parsed {@link ColumnOptions}, or `undefined` when no cookie exists.
   */
  private loadColumnConfiguration(): ColumnOptions | undefined {
    if (this.cookieService.check('columnsOptions')) {
      const columns = this.cookieService.get('columnsOptions');
      return JSON.parse(columns) as ColumnOptions;
    }
    return undefined;
  }

  /**
   * Reads the listing configuration previously stored in the `listingOptions`
   * cookie, if present.
   *
   * @returns The parsed {@link ListingOptions}, or `undefined` when no cookie exists.
   */
  private loadListingConfiguration(): ListingOptions | undefined {
    if (this.cookieService.check('listingOptions')) {
      const listing = this.cookieService.get('listingOptions');
      return JSON.parse(listing) as ListingOptions;
    }
    return undefined;
  }

  /**
   * Type guard that narrows a group-like value to a full {@link InterestGroup}
   * by checking for the presence of a `name` property.
   *
   * @param group The value to test, either an {@link InterestGroup} or a minimal `{ id }` object.
   * @returns `true` (with type narrowing) when the value is an {@link InterestGroup}.
   */
  private isInterestGroup(
    group: InterestGroup | { id: string }
  ): group is InterestGroup {
    return (group as InterestGroup).name !== undefined;
  }
}
