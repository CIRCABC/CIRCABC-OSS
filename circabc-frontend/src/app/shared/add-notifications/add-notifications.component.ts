import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  Node as ModelNode,
  NotificationDefinition,
  NotificationDefinitionProfiles,
  NotificationDefinitionUsers,
  NotificationService,
  Profile,
  User,
} from 'app/core/generated/circabc';
import {
  AuthConfig,
  NotifDef,
} from 'app/shared/add-notifications/notification-definition-model';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-notifications',
  templateUrl: './add-notifications.component.html',
  styleUrls: ['./add-notifications.component.scss'],
  preserveWhitespaces: true,
})
export class AddNotificationsComponent implements OnInit {
  @Input()
  public showModal = false;
  @Input()
  node!: ModelNode;
  @Input()
  ig!: string;
  @Output()
  readonly finished = new EventEmitter<ActionEmitterResult>();

  public adding = false;
  public newNotifModel: { [authority: string]: NotifDef } = {};
  public newNotificationForm!: FormGroup;

  public configModel: AuthConfig[] = [];

  constructor(
    private fb: FormBuilder,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.newNotificationForm = this.fb.group(
      {
        invitedUsersOrProfiles: [],
      },
      {
        updateOn: 'change',
      }
    );
  }

  public cancelWizard() {
    this.showModal = false;
    this.newNotifModel = {};
    this.newNotificationForm.controls.invitedUsersOrProfiles.setValue(null);
    const res: ActionEmitterResult = {};
    res.type = ActionType.ADD_NOTIFICATIONS;
    res.result = ActionResult.CANCELED;
    this.configModel = [];
    this.finished.emit(res);
  }

  public async addNotifications() {
    this.adding = true;
    const res: ActionEmitterResult = {};
    res.type = ActionType.ADD_NOTIFICATIONS;

    try {
      const body: NotificationDefinition = {
        profiles: [],
        users: [],
      };

      for (const newPermKey of Object.keys(this.newNotifModel)) {
        const authTmp = this.newNotifModel[newPermKey].authority;
        if ((authTmp as User).userId) {
          const ndu: NotificationDefinitionUsers = {};
          ndu.notifications = this.newNotifModel[newPermKey].notifications;
          ndu.user = authTmp as User;
          if (body && body.users) {
            body.users.push(ndu);
          }
        } else {
          const ndp: NotificationDefinitionProfiles = {};
          ndp.notifications = this.newNotifModel[newPermKey].notifications;
          ndp.profile = authTmp as Profile;
          if (body && body.profiles) {
            body.profiles.push(ndp);
          }
        }
      }
      if (this.node.id) {
        await firstValueFrom(
          this.notificationService.postNotification(this.node.id, body)
        );
        this.newNotifModel = {};
        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      res.result = ActionResult.FAILED;
    }
    this.adding = false;
    this.finished.emit(res);
  }

  public assign() {
    this.configModel = [];
    for (const auth of this.newNotificationForm.value.invitedUsersOrProfiles) {
      const notifDef: NotifDef = { authority: auth, notifications: 'ALLOWED' };
      if (auth.userId) {
        this.newNotifModel[auth.userId] = notifDef;
      } else {
        this.newNotifModel[auth.groupName] = notifDef;
      }
    }
    this.newNotificationForm.reset();
    this.configModel = this.getNotifModel();
  }

  getAuthorityDisplay(authority: User | Profile): string {
    if ((authority as User).userId) {
      return `${(authority as User).firstname} ${(authority as User).lastname}`;
    } else {
      return (authority as Profile).name as string;
    }
  }

  public removeNotification(authorityKey: string) {
    this.configModel = [];
    // eslint-disable-next-line
    delete this.newNotifModel[authorityKey];
    this.configModel = this.getNotifModel();
  }

  public getNotifModel() {
    const result = [];

    for (const newPermKey of Object.keys(this.newNotifModel)) {
      result.push({
        authKey: newPermKey,
        authValue: this.newNotifModel[newPermKey],
      });
    }

    return result;
  }

  public isShowAddButton(): boolean {
    return Object.keys(this.newNotifModel).length > 0;
  }

  public switchValue(key: string) {
    this.configModel = [];
    if (this.newNotifModel[key].notifications === 'ALLOWED') {
      this.newNotifModel[key].notifications = 'DENIED';
    } else {
      this.newNotifModel[key].notifications = 'ALLOWED';
    }
    this.configModel = this.getNotifModel();
  }
}
