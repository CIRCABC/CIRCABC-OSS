import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChange,
  signal,
  viewChild,
} from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { assertDefined } from 'app/core/asserts';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ForumService,
  type GroupConfiguration,
  type InterestGroup,
  Node as ModelNode,
  NodesService,
  NotificationService,
  PagedNodes,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageLevel } from 'app/core/message/ui-message-level';
import { BreadcrumbComponent } from 'app/group/breadcrumb/breadcrumb.component';
import { DeleteForumComponent } from 'app/group/forum/delete-forum.component';
import { ModerateComponent } from 'app/group/forum/moderate/moderate.component';
import { DeleteTopicComponent } from 'app/group/forum/topic/delete-topic.component';
import { ListingOptions } from 'app/group/listing-options/listing-options';
import { IfRoleGEDirective } from 'app/shared/directives/ifrolege.directive';
import { HintComponent } from 'app/shared/hint/hint.component';
import { HtmlTooltipDirective } from 'app/shared/html-tooltip/html-tooltip.directive';
import { NotificationMessageComponent } from 'app/shared/notification-message/notification-message.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { ShareComponent } from 'app/shared/share/share.component';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeViewComponent } from 'app/shared/treeview/tree-view.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { Subscription } from 'rxjs';

/**
 * Standalone Angular component that renders the forum (newsgroup) browser for an
 * interest group.
 *
 * It displays a tree view of the forum hierarchy alongside the content of the
 * currently selected forum, split into sub-forums and topics. It also renders
 * breadcrumbs, notification subscription controls, and the inline dialogs used to
 * delete topics, delete forums and moderate forums.
 *
 * The component coordinates several collaborators:
 * - {@link ForumService} / {@link NodesService} to load forum content and node paths.
 * - {@link NotificationService} to manage the user's notification subscription.
 * - {@link PermissionEvaluatorService} to determine newsgroup admin/moderation rights.
 * - {@link ActionService} to react to forum create/delete actions and reload the tree.
 * - {@link Router} / {@link ActivatedRoute} to navigate between forum nodes.
 *
 * Paging preferences ({@link ListingOptions}) are persisted to `sessionStorage` so that
 * they survive navigation within the session.
 */
