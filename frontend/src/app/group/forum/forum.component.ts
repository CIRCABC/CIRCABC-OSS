import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute, Data, Router, RouterLink } from '@angular/router';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  GroupConfiguration,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { LoadingService } from 'app/core/loading.service';
import { LoginService } from 'app/core/login.service';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { ForumBrowserComponent } from './browser/forum-browser.component';
import { ConfigureForumServiceComponent } from './configure-forum-service/configure-forum-service.component';
import { ForumDropdownComponent } from './dropdown/forum-dropdown.component';

/**
 * Top-level container component for the forum (newsgroup) service of an
 * interest group.
 *
 * Rendered at `cbc-forum`, it resolves the current forum {@link ModelNode}
 * from the route, loads the owning {@link InterestGroup} and its
 * {@link GroupConfiguration}, and orchestrates the forum sub-views. It renders:
 * - a responsive sub-menu and forum dropdown for navigation and actions,
 * - the {@link ForumBrowserComponent} which displays the forum/topic tree,
 * - the {@link ConfigureForumServiceComponent} configuration modal,
 * - and a {@link HorizontalLoaderComponent} while data is being fetched.
 *
 * It also exposes a set of permission helper methods (backed by
 * {@link PermissionEvaluatorService} and {@link LoginService}) used by the
 * template to decide which forum actions and controls are available to the
 * current user.
 *
 * Key collaborators: {@link NodesService} (fetch forum nodes),
 * {@link InterestGroupService} (fetch group configuration),
 * {@link PermissionEvaluatorService} (permission checks),
 * {@link LoginService} (current user) and Angular's {@link Router} /
 * {@link ActivatedRoute} (navigation and route data).
 */
