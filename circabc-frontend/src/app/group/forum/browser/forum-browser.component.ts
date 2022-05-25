import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChange,
  ViewChild,
} from '@angular/core';

import { ActivatedRoute, Router } from '@angular/router';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ForumService,
  GroupConfiguration,
  InterestGroup,
  Node as ModelNode,
  NodesService,
  NotificationService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeViewComponent } from 'app/shared/treeview/tree-view.component';
import { firstValueFrom, Subscription } from 'rxjs';
import { UiMessage } from 'app/core/message/ui-message';
import { UiMessageLevel } from 'app/core/message/ui-message-level';
import { TranslocoService } from '@ngneat/transloco';
import { assertDefined } from 'app/core/asserts';

@Component({
  selector: 'cbc-forum-browser',
  templateUrl: './forum-browser.component.html',
  styleUrls: ['./forum-browser.component.scss'],
  preserveWhitespaces: true,
})
export class ForumBrowserComponent implements OnChanges, OnInit, OnDestroy {
  public content!: ModelNode[];
  public path: ModelNode[] = [];
  public root!: TreeNode;

  @Input()
  public forum: ModelNode | undefined;
  @Input()
  public group!: InterestGroup;
  @Input()
  public groupConfiguration!: GroupConfiguration;

  public forums!: ModelNode[];
  public topics!: ModelNode[];
  public heightMenu!: string;
  public heightMain!: string;

  // topic deletion variables
  // topic deletion variables
  public currentDeletedTopic!: ModelNode;
  public showDeleteTopic = false;

  // forum deletion variables
  // forum deletion variables
  public currentDeletedForum!: ModelNode;
  public showDeleteForum = false;

  // forum moderation variables
  // forum moderation variables
  public currentModerateForum!: ModelNode;
  public showModerateForum = false;

  public searchedNodeId!: string;

  private actionFinishedSubscription$!: Subscription;
  @ViewChild(TreeViewComponent)
  treeViewComponent!: TreeViewComponent;

  uiMessage = new UiMessage(
    UiMessageLevel.WARNING,
    this.translateService.translate('forums.message.publicly.available'),
    false,
    0
  );

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
    this.subscribe();
    this.init();
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
            await this.treeViewComponent.reload();
          }
        }
      );
  }
  private init() {
    if (this.group && this.group.newsgroupId) {
      this.root = new TreeNode('Newsgroups', this.group.newsgroupId);
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
    const chng = changes.forum;
    if (chng) {
      const cur = JSON.stringify(chng.currentValue);
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
    } else if (this.path && this.path.length > 0) {
      return this.path[this.path.length - 1].id;
    } else {
      return undefined;
    }
  }

  public async getCurrentForum() {
    assertDefined(this.forum);
    if (this.forum.id) {
      this.path = await firstValueFrom(
        this.nodesService.getPath(this.forum.id)
      );

      this.content = await firstValueFrom(
        this.forumService.getForumContent(this.forum.id)
      );
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
    if (this.groupConfiguration && this.groupConfiguration.newsgroups) {
      const newsConf = this.groupConfiguration.newsgroups;
      if (newsConf.enableFlagNewTopic === true) {
        const comparableDate = new Date();
        const day = comparableDate.getDate() - newsConf.ageFlagNewTopic;
        comparableDate.setDate(day);
        if (node && node.properties && node.properties.created) {
          const nodeDate = new Date(node.properties.created);
          return nodeDate >= comparableDate;
        }
      }
    }
    return false;
  }

  public isNewForum(node: ModelNode): boolean {
    if (this.groupConfiguration && this.groupConfiguration.newsgroups) {
      const newsConf = this.groupConfiguration.newsgroups;
      if (newsConf.enableFlagNewForum === true) {
        const comparableDate = new Date();
        const day = comparableDate.getDate() - newsConf.ageFlagNewForum;
        comparableDate.setDate(day);
        if (node && node.properties && node.properties.created) {
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
    if (this.group.permissions.newsgroup === 'NwsAdmin') {
      return true;
    }
    return this.permEvalService.isNewsgroupAdmin(this.forum);
  }
}
