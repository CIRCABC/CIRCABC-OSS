import { DatePipe, I18nSelectPipe, Location } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { ActivatedRoute, Data, Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { AlfrescoService } from 'app/core/alfresco.service';
import { AresBridgeHelperService } from 'app/core/ares-bridge-helper.service';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ContentService,
  DynamicPropertiesService,
  DynamicPropertyDefinition,
  ExternalRepositoryData,
  GuardsService,
  InterestGroup,
  KeywordDefinition,
  KeywordsService,
  Node as ModelNode,
  NodesService,
  NotificationService,
  OfficeEditResult,
  PagedNodes,
  SpaceService,
  TopicService,
  Translations,
  User,
  Version,
} from 'app/core/generated/circabc';
import { LibraryIdService } from 'app/core/libraryId.service';
import { LoadingService } from 'app/core/loading.service';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { SaveAsService } from 'app/core/save-as.service';
import { Quote } from 'app/core/ui-model/index';
import {
  getErrorTranslation,
  getSuccessTranslation,
  isContentPreviewable,
  isContentPreviewableFull,
} from 'app/core/util';
import { BreadcrumbComponent } from 'app/group/breadcrumb/breadcrumb.component';
import { AddPostComponent } from 'app/group/forum/post/add-post.component';
import { PostComponent } from 'app/group/forum/post/post.component';
import { CreateDetailsTopicComponent } from 'app/group/forum/topic/create-topic/create-details-topic.component';
import { DeleteTopicComponent } from 'app/group/forum/topic/delete-topic.component';
import { EditTopicComponent } from 'app/group/forum/topic/edit-topic/edit-topic.component';
import { AddKeywordComponent } from 'app/group/keywords/add/add-keyword.component';
import { KeywordTagComponent } from 'app/group/keywords/tag/keyword-tag.component';
import { ClipboardService } from 'app/group/library/clipboard/clipboard.service';
import { ContentPreviewExtendedComponent } from 'app/group/library/content-preview-ext/content-preview-ext.component';
import { DeleteActionComponent } from 'app/group/library/delete/delete-action.component';
import { FavouriteSwitchComponent } from 'app/group/library/favourite-switch/favourite-switch.component';
import { SnackbarComponent } from 'app/group/library/snackbar/snackbar.component';
import { UpdateContentComponent } from 'app/group/library/update-content/update-content.component';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { ConfirmDialogComponent } from 'app/shared/confirm-dialog/confirm-dialog.component';
import { IfRoleGEDirective } from 'app/shared/directives/ifrolege.directive';
import { IfRolesDirective } from 'app/shared/directives/ifroles.directive';
import { HistoryComponent } from 'app/shared/history/history.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { IfRolePipe } from 'app/shared/pipes/if-role.pipe';
import { IfRoleGePipe } from 'app/shared/pipes/if-role-ge.pipe';
import { SecurePipe } from 'app/shared/pipes/secure.pipe';
import { SizePipe } from 'app/shared/pipes/size.pipe';
import { ThumbnailPipe } from 'app/shared/pipes/thumbnail.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { SaveAsComponent } from 'app/shared/save-as/save-as.component';
import { ShareComponent } from 'app/shared/share/share.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { environment } from 'environments/environment';
import { firstValueFrom } from 'rxjs';
import { CancelCheckoutComponent } from './cancel-checkout/cancel-checkout.component';
import { CheckinComponent } from './checkin/checkin.component';
import { EnableMultilingualComponent } from './enable-multilingual/enable-multilingual.component';
import { SummarizeService } from './summarize/summarize.service';
import {
  SummarizeDialogComponent,
  SummarizeDialogData,
} from './summarize/summarize-dialog.component';

/**
 * Standalone Angular component (selector `cbc-node-properties`) that renders the
 * detailed view of a single library node — a document, folder or link — within an
 * interest group's library.
 *
 * It displays the node's metadata (title, description, dates, authors, size,
 * security ranking, dynamic properties, keywords, ARES bridge registration data,
 * etc.), version history and, for documents, an inline content preview and an
 * associated comment/forum thread. It also exposes the full set of node actions:
 * download / save-as, checkout / checkin / cancel-checkout, edit online in
 * Office, update content, delete, take ownership, enable multilingual, add to
 * clipboard, subscribe to notifications, send to the ARES bridge and AI
 * summarization.
 *
 * The component collaborates with a large number of backend services
 * (via {@link NodesService}, {@link ContentService}, {@link TopicService},
 * {@link KeywordsService}, {@link DynamicPropertiesService},
 * {@link NotificationService}, {@link SpaceService}, {@link GuardsService}),
 * permission evaluation ({@link PermissionEvaluatorService}), the ARES bridge
 * ({@link AresBridgeHelperService}), Alfresco renditions ({@link AlfrescoService})
 * and AI summarization ({@link SummarizeService}). Route data and parameters drive
 * which node and version are loaded.
 *
 * The view is built from many child components declared in the `imports` array
 * (breadcrumb, share, history, forum posts, update/checkin wizards, preview, …).
 */