@Component({
  selector: 'cbc-forum-browser',
  templateUrl: './forum-browser.component.html',
  styleUrl: './forum-browser.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    TreeViewComponent,
    BreadcrumbComponent,
    RouterLink,
    ShareComponent,
    HintComponent,
    NotificationMessageComponent,
    MatTooltipModule,
    HtmlTooltipDirective,
    IfRoleGEDirective,
    UserCardComponent,
    DeleteTopicComponent,
    DeleteForumComponent,
    ModerateComponent,
    DatePipe,
    I18nPipe,
    TranslocoModule,
    PagerComponent,
  ],
})
export class ForumBrowserComponent implements OnChanges, OnInit, OnDestroy {
  private readonly forumService = inject(ForumService);
  private readonly loginService = inject(LoginService);
  private readonly nodesService = inject(NodesService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly actionService = inject(ActionService);
  private readonly translateService = inject(TranslocoService);

  /** Flat list of the nodes contained in the currently displayed forum (the current page). */
  public content!: ModelNode[];
  /** Ancestor path of the current forum, from the root down to the current node, used for breadcrumbs. */
  public path = signal<ModelNode[]>([]);
  /** Root {@link TreeNode} of the newsgroup tree rendered by the tree view. */
  public root!: TreeNode;

  /**
   * Two-way bound model of the currently selected forum node. Changes to this model
   * trigger reloading of the forum content via {@link ngOnChanges}.
   */
  public forum = model.required<ModelNode>();
  /** Required input: the interest group that owns the forums being browsed. */
  public readonly group = input.required<InterestGroup>();
  /** Required input: the group configuration, used to evaluate "new topic"/"new forum" flags. */
  public groupConfiguration = input.required<GroupConfiguration>();

  /** Sub-forums extracted from {@link content} for rendering (see {@link extractData}). */
  public forums = signal<ModelNode[] | undefined>(undefined);
  /** Topics extracted from {@link content} for rendering (see {@link extractData}). */
  public topics = signal<ModelNode[] | undefined>(undefined);
  /** Computed CSS height of the side menu area. */
  public heightMenu!: string;
  /** Computed CSS height of the main content area. */
  public heightMain!: string;

  // topic deletion variables
  /** Topic currently targeted for deletion by the delete-topic dialog. */
  public currentDeletedTopic!: ModelNode;
  /** Controls the visibility of the delete-topic dialog. */
  public showDeleteTopic = signal(false);

  // forum deletion variables
  /** Forum currently targeted for deletion by the delete-forum dialog. */
  public currentDeletedForum!: ModelNode;
  /** Controls the visibility of the delete-forum dialog. */
  public showDeleteForum = signal(false);

  // forum moderation variables
  /** Forum currently targeted by the moderation dialog. */
  public currentModerateForum!: ModelNode;
  /** Controls the visibility of the moderation dialog. */
  public showModerateForum = false;

  /** Id of the node that should be highlighted/selected in the tree view. */
  public searchedNodeId!: string;

  /** Subscription to {@link ActionService.actionFinished$}; cleaned up on destroy. */
  private actionFinishedSubscription$!: Subscription;
  /** Reference to the embedded {@link TreeViewComponent}, used to reload the tree. */
  readonly treeViewComponent = viewChild.required(TreeViewComponent);

  /** Warning message shown when the forum content is publicly available. */
  uiMessage = new UiMessage(
    UiMessageLevel.WARNING,
    this.translateService.translate('forums.message.publicly.available'),
    false,
    0
  );

  /** Current paging/sorting options for the forum content listing. */
  public listingOptions = signal<ListingOptions>({
    page: 1,
    limit: 10,
    sort: '',
  });
  /** List of available page numbers for the pager. */
  public pages: number[] = [];
  /** Total number of items available for the current forum, used by the pager. */
  public totalItems = signal(10);
  /** Paged model returned by the forum content service for the current page. */
  public forumsPagedModel!: PagedNodes;

  /** Key under which {@link listingOptions} is persisted in `sessionStorage`. */
  private readonly forumBrowserListingOptionsKey = 'forumBrowserListingOptions';

  /**
   * Angular lifecycle hook. Restores persisted listing options, subscribes to action
   * events and initialises the tree root.
   */
  ngOnInit() {
    this.loadListingOptions();
    this.subscribe();
    this.init();
  }
  // save listing options into session storage
  /** Persists the current {@link listingOptions} to `sessionStorage`. */
  private saveListingOptions() {
    sessionStorage.setItem(
      this.forumBrowserListingOptionsKey,
      JSON.stringify(this.listingOptions())
    );
  }
  // load listing options from session storage
  /** Restores {@link listingOptions} from `sessionStorage` if a value was previously stored. */
  private loadListingOptions() {
    const listingOptions = sessionStorage.getItem(
      this.forumBrowserListingOptionsKey
    );
    if (listingOptions) {
      this.listingOptions.set(JSON.parse(listingOptions) as ListingOptions);
    }
  }

  /**
   * Subscribes to {@link ActionService.actionFinished$} and reloads the tree view when a
   * forum is successfully created or deleted.
   */
  private subscribe() {
    this.actionFinishedSubscription$ =
      this.actionService.actionFinished$.subscribe(
        async (action: ActionEmitterResult) => {
          if (
            (action.type === ActionType.CREATE_FORUM ||
              action.type === ActionType.DELETE_FORUM) &&
            action.result === ActionResult.SUCCEED
          ) {
            await this.treeViewComponent().reload();
          }
        }
      );
  }
  /** Initialises the {@link root} tree node from the group's newsgroup id and expands it. */
  private init() {
    const group = this.group();
    if (group?.newsgroupId) {
      this.root = new TreeNode('Newsgroups', group.newsgroupId);
      this.root.expanded = true;
    }
  }

  /** Angular lifecycle hook. Unsubscribes from action events to prevent memory leaks. */
  ngOnDestroy(): void {
    this.unsubscribe();
  }

  /** Tears down the {@link actionFinishedSubscription$} if it exists. */
  private unsubscribe() {
    if (this.actionFinishedSubscription$) {
      this.actionFinishedSubscription$.unsubscribe();
    }
  }
  /**
   * Angular lifecycle hook. When the bound {@link forum} changes, records the searched node
   * id and reloads the current forum content.
   *
   * @param changes Map of changed input properties provided by Angular.
   * @returns A promise that resolves once the current forum content has been refreshed.
   */
  public ngOnChanges(changes: { [propertyName: string]: SimpleChange }) {
    void this.handleChanges(changes);
  }

  private async handleChanges(changes: {
    [propertyName: string]: SimpleChange;
  }) {
    if (this.forum() === undefined) {
      return;
    }
    const forumId = this.forum().id;
    const forumChanges = changes.forum;
    if (forumChanges) {
      const cur = JSON.stringify(forumChanges.currentValue);
      if (cur !== undefined) {
        if (forumId) {
          this.searchedNodeId = forumId;
        }
        await this.getCurrentForum();
      }
    }
  }

  /**
   * Returns the id of the node that should be selected in the tree view.
   *
   * @returns The explicitly searched node id, otherwise the id of the last node in the
   * {@link path}, or `undefined` if neither is available.
   */
  public getSearchedNodeId() {
    if (this.searchedNodeId) {
      return this.searchedNodeId;
    }
    const path = this.path();
    if (path && path.length > 0) {
      return path.at(-1)?.id;
    }
    return undefined;
  }

  /**
   * Loads the path and paged content for the current forum. If the requested page is empty
   * (beyond page 1), it falls back to the first page. Populates {@link path},
   * {@link forumsPagedModel}, {@link content} and {@link totalItems}, then splits the content
   * into forums and topics.
   *
   * @returns A promise that resolves once the forum content has been loaded and processed.
   */
  private async getCurrentForum() {
    assertDefined(this.forum);
    const forumId = this.forum().id;
    if (forumId) {
      this.path.set(await this.nodesService.getPathAsync({ id: forumId }));

      this.forumsPagedModel = await this.forumService.getForumContentAsync({
        id: forumId,
        limit: this.listingOptions().limit,
        page: this.listingOptions().page,
        order: this.listingOptions().sort,
      });
      if (
        1 < this.listingOptions().page &&
        this.forumsPagedModel.data.length === 0
      ) {
        this.listingOptions.update((options) => ({ ...options, page: 1 }));
        this.saveListingOptions();
        this.forumsPagedModel = await this.forumService.getForumContentAsync({
          id: forumId,
          limit: this.listingOptions().limit,
          page: this.listingOptions().page,
          order: this.listingOptions().sort,
        });
      }
      this.content = this.forumsPagedModel.data;
      this.totalItems.set(
        this.forumsPagedModel.total > 0
          ? this.forumsPagedModel.total
          : this.listingOptions().limit
      );

      this.extractData(this.content);
    }
  }

  /**
   * Indicates whether the current forum is the root forum.
   *
   * @returns `true` when no forum is set.
   */
  public isRootForum(): boolean {
    return this.forum === undefined;
  }

  /*
   * split the forum and the topics in two lists to avoid rendering conflicts in the HTML
   */
  /**
   * Splits the given content into {@link forums} and {@link topics} based on each node's
   * type, to avoid rendering conflicts in the template.
   *
   * @param content The list of nodes to classify.
   */
  private extractData(content: ModelNode[]): void {
    const forums: ModelNode[] = [];
    const topics: ModelNode[] = [];

    for (const contentItem of content) {
      if (contentItem.type?.includes('topic')) {
        topics.push(contentItem);
      } else if (contentItem.type?.includes('forum')) {
        forums.push(contentItem);
      }
    }

    this.forums.set(forums);
    this.topics.set(topics);
  }

  /**
   * Navigates to the given node, updating the router to the sibling route for that node id.
   *
   * @param node The tree node (or an object carrying a `nodeId`) to navigate to.
   * @returns A promise that resolves once navigation has been triggered.
   */
  public async propagateNavigation(node: TreeNode | { nodeId: string }) {
    assertDefined(this.forum);
    this.searchedNodeId = node.nodeId;

    this.router.navigate(['..', node.nodeId], { relativeTo: this.route });
  }

  /**
   * Prepares and opens the delete-topic dialog for the given topic.
   *
   * @param topic The topic node to delete.
   */
  public prepareTopicDeletion(topic: ModelNode) {
    this.currentDeletedTopic = topic;
    this.showDeleteTopic.set(true);
  }

  /**
   * Handles the result of a topic deletion. Reloads the forum content on success and closes
   * the dialog on success, failure or cancellation.
   *
   * @param res The action result emitted by the delete-topic dialog.
   * @returns A promise that resolves once the content has been refreshed (on success).
   */
  public async refreshDeleteTopic(res: ActionEmitterResult) {
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      await this.getCurrentForum();
      this.showDeleteTopic.set(false);
    } else if (
      res.result === ActionResult.FAILED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      this.showDeleteTopic.set(false);
    } else if (
      res.result === ActionResult.CANCELED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      this.showDeleteTopic.set(false);
    }
  }

