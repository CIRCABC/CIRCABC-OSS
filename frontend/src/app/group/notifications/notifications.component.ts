import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  Node as ModelNode,
  NodesService,
  NotificationDefinition,
  NotificationService,
} from 'app/core/generated/circabc';
import { LoadingService } from 'app/core/loading.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { AddNotificationsComponent } from 'app/shared/add-notifications/add-notifications.component';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';

/**
 * Standalone Angular component (`cbc-notifications`) that renders the
 * notification-management view for a given node (a library folder/document or
 * a forum/topic) within an interest group.
 *
 * It displays the list of notification authorities currently subscribed to the
 * node, allows administrators to add new notification subscriptions (via an
 * "add" modal), delete existing ones inline, and navigate back to the
 * originating context (library, forum or topic).
 *
 * Key collaborators:
 * - {@link NodesService} to resolve the current node.
 * - {@link NotificationService} to read, add and delete notification
 *   definitions/authorities for the node.
 * - {@link UiMessageService} to surface error messages to the user.
 * - {@link TranslocoService} for i18n of error messages.
 * - {@link ActivatedRoute} / {@link Router} for reading route parameters and
 *   performing back-navigation.
 */
@Component({
  selector: 'cbc-notifications',
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    InlineDeleteComponent,
    AddNotificationsComponent,
    I18nPipe,
    TranslocoModule,
  ],
})
export class NotificationsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly nodesService = inject(NodesService);
  private readonly notificationService = inject(NotificationService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translateService = inject(TranslocoService);
  private readonly router = inject(Router);
  private readonly loadingService = inject(LoadingService);

  /** Whether the node and its notifications are currently being loaded. */
  public readonly loading = signal(false);
  /** The node (folder, document, forum or topic) whose notifications are managed. */
  public readonly currentNode = signal<ModelNode | undefined>(undefined);
  /** Whether a notification operation is currently in progress. */
  public processing = false;
  /** Controls the visibility of the "add notifications" modal. */
  public readonly showAddModal = signal(false);
  /** Identifier of the interest group the node belongs to (from route params). */
  public readonly currentIg = signal('');
  /** The notification definition holding the authorities subscribed to the node. */
  public readonly notifs = signal<NotificationDefinition | undefined>(
    undefined
  );
  /**
   * The originating context used for back-navigation, populated from the
   * `from` query parameter. Expected values: `'library'`, `'forum'` or `'topic'`.
   */
  public readonly from = signal<string | undefined>(undefined);

  /**
   * Angular lifecycle hook. Subscribes to route params to capture the interest
   * group id and load the target node with its notifications, and to query
   * params to capture the originating context ({@link from}).
   */
  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      this.currentIg.set(params.id);
      await this.loadNode(params);
    });

    this.route.queryParams.subscribe((params) => {
      if (params.from) {
        this.from.set(params.from);
      }
    });
  }

  /**
   * Loads the target node and its notification definition based on the route
   * parameters, toggling the {@link loading} flag around the async work.
   *
   * @param params Route parameters; the `nodeId` entry identifies the node to
   * load. If `nodeId` is absent, no node is fetched.
   * @returns A promise that resolves once loading has completed.
   */
  public async loadNode(params: { [id: string]: string }) {
    const id = params.nodeId;
    if (!id) {
      return;
    }
    await this.loadingService.run(this.loading, async () => {
      this.currentNode.set(await this.nodesService.getNodeAsync({ id }));
      this.notifs.set(
        await this.notificationService.getNotificationsAsync({ id })
      );
    });
  }

  /**
   * Deletes a notification subscription for the given authority on the current
   * node and refreshes the notification list. On failure, a translated error
   * message is shown via {@link UiMessageService}.
   *
   * @param authority The identifier of the authority (user/group) whose
   * notification should be removed. When `undefined` the method is a no-op.
   * @returns A promise that resolves once the deletion and refresh complete.
   */
  public async deleteNotification(authority: string | undefined) {
    const nodeId = this.currentNode()?.id;
    if (authority !== undefined && nodeId) {
      try {
        await this.notificationService.deleteNotificationAuthorityAsync({
          id: nodeId,
          authority,
        });
        this.notifs.set(
          await this.notificationService.getNotificationsAsync({ id: nodeId })
        );
      } catch (error) {
        console.error(error);
        const text = this.translateService.translate(
          getErrorTranslation(ActionType.DELETE_NOTIFICATION)
        );
        if (text) {
          this.uiMessageService.addErrorMessage(text, false);
        }
      }
    }
  }

  /**
   * Handles the result emitted by the "add notifications" modal. On a
   * successful add it reloads the notification list and closes the modal; on
   * cancellation it simply closes the modal.
   *
   * @param result The action result emitted by the child add-notifications
   * component.
   * @returns A promise that resolves once any required refresh completes.
   */
  public async refresh(result: ActionEmitterResult) {
    const nodeId = this.currentNode()?.id;
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.ADD_NOTIFICATIONS &&
      nodeId
    ) {
      this.notifs.set(
        await this.notificationService.getNotificationsAsync({ id: nodeId })
      );
      this.showAddModal.set(false);
    } else if (
      result.result === ActionResult.CANCELED &&
      result.type === ActionType.ADD_NOTIFICATIONS
    ) {
      this.showAddModal.set(false);
    }
  }

  /**
   * Navigates back to the originating context based on the {@link from} value:
   * the library details view, the forum view, or the forum topic view.
   *
   * @returns A promise that resolves once navigation is triggered.
   */
  public async goBack() {
    if (this.from() === 'library') {
      this.router.navigate(
        ['../../library', this.currentNode()?.id, 'details'],
        {
          relativeTo: this.route,
        }
      );
    } else if (this.from() === 'forum') {
      this.router.navigate(['../../forum', this.currentNode()?.id], {
        relativeTo: this.route,
      });
    } else if (this.from() === 'topic') {
      this.router.navigate(['../../forum/topic', this.currentNode()?.id], {
        relativeTo: this.route,
      });
    }
  }

  /**
   * Navigates to the library view of the current node's parent folder.
   *
   * @returns A promise that resolves once navigation is triggered.
   */
  public async goBackToFolder() {
    this.router.navigate(['../../library', this.currentNode()?.parentId], {
      relativeTo: this.route,
    });
  }

  /**
   * Returns the identifier of the current node.
   *
   * @returns The current node's id, or an empty string if no node is loaded.
   */
  public getNodeId() {
    const node = this.currentNode();
    if (node) {
      return node.id;
    }

    return '';
  }
}