@Component({
  selector: 'cbc-node-properties',
  templateUrl: './details.component.html',
  styleUrl: './details-component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    BreadcrumbComponent,
    FavouriteSwitchComponent,
    ShareComponent,
    SpinnerComponent,
    UserCardComponent,
    KeywordTagComponent,
    AddKeywordComponent,
    MatExpansionModule,
    SaveAsComponent,
    IfRoleGEDirective,
    IfRolesDirective,
    DeleteActionComponent,
    MatDividerModule,
    HistoryComponent,
    PostComponent,
    PagerComponent,
    AddPostComponent,
    SnackbarComponent,
    CancelCheckoutComponent,
    CheckinComponent,
    UpdateContentComponent,
    CreateDetailsTopicComponent,
    DeleteTopicComponent,
    EditTopicComponent,
    EnableMultilingualComponent,
    ContentPreviewExtendedComponent,
    DatePipe,
    I18nPipe,
    IfRoleGePipe,
    IfRolePipe,
    SecurePipe,
    SizePipe,
    ThumbnailPipe,
    TranslocoModule,
    MatDialogModule,
  ],
})
export class DetailsComponent implements OnInit {
  private readonly nodesService = inject(NodesService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly translateService = inject(TranslocoService);
  private readonly contentService = inject(ContentService);
  private readonly loadingService = inject(LoadingService);
  private readonly loginService = inject(LoginService);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly keywordService = inject(KeywordsService);
  private readonly dynamicPropertiesService = inject(DynamicPropertiesService);
  private readonly topicService = inject(TopicService);
  private readonly clipboardService = inject(ClipboardService);
  private readonly location = inject(Location);
  private readonly notificationService = inject(NotificationService);
  private readonly saveAsService = inject(SaveAsService);
  private readonly i18nPipe = inject(I18nPipe);
  private readonly i18nSelectPipe = inject(I18nSelectPipe);
  private readonly guardsService = inject(GuardsService);
  private readonly aresBridgeHelperService = inject(AresBridgeHelperService);
  private readonly spaceService = inject(SpaceService);
  private readonly dialog = inject(MatDialog);
  private readonly libraryIdService = inject(LibraryIdService);
  private readonly alfrescoService = inject(AlfrescoService);
  private readonly summarizeService = inject(SummarizeService);
  // This component loads almost all of its template-bound state imperatively
  // from route subscriptions, async/await continuations and RxJS callbacks
  // rather than from synchronous template events. Converting every field to a
  // signal is impractical here, so under OnPush we mark the view for check after
  // those asynchronous mutations.
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  /** Log of ARES bridge registration entries for the current node's versions. */
  public nodeLog: ExternalRepositoryData[] = [];
  /** The library node (document, folder or link) currently being displayed. */
  public node!: ModelNode;
  /** Identifier of the node being displayed, taken from the route parameters. */
  public nodeId!: string;
  /** Version label (e.g. `1.0`) of the node version requested via the route, if any. */
  public versionLabel!: string;
  /** Version history entries loaded for the current node. */
  public versions!: Version[];
  /** Keyword/tag definitions associated with the current node. */
  public keywords!: KeywordDefinition[];
  /** Forum topics attached to the current node. */
  public topics: ModelNode[] = [];
  /** Set of language translations available for a multilingual node. */
  public translationSet!: Translations;
  /** Whether the "update content" wizard is currently shown. */
  public showUpdateWizard!: boolean;
  /** The interest group owning the node, or a lightweight object holding only its id. */
  public group!: InterestGroup | { id: string };
  /** Definitions of the group's dynamic (custom) property fields to display. */
  public dynamicPropertiesModel: DynamicPropertyDefinition[] = [];
  /** Paged list of comments/replies for the currently selected topic. */
  public comments!: PagedNodes;
  /** The forum topic whose comments are currently displayed. */
  public currentTopic!: ModelNode;
  /** Post selected as the source of a quote for a new reply. */
  public futureQuote!: Quote;
  /** Post currently being edited. */
  public editPost!: ModelNode;
  /** Whether the "create topic" panel is shown. */
  public showCreateTopic = false;
  /** Topic pending deletion, if any. */
  public currentDeletedTopic: ModelNode | undefined;
  /** Topic currently being edited, if any. */
  public currentEditedTopic: ModelNode | undefined;
  /** Whether the "delete topic" confirmation is shown. */
  public showDeleteTopic = false;
  /** Whether the "edit topic" panel is shown. */
  public showEditTopic = false;
  /** Whether the "enable multilingual" modal is shown. */
  public showEnableMultilingualModal = false;
  /**
   * Whether the current user has restricted access to the node's parent, which
   * affects back-navigation behaviour. Defaults to `true` (restricted).
   */
  public restrictedMode = true;
  /** Resolved destination node id for filelink nodes. */
  public destinationId = '';
  /** Active tab/step of the details view (e.g. `metadata`). */
  public step = 'metadata';
  /** Computed size, in bytes, of the current folder. */
  public folderSize = 0;
  /** Whether an ARES bridge log entry exists for the selected version. */
  public hasAresLogVersion = false;
  /** ARES bridge detail for the currently selected version. */
  public nodeLogVersionDetail!: ExternalRepositoryData;
  /** Reactive form holding ARES bridge registration info (save/registration/document numbers). */
  public formAresInfo!: FormGroup;

  /** Paging options (page, limit, sort) used when loading topic comments. */
  public listingCommentOptions: ListingOptions = {
    page: 1,
    limit: 10,
    sort: '',
  };

  /** Page numbers available in the comment pager. */
  public commentPages: number[] = [];
  /** Total number of comment items for the current topic. */
  public totalCommentItems = 10;

  // for document preview
  /** Whether the inline document preview is shown. */
  public showPreview = false;
  /** URL used to fetch the previewable content or rendition. */
  public contentURL!: string;
  /** Id of the document currently rendered in the preview. */
  public previewDocumentId!: string;
  /** Toggled flag appended to the preview URL to force a reload/refresh. */
  private dummyPreviewUrlChange = false;

  /** The currently authenticated user. */
  public user!: User;

  // history modal
  /** Whether the version history modal is shown. */
  public historyShowModal = false;
  /** Whether the ARES bridge log modal is shown. */
  public areslogShowModal = false;
  /** Whether a long-running action (checkout, send to ARES, …) is in progress. */
  public processing = false;
  /** Whether the checkin modal is shown. */
  public checkinShowModal = false;
  /** Whether the cancel-checkout confirmation modal is shown. */
  public cancelCheckoutShowModal = false;
  /** Whether node/version data is currently loading. */
  public loading = signal(false);
  /** Whether more than the initially loaded versions exist. */
  public hasMoreVersions = false;
  /** Name of the item most recently added to the clipboard, if any. */
  public itemToClipboard: string | undefined;
  /** Results of Office online-edit operations. */
  public officeEditResult: OfficeEditResult[] = [];
  /** Whether the ARES bridge integration is enabled for the current group. */
  private isAresBridgeEnabled = false;

  /** Angular lifecycle hook; delegates initialization to {@link ngInit}. */
  public ngOnInit() {
    this.ngInit();
  }

  /**
   * Type guard narrowing the {@link group} union to a full {@link InterestGroup}.
   *
   * @param group The group value to test.
   * @returns `true` if the value is a fully-loaded {@link InterestGroup}.
   */
  private isInterestGroup(
    group: InterestGroup | { id: string }
  ): group is InterestGroup {
    return (group as InterestGroup).name !== undefined;
  }
  /**
   * Core initialization routine wired from {@link ngOnInit}. Subscribes to route
   * data and params to resolve the interest group, load the requested node, and
   * trigger an automatic download when the `download=true` query parameter is set.
   * Also resolves the current user.
   */
  private ngInit() {
    this.route.data.subscribe(async (value: Data) => {
      this.group = value.group;
      if (this.group.id && !this.isGuest()) {
        this.isAresBridgeEnabled =
          await this.aresBridgeHelperService.isAresBridgeEnabled(this.group.id);
      }
      this.changeDetectorRef.markForCheck();
    });
    this.route.params.subscribe(async (params) => {
      await this.loadNode(params);
      if (!this.isInterestGroup(this.group)) {
        const interestGroup = await this.nodesService.getGroupAsync({
          id: this.nodeId,
        });
        this.group = interestGroup;
        this.libraryIdService.updateLibraryId(this.group.libraryId as string);
      }

      this.route.queryParams.subscribe((queryParams) => {
        if (
          queryParams.download &&
          queryParams.download === 'true' &&
          this.node.name
        ) {
          this.saveAsService.saveAs(this.nodeId, this.node.name);
        }
      });
      this.changeDetectorRef.markForCheck();
    });

    this.user ??= this.loginService.getUser();
  }

  /**
   * Loads the node identified by the given route parameters together with its
   * versions, rendition, parent-access status, translations and metadata, then
   * resolves the destination id and (for authenticated users) the ARES bridge log.
   *
   * @param params Route parameters containing `nodeId` and optional `versionLabel`.
   */
  public async loadNode(params: { [key: string]: string }) {
    this.nodeId = params.nodeId;
    this.versionLabel = params.versionLabel;

    const loaded = await this.loadingService.run(this.loading, async () => {
      await this.loadVersionsAndNode();
      await this.handleRendition();
      await this.verifyParentAccess();
      await this.loadTranslations();
      await this.loadNodeMetadata();

      this.extractDestinationId();

      if (!this.isGuest()) {
        this.nodeLog = await this.aresBridgeHelperService.nodeLog(this.nodeId);
      }
      return true;
    });

    if (loaded && this.node.properties) {
      this.aresBridgeVersionSelected(this.node.properties.versionLabel);
    }
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Loads the first page of version history and resolves {@link node} to either
   * the requested historical version or the latest node. Falls back to loading all
   * versions when the node cannot be found among the first page.
   */
  private async loadVersionsAndNode() {
    this.versions = await this.contentService.getFirstVersionsAsync({
      id: this.nodeId,
    });
    this.verifyMoreVersions();

    if (this.versionLabel && !this.isLastVersionOfContent()) {
      const foundNode = this.findNodeInVersions(this.versions);
      if (foundNode) {
        this.node = foundNode;
      }
    } else {
      this.node = await this.nodesService.getNodeAsync({ id: this.nodeId });
    }

    if (!this.node && this.hasMoreVersions) {
      await this.loadAllVersionsAndFindNode();
    }
  }

  /**
   * Loads the complete version history and sets {@link node} to the version
   * matching {@link versionLabel}, if found.
   */
  private async loadAllVersionsAndFindNode() {
    this.versions = await this.contentService.getVersionsAsync({
      id: this.nodeId,
    });
    const foundNode = this.findNodeInVersions(this.versions);
    if (foundNode) {
      this.node = foundNode;
    }
  }

  /**
   * Finds the node belonging to the version matching {@link versionLabel}.
   *
   * @param versions Version history entries to search.
   * @returns The matching version's node, or `undefined` if not found.
   */
  private findNodeInVersions(versions: Version[]): ModelNode | undefined {
    const version = versions.find((v) => v.versionLabel === this.versionLabel);
    return version?.node;
  }

  /**
   * Ensures a `doclib` rendition exists for the current file when the Alfresco API
   * is in use, creating it if necessary. Rendition errors are logged and swallowed.
   */
  private async handleRendition() {
    if (!(environment.useAlfrescoAPI && this.isFile())) return;

    try {
      const rendition = await this.alfrescoService.getRendition(
        this.nodeId,
        'doclib'
      );
      if (rendition.entry.status !== 'CREATED') {
        await this.alfrescoService.createRendition(this.nodeId, 'doclib');
      }
    } catch (error) {
      console.error(error);
    }
  }

  /** Loads the translation set for the node when it is flagged as multilingual. */
  private async loadTranslations() {
    if (this.node.properties?.multilingual === 'true' && this.node.id) {
      this.translationSet = await this.contentService.getTranslationsAsync({
        id: this.node.id,
      });
    }
  }

  /**
   * Loads the node's keywords, the group's dynamic property definitions and the
   * node's forum topics, provided the node type, id and group id are all available.
   */
  private async loadNodeMetadata() {
    if (this.node.type && this.node.id && this.group.id) {
      await this.loadKeywords(this.node.id);
      await this.loadDynamicPropertiesModel(this.group.id);
      await this.loadTopics(this.node.id);
    }
  }

  /**
   * Determines whether the current user has restricted access to the node's parent
   * and updates {@link restrictedMode} accordingly. A `403` response is treated as
   * restricted access.
   */
  private async verifyParentAccess() {
    if (this.node.parentId) {
      try {
        const authorization = await this.guardsService.getGuardAccessAsync({
          id: this.node.parentId,
        });
        if (authorization === null || authorization === undefined) {
          this.restrictedMode = true;
        } else if (authorization?.granted !== undefined) {
          this.restrictedMode = !authorization.granted;
        }
      } catch (error) {
        if (error.status === 403) {
          this.restrictedMode = true;
        }
      }
    }
  }

  /**
   * Sets {@link hasMoreVersions} when the first page is full (10 entries) and does
   * not yet reach the initial `1.0` version, indicating additional versions exist.
   */
  private verifyMoreVersions() {
    if (
      this.versions.length === 10 &&
      this.versions[9].versionLabel !== '1.0'
    ) {
      this.hasMoreVersions = true;
    }
  }

  /** Loads the complete version history for the current node. */
  public async loadAllVersions() {
    await this.loadingService.run(this.loading, async () => {
      this.versions = await this.contentService.getVersionsAsync({
        id: this.nodeId,
      });
      this.verifyMoreVersions();
    });
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Resolves {@link destinationId} from a filelink node's `destination` property,
   * or clears it for other node types.
   */
  public extractDestinationId() {
    this.destinationId = '';

    if (this.node.type?.includes('filelink') && this.node.properties) {
      this.destinationId = this.getDestinationId(
        this.node.properties.destination
      );
    }
  }

  /**
   * Loads the keyword definitions for the given node id into {@link keywords}.
   *
   * @param id Node identifier whose keywords should be loaded.
   */
  public async loadKeywords(id: string) {
    this.keywords = await this.keywordService.getKeywordsAsync({ id });
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Loads the dynamic property definitions for the given group id into
   * {@link dynamicPropertiesModel}.
   *
   * @param id Group identifier whose dynamic property definitions to load.
   */
  public async loadDynamicPropertiesModel(id: string) {
    this.dynamicPropertiesModel =
      await this.dynamicPropertiesService.getDynamicPropertyDefinitionsAsync({
        id,
      });
  }

  /**
   * Loads the forum topics for the given node id and, if any exist, loads the
   * comments of the first topic. Resets {@link comments} to empty beforehand.
   *
   * @param id Node identifier whose topics should be loaded.
   */
  public async loadTopics(id: string) {
    const emptyNodesArray: ModelNode[] = [];
    this.comments = { data: emptyNodesArray, total: 0 };
    this.topics = await this.contentService.getTopicsAsync({ id });
    if (this.topics.length > 0) {
      await this.loadTopic(this.topics[0].id as string);
    }
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Selects the topic with the given id as {@link currentTopic} and loads its
   * replies into {@link comments} using the current paging options.
   *
   * @param id Topic identifier to load; a no-op when `undefined`.
   */
  public async loadTopic(id: string | undefined) {
    if (id === undefined) {
      return;
    }
    for (const topic of this.topics) {
      if (topic.id === id) {
        this.currentTopic = topic;
      }
    }

    this.comments = await this.topicService.getRepliesAsync({
      id,
      limit: this.listingCommentOptions.limit,
      page: this.listingCommentOptions.page,
      order: this.listingCommentOptions.sort,
    });
    this.totalCommentItems = this.comments.total;
    this.changeDetectorRef.markForCheck();
  }

  /** Reloads the node's keywords after one has been removed. */
  public async onKeywordRemoved() {
    await this.loadKeywords(this.node.id as string);
  }

  /**
   * Determines whether the displayed node represents the latest version of the
   * content. A missing {@link versionLabel} is treated as the latest version.
   *
   * @returns `true` when showing the most recent version.
   */
  public isLastVersionOfContent(): boolean {
    let result = false;

    if (this.versionLabel === undefined) {
      result = true;
    } else if (this.versionLabel !== undefined && this.versions !== undefined) {
      if (this.versionLabel === this.versions[0].versionLabel) {
        result = true;
      }
    }

    return result;
  }

  /**
   * Handles the outcome of a delete-space or delete-content action: shows a success
   * or error message, removes the node from the clipboard and navigates back to the
   * parent folder on success.
   *
   * @param result The action result emitted by the delete component.
   */
  public async onDeletedElement(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      (result.type === ActionType.DELETE_SPACE ||
        result.type === ActionType.DELETE_CONTENT)
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(result.type)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
      if (result.node) {
        this.clipboardService.removeItem(result.node);
      }
      if (this.versionLabel === undefined) {
        this.router.navigate(['../../', this.node.parentId], {
          relativeTo: this.route,
        });
      } else {
        this.router.navigate(['../../../', this.node.parentId], {
          relativeTo: this.route,
        });
      }
    } else if (
      result.result === ActionResult.FAILED &&
      (result.type === ActionType.DELETE_SPACE ||
        result.type === ActionType.DELETE_CONTENT)
    ) {
      const text = this.translateService.translate(
        getErrorTranslation(result.type)
      );
      if (text) {
        this.uiMessageService.addErrorMessage(text, false);
      }
    }
  }

  /**
   * @returns `true` when the node is a file (i.e. not a folder).
   */
  public isFile(): boolean {
    let result = false;
    if (this.node?.type !== undefined) {
      result = !this.node.type.includes('folder');
    }
    return result;
  }

  /**
   * Closes the update-content wizard, reinitializes the view and shows a success
   * message when the content update succeeded.
   *
   * @param result The action result emitted by the update wizard.
   */
  public async onCloseUpdateWizard(result: ActionEmitterResult) {
    this.showUpdateWizard = false;
    this.ngInit();
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.UPDATE_FILE_CONTENT
    ) {
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.UPDATE_FILE_CONTENT)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }
  }

  /**
   * Reloads keywords and shows a success message once a keyword has been added.
   *
   * @param res The action result emitted by the add-keyword component.
   */
  public async onKeywordAdded(res: ActionEmitterResult) {
    if (res.result === ActionResult.SUCCEED) {
      await this.loadKeywords(this.node.id as string);
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.ADD_KEYWORD)
      );
      if (text) {
        this.uiMessageService.addSuccessMessage(text, true);
      }
    }
  }

