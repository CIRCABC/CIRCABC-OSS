import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChange,
  viewChild,
  input,
} from '@angular/core';

import { DatePipe } from '@angular/common';
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
import { NotificationMessageComponent } from 'app/shared/notification-message/notification-message.component';
import { PagerComponent } from 'app/shared/pager/pager.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { ShareComponent } from 'app/shared/share/share.component';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeViewComponent } from 'app/shared/treeview/tree-view.component';
import { UserCardComponent } from 'app/shared/user-card/user-card.component';
import { TooltipModule } from 'primeng/tooltip';
import { Subscription, firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-forum-browser',
  templateUrl: './forum-browser.component.html',
  styleUrl: './forum-browser.component.scss',
  preserveWhitespaces: true,
  imports: [
    TreeViewComponent,
    BreadcrumbComponent,
    RouterLink,
    ShareComponent,
    HintComponent,
    NotificationMessageComponent,
    TooltipModule,
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
  public content!: ModelNode[];
  public path: ModelNode[] = [];
  public root!: TreeNode;

  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  public forum: ModelNode | undefined;
  public readonly group = input.required<InterestGroup>();
  // TODO: Skipped for migration because:
  //  This input is used in a control flow expression (e.g. `@if` or `*ngIf`)
  //  and migrating would break narrowing currently.
  @Input()
  public groupConfiguration!: GroupConfiguration;

  public forums!: ModelNode[];
  public topics!: ModelNode[];
  public heightMenu!: string;
  public heightMain!: string;

  // topic deletion variables
  public currentDeletedTopic!: ModelNode;
  public showDeleteTopic = false;

  // forum deletion variables
  public currentDeletedForum!: ModelNode;
  public showDeleteForum = false;

  // forum moderation variables
  public currentModerateForum!: ModelNode;
  public showModerateForum = false;

  public searchedNodeId!: string;

  private actionFinishedSubscription$!: Subscription;
  readonly treeViewComponent = viewChild.required(TreeViewComponent);

  uiMessage = new UiMessage(
    UiMessageLevel.WARNING,
    this.translateService.translate('forums.message.publicly.available'),
    false,
    0
  );

  public listingOptions: ListingOptions = { page: 1, limit: 10, sort: '' };
  public pages: number[] = [];
  public totalItems = 10;
  public forumsPagedModel!: PagedNodes;

  private readonly forumBrowserListingOptionsKey = 'forumBrowserListingOptions';

  constructor(
    private forumService: ForumService,
    private loginService: LoginService,
    private nodesService: NodesService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute,
    private permEvalService: PermissionEvaluatorService,
    private actionService: ActionService,
    private translateService: TranslocoService
  ) {}

  ngOnInit() {
    this.loadListingOptions();
    this.subscribe();
    this.init();
  }
  // save listing options into session storage
  private saveListingOptions() {
    sessionStorage.setItem(
      this.forumBrowserListingOptionsKey,
      JSON.stringify(this.listingOptions)
    );
  }
  // load listing options from session storage
  private loadListingOptions() {
    const listingOptions = sessionStorage.getItem(
      this.forumBrowserListingOptionsKey
    );
    if (listingOptions) {
      this.listingOptions = JSON.parse(listingOptions) as ListingOptions;
    }
  }

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
  private init() {
    const group = this.group();
    if (group?.newsgroupId) {
      this.root = new TreeNode('Newsgroups', group.newsgroupId);
      this.root.expanded = true;
    }
  }

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  private unsubscribe() {
    if (this.actionFinishedSubscription$) {
      this.actionFinishedSubscription$.unsubscribe();
    }
  }
  public async ngOnChanges(changes: { [propertyName: string]: SimpleChange }) {
    if (this.forum === undefined) {
      return;
    }
    const forumChanges = changes.forum;
    if (forumChanges) {
      const cur = JSON.stringify(forumChanges.currentValue);
      if (cur !== undefined) {
        if (this.forum.id) {
          this.searchedNodeId = this.forum.id;
        }
        await this.getCurrentForum();
      }
    }
  }

  public getSearchedNodeId() {
    if (this.searchedNodeId) {
      return this.searchedNodeId;
    }
    if (this.path && this.path.length > 0) {
      return this.path[this.path.length - 1].id;
    }
    return undefined;
  }

  private async getCurrentForum() {
    assertDefined(this.forum);
    if (this.forum.id) {
      this.path = await firstValueFrom(
        this.nodesService.getPath(this.forum.id)
      );

      this.forumsPagedModel = await firstValueFrom(
        this.forumService.getForumContent(
          this.forum.id,
          undefined,
          this.listingOptions.limit,
          this.listingOptions.page,
          this.listingOptions.sort
        )
      );
      if (
        1 < this.listingOptions.page &&
        this.forumsPagedModel.data.length === 0
      ) {
        this.listingOptions.page = 1;
        this.saveListingOptions();
        this.forumsPagedModel = await firstValueFrom(
          this.forumService.getForumContent(
            this.forum.id,
            undefined,
            this.listingOptions.limit,
            this.listingOptions.page,
            this.listingOptions.sort
          )
        );
      }
      this.content = this.forumsPagedModel.data;
      this.totalItems =
        this.forumsPagedModel.total > 0
          ? this.forumsPagedModel.total
          : this.listingOptions.limit;

      this.extractData(this.content);
    }
  }

  public isRootForum(): boolean {
    return this.forum === undefined;
  }

  /*
   * split the forum and the topics in two lists to avoid rendering conflicts in the HTML
   */
  private extractData(content: ModelNode[]): void {
    this.forums = [];
    this.topics = [];

    for (const contentItem of content) {
      if (
        contentItem.type !== undefined &&
        contentItem.type.indexOf('topic') !== -1
      ) {
        this.topics.push(contentItem);
      } else if (
        contentItem.type !== undefined &&
        contentItem.type.indexOf('forum') !== -1
      ) {
        this.forums.push(contentItem);
      }
    }
  }

  public async propagateNavigation(node: TreeNode | { nodeId: string }) {
    assertDefined(this.forum);
    this.searchedNodeId = node.nodeId;
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['..', node.nodeId], { relativeTo: this.route });
  }

  public prepareTopicDeletion(topic: ModelNode) {
    this.currentDeletedTopic = topic;
    this.showDeleteTopic = true;
  }

  public async refreshDeleteTopic(res: ActionEmitterResult) {
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      await this.getCurrentForum();
      this.showDeleteTopic = false;
    } else if (
      res.result === ActionResult.FAILED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      this.showDeleteTopic = false;
    } else if (
      res.result === ActionResult.CANCELED &&
      res.type === ActionType.DELETE_TOPIC
    ) {
      this.showDeleteTopic = false;
    }
  }

  public prepareForumDeletion(forum: ModelNode | undefined) {
    assertDefined(forum);
    this.currentDeletedForum = forum;
    this.showDeleteForum = true;
  }

  public async refreshDeleteForum(res: ActionEmitterResult) {
    assertDefined(this.forum);
    if (
      res.result === ActionResult.SUCCEED &&
      res.type === ActionType.DELETE_FORUM
    ) {
      if (this.isForumRoot()) {
        this.forum = await firstValueFrom(
          this.nodesService.getNode(this.forum.id as string)
        );
      } else {
        this.forum = await firstValueFrom(
          this.nodesService.getNode(this.forum.parentId as string)
        );
      }
      await this.getCurrentForum();
      this.showDeleteForum = false;
      if (this.forum.id) {
        // eslint-disable-next-line @typescript-eslint/no-floating-promises
        this.propagateNavigation({ nodeId: this.forum.id });
      }
    } else if (
      res.result === ActionResult.FAILED &&
      res.type === ActionType.DELETE_FORUM
    ) {
      this.showDeleteForum = false;
    } else if (
      res.result === ActionResult.CANCELED &&
      res.type === ActionType.DELETE_FORUM
    ) {
      this.showDeleteForum = false;
    }
  }

  public isGuest(): boolean {
    return this.loginService.isGuest();
  }

  isSubscribedToNotifications(): boolean {
    assertDefined(this.forum);
    return this.forum.notifications === 'ALLOWED';
  }

  public async changeNotificationSubscription(value: string) {
    assertDefined(this.forum);
    if (value && value !== '' && this.forum.id) {
      await firstValueFrom(
        this.notificationService.putNotificationAuthority(
          this.forum.id,
          this.loginService.getCurrentUsername(),
          value
        )
      );
      this.forum = await firstValueFrom(
        this.nodesService.getNode(this.forum.id)
      );
    }
  }

  public isForumRoot() {
    assertDefined(this.forum);
    return (
      this.forum.type === '{http://www.alfresco.org/model/forum/1.0}forums'
    );
  }

  public prepareForumModeration(forum: ModelNode | undefined) {
    assertDefined(forum);
    this.currentModerateForum = forum;
    this.showModerateForum = true;
  }

  public async refreshModerateForum(_result: ActionEmitterResult) {
    this.showModerateForum = false;
  }
  public isForum(node: ModelNode): boolean {
    if (node.type) {
      return node.type.indexOf('forum') !== -1;
    }
    return false;
  }

  public modified(node: ModelNode): string | null {
    if (node.properties) {
      return node.properties.modified;
    }
    return null;
  }

  public creator(node: ModelNode): string | null {
    if (node.properties) {
      return node.properties.creator;
    }
    return null;
  }

  public isNwsRoot(forum: ModelNode | undefined): boolean {
    return forum !== undefined && forum.name === 'Newsgroups';
  }

  public hasSubForums() {
    return this.forums && this.forums.length > 0;
  }

  public nameExists(item: ModelNode): boolean {
    return item.name !== undefined && item.name !== '';
  }

  public isNewTopic(node: ModelNode): boolean {
    if (this.groupConfiguration?.newsgroups) {
      const newsConf = this.groupConfiguration.newsgroups;
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

  public isNewForum(node: ModelNode): boolean {
    if (this.groupConfiguration?.newsgroups) {
      const newsConf = this.groupConfiguration.newsgroups;
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

  public isNewsgroupAdminModerate(): boolean {
    assertDefined(this.forum);
    return (
      this.permEvalService.isNewsgroupAdmin(this.forum) ||
      this.permEvalService.isNewsgroupModerate(this.forum) ||
      this.isNewsgroupAdmin()
    );
  }

  public isNewsgroupAdmin(): boolean {
    assertDefined(this.forum);
    if (this.group().permissions.newsgroup === 'NwsAdmin') {
      return true;
    }
    return this.permEvalService.isNewsgroupAdmin(this.forum);
  }

  public async changePage(p: number) {
    assertDefined(this.forum);
    if (this.forum.id) {
      this.listingOptions.page = p;
      this.saveListingOptions();
      await this.getCurrentForum();
    }
  }

  public async changeLimit(limit: number) {
    this.listingOptions.limit = limit;
    this.listingOptions.page = 1;
    this.saveListingOptions();
    await this.getCurrentForum();
  }
}
