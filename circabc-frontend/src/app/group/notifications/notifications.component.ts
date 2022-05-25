import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoService } from '@ngneat/transloco';

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
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation } from 'app/core/util';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss'],
  preserveWhitespaces: true,
})
export class NotificationsComponent implements OnInit {
  public loading = false;
  public currentNode!: ModelNode;
  public processing = false;
  public showAddModal = false;
  public currentIg!: string;
  public notifs!: NotificationDefinition;
  public from!: string;

  constructor(
    private route: ActivatedRoute,
    private nodesService: NodesService,
    private notificationService: NotificationService,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.params.subscribe(async (params) => {
      this.currentIg = params.id;
      await this.loadNode(params);
    });

    this.route.queryParams.subscribe((params) => {
      if (params.from) {
        this.from = params.from;
      }
    });
  }

  public async loadNode(params: { [id: string]: string }) {
    this.loading = true;
    const id = params.nodeId;
    if (id) {
      this.currentNode = await firstValueFrom(this.nodesService.getNode(id));
      this.notifs = await firstValueFrom(
        this.notificationService.getNotifications(id)
      );
    }

    this.loading = false;
  }

  public async deleteNotification(authority: string | undefined) {
    if (authority !== undefined && this.currentNode.id) {
      try {
        await firstValueFrom(
          this.notificationService.deleteNotificationAuthority(
            this.currentNode.id,
            authority
          )
        );
        this.notifs = await firstValueFrom(
          this.notificationService.getNotifications(this.currentNode.id)
        );
      } catch (error) {
        const text = this.translateService.translate(
          getErrorTranslation(ActionType.DELETE_NOTIFICATION)
        );
        if (text) {
          this.uiMessageService.addErrorMessage(text, false);
        }
      }
    }
  }

  public async refresh(result: ActionEmitterResult) {
    if (
      result.result === ActionResult.SUCCEED &&
      result.type === ActionType.ADD_NOTIFICATIONS &&
      this.currentNode.id
    ) {
      this.notifs = await firstValueFrom(
        this.notificationService.getNotifications(this.currentNode.id)
      );
      this.showAddModal = false;
    } else if (
      result.result === ActionResult.CANCELED &&
      result.type === ActionType.ADD_NOTIFICATIONS
    ) {
      this.showAddModal = false;
    }
  }

  public async goBack() {
    if (this.from === 'library') {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['../../library', this.currentNode.id, 'details'], {
        relativeTo: this.route,
      });
    } else if (this.from === 'forum') {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['../../forum', this.currentNode.id], {
        relativeTo: this.route,
      });
    } else if (this.from === 'topic') {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['../../forum/topic', this.currentNode.id], {
        relativeTo: this.route,
      });
    }
  }

  public async goBackToFolder() {
    // eslint-disable-next-line @typescript-eslint/no-floating-promises
    this.router.navigate(['../../library', this.currentNode.parentId], {
      relativeTo: this.route,
    });
  }

  public getNodeId() {
    if (this.currentNode) {
      return this.currentNode.id;
    }

    return '';
  }
}