  /** The currently active UI language code. */
  public get currentLang(): string {
    return this.translateService.getActiveLang();
  }
  /**
   * @returns `true` when the user has "manage own" library rights or higher on the node.
   */
  public isLibManageOwnOrHigher(): boolean {
    return this.permEvalService.isLibManageOwnOrHigher(this.node);
  }

  /**
   * @returns The i18n key for the page title, depending on whether the node is a
   * link, file or folder.
   */
  public getPageTitle() {
    if (this.isLink()) {
      return 'label.library.details.of.url';
    }
    if (this.isFile()) {
      return 'label.library.details.of.document';
    }
    return 'label.library.details.of.folder';
  }

  /**
   * @returns `true` when the node is regular content (i.e. not a filelink).
   */
  public isContent(): boolean {
    if (this.node?.type) {
      return !this.isLibraryLink(this.node);
    }
    return false;
  }

  /**
   * @param node The node to test.
   * @returns `true` when the node is a folder and not a shared-space link.
   */
  public isFolder(node: ModelNode): boolean {
    if (node?.type) {
      return node.type.includes('folder') && !this.isLibraryLink(node);
    }
    return false;
  }

  /**
   * @returns `true` when the node represents a URL link.
   */
  public isLink(): boolean {
    if (this.node?.type && this.node.properties) {
      if (this.node.name?.includes('.url')) {
        return true;
      }
      return this.node.properties.isUrl === 'true';
    }
    return false;
  }