  /**
   * Prepares and opens the delete-forum dialog for the given forum.
   *
   * @param forum The forum node to delete.
   * @throws If `forum` is undefined (via {@link assertDefined}).
   */
  public prepareForumDeletion(forum: ModelNode | undefined) {
    assertDefined(forum);
    this.currentDeletedForum = forum;
    this.showDeleteForum.set(true);
  }

  /**
   * Handles the result of a forum deletion. On success, navigates to the parent (or current)
   * forum, reloads its content and closes the dialog; closes the dialog on failure or
   * cancellation.
   *
   * @param res The action result emitted by the delete-forum dialog.
   * @returns A promise that resolves once navigation and refresh have completed (on success).
   */
  public async refreshDeleteForum(res: ActionEmitterResult) {
    assertDefined(this.forum);
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.DELETE_FORUM
    ) {
      let forumId = this.forum().id as string;
      if (!this.isForumRoot()) {
        forumId = this.forum().parentId as string;
      }
      const forum = await this.nodesService.getNodeAsync({ id: forumId });

      this.forum.set(forum);

      await this.getCurrentForum();
      this.showDeleteForum.set(false);
      forumId = this.forum().id as string;
      if (forumId) {
        this.propagateNavigation({ nodeId: forumId });
      }
    } else if (
      res.result === ActionResult.FAILED &&
      res.type === ActionType.DELETE_FORUM
    ) {
      this.showDeleteForum.set(false);
    } else if (
      res.result === ActionResult.CANCELED &&
      res.type === ActionType.DELETE_FORUM
    ) {
      this.showDeleteForum.set(false);
    }
  }

  /**
   * Indicates whether the current user is authenticated as a guest.
   *
   * @returns `true` if the user is a guest.
   */
  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  /**
   * Indicates whether the current user is subscribed to notifications for the current forum.
   *
   * @returns `true` if notifications are set to `ALLOWED` on the forum.
   */
  isSubscribedToNotifications(): boolean {
    assertDefined(this.forum);
    return this.forum().notifications === 'ALLOWED';
  }

  /**
   * Changes the current user's notification subscription for the current forum and refreshes
   * the forum model.
   *
   * @param value The new notification authority value to apply.
   * @returns A promise that resolves once the subscription has been updated and the forum reloaded.
   */
  public async changeNotificationSubscription(value: string) {
    assertDefined(this.forum);
    const forumId = this.forum().id;
    if (value && value !== '' && forumId) {
      await this.notificationService.putNotificationAuthorityAsync({
        id: forumId,
        authority: this.loginService.getCurrentUsername(),
        body: value,
      });
      this.forum.set(await this.nodesService.getNodeAsync({ id: forumId }));
    }
  }

  /**
   * Indicates whether the current forum is the top-level "forums" container node.
   *
   * @returns `true` if the current forum's type is the Alfresco forums root type.
   */
  public isForumRoot() {
    assertDefined(this.forum);
    return (
      this.forum().type === '{http://www.alfresco.org/model/forum/1.0}forums'
    );
  }

  /**
   * Prepares and opens the moderation dialog for the given forum.
   *
   * @param forum The forum node to moderate.
   * @throws If `forum` is undefined (via {@link assertDefined}).
   */
  public prepareForumModeration(forum: ModelNode | undefined) {
    assertDefined(forum);
    this.currentModerateForum = forum;
    this.showModerateForum = true;
  }

  /**
   * Handles the result of the moderation dialog by closing it.
   *
   * @param _result The action result emitted by the moderation dialog (unused).
   * @returns A promise that resolves once the dialog has been closed.
   */
  public async refreshModerateForum(_result: ActionEmitterResult) {
    this.showModerateForum = false;
  }
  /**
   * Indicates whether the given node is a forum.
   *
   * @param node The node to test.
   * @returns `true` if the node's type contains `forum`.
   */
  public isForum(node: ModelNode): boolean {
    if (node.type) {
      return node.type.includes('forum');
    }
    return false;
  }

  /**
   * Returns the modification date of the given node.
   *
   * @param node The node to inspect.
   * @returns The `modified` property value, or `null` if the node has no properties.
   */
  public modified(node: ModelNode): string | null {
    if (node.properties) {
      return node.properties.modified;
    }
    return null;
  }

  /**
   * Returns the creator of the given node.
   *
   * @param node The node to inspect.
   * @returns The `creator` property value, or `null` if the node has no properties.
   */
  public creator(node: ModelNode): string | null {
    if (node.properties) {
      return node.properties.creator;
    }
    return null;
  }

  /**
   * Indicates whether the given forum is the newsgroups root node.
   *
   * @param forum The forum node to test.
   * @returns `true` if the forum is named `Newsgroups`.
   */
  public isNwsRoot(forum: ModelNode | undefined): boolean {
    return forum?.name === 'Newsgroups';
  }

  /**
   * Indicates whether the current forum contains sub-forums.
   *
   * @returns `true` if {@link forums} is non-empty.
   */
  public hasSubForums() {
    const forums = this.forums();
    return forums !== undefined && forums.length > 0;
  }

  /**
   * Indicates whether the given node has a non-empty name.
   *
   * @param item The node to test.
   * @returns `true` if the node's name is defined and not empty.
   */
  public nameExists(item: ModelNode): boolean {
    return item.name !== undefined && item.name !== '';
  }

  /**
   * Determines whether the given node should be flagged as a new topic, based on the group's
   * newsgroup configuration (flag enabled and topic age within the configured threshold).
   *
   * @param node The topic node to evaluate.
   * @returns `true` if the node qualifies as a new topic.
   */
  public isNewTopic(node: ModelNode): boolean {
    if (this.groupConfiguration().newsgroups) {
      const newsConf = this.groupConfiguration().newsgroups;
      if (newsConf.enableFlagNewTopic === true) {
        const comparableDate = new Date();
        const day = comparableDate.getDate() - newsConf.ageFlagNewTopic;
        comparableDate.setDate(day);
        if (node?.properties?.created) {
          const nodeDate = new Date(node.properties.created);
          return nodeDate >= comparableDate;
        }
      }
    }
    return false;
  }

  /**
   * Determines whether the given node should be flagged as a new forum, based on the group's
   * newsgroup configuration (flag enabled and forum age within the configured threshold).
   *
   * @param node The forum node to evaluate.
   * @returns `true` if the node qualifies as a new forum.
   */
  public isNewForum(node: ModelNode): boolean {
    if (this.groupConfiguration().newsgroups) {
      const newsConf = this.groupConfiguration().newsgroups;
      if (newsConf.enableFlagNewForum === true) {
        const comparableDate = new Date();
        const day = comparableDate.getDate() - newsConf.ageFlagNewForum;
        comparableDate.setDate(day);
        if (node?.properties?.created) {
          const nodeDate = new Date(node.properties.created);
          return nodeDate >= comparableDate;
        }
      }
    }
    return false;
  }

  /**
   * Indicates whether the current user can administer or moderate the current newsgroup.
   *
   * @returns `true` if the user is a newsgroup admin, a newsgroup moderator, or a group-level
   * newsgroup admin.
   */
  public isNewsgroupAdminModerate(): boolean {
    assertDefined(this.forum);
    return (
      this.permEvalService.isNewsgroupAdmin(this.forum()) ||
      this.permEvalService.isNewsgroupModerate(this.forum()) ||
      this.isNewsgroupAdmin()
    );
  }

  /**
   * Indicates whether the current user is a newsgroup administrator.
   *
   * @returns `true` if the group grants `NwsAdmin` permission or the permission evaluator
   * identifies the user as a newsgroup admin for the current forum.
   */
  public isNewsgroupAdmin(): boolean {
    assertDefined(this.forum);
    if (this.group().permissions.newsgroup === 'NwsAdmin') {
      return true;
    }
    return this.permEvalService.isNewsgroupAdmin(this.forum());
  }

  /**
   * Changes the current page of the forum content listing, persists the new options and
   * reloads the content.
   *
   * @param p The 1-based page number to load.
   * @returns A promise that resolves once the content for the new page has been loaded.
   */
  public async changePage(p: number) {
    assertDefined(this.forum);
    if (this.forum().id) {
      this.listingOptions.update((options) => ({ ...options, page: p }));
      this.saveListingOptions();
      await this.getCurrentForum();
    }
  }

  /**
   * Changes the page size of the forum content listing, resets to the first page, persists the
   * new options and reloads the content.
   *
   * @param limit The new number of items per page.
   * @returns A promise that resolves once the content has been reloaded.
   */
  public async changeLimit(limit: number) {
    this.listingOptions.update((options) => ({ ...options, limit, page: 1 }));
    this.saveListingOptions();
    await this.getCurrentForum();
  }
}