@Component({
  selector: 'cbc-forum',
  templateUrl: './forum.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    ForumDropdownComponent,
    ForumBrowserComponent,
    ConfigureForumServiceComponent,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class ForumComponent implements OnInit {
  private readonly nodesService = inject(NodesService);
  private readonly route = inject(ActivatedRoute);
  private readonly permEvalService = inject(PermissionEvaluatorService);
  private readonly groupsService = inject(InterestGroupService);
  private readonly loginService = inject(LoginService);
  private readonly router = inject(Router);
  private readonly loadingService = inject(LoadingService);

  /** The currently displayed forum node (a forums root, a forum, or a topic). */
  public readonly node = signal<ModelNode | undefined>(undefined);
  /** Identifier of the currently displayed forum {@link node}. */
  public nodeId!: string;
  /** Identifier of the owning interest group, derived from the resolved {@link group}. */
  public readonly groupId = signal('');
  /** The interest group that owns this forum, resolved from the route data. */
  public readonly group = signal<InterestGroup | undefined>(undefined);
  /** Whether a forum node is currently being loaded (drives the loader UI). */
  public readonly loading = signal(false);
  /** Whether the forum service configuration modal is displayed. */
  public showConfigurationModal = false;
  /** Configuration of the owning group, used to tailor forum behavior. */
  public readonly groupConfiguration = signal<GroupConfiguration | undefined>(
    undefined
  );

  /**
   * Angular lifecycle hook. Subscribes to the route data to obtain the owning
   * {@link group}, loads its {@link groupConfiguration} when the group has a
   * newsgroup, then subscribes to the route params to load the requested forum
   * node.
   */
  public ngOnInit() {
    this.route.data.subscribe(async (value: Data) => {
      this.group.set(value.group);
      const group = this.group();

      if (group?.id) {
        this.groupId.set(group.id);
      }

      if (group?.newsgroupId) {
        await this.getGroupConf();
      }

      this.route.params.subscribe(async (params) => {
        await this.loadForum({ nodeId: params.nodeId });
      });
    });
  }

  /**
   * Fetches the {@link GroupConfiguration} for the current {@link groupId} and
   * stores it in {@link groupConfiguration}. No-op when no group id is set.
   *
   * @returns A promise that resolves once the configuration has been loaded.
   */
  private async getGroupConf() {
    const groupId = this.groupId();
    if (groupId) {
      this.groupConfiguration.set(
        await this.groupsService.getGroupConfigurationAsync({ id: groupId })
      );
    }
  }

  /**
   * Loads the forum node identified by the given params and updates
   * {@link node}, {@link nodeId} and {@link loading} accordingly.
   *
   * @param params - A map expected to contain a `nodeId` entry identifying the
   * forum node to load.
   * @returns A promise that resolves once the node has been fetched.
   */
  public async loadForum(params: { [key: string]: string }) {
    this.nodeId = params.nodeId;
    const node = await this.loadingService.run(this.loading, () =>
      this.nodesService.getNodeAsync({ id: this.nodeId })
    );
    if (node) {
      this.node.set(node);
    }
  }

  /**
   * Reacts to actions emitted by child components. On a successful
   * {@link ActionType.CREATE_FORUM} it navigates to the newly created forum;
   * on a successful {@link ActionType.CREATE_TOPIC} it reloads the current
   * forum node.
   *
   * @param result - The outcome of the emitted action, including its type and
   * any related node.
   * @returns A promise that resolves once navigation or reload has completed.
   */
  public async refresh(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.CREATE_FORUM
    ) {
      if (result?.node) {
        this.router.navigate(['..', result.node.id], {
          relativeTo: this.route,
        });
      }
    } else if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.CREATE_TOPIC
    ) {
      await this.loadForum({ nodeId: this.nodeId });
    }
  }
  /**
   * Indicates whether the current user may post in the current newsgroup node,
   * either through post or moderate permissions.
   *
   * @returns `true` if the user can post or moderate the current node.
   */
  public isNewsgroupPost(): boolean {
    const node = this.node();
    if (!node) {
      return false;
    }
    return (
      this.permEvalService.isNewsgroupPost(node) ||
      this.permEvalService.isNewsgroupModerate(node)
    );
  }

  /**
   * Indicates whether the current user has moderation-level rights over the
   * current newsgroup node (newsgroup admin, moderator, or group-level admin).
   *
   * @returns `true` if the user can moderate or administer the newsgroup.
   */
  public isNewsgroupModerateAdmin(): boolean {
    const node = this.node();
    if (!node) {
      return this.isNewsgroupAdmin();
    }
    return (
      this.permEvalService.isNewsgroupAdmin(node) ||
      this.permEvalService.isNewsgroupModerate(node) ||
      this.isNewsgroupAdmin()
    );
  }

  /**
   * Indicates whether the current {@link node} is the newsgroup (forums) root,
   * as opposed to an individual forum or topic.
   *
   * @returns `true` if the node type ends with `forums`, otherwise `false`.
   */
  public isNewsgroupRoot(): boolean {
    return this.node()?.type?.endsWith('forums') ?? false;
  }

  /**
   * Indicates whether the current user is a newsgroup administrator, either via
   * the group-level `NwsAdmin` permission, node-level admin rights, or by owning
   * the current node.
   *
   * @returns `true` if the user administers the newsgroup.
   */
  public isNewsgroupAdmin(): boolean {
    if (this.group()?.permissions.newsgroup === 'NwsAdmin') {
      return true;
    }
    const node = this.node();
    if (!node) {
      return false;
    }
    return (
      this.permEvalService.isNewsgroupAdmin(node) ||
      this.permEvalService.isOwner(node, this.loginService.getCurrentUsername())
    );
  }

  /**
   * Indicates whether the current user is a library administrator, either via
   * the group-level `LibAdmin` permission or node-level admin rights.
   *
   * @returns `true` if the user administers the library.
   */
  public isLibAdmin(): boolean {
    if (this.group()?.permissions.library === 'LibAdmin') {
      return true;
    }
    const node = this.node();
    if (!node) {
      return false;
    }
    return this.permEvalService.isLibAdmin(node);
  }

  /**
   * Reloads the {@link groupConfiguration} after a successful configuration
   * action, typically emitted by the forum configuration modal.
   *
   * @param res - The outcome of the configuration action.
   * @returns A promise that resolves once the configuration has been reloaded.
   */
  public async refreshConf(res: ActionEmitterResult) {
    const groupId = this.groupId();
    if (res.result === ActionResult.SUCCEED && groupId) {
      this.groupConfiguration.set(
        await this.groupsService.getGroupConfigurationAsync({ id: groupId })
      );
    }
  }
}