  /**
   * Extracts the destination node id from a filelink `destination` path of the form
   * `.../.../.../{id}`.
   *
   * @param destination The destination path string.
   * @returns The destination id, or an empty string if it cannot be parsed.
   */
  getDestinationId(destination: string): string {
    const destinationPart = destination.split('/');
    if (destinationPart.length === 4) {
      return destinationPart[3];
    }
    return '';
  }

  /**
   * @param node The node to test.
   * @param item The link kind to check for.
   * @returns `true` when the node's type includes the given link kind.
   */
  private isNodeTypeOf(
    node: ModelNode,
    item: 'filelink' | 'folderlink'
  ): boolean {
    if (node?.type) {
      return node.type.includes(item);
    }
    return false;
  }

  /**
   * @param node The node to test.
   * @returns `true` when the node is a library (file) link.
   */
  isLibraryLink(node: ModelNode): boolean {
    return this.isNodeTypeOf(node, 'filelink');
  }

  /**
   * @param node The node to test.
   * @returns `true` when the node is a shared-space (folder) link.
   */
  isSharedSpaceLink(node: ModelNode): boolean {
    return this.isNodeTypeOf(node, 'folderlink');
  }

  /**
   * Reloads the current topic's comments after a comment action.
   *
   * @param _result The action result (unused).
   */
  public async refreshComments(_result: ActionEmitterResult) {
    await this.loadTopic(this.currentTopic.id as string);
  }

  /**
   * Reloads topics after a successful topic creation and closes the create panel.
   *
   * @param res The action result emitted by the create-topic component.
   */
  public async refreshCreateTopic(res: ActionEmitterResult) {
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.CREATE_TOPIC
    ) {
      await this.loadTopics(this.node.id as string);
    }
    this.showCreateTopic = false;
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Handles the outcome of a topic deletion: reloads topics on success, reloads the
   * current topic on failure, or simply closes the confirmation on cancellation.
   *
   * @param res The action result emitted by the delete-topic component.
   */
  public async refreshDeleteTopic(res: ActionEmitterResult) {
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      await this.loadTopics(this.node.id as string);
      this.showDeleteTopic = false;
    } else if (
      res.result === ActionResult.FAILED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      await this.loadTopic(this.currentTopic.id as string);
    } else if (
      res.result === ActionResult.CANCELED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      this.showDeleteTopic = false;
    }
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Stores a post to be quoted in a subsequent reply.
   *
   * @param post The post to quote.
   */
  public prepareQuote(post: Quote) {
    this.futureQuote = post;
  }

  /**
   * Stores a post to be edited.
   *
   * @param post The post to edit.
   */
  public prepareEdit(post: ModelNode) {
    this.editPost = post;
  }

  /**
   * Marks a topic for deletion and opens the delete confirmation.
   *
   * @param topic The topic to delete.
   */
  public prepareDeletion(topic: ModelNode) {
    this.currentDeletedTopic = topic;
    this.showDeleteTopic = true;
  }

  /** Navigates to and loads the previous page of comments, when possible. */
  public async previousCommentPage() {
    if (this.canPreviousCommentPage()) {
      if (this.listingCommentOptions.page > 1) {
        this.listingCommentOptions.page = this.listingCommentOptions.page - 1;
        await this.loadTopic(this.currentTopic.id as string);
      }
    }
  }

  /** Navigates to and loads the next page of comments, when possible. */
  public async nextCommentPage() {
    if (this.canNextCommentPage()) {
      if (
        this.listingCommentOptions.page <
        Math.floor(
          this.totalCommentItems / this.listingCommentOptions.limit + 1
        )
      ) {
        this.listingCommentOptions.page = this.listingCommentOptions.page + 1;
        await this.loadTopic(this.currentTopic.id as string);
      }
    }
  }

  /**
   * Navigates to a specific comment page and reloads its comments.
   *
   * @param p The target page number.
   */
  public async changeCommentPage(p: number) {
    this.listingCommentOptions.page = p;
    await this.loadTopic(this.currentTopic.id as string);
  }

  /**
   * Computes the list of available comment page numbers based on the total items
   * and page limit.
   *
   * @returns An array of page numbers.
   */
  public getCommentPages(): number[] {
    const result: number[] = [];

    for (
      let i = 1;
      i < this.totalCommentItems / this.listingCommentOptions.limit + 1;
      i += 1
    ) {
      result.push(i);
    }

    return result;
  }

