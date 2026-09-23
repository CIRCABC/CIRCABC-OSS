import { DatePipe, Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  linkedSignal,
  OnInit,
  output,
  resource,
  signal,
} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { AlfrescoService } from 'app/core/alfresco.service';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ColumnOptions,
  ExternalRepositoryData,
  ListingOptions,
  Node as ModelNode,
  NotificationService,
  type PreferenceConfiguration,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { SelectableNode } from 'app/core/ui-model/index';
import {
  changeSort,
  getErrorTranslation,
  getSuccessTranslation,
  isContentPreviewable,
  isContentPreviewableFull,
} from 'app/core/util';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { ContentPreviewExtendedComponent } from 'app/group/library/content-preview-ext/content-preview-ext.component';
import { DeleteActionComponent } from 'app/group/library/delete/delete-action.component';
import { DeleteMultipleComponent } from 'app/group/library/delete/delete-multiple.component';
import { FavouriteSwitchComponent } from 'app/group/library/favourite-switch/favourite-switch.component';
import { BulkDownloadPipe } from 'app/group/library/pipes/bulk-download.pipe';
import { SnackbarComponent } from 'app/group/library/snackbar/snackbar.component';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { HintComponent } from 'app/shared/hint/hint.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { NumberBadgeComponent } from 'app/shared/number-badge/number-badge.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { IfRolesPipe } from 'app/shared/pipes/if-roles.pipe';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { SaveAsComponent } from 'app/shared/save-as/save-as.component';
import { ShareComponent } from 'app/shared/share/share.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { environment } from 'environments/environment';
import { CookieService } from 'ngx-cookie-service';
import { firstValueFrom } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

/**
 * Document library browser component (`<cbc-library-browser>`).
 *
 * Renders the main listing of an interest group's document library: the table
 * (or tree) of folders, files and links contained in a parent node, together
 * with the toolbar and per-row actions that operate on them. Responsibilities
 * include:
 * - Displaying the node listing with configurable, sortable, paginated columns
 *   and persisting the column configuration to a cookie and the user's server
 *   side preferences.
 * - Managing item selection (single and select-all) and bulk operations such as
 *   bulk download (with size/count limits and sensitive-content confirmation),
 *   multiple deletion and clipboard (copy/cut) operations.
 * - Providing content preview (via Alfresco renditions or the legacy PDF
 *   rendition endpoint), Office online editing, notification subscription
 *   management and ARES bridge document registration.
 * - Handling navigation through shared-space / library links, remembering the
 *   origin group/node so the UI can offer a "back to origin" affordance.
 *
 * Key collaborators: {@link PermissionEvaluatorService} / {@link PermissionEvaluator}
 * for authorization checks, {@link ClipboardService} for copy/cut operations,
 * {@link NotificationService} and {@link UserService} (generated API) for
 * subscriptions and preferences, {@link AresBridgeHelperService} for ARES
 * registration, {@link SaveAsService} for downloads, {@link AlfrescoService}
 * for renditions and {@link LoginService} for the current user/session.
 */