  /**
   * @returns `true` when the current topic has at least one comment.
   */
  public hasComments(): boolean {
    let result = false;

    if (this.comments?.total) {
      if (this.comments.total > 0) {
        result = true;
      }
    }
    return result;
  }

  /**
   * @returns `true` when the node has at least one forum topic.
   */
  public hasTopics(): boolean {
    if (this.topics) {
      return this.topics.length > 0;
    }
    return false;
  }

  /**
   * @returns `true` when the user has library administrator rights on the node.
   */
  public isLibAdmin(): boolean {
    return this.permEvalService.isLibAdmin(this.node);
  }

  /**
   * @returns `true` when the user has library access rights on the node.
   */
  public isAccess(): boolean {
    return this.permEvalService.isLibAccess(this.node);
  }

  /**
   * @returns `true` when the user has full-edit library rights on the node.
   */
  public isLibFullEdit(): boolean {
    return this.permEvalService.isLibFullEdit(this.node);
  }

  /**
   * Takes ownership of the current node, showing a success or error message
   * depending on the outcome.
   */
  public async takeOwnership() {
    try {
      await this.nodesService.putOwnershipAsync({ id: this.nodeId });
      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.TAKE_OWNERSHIP)
      );
      this.uiMessageService.addSuccessMessage(text, true);
    } catch (error) {
      console.error(error);
      const text = this.translateService.translate(
        getErrorTranslation(ActionType.TAKE_OWNERSHIP)
      );
      this.uiMessageService.addErrorMessage(text, true);
    }
  }

  /**
   * @returns `true` when the node is locked.
   */
  public isLocked(): boolean {
    return this.node?.properties?.locked === 'true';
  }

  /**
   * @returns `true` when a working copy exists and is owned by the current user.
   */
  public showWorkingCopy(): boolean {
    return (
      this.workingCopyId !== '' &&
      this.node.properties?.lockOwner === this.user.userId
    );
  }

  /**
   * @returns `true` when the node can be edited online by the current user
   * (inline editing enabled and current user owns the working copy).
   */
  public isEditOnline(): boolean {
    if (this.loginService.isGuest()) {
      return false;
    }
    return (
      this.node?.properties?.editInline === 'true' &&
      this.loginService.getUser().userId ===
        this.node?.properties?.workingCopyOwner
    );
  }

  /**
   * @returns `true` when the node itself is a working copy.
   */
  public isWorkingCopy(): boolean {
    return this.node?.properties?.workingCopy === 'true';
  }

  /**
   * @returns `true` when the current user has access to the node.
   */
  public currentUserHasAccess(): boolean {
    return this.node?.properties?.currentUserHasAccess === 'true';
  }

  /** Checks out the current node (creating a working copy) and reloads it. */
  public async checkout() {
    this.processing = true;

    if (this.node.id !== undefined) {
      await this.contentService.postCheckoutAsync({ id: this.node.id });
      this.route.params.subscribe(
        async (params) => await this.loadNode(params)
      );
    }

    this.processing = false;
    this.changeDetectorRef.markForCheck();
  }

  /**
   * Cancels the checkout of the working copy, discarding it, and navigates back to
   * the original node's details view.
   */
  public async cancelCheckout() {
    this.cancelCheckoutShowModal = false;
    this.processing = true;

    if (this.node.id !== undefined && this.node.properties !== undefined) {
      await this.contentService.deleteCheckoutAsync({
        id: this.node.properties.originalNodeId,
      });

      this.router.navigate(
        [`../../${this.node.properties.originalNodeId}/details`],
        { relativeTo: this.route }
      );
    }

    this.processing = false;
    this.changeDetectorRef.markForCheck();
  }

  /**
   * @returns `true` when the AI summarization feature is enabled.
   */
  public isAiEnabled(): boolean {
    return this.summarizeService.isEnabled;
  }

  /** Marker delimiting a cached AI summary appended to a node's description. */
  private static readonly SUMMARY_MARKER = '\n\n--- AI Summary ---\n';

  /**
   * Extracts a previously cached AI summary from the node description, if present.
   *
   * @returns The cached summary text, or `null` when none is stored.
   */
  private getCachedSummary(): string | null {
    const desc = this.description;
    const idx = desc.indexOf(DetailsComponent.SUMMARY_MARKER);
    return idx >= 0
      ? desc.substring(idx + DetailsComponent.SUMMARY_MARKER.length).trim()
      : null;
  }

  /**
   * Opens the AI summarization dialog. Uses a cached summary when available;
   * otherwise requests a fresh summary from the {@link SummarizeService} and, on
   * success, refreshes the node once the dialog closes. Errors are surfaced in the
   * dialog via a translated message.
   *
   * @returns A promise that resolves once the summarization workflow completes.
   */
  public async summarizeDocument(): Promise<void> {
    const nodeId = this.node.id;
    if (!nodeId) {
      return;
    }
    const cached = this.getCachedSummary();
    const data: SummarizeDialogData = {
      nodeName: this.node.name ?? '',
      loading: signal(!cached),
      summary: signal(cached ?? ''),
      error: signal(''),
    };
    let freshSummary = false;
    const dialogRef = this.dialog.open(SummarizeDialogComponent, {
      width: '600px',
      data,
    });
    dialogRef.afterClosed().subscribe(() => {
      if (freshSummary) {
        this.nodesService.getNode({ id: nodeId }).subscribe((n) => {
          this.node = n;
          this.changeDetectorRef.markForCheck();
        });
      }
    });

    if (cached) {
      return;
    }

    try {
      const result = await this.summarizeService.summarize(nodeId);
      data.loading.set(false);
      data.summary.set(result.summary);
      freshSummary = true;
    } catch {
      data.loading.set(false);
      data.error.set(this.translateService.translate('summarize.error'));
    }
  }

  /**
   * Closes the checkin modal and navigates back to the original node's details view.
   */
  public async checkinDone() {
    this.checkinShowModal = false;
    if (this.node.id !== undefined && this.node.properties !== undefined) {
      this.router.navigate(
        [`../../${this.node.properties.originalNodeId}/details`],
        { relativeTo: this.route }
      );
    }
  }

  /** Adds the current node to the clipboard for a copy/move operation. */
  public addToClipboard() {
    this.clipboardService.addItem(this.node, true, this.group.id);
    this.itemToClipboard = this.node.name;
  }

  /**
   * @returns `true` when the node has no security ranking or is ranked `PUBLIC`.
   */
  public isPublic() {
    return (
      this.node.properties !== undefined &&
      (this.node.properties.security_ranking === undefined ||
        this.node.properties.security_ranking === 'PUBLIC')
    );
  }

  /**
   * @returns `true` when the node is multilingual and its translation set is loaded.
   */
  public isMultiLingual(): boolean {
    if (this.node?.properties) {
      return (
        this.node.properties.multilingual === 'true' &&
        this.translationSet !== undefined
      );
    }

    return false;
  }

  /**
   * Closes the enable-multilingual modal and reinitializes the view when the node
   * was successfully made multilingual.
   *
   * @param result The action result emitted by the enable-multilingual component.
   */
  public async onCloseMakeMultiModal(result: ActionEmitterResult) {
    this.showEnableMultilingualModal = false;
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.ENABLE_MULTILINGUAL
    ) {
      this.ngInit();
    }
  }

  /**
   * @returns `true` when the node is an unlocked, non-working-copy Office document
   * (`.docx`, `.xlsx` or `.pptx`) and Office integration is enabled.
   */
  public canEditInOffice() {
    if (
      this.isOfficeIntegrationEnabled() &&
      !this.isWorkingCopy() &&
      !this.isLocked()
    ) {
      return (
        this.isFile() &&
        this.node.name !== undefined &&
        (this.node.name.endsWith('.docx') ||
          this.node.name.endsWith('.xlsx') ||
          this.node.name.endsWith('.pptx'))
      );
    }
    return false;
  }

  /**
   * @returns `true` when Office integration is configured (a client id is set).
   */
  private isOfficeIntegrationEnabled(): boolean {
    return environment.officeClientId !== '';
  }
  /**
   * Opens the node in Office online for editing or updating in a new browser tab,
   * then schedules a navigation/reload so the view reflects the resulting changes.
   */
  public openDocInOffice() {
    const id = this.node?.properties?.workingCopyId ?? this.node.id;
    if (this.isWorkingCopy()) {
      setTimeout(
        () =>
          this.router.navigate([
            '/group',
            this.group.id,
            'library',
            this.node.properties?.originalNodeId ?? this.node.id,
            'details',
          ]),
        2000
      );
      const url = `${environment.serverURL.substring(
        0,
        environment.serverURL.length - 1
      )}${environment.baseHref}office?id=${id}&mode=update`;
      globalThis.open(url, '_blank');
    } else {
      setTimeout(() => location.reload(), 10000);
      const url = `${environment.serverURL.substring(
        0,
        environment.serverURL.length - 1
      )}${environment.baseHref}office?id=${id}&mode=edit`;
      globalThis.open(url, '_blank');
    }
  }

  /** The node's security ranking, or an empty string when unavailable. */
  get securityRanking(): string {
    if (this.node.properties) {
      return this.node.properties.security_ranking;
    }
    return '';
  }

  /** The node's size property, or `null` when unavailable. */
  get size(): string | null {
    if (this.node.properties) {
      return this.node.properties.size;
    }
    return null;
  }

  /** The node's locale, or an empty string when unavailable. */
  get locale(): string {
    if (this.node.properties) {
      return this.node.properties.locale;
    }
    return '';
  }

  /** The language portion of the node's locale (locale without the country suffix). */
  get language(): string {
    if (this.node.properties) {
      const index = this.node.properties.locale.lastIndexOf('_');
      if (index > 0) {
        return this.node.properties.locale.substring(0, index);
      }
      return this.node.properties.locale;
    }
    return '';
  }
  /** The human-readable MIME type name, or an empty string when unavailable. */
  get mimetypeName(): string {
    if (this.node.properties) {
      return this.node.properties.mimetypeName;
    }
    return '';
  }
  /** The node's creation date, or `null` when unavailable. */
  get created(): string | null {
    if (this.node.properties) {
      return this.node.properties.created;
    }
    return null;
  }

  /** The node's last-modified date, or `null` when unavailable. */
  get modified(): string | null {
    if (this.node.properties) {
      return this.node.properties.modified;
    }
    return null;
  }
  /** The username of the last modifier, or an empty string when unavailable. */
  get modifier(): string {
    if (this.node.properties) {
      return this.node.properties.modifier;
    }
    return '';
  }
  /** The username of the creator, or an empty string when unavailable. */
  get creator(): string {
    if (this.node.properties) {
      return this.node.properties.creator;
    }
    return '';
  }
  /** The declared document author, or an empty string when unavailable. */
  get author(): string {
    if (this.node.properties) {
      return this.node.properties.author;
    }
    return '';
  }

  /** The link node's target URL, or an empty string when unavailable. */
  get url(): string {
    if (this.node.properties) {
      return this.node.properties.url;
    }
    return '';
  }

  /** The content encoding, or an empty string when unavailable. */
  get encoding(): string {
    if (this.node.properties) {
      return this.node.properties.encoding;
    }
    return '';
  }

  /** The node's status, or an empty string when unavailable. */
  get status(): string {
    if (this.node.properties) {
      return this.node.properties.status;
    }
    return '';
  }

  /** The node's reference, or an empty string when unavailable. */
  get reference(): string {
    if (this.node.properties) {
      return this.node.properties.reference;
    }
    return '';
  }
  /** The node's version label from its properties, or an empty string when unavailable. */
  get nodeVersionLabel(): string {
    if (this.node.properties) {
      return this.node.properties.versionLabel;
    }
    return '';
  }
  /** The document's issue date, or `null` when unavailable. */
  get issueDate(): string | null {
    if (this.node.properties) {
      return this.node.properties.issue_date;
    }
    return null;
  }
  /** The document's expiration date, or `null` when unavailable. */
  get expirationDate(): string | null {
    if (this.node.properties) {
      return this.node.properties.expiration_date;
    }
    return null;
  }

  /**
   * Rebuilds {@link formAresInfo} and populates it with the ARES bridge registration
   * data (save number, registration number, document id) matching the given version,
   * setting {@link hasAresLogVersion} when a valid entry is found. Iterates over a
   * reversed copy of {@link nodeLog} so the original log is not mutated.
   *
   * @param version The version label to look up in the ARES bridge log.
   */
  aresBridgeVersionSelected(version: string) {
    this.formAresInfo = new FormGroup(
      {
        saveNumber: new FormControl(),
        registrationNumber: new FormControl(),
        documentId: new FormControl(),
      },
      {
        updateOn: 'change',
      }
    );

    // do not mutate this.nodeLog
    const nodeLogReversed = this.nodeLog.slice().reverse();
    nodeLogReversed.forEach((response) => {
      if (
        response.versionLabel === version &&
        response.documentId !== 'null' &&
        response.documentId !== ''
      ) {
        this.hasAresLogVersion = true;
        this.formAresInfo.controls.saveNumber.setValue(response.saveNumber);
        this.formAresInfo.controls.registrationNumber.setValue(
          response.registrationNumber
        );
        this.formAresInfo.controls.documentId.setValue(response.documentId);
      }
    });
  }

  /** URL to the ARES document detail page for the currently selected document id. */
  get aresDocumentLink(): string | null {
    const documentId = this.formAresInfo.value.documentId;
    return `${environment.aresBridgeServer}/Ares/document/show.do?documentId=${documentId}`;
  }

  /**
   * @param dpd A dynamic property definition.
   * @returns `true` when the property is a date field.
   */
  public isDateField(dpd: DynamicPropertyDefinition): boolean {
    return dpd.propertyType === 'DATE_FIELD';
  }

  /**
   * @param dpd A dynamic property definition.
   * @returns `true` when the property is a single- or multi-selection field.
   */
  public isSelectionField(dpd: DynamicPropertyDefinition): boolean {
    return (
      dpd.propertyType === 'SELECTION' || dpd.propertyType === 'MULTI_SELECTION'
    );
  }

  /**
   * @param dpd A dynamic property definition.
   * @returns The property's ordering index.
   */
  public getIndex(dpd: DynamicPropertyDefinition): number {
    return dpd.index as number;
  }

  /**
   * @returns `true` when the current browser URL ends with `details`.
   */
  public endsWithDetails(): boolean {
    return globalThis.location.href.endsWith('details');
  }
  /** The node's working-copy id, or an empty string when unavailable. */
  get workingCopyId(): string {
    if (this.node.properties) {
      return this.node.properties.workingCopyId;
    }
    return '';
  }
  /** Whether an item has been placed on the clipboard. */
  get isNotItemToClipboardUndefined(): boolean {
    return this.itemToClipboard !== undefined;
  }

  /**
   * @returns `true` when the node is a specific (non-latest) version.
   */
  public isVersion() {
    return this.node.properties?.isVersion === 'true';
  }

  /**
   * @param version A version history entry.
   * @returns The modifier username for that version, or an empty string.
   */
  public getVersionModifier(version: Version): string {
    if (version.node?.properties) {
      return version.node.properties.modifier;
    }
    return '';
  }

  /**
   * @param version A version history entry.
   * @returns The modification date for that version, or `null`.
   */
  public getVersionModifed(version: Version): string | null {
    if (version.node?.properties) {
      return version.node.properties.modified;
    }
    return null;
  }

  /** Whether the node is not a folder (defaults to `true` when the type is unknown). */
  get isNotFolder(): boolean {
    if (this.node.type) {
      return !this.node.type.includes('folder');
    }
    return true;
  }
  /** The id of the pivot (reference) translation, or `null` when unavailable. */
  get pivotId(): string | null {
    if (this.translationSet.pivot?.id) {
      return this.translationSet.pivot.id;
    }
    return null;
  }

  /**
   * The node's localized title, falling back to the English value when no
   * translation exists for the active language.
   */
  get title(): string {
    if (this.node?.title) {
      const title = this.i18nPipe.transform(this.node.title);
      if (title !== '') {
        return title;
      }
      return this.i18nSelectPipe.transform('en', this.node.title);
    }
    return '';
  }

  /**
   * The node's localized description, falling back to the English value when no
   * translation exists for the active language.
   */
  get description(): string {
    if (this.node?.description) {
      const description = this.i18nPipe.transform(this.node.description);
      if (description !== '') {
        return description;
      }
      return this.i18nSelectPipe.transform('en', this.node.description);
    }

    return '';
  }

  /**
   * Marks a topic for editing and opens the edit panel.
   *
   * @param topic The topic to edit.
   */
  public prepareEdition(topic: ModelNode) {
    this.currentEditedTopic = topic;
    this.showEditTopic = true;
  }

  /**
   * Handles the outcome of a topic edit: reloads topics on success, reloads the
   * current topic on failure, or closes the edit panel on cancellation.
   *
   * @param res The action result emitted by the edit-topic component.
   */
  public async refreshEditTopic(res: ActionEmitterResult) {
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.EDIT_TOPIC
    ) {
      await this.loadTopics(this.node.id as string);
      this.showEditTopic = false;
    } else if (
      res.result === ActionResult.FAILED &&
      res.type === ActionType.EDIT_TOPIC
    ) {
      await this.loadTopic(this.currentTopic.id as string);
    } else if (
      res.result === ActionResult.CANCELED &&
      res.type === ActionType.EDIT_TOPIC
    ) {
      this.showEditTopic = false;
    }
    this.changeDetectorRef.markForCheck();
  }

  /**
   * @returns `true` when a previous comment page is available.
   */
  public canPreviousCommentPage() {
    return this.listingCommentOptions.page > 1;
  }

  /**
   * @returns `true` when a next comment page is available.
   */
  public canNextCommentPage() {
    return (
      this.listingCommentOptions.page <
      Math.ceil(this.totalCommentItems / this.listingCommentOptions.limit)
    );
  }

  /**
   * Converts a stored selection dynamic-property value into a human-readable,
   * comma-separated string. Array-style values (`[a, b]`) are unwrapped; scalar
   * values are returned as-is.
   *
   * @param value The raw stored property value.
   * @returns The formatted, human-readable value.
   */
  public humanReadableSelectionDynProp(value: string): string {
    // only if [..., ...] - in case of an array
    if (value?.includes('[') && value.includes(']')) {
      let result: string = value.substring(1);
      result = result.substring(0, result.length - 1);
      const parts = result.split(',');
      result = '';
      let i = 0;
      for (const part of parts) {
        const itemString = part.substring(1, part.length - 1);
        result = `${itemString}${i === 0 ? '' : ', '}${result}`;
        i = 1;
      }
      return result;
    }
    if (value) {
      return value;
    }
    return '';
  }

  /**
   * @returns The number of path segments (`/`) in the current router URL.
   */
  public urlLength(): number {
    return (this.router.url.match(/\//g) || []).length;
  }

  /** Navigates back to the previous location in browser history. */
  public goBack() {
    this.location.back();
  }

  /**
   * Navigates back to the container holding the current node, choosing the
   * appropriate strategy based on the URL depth. No-op when the group is not fully
   * loaded or the node has no original container.
   */
  public async backToContainer() {
    if (!(
      this.isInterestGroup(this.group) &&
      this.node?.properties?.originalContainerId
    )) {
      return;
    }

    const urlLen = this.urlLength();

    if (urlLen === 5) {
      await this.navigateFromUrlLength5();
    } else if (urlLen === 6) {
      await this.navigateFromUrlLength6();
    }
  }

  /**
   * Back-navigation strategy for a URL of depth 5: routes to the library root when
   * in restricted mode, to the shared space for folder links, or to the original
   * container otherwise.
   */
  private async navigateFromUrlLength5() {
    if (this.restrictedMode) {
      await this.navigateToLibraryRoot('../../');
    } else if (this.isSharedSpaceLink(this.node)) {
      await this.navigateToSharedSpace();
    } else {
      await this.navigateToOriginalContainer('../../');
    }
  }

  /**
   * Back-navigation strategy for a URL of depth 6: routes to the library root in
   * restricted mode, or to the node's original container otherwise.
   */
  private async navigateFromUrlLength6() {
    const targetId = this.restrictedMode
      ? (this.group as InterestGroup).libraryId
      : this.node.properties?.originalContainerId;

    if (!targetId) return;

    await this.router.navigate(['../../../', targetId], {
      relativeTo: this.route,
      queryParams: this.getLibraryQueryParams(),
    });
  }

  /**
   * Navigates to the destination interest group and folder that a shared-space link
   * points to, tagging the route with `fromLink=true`.
   */
  private async navigateToSharedSpace() {
    const destIgId = this.node.properties?.destinationIgId;
    const destId = this.node.properties?.destinationId;

    if (!(destIgId && destId)) return;

    await this.router.navigate(['../../../../', destIgId, 'library', destId], {
      relativeTo: this.route,
      queryParams: { fromLink: 'true' },
    });
  }

  /**
   * Navigates to the node's original container (or the folder itself when the node
   * is a folder) using the given relative path.
   *
   * @param path The relative router path prefix to navigate from.
   */
  private async navigateToOriginalContainer(path: string) {
    const targetId = this.isFolder(this.node)
      ? this.node.id
      : this.node.properties?.originalContainerId;

    if (!targetId) return;

    await this.router.navigate([path, targetId], {
      relativeTo: this.route,
      queryParams: this.getLibraryQueryParams(),
    });
  }

  /**
   * Navigates to the interest group's library root using the given relative path.
   *
   * @param path The relative router path prefix to navigate from.
   */
  private async navigateToLibraryRoot(path: string) {
    await this.router.navigate(
      [path, (this.group as InterestGroup).libraryId],
      {
        relativeTo: this.route,
        queryParams: this.getLibraryQueryParams(),
      }
    );
  }

  /**
   * Builds the library listing query parameters (page, limit, sort) from session
   * storage so that returning to the library preserves the previous listing state.
   *
   * @returns The query parameter object for library navigation.
   */
  private getLibraryQueryParams() {
    return {
      p: sessionStorage.getItem('libraryPage'),
      n: sessionStorage.getItem('libraryLimit'),
      s: sessionStorage.getItem('librarySort'),
    };
  }

  /**
   * @returns `true` when the node's content can be previewed, taking the active API
   * (Alfresco vs. legacy) into account.
   */
  public isPreviewable(): boolean {
    if (environment.useAlfrescoAPI) {
      return isContentPreviewableFull(this.node);
    }
    return isContentPreviewable(this.node);
  }

  /**
   * Prepares and shows the inline content preview. For the Alfresco API it ensures a
   * PDF rendition exists for non-natively-previewable files, then builds the
   * appropriate {@link contentURL} (with a cache-busting flag) before displaying the
   * preview.
   */
  public async previewContent() {
    this.dummyPreviewUrlChange = !this.dummyPreviewUrlChange;
    if (environment.useAlfrescoAPI && this.isFile()) {
      if (
        !isContentPreviewable(this.node) &&
        isContentPreviewableFull(this.node)
      ) {
        try {
          const rendition = await this.alfrescoService.getRendition(
            this.nodeId,
            'pdf'
          );
          if (rendition.entry.status !== 'CREATED') {
            await this.alfrescoService.createRendition(this.nodeId, 'pdf');
          }
        } catch (error) {
          // Do not show popup for preview not available
          console.error(error);
        }

        this.contentURL = `${environment.serverURL}api/-default-/public/alfresco/versions/1/nodes/${this.node.id}/renditions/pdf/content?attachment=true&alf_ticket=${this.loginService.getTicket()}&dummy=${
          this.dummyPreviewUrlChange
        }`;
      } else {
        this.contentURL = `${environment.serverURL}api/-default-/public/alfresco/versions/1/nodes/${this.node.id}/content?attachment=false&alf_ticket=${this.loginService.getTicket()}&dummy=${
          this.dummyPreviewUrlChange
        }`;
      }
    } else {
      this.contentURL = `${
        environment.serverURL
      }pdfRendition?documentId=workspace://SpacesStore/${
        this.node.id
      }&response=content&ticket=${this.loginService.getTicket()}&dummy=${
        this.dummyPreviewUrlChange
      }`;
    }
    this.previewDocumentId = this.node.id as string;
    this.showPreview = true;
    this.changeDetectorRef.markForCheck();
  }

  /** Hides the inline content preview. */
  public closePreview() {
    this.showPreview = false;
  }

  /**
   * Updates the current user's notification subscription level for the node and
   * reloads the node to reflect the change. Shows an error message on failure.
   *
   * @param value The new notification subscription value.
   */
  public async changeNotificationSubscription(value: string) {
    if (value && value !== '' && this.node.id) {
      try {
        await this.notificationService.putNotificationAuthorityAsync({
          id: this.node.id,
          authority: this.loginService.getCurrentUsername(),
          body: value,
        });
        this.node = await this.nodesService.getNodeAsync({ id: this.node.id });
        this.changeDetectorRef.markForCheck();
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
   * @returns `true` when the current user is subscribed to notifications on the node.
   */
  isSubscribedToNotifications(): boolean {
    return this.node.notifications === 'ALLOWED';
  }

  /**
   * @returns `true` when the current user is an unauthenticated guest.
   */
  public isGuest() {
    return this.loginService.isGuest();
  }

  /**
   * @returns `true` when the current user is the owner of the node.
   */
  public isCurrentOwner() {
    if (this.node?.properties) {
      return this.node.properties.owner === this.user.userId;
    }
    return false;
  }

  /**
   * @param node The node to test.
   * @returns `true` when the node is a folder link.
   */
  public isFolderLink(node: ModelNode) {
    if (node?.type) {
      return node.type.includes('folderlink');
    }
    return false;
  }

  /**
   * Determines whether the node may be sent to the ARES bridge, considering the
   * user's domain, node type, per-group and environment ARES settings, whether the
   * current version was already registered, the CIRCABC release, and the user's
   * library rights.
   *
   * @returns `true` when the node can be sent to the ARES bridge.
   */
  public canSendToAresBridge() {
    if (this.user?.properties?.domain === 'external') {
      return false;
    }
    if (!this.isFile()) {
      return false;
    }
    if (!this.isAresBridgeEnabled) {
      return false;
    }
    if (!environment.aresBridgeEnabled) {
      return false;
    }

    if (
      this.nodeLog.some(
        (log) =>
          log.versionLabel === this.node.properties?.versionLabel &&
          log.saveNumber
      )
    ) {
      return false;
    }

    if (environment.circabcRelease !== 'oss') {
      return this.isLibManageOwnOrHigher();
    }
    return false;
  }

  /**
   * Sends the current node to the ARES bridge. If the node was previously
   * registered, prompts for confirmation before resending. Guards against
   * concurrent invocations via {@link processing}.
   */
  public async sendToAresBridge() {
    if (this.processing) {
      return;
    }
    this.processing = true;
    const lastVersionRegistered = this.nodeLog.filter(
      (register) => register.registrationNumber
    );
    if (lastVersionRegistered.length > 0) {
      await this.showConfirmationDialog(lastVersionRegistered);
      this.processing = false;
      this.changeDetectorRef.markForCheck();
      return;
    }
    try {
      await this.aresBridgeHelperService.sendToAresBridge(this.node);
    } finally {
      this.processing = false;
      this.changeDetectorRef.markForCheck();
    }
  }
  /**
   * Opens a confirmation dialog warning that the node was already registered and,
   * if the user confirms, resends it to the ARES bridge.
   *
   * @param lastVersionRegistred The previously registered ARES log entries.
   */
  private async showConfirmationDialog(
    lastVersionRegistred: ExternalRepositoryData[]
  ) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      ariaLabel: 'Dialog',
      data: {
        title: 'label.ares.bridge.resend.title',
        message: 'label.ares.bridge.resend.message',
        layoutStyle: 'sendOneToAres',
        nodeLog: lastVersionRegistred,
      },
    });

    // listen to response
    const dialogResult = await firstValueFrom(dialogRef.afterClosed());
    if (dialogResult) {
      await this.aresBridgeHelperService.sendToAresBridge(this.node);
    }
  }

  /**
   * Computes and stores the size of the given folder in {@link folderSize}. Sets the
   * size to `0` when no folder id is provided.
   *
   * @param folderId The folder identifier whose size to compute.
   */
  public async getFolderSize(folderId: string | undefined) {
    if (folderId === undefined) {
      this.folderSize = 0;
    } else {
      const result = await this.spaceService.getFolderSizeAsync({
        id: folderId,
      });
      if (result.code !== undefined) {
        this.folderSize = result.code;
      }
    }
    this.changeDetectorRef.markForCheck();
  }

  /**
   * @returns `true` when the ARES registration number is empty or the literal
   * string `'null'`.
   */
  isEmptyRegistrationNumber() {
    return (
      this.formAresInfo.value.registrationNumber === 'null' ||
      this.formAresInfo.value.registrationNumber === ''
    );
  }

  /**
   * @returns `true` when the node's security ranking is `SENSITIVE` or
   * `SPECIAL_HANDLING`.
   */
  public isSensitive() {
    return (
      this.node?.properties?.security_ranking === 'SENSITIVE' ||
      this.node?.properties?.security_ranking === 'SPECIAL_HANDLING'
    );
  }
}