@Component({
  selector: 'cbc-library-browser',
  templateUrl: './library-browser.component.html',
  styleUrl: './library-browser.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    PagerComponent,
    ShareComponent,
    ReactiveFormsModule,
    DataCyDirective,
    NumberBadgeComponent,
    SpinnerComponent,
    HintComponent,
    FavouriteSwitchComponent,
    UserCardComponent,
    SaveAsComponent,
    DeleteActionComponent,
    SnackbarComponent,
    DeleteMultipleComponent,
    ContentPreviewExtendedComponent,
    ModalComponent,
    DatePipe,
    I18nPipe,
    IfRolesPipe,
    SizePipe,
    TranslocoModule,
    MatDialogModule,
  ],
})
export class LibraryBrowserComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translateService = inject(TranslocoService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly clipboardService = inject(ClipboardService);
  private readonly notificationService = inject(NotificationService);
  private readonly loginService = inject(LoginService);
  private readonly bulkDownloadPipe = inject(BulkDownloadPipe);
  private readonly saveAsService = inject(SaveAsService);
  private readonly permissionEvaluator = inject(PermissionEvaluator);
  private readonly location = inject(Location);
  private readonly i18nPipe = inject(I18nPipe);
  private readonly cookieService = inject(CookieService);
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly aresBridgeHelperService = inject(AresBridgeHelperService);
  private readonly dialog = inject(MatDialog);
  private readonly alfrescoService = inject(AlfrescoService);

  /** Maximum number of nodes that may be included in a single bulk download. */
  public readonly MAX_NODES = 160;
  /**
   * Default listing options (pagination page/limit and sort order) used when the
   * incoming {@link preferences} input does not provide its own.
   */
  private readonly listingOptions: ListingOptions = {
    page: 1,
    limit: 10,
    sort: 'modified_DESC',
  };

  /**
   * Default set of columns to display, used to build the {@link columnForm} when
   * the incoming {@link preferences} input does not provide a column configuration.
   */
  private readonly columnOptions: ColumnOptions = {
    name: true,
    title: true,
    version: true,
    modification: true,
    creation: false,
    size: true,
    expiration: true,
    status: false,
    description: true,
    author: false,
    securityRanking: false,
  };

  /** Required input: the list of selectable nodes (files, folders, links) to display. */
  public readonly contents = input.required<SelectableNode[]>();
  /** Required input: the parent node whose children are listed. */
  public readonly parent = input.required<ModelNode>();
  /**
   * Input: user preference configuration driving the visible columns and the
   * listing (pagination/sort) options. Defaults to {@link columnOptions} and
   * {@link listingOptions} when not supplied.
   */
  public readonly preferences = input<PreferenceConfiguration>({
    library: {
      column: this.columnOptions,
      listing: this.listingOptions,
    },
    search: [],
  });
  /** Input: total number of items available across all pages (for the pager). Defaults to 25. */
  public readonly totalItems = input(25);

  /**
   * Input (aliased as `treeView`): whether the browser should initially render
   * as a tree view rather than the flat listing. Backing value for {@link treeView}.
   */
  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly treeViewInput = input(false, { alias: 'treeView' });
  /** Writable signal mirroring {@link treeViewInput}; toggled locally via {@link toggleTreeView}. */
  readonly treeView = linkedSignal(this.treeViewInput);
  /** Input: when true, the browser runs in a restricted mode with reduced actions. */
  readonly restrictedMode = input(false);
  /** Input: the interest group id the library belongs to (used for ARES bridge and link navigation). */
  readonly igId = input<string>();
  /** Required input: when true, the listing should reset to the first page. */
  readonly resetPage = input.required<boolean>();

  /** Output: emitted when an action requires the parent to reload the listing. */
  public readonly needRefresh = output<ActionEmitterResult>();
  /** Output: emitted with the new page number when the user changes page. */
  public readonly changedPage = output<number>();
  /** Output: emitted with updated listing options when sort or page size changes. */
  public readonly changedListing = output<ListingOptions>();
  /** Output: emitted with the new tree-view state when the user toggles the view mode. */
  public readonly treeViewChange = output<boolean>();

  /** Reactive form holding the visible-column checkboxes, built from the preferences. */
  public columnForm!: FormGroup;
  /** Snapshot of currently selected nodes used when opening the multiple-delete wizard. */
  public selection!: SelectableNode[];
  /** Whether the multiple-deletion wizard/modal is currently shown. */
  public showMultipleDeleteWizard = false;
  /** Local, mutable copy of the listing options (page/limit/sort) for this component. */
  public localListingOptions!: ListingOptions;
  /** Name of the last single item added to the clipboard, used for the confirmation message. */
  public itemToClipboard: string | undefined;
  /** Whether the last clipboard action added the current selection (multiple items). */
  public selecteditemsToClipboard = false;
  /** Whether the content preview panel is currently open. */
  public readonly showPreview = signal(false);
  /** URL of the content currently loaded in the preview panel. */
  public readonly contentURL = signal('');
  /** Node id of the document currently being previewed. */
  public readonly previewDocumentId = signal('');
  /** The node currently selected for preview. */
  public contentToPreview!: SelectableNode;
  /** Toggled on each preview to force the preview URL to change and bust caching. */
  private dummyPreviewUrlChange = false;
  /** The currently logged-in user. */
  private user!: User;
  /** The interest group id extracted from the most recent navigation, used to detect group changes. */
  private currentIgId!: string;
  /** Whether an ARES bridge send operation is currently in progress (guards re-entry). */
  public readonly processing = signal(false);
  /** Whether a bulk download is currently in progress. */
  public readonly bulkDownloading = signal(false);
  /** Signal indicating whether all (selectable) nodes are currently selected. */
  public allSelected = signal(false);

  /**
   * Loads whether the current (non-guest) user is subscribed to notifications
   * for the parent node. Idle while there is no parent id or the user is a
   * guest. Errors are logged and treated as "not subscribed".
   */
  private readonly notificationSubscriptionResource = resource({
    params: () => {
      const parentId = this.parent().id;
      return parentId && !this.isGuest() ? parentId : undefined;
    },
    loader: async ({ params: parentId }) => {
      try {
        const isSubscribed =
          await this.notificationService.isUserSubscribedAsync({
            id: parentId,
            userId: this.loginService.getCurrentUsername(),
          });
        return isSubscribed.subscribed;
      } catch (error) {
        console.error(error);
        return false;
      }
    },
    defaultValue: false,
  });
  /** Whether the current user is subscribed to notifications for the parent node. */
  public readonly isSubscribedToNotifications =
    this.notificationSubscriptionResource.value;

  /**
   * Loads whether the ARES bridge integration is enabled for the current
   * interest group. Idle while there is no {@link igId} or the user is a
   * guest. Errors are logged and treated as "disabled".
   */
  private readonly aresBridgeEnabledResource = resource({
    params: () => {
      const igId = this.igId();
      return igId && !this.isGuest() ? igId : undefined;
    },
    loader: async ({ params: igId }) => {
      try {
        return await this.aresBridgeHelperService.isAresBridgeEnabled(igId);
      } catch (error) {
        console.error(error);
        return false;
      }
    },
    defaultValue: false,
  });
  /** Whether the ARES bridge integration is enabled for the current interest group. */
  private readonly isAresBridgeEnabled = this.aresBridgeEnabledResource.value;
  /** Whether the sensitive-content (SNC) acknowledgement modal is shown before bulk download. */
  public acceptSncShowModal = false;

  /**
   * Subscribes to router navigation events so that stored shared-link navigation
   * information is cleared when the user moves to a different interest group
   * (unless the navigation itself originates from following a link).
   */
  public constructor() {
    // analyses which condition should make the shared space message disappear...
    this.router.events.subscribe((value) => {
      if (value instanceof NavigationEnd) {
        const newIgId = value.url.substring(7, 7 + 36);
        if (
          this.currentIgId !== undefined &&
          this.currentIgId !== newIgId &&
          !value.url.includes('fromLink=true')
        ) {
          this.removeStoredLinkNavigationInfo();
        }
        this.currentIgId = newIgId;
      }
    });
  }

  /**
   * Angular lifecycle hook. Initialises the current user and applies the listing
   * and column preferences (wiring the column form to persist changes after a
   * debounce). Notification subscription state and ARES bridge availability are
   * loaded reactively via {@link notificationSubscriptionResource} and
   * {@link aresBridgeEnabledResource}.
   */
  public ngOnInit() {
    this.user ??= this.loginService.getUser();

    const preferences = this.preferences();
    if (preferences.library?.listing) {
      this.localListingOptions = preferences.library.listing;
    }

    if (preferences.library?.column) {
      this.columnForm = this.fb.group(preferences.library.column);
    }

    this.columnForm.valueChanges.pipe(debounceTime(1000)).subscribe((value) => {
      this.saveColumnConfiguration(value);
    });
  }

  /**
   * Handles the result of a single space/content deletion, showing a success
   * message for supported deletion types and re-emitting the result to trigger a refresh.
   * @param result - The action result emitted by the delete action component.
   */
  public async onDeletedElement(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_SPACE
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_SPACE)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    } else if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_CONTENT
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_CONTENT)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }

    this.needRefresh.emit(result);
  }

  /**
   * Updates the local page number and emits {@link changedPage}.
   * @param p - The new page number to navigate to.
   */
  public changePage(p: number) {
    this.localListingOptions.page = p;
    this.changedPage.emit(this.localListingOptions.page);
  }

  /**
   * Toggles the sort direction/field for the given column and emits {@link changedListing}.
   * @param sort - The column key to sort by.
   */
  public changeSort(sort: string) {
    this.localListingOptions.sort = changeSort(
      this.localListingOptions.sort,
      sort
    );
    this.changedListing.emit(this.localListingOptions);
  }

  /**
   * Updates the page size, resets to the first page and emits {@link changedListing}.
   * @param limit - The new number of items per page.
   */
  public changeLimit(limit: number) {
    this.localListingOptions.limit = limit;
    this.localListingOptions.page = 1;
    this.changedListing.emit(this.localListingOptions);
  }

  /**
   * Selects or deselects all selectable nodes (skipping locked nodes and working
   * copies) and flips the {@link allSelected} state accordingly.
   */
  public toggleSelect() {
    const contents = this.contents();
    if (contents) {
      contents.forEach((content: SelectableNode) => {
        if (!(this.locked(content) || this.workingCopy(content))) {
          if (this.allSelected()) {
            content.selected = false;
          } else {
            content.selected = true;
          }
        }
      });
    }
    this.allSelected.set(!this.allSelected());
  }

  /**
   * Toggles the selection state of a single node.
   * @param content - The node to toggle.
   * @returns `false` if the node is locked or a working copy (and thus cannot be
   * selected), otherwise `true`.
   */
  public toggleSelected(content: SelectableNode): boolean {
    if (this.locked(content) || this.workingCopy(content)) {
      return false;
    }
    this.contents().forEach((contentTmp) => {
      const idx = this.contents().indexOf(contentTmp);
      if (contentTmp.id === content.id) {
        contentTmp.selected = !contentTmp.selected;
        this.contents()[idx] = contentTmp;
      }
    });

    return true;
  }

  /**
   * Builds the {@link selection} from the currently selected nodes and opens the
   * multiple-deletion wizard.
   */
  public showDeleteAllModal() {
    this.selection = [];

    this.contents().forEach((content: SelectableNode) => {
      if (content.selected) {
        this.selection.push(content);
      }
    });

    this.showMultipleDeleteWizard = true;
  }

  /**
   * Handles the result of a multiple-deletion operation, closing the wizard,
   * showing a success or error message, clearing the selection on success and
   * re-emitting the result to trigger a refresh.
   * @param result - The action result emitted by the multiple-delete component.
   */
  public async afterMultipleDeletion(result: ActionEmitterResult) {
    this.showMultipleDeleteWizard = false;
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.DELETE_ALL
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_ALL)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
      this.toggleSelect();
    } else if (
      result.result === ActionResult.FAILED &&
      result.type === ActionType.DELETE_ALL
    ) {
      const text = this.translateService.translate(
        getErrorTranslation(ActionType.DELETE_ALL)
      );
      if (text) {
        this.uiMessageService.addErrorMessage(text, true);
      }
    }
    this.showMultipleDeleteWizard = false;
    this.needRefresh.emit(result);
  }
  /**
   * Whether the current user is a library administrator for the given node.
   * @param selectableNode - The node to evaluate.
   * @returns `true` if the user has library admin rights on the node.
   */
  public isLibAdmin(selectableNode: SelectableNode) {
    return this.permEvalService.isLibAdmin(selectableNode);
  }

  /**
   * Whether the current user owns the given node.
   * @param selectableNode - The node to evaluate.
   * @returns `true` if the current user is the owner, otherwise `false`.
   */
  public isOwner(selectableNode: SelectableNode) {
    if (this.user?.userId) {
      return this.permEvalService.isOwner(selectableNode, this.user.userId);
    }
    return false;
  }

  /**
   * Whether the given node is a plain file (not a folder and not a library link).
   * @param node - The node to test.
   * @returns `true` if the node is a file.
   */
  isFile(node: ModelNode): boolean {
    if (node.type) {
      return !(node.type.includes('folder') || this.isLibraryLink(node));
    }
    return false;
  }

  /**
   * Whether the given node is a folder (and not a library link).
   * @param node - The node to test.
   * @returns `true` if the node is a folder.
   */
  isFolder(node: ModelNode): boolean {
    if (node.type) {
      return node.type.includes('folder') && !this.isLibraryLink(node);
    }
    return false;
  }

  /**
   * Whether the given node is an external URL link.
   * @param node - The node to test.
   * @returns `true` if the node represents a URL link.
   */
  isLink(node: ModelNode): boolean {
    if (node.properties?.mimetype && node.properties.url) {
      return node.properties.isUrl === 'true';
    }
    return false;
  }

  /**
   * Extracts the destination node id from a destination path of the form
   * `group/{ig}/library/{id}`.
   * @param destination - The destination path string.
   * @returns The destination id, or an empty string if the path is not well-formed.
   */
  getDestinationId(destination: string): string {
    const destinationPart = destination.split('/');
    if (destinationPart.length === 4) {
      return destinationPart[3];
    }
    return '';
  }

  /**
   * Whether the given node's type includes the specified library-item marker.
   * @param node - The node to test.
   * @param item - The library item marker (`filelink` or `folderlink`).
   * @returns `true` if the node type includes the marker.
   */
  private isLibraryItem(
    node: ModelNode,
    item: 'filelink' | 'folderlink'
  ): boolean {
    if (node.type) {
      return node.type.includes(item);
    }
    return false;
  }

  /**
   * Whether the given node is a link to a library file.
   * @param node - The node to test.
   * @returns `true` if the node is a file link.
   */
  isLibraryLink(node: ModelNode): boolean {
    return this.isLibraryItem(node, 'filelink');
  }

  /**
   * Whether the given node is a link to a shared space (folder).
   * @param node - The node to test.
   * @returns `true` if the node is a folder link.
   */
  isSharedSpaceLink(node: ModelNode): boolean {
    return this.isLibraryItem(node, 'folderlink');
  }

  /**
   * Whether the given node has a (non-null) expiration date property.
   * @param node - The node to test.
   * @returns `true` if the node has an expiration date.
   */
  hasExpirationDate(node: ModelNode): boolean {
    const expirationDate = node.properties?.expiration_date;
    const hasDate =
      expirationDate !== undefined &&
      expirationDate !== null &&
      expirationDate !== 'null';
    return hasDate;
  }

  /** Removes any stored shared-link navigation info associated with the current parent node. */
  removeStoredLinkNavigationInfo() {
    localStorage.removeItem(`sharedSpaceLink-${this.parent().id}`);
  }

  /**
   * Whether shared-link navigation info is stored for the current parent and the
   * current interest group differs from the link's origin group.
   * @returns `true` if the user arrived here via a shared link from another group.
   */
  isStoredInSharedLinkNavigationInfo(): boolean {
    return (
      localStorage.getItem(`sharedSpaceLink-${this.parent().id}`) !== null &&
      this.igId() !== this.getOriginIgId()
    );
  }

  /**
   * Reads the origin interest group id from the stored shared-link navigation info.
   * @returns The origin interest group id.
   */
  getOriginIgId(): string {
    const item = localStorage.getItem(
      `sharedSpaceLink-${this.parent().id}`
    ) as string;
    return (JSON.parse(item) as { originIgId: string }).originIgId;
  }

  /**
   * Reads the origin node id from the stored shared-link navigation info.
   * @returns The origin node id.
   */
  getOriginId(): string {
    const item = localStorage.getItem(
      `sharedSpaceLink-${this.parent().id}`
    ) as string;
    return (JSON.parse(item) as { originId: string }).originId;
  }

  /**
   * Follows a shared-space/library link: stores the origin navigation info and
   * navigates to the link's destination group/node, showing an error message if
   * the navigation fails.
   * @param node - The link node to open.
   */
  openSharedLinkNavigation(node: ModelNode) {
    if (
      node.properties &&
      (node.properties.destinationIgId || node.properties.destinationId)
    ) {
      this.storeSharedLinkNavigationInfo(node);
      this.router
        .navigate(
          [
            `/group/${node.properties.destinationIgId}/library/${node.properties.destinationId}`,
          ],
          { queryParams: { fromLink: 'true' } }
        )
        .then((resolve) => {
          if (!resolve) {
            const text = this.translateService.translate(
              'spaces.sharing.error'
            );
            this.uiMessageService.addErrorMessage(text);
          }
        });
    }
  }

  /**
   * Persists the origin navigation info (origin group and parent id) for a link's
   * destination so that a "back to origin" affordance can be offered after navigation.
   * @param node - The link node being followed.
   */
  storeSharedLinkNavigationInfo(node: ModelNode) {
    if (node.properties) {
      this.localListingOptions.page = 1;
      localStorage.setItem(
        `sharedSpaceLink-${node.properties.destinationId}`,
        JSON.stringify({
          originIgId: node.properties.originIgId,
          originId: node.parentId,
        })
      );
    }
  }

  /**
   * Adds a single node to the clipboard and records its name for the confirmation message.
   * @param content - The node to copy to the clipboard.
   */
  public addToClipboard(content: SelectableNode) {
    this.clipboardService.addItem(content);
    this.itemToClipboard = content.name;
  }

  /**
   * Adds all currently selected, non-locked nodes to the clipboard and flags that
   * a multi-item clipboard action occurred.
   */
  public addSelectedToClipboard() {
    this.contents().forEach((node: SelectableNode) => {
      if (
        node.selected &&
        node.properties !== undefined &&
        node.properties.locked !== 'true'
      ) {
        this.clipboardService.addItem(node);
      }
    });

    this.selecteditemsToClipboard = true;
  }

  /**
   * Returns the ids of all currently selected nodes.
   * @returns An array of selected node ids.
   */
  public getSelectedNodes() {
    return this.contents()
      .filter((node: SelectableNode) => node.selected)
      .map((node: SelectableNode) => node.id);
  }

  /**
   * Whether the given node is publicly ranked (no security ranking or `PUBLIC`).
   * @param content - The node to evaluate.
   * @returns `true` if the node is public.
   */
  public isPublic(content: ModelNode) {
    return (
      content.properties !== undefined &&
      (content.properties.security_ranking === undefined ||
        content.properties.security_ranking === 'PUBLIC')
    );
  }

  /**
   * Changes the current user's notification subscription for the parent node and
   * refreshes the local subscription state. Shows an error message on failure.
   * @param value - The subscription value to apply.
   */
  public async changeNotificationSubscription(value: string) {
    const parent = this.parent();
    if (value && value !== '' && parent.id) {
      try {
        await this.notificationService.putNotificationAuthorityAsync({
          id: parent.id,
          authority: this.loginService.getCurrentUsername(),
          body: value,
        });
        this.notificationSubscriptionResource.reload();
      } catch (error) {
        console.error(error);
        const text = this.translateService.translate(
          getErrorTranslation(ActionType.CHANGE_SUBSCRIPTION)
        );
        this.uiMessageService.addErrorMessage(text);
      }
    }
  }

  /**
   * Downloads all currently selected nodes as a single zip archive. Warns if the
   * selection exceeds {@link MAX_NODES}, and handles the server's "too big"
   * (HTTP 507) response with a dedicated error message.
   */
  public async bulkDownload() {
    this.acceptSncShowModal = false;

    if (this.getSelectedNodes().length > this.MAX_NODES) {
      const text = this.translateService.translate(
        'label.bulk.download.limit',
        { limit: this.MAX_NODES }
      );
      this.uiMessageService.addErrorMessage(text);
    }
    const url = this.bulkDownloadPipe.transform(this.getSelectedNodes());
    const name = 'bulk.zip';
    this.bulkDownloading.set(true);
    try {
      await this.saveAsService.saveUrlAsync(url, name);
    } catch (error) {
      if (error.name === 'HttpErrorResponse' && error.status === 507) {
        const text = this.translateService.translate(
          'label.bulk.download.too.big',
          { fileSize: error?.error?.size ?? 2147479168 }
        );
        this.uiMessageService.addErrorMessage(text);
      } else {
        this.uiMessageService.addErrorMessage(error.message);
      }
      console.error(error);
    } finally {
      this.bulkDownloading.set(false);
    }
  }

  /**
   * Whether the current user is an anonymous guest.
   * @returns `true` if the user is a guest.
   */
  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  /**
   * Whether the given content can be previewed, using the Alfresco full-preview
   * check when the Alfresco API is enabled and the legacy check otherwise.
   * @param content - The node to evaluate.
   * @returns `true` if the content is previewable.
   */
  public isPreviewable(content: SelectableNode): boolean {
    if (environment.useAlfrescoAPI) {
      return isContentPreviewableFull(content);
    }
    return isContentPreviewable(content);
  }

  /**
   * Opens the preview panel for the given content, building the appropriate
   * content URL. When using the Alfresco API and only a full-preview rendition is
   * available, a PDF rendition is requested (and created if needed) before
   * building the rendition URL; otherwise the direct content or legacy PDF
   * rendition URL is used.
   * @param content - The node to preview.
   */
  public async previewContent(content: SelectableNode) {
    this.contentToPreview = content;
    this.dummyPreviewUrlChange = !this.dummyPreviewUrlChange;
    if (environment.useAlfrescoAPI) {
      if (!isContentPreviewable(content) && isContentPreviewableFull(content)) {
        try {
          const rendition = await this.alfrescoService.getRendition(
            content.id as string,
            'pdf'
          );
          if (rendition.entry.status !== 'CREATED') {
            await this.alfrescoService.createRendition(
              content.id as string,
              'pdf'
            );
          }
        } catch (error) {
          // Do not show popup for preview not available
          console.error(error);
        }

        this.contentURL.set(
          `${environment.serverURL}api/-default-/public/alfresco/versions/1/nodes/${content.id}/renditions/pdf/content?attachment=true&alf_ticket=${this.loginService.getTicket()}&dummy=${
            this.dummyPreviewUrlChange
          }`
        );
      } else {
        this.contentURL.set(
          `${environment.serverURL}api/-default-/public/alfresco/versions/1/nodes/${content.id}/content?attachment=false&alf_ticket=${this.loginService.getTicket()}&dummy=${
            this.dummyPreviewUrlChange
          }`
        );
      }
    } else {
      this.contentURL.set(
        `${
          environment.serverURL
        }pdfRendition?documentId=workspace://SpacesStore/${
          content.id
        }&response=content&ticket=${this.loginService.getTicket()}&dummy=${
          this.dummyPreviewUrlChange
        }`
      );
    }

    this.previewDocumentId.set(content.id as string);
    this.showPreview.set(true);
  }

  /** Closes the content preview panel. */
  public closePreview() {
    this.showPreview.set(false);
  }

  /**
   * Whether the given content can be edited in Office online: Office integration
   * must be enabled, the node must be a supported Office file (docx/xlsx/pptx),
   * and it must not be locked or already editable inline.
   * @param content - The node to evaluate.
   * @returns `true` if the content can be edited in Office.
   */
  public canEditInOffice(content: SelectableNode) {
    return (
      this.isOfficeIntegrationEnabled() &&
      this.isFile(content) &&
      content.name !== undefined &&
      (content.name.endsWith('.docx') ||
        content.name.endsWith('.xlsx') ||
        content.name.endsWith('.pptx')) &&
      content?.properties?.locked !== 'true' &&
      content?.properties?.editInline !== 'true'
    );
  }

  /**
   * Whether Office online integration is configured (an Office client id is set).
   * @returns `true` if Office integration is enabled.
   */
  private isOfficeIntegrationEnabled(): boolean {
    return environment.officeClientId !== '';
  }

  /**
   * Opens the given document in Office online in a new tab, using update mode for
   * an existing working copy or edit mode otherwise, and schedules a page reload
   * to reflect the resulting changes.
   * @param content - The node to open in Office.
   */
  public openDocInOffice(content: SelectableNode) {
    const id = content?.properties?.workingCopyId ?? content.id;
    if (content?.properties?.workingCopy === 'true') {
      setTimeout(() => location.reload(), 2000);
      const url = `${environment.serverURL.substring(
        0,
        environment.serverURL.length - 1
      )}${environment.baseHref}office?id=${id}&mode=update`;
      window.open(url, '_blank');
    } else {
      setTimeout(() => location.reload(), 10000);
      const url = `${environment.serverURL.substring(
        0,
        environment.serverURL.length - 1
      )}${environment.baseHref}office?id=${id}&mode=edit`;
      window.open(url, '_blank');
    }
  }

  /**
   * Whether the given content is currently being edited online by the current user
   * (edit-inline enabled and the current user owns the working copy).
   * @param content - The node to evaluate.
   * @returns `true` if the current user is editing the content online.
   */
  public isEditOnline(content: SelectableNode): boolean {
    if (this.loginService.isGuest()) {
      return false;
    }
    return (
      content?.properties?.editInline === 'true' &&
      this.loginService.getUser().userId ===
        content?.properties?.workingCopyOwner
    );
  }

  /**
   * Whether the current selection can be sent to the ARES bridge. Requires an
   * internal user, the ARES bridge to be globally and group-enabled, no folder in
   * the selection, a non-OSS release and all selected nodes to be deletable.
   * @returns `true` if sending to the ARES bridge is allowed.
   */
  public canSendToAresBridge() {
    if (this.user?.properties?.domain === 'external') {
      return false;
    }
    if (!environment.aresBridgeEnabled) {
      return false;
    }
    if (!this.isAresBridgeEnabled()) {
      return false;
    }

    if (this.isFolderSelected()) {
      return false;
    }

    if (environment.circabcRelease !== 'oss') {
      return this.areNodesDeletable();
    }
    return false;
  }

  /**
   * Whether at least one folder is present in the current selection.
   * @returns `true` if a folder is selected.
   */
  private isFolderSelected() {
    return this.contents().some((node) => node.selected && this.isFolder(node));
  }

  /**
   * Sends the selected file nodes to the ARES bridge. Detects nodes already sent
   * (grouped by save number and version) and, when duplicates or previously-sent
   * nodes are found, prompts the user with the appropriate confirmation dialog
   * before performing the send. Guards against concurrent invocations via
   * {@link processing}.
   */
  public async sendToAresBridge() {
    const selectedNodes: SelectableNode[] = [];
    const nodeLogDuplicated: ExternalRepositoryData[] = [];

    this.contents().forEach((content: SelectableNode) => {
      if (content.selected && this.isFile(content)) {
        selectedNodes.push(content);
      }
    });

    if (selectedNodes.length === 0) {
      return;
    }
    if (this.processing()) {
      return;
    }

    const sentNodes =
      await this.aresBridgeHelperService.getAlreadySentToAresBridge(
        selectedNodes
      );

    this.processing.set(true);

    //map all logs of selectedNodes by saveNumber
    const result = this.groupBy(sentNodes, (log) => log.saveNumber);

    selectedNodes.forEach((e) => {
      result.forEach((element, index) => {
        //remove index if node not include
        if (!element.some((elem) => elem.nodeId === e.id)) {
          result.delete(index);
        }
        //remove index if has different version
        if (
          !element.some(
            (elem) => elem.versionLabel === e.properties?.versionLabel
          )
        ) {
          result.delete(index);
        }
      });
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    result.forEach((value: any) => {
      if (selectedNodes.length === 1) {
        if (
          selectedNodes[0].properties?.versionLabel === value[0].versionLabel
        ) {
          nodeLogDuplicated.push(value[0]);
        }
      } else {
        nodeLogDuplicated.push(value[0]);
      }
    });

    if (nodeLogDuplicated.length > 0) {
      await this.showDuplicatesAresBrigeDialog(nodeLogDuplicated);
      this.processing.set(false);
      return;
    }

    if (sentNodes.length > 0) {
      await this.showConfirmationDialog(selectedNodes, sentNodes);
      this.processing.set(false);
      return;
    }
    try {
      await this.aresBridgeHelperService.sendToAresBridge(selectedNodes);
    } finally {
      this.processing.set(false);
    }
  }

  /**
   * Groups the items of an array into a Map keyed by the value returned by the
   * grouper function.
   * @typeParam K - The key type.
   * @typeParam V - The item type.
   * @param arrayr - The array of items to group.
   * @param grouper - Function producing the grouping key for an item.
   * @returns A Map from key to the list of items sharing that key.
   */
  groupBy<K, V>(arrayr: V[], grouper: (item: V) => K) {
    return arrayr.reduce((store, item) => {
      const key = grouper(item);
      if (store.has(key)) {
        store.get(key)?.push(item);
      } else {
        store.set(key, [item]);
      }
      return store;
    }, new Map<K, V[]>());
  }

  /**
   * Opens a confirmation dialog informing the user that the selected nodes are
   * duplicates already registered in the ARES bridge, and waits for it to close.
   * @param nodeLogDuplicated - The external repository logs of the duplicated nodes.
   */
  private async showDuplicatesAresBrigeDialog(
    nodeLogDuplicated: ExternalRepositoryData[]
  ) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      ariaLabel: 'Dialog',
      data: {
        title: 'label.send.to.ares',
        layoutStyle: 'sendDuplicatesManyToAres',
        nodeLog: nodeLogDuplicated,
      },
    });

    // listen to response
    await firstValueFrom(dialogRef.afterClosed());
  }

  /**
   * Opens a confirmation dialog for nodes previously sent to the ARES bridge and,
   * if the user confirms, sends the given nodes.
   * @param nodes - The nodes to send if confirmed.
   * @param nodeLog - The external repository logs of previously-sent nodes.
   */
  private async showConfirmationDialog(
    nodes: SelectableNode[],
    nodeLog: ExternalRepositoryData[]
  ) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      ariaLabel: 'Dialog',
      data: {
        title: 'label.send.to.ares',
        layoutStyle: 'sendManyToAres',
        nodeLog: nodeLog,
      },
    });

    // listen to response
    const dialogResult = await firstValueFrom(dialogRef.afterClosed());
    if (dialogResult) {
      await this.aresBridgeHelperService.sendToAresBridge(nodes);
    }
  }

  /** Whether a single item has been added to the clipboard (drives the confirmation UI). */
  get isNotItemToClipboardUndefined(): boolean {
    return this.itemToClipboard !== undefined;
  }

  /** Whether selected items have been added to the clipboard (drives the confirmation UI). */
  get isNotSelectedItemsToClipboardUndefined(): boolean {
    return this.selecteditemsToClipboard !== false;
  }

  /**
   * Returns the URL property of the given content.
   * @param content - The node to read.
   * @returns The URL, or an empty string if unavailable.
   */
  public url(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.url;
    }
    return '';
  }

  /**
   * Returns the locale property of the given content.
   * @param content - The node to read.
   * @returns The locale, or an empty string if unavailable.
   */
  public locale(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.locale;
    }
    return '';
  }

  /**
   * Returns the version label of the given content.
   * @param content - The node to read.
   * @returns The version label, or an empty string if unavailable.
   */
  public versionLabel(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.versionLabel;
    }
    return '';
  }
  /**
   * Whether the given content is locked.
   * @param content - The node to read.
   * @returns `true` if the content is locked.
   */
  public locked(content: SelectableNode): boolean {
    if (content.properties) {
      return content.properties.locked === 'true';
    }
    return false;
  }
  /**
   * Whether the given content is a working copy.
   * @param content - The node to read.
   * @returns `true` if the content is a working copy.
   */
  public workingCopy(content: SelectableNode): boolean {
    if (content.properties) {
      return content.properties.workingCopy === 'true';
    }
    return false;
  }
  /**
   * Returns the multilingual flag of the given content.
   * @param content - The node to read.
   * @returns The multilingual property value, or `'false'` if unavailable.
   */
  public multilingual(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.multilingual;
    }
    return 'false';
  }

  /**
   * Returns the modification date of the given content.
   * @param content - The node to read.
   * @returns The modified date string, or `null` if unavailable.
   */
  public modified(content: SelectableNode): string | null {
    if (content.properties) {
      return content.properties.modified;
    }
    return null;
  }

  /**
   * Returns the creation date of the given content.
   * @param content - The node to read.
   * @returns The created date string, or `null` if unavailable.
   */
  public created(content: SelectableNode): string | null {
    if (content.properties) {
      return content.properties.created;
    }
    return null;
  }

  /**
   * Returns the size of the given content.
   * @param content - The node to read.
   * @returns The size string, or `null` if unavailable.
   */
  public size(content: SelectableNode): string | null {
    if (content.properties) {
      return content.properties.size;
    }
    return null;
  }

  /**
   * Returns the modifier (last editor) of the given content.
   * @param content - The node to read.
   * @returns The modifier, or an empty string if unavailable.
   */
  public modifier(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.modifier;
    }
    return '';
  }

  /**
   * Returns the owner of the content, falling back to the creator when no owner
   * is set.
   * @param content - The node to read.
   * @returns The owner or creator, or an empty string if unavailable.
   */
  public creatoOrOwner(content: SelectableNode): string {
    if (content.properties) {
      return content.properties.owner
        ? content.properties.owner
        : content.properties.creator;
    }
    return '';
  }

  /**
   * Returns the tooltip translation key for a node's selection checkbox, warning
   * when the node cannot be deleted because it is locked or a working copy.
   * @param content - The node to evaluate.
   * @returns A translation key, or an empty string when selectable.
   */
  public getSelectCheckboxTooltip(content: SelectableNode): string {
    if (this.locked(content) || this.workingCopy(content)) {
      return 'label.locked.no.deletion';
    }

    return '';
  }

  /**
   * Whether every currently selected node can be deleted by the current user
   * according to the required library permissions.
   * @returns `true` if all selected nodes are deletable.
   */
  public areNodesDeletable(): boolean {
    let result = true;

    const selecteds: SelectableNode[] = [];
    this.contents().forEach((node: SelectableNode) => {
      if (node.selected) {
        selecteds.push(node);
      }
    });

    for (const node of selecteds) {
      if (
        !this.permissionEvaluator.hasAnyOfPermissions(
          node,
          ['LibManageOwn', 'LibAdmin', 'LibFullEdit'],
          ['LibFullEdit', 'LibManageOwn']
        )
      ) {
        result = false;
        break;
      }
    }

    return result;
  }

  /**
   * Toggles between tree view and flat listing, persisting the choice to
   * `localStorage` and emitting {@link treeViewChange}.
   */
  public toggleTreeView() {
    this.treeView.set(!this.treeView());
    const treeView = this.treeView();
    localStorage.setItem('showTreeView', `${treeView}`);
    this.treeViewChange.emit(treeView);
  }

  /** Navigates back to the previous location in browser history. */
  goBack() {
    this.location.back();
  }

  /**
   * Returns the localized title of the given node.
   * @param content - The node to read.
   * @returns The translated title, or an empty string if none is available.
   */
  public getTitle(content: ModelNode): string {
    let result = '';

    if (content.title) {
      const title = this.i18nPipe.transform(content.title);

      if (title !== '') {
        result = title;
      }
    }

    return result;
  }

  /**
   * Persists the column configuration to a cookie and to the user's server-side
   * preferences.
   * @param column - The column configuration values from the form.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private saveColumnConfiguration(column: any) {
    this.cookieService.set('columnsOptions', JSON.stringify(column));
    this.saveUserPreferences(column);
  }

  /**
   * Saves the given column configuration into the user's server-side preferences.
   * Errors are logged and swallowed.
   * @param column - The column configuration to persist.
   */
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private async saveUserPreferences(column: any) {
    const columnPref = column as ColumnOptions;
    const preferences = this.preferences();
    if (preferences?.library?.column) {
      preferences.library.column = columnPref;
    }
    try {
      if (this.user?.userId) {
        await this.userService.saveUserPreferencesAsync({
          userId: this.user.userId,
          preferenceConfiguration: preferences,
        });
      }
    } catch (error) {
      console.error(error);
    }
  }

  /**
   * Computes the number of table columns to render, starting from a base count
   * and adding one per enabled column control in {@link columnForm}.
   * @returns The total column count.
   */
  public getColumnCount() {
    let count = 3;
    if (this.columnForm) {
      Object.keys(this.columnForm.controls).forEach((key: string) => {
        const control = this.columnForm.get(key);
        if (control instanceof AbstractControl && control.value !== null) {
          if (control.value) {
            count++;
          }
        }
      });
    }
    return count;
  }
  /**
   * Returns the number of additional translations for a node (translations count
   * minus the original), as a string.
   * @param content - The node to read.
   * @returns The number of extra translations, or `'0'` if none.
   */
  public getTranslationsCount(content: SelectableNode): string {
    return content?.properties?.translations === undefined
      ? '0'
      : `${Number(content?.properties?.translations) - 1}`;
  }

  /**
   * Whether the given content is sensitive (security ranking `SENSITIVE` or
   * `SPECIAL_HANDLING`).
   * @param content - The node to evaluate.
   * @returns `true` if the content is sensitive.
   */
  public isSensitive(content: SelectableNode) {
    return (
      content?.properties?.security_ranking === 'SENSITIVE' ||
      content?.properties?.security_ranking === 'SPECIAL_HANDLING'
    );
  }

  /**
   * Returns the names of the currently selected sensitive (SNC) files.
   * @returns An array of selected sensitive file names.
   */
  public getSncFiles() {
    return this.contents()
      .filter(
        (node: SelectableNode) =>
          node.selected &&
          (node?.properties?.security_ranking === 'SENSITIVE' ||
            node?.properties?.security_ranking === 'SPECIAL_HANDLING')
      )
      .map((node: SelectableNode) => node.name);
  }

  /**
   * Initiates a bulk download, first showing the sensitive-content acknowledgement
   * modal when running the ECHA release and sensitive files are selected;
   * otherwise proceeds directly with {@link bulkDownload}.
   */
  public bulkDownloadCheck() {
    if (
      environment.circabcRelease === 'echa' &&
      this.getSncFiles().length > 0
    ) {
      this.acceptSncShowModal = true;
    } else {
      this.bulkDownload();
    }
  }

  /** Translated warning message shown before downloading sensitive content, including a policy link. */
  get message() {
    return this.translateService.translate('label.dialog.alert.snc.download', {
      link: `<a href="https://ec.europa.eu/transparency/documents-register/detail?ref=C(2019)1904&lang=en" target="_blank">C(2019)1904</a>`,
    });
  }
}
