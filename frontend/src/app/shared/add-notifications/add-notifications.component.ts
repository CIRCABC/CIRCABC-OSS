import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  OnInit,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
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
} from 'app/core/generated/circabc/';
import {
  AuthConfig,
  NotifDef,
} from 'app/shared/add-notifications/notification-definition-model';
import { InlineSelectComponent } from 'app/shared/inline-select/inline-select.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { UsersPickerComponent } from 'app/shared/users/users-picker.component';

/**
 * Modal wizard component that lets a user configure and create notification
 * definitions for a given node.
 *
 * The component renders a modal (via {@link ModalComponent}) containing a
 * reactive form. Through that form the user picks a set of users and/or
 * profiles (using {@link UsersPickerComponent}) and toggles, per authority,
 * whether notifications are `ALLOWED` or `DENIED` (using
 * {@link InlineSelectComponent}). On confirmation the collected configuration
 * is submitted to the backend through {@link NotificationService}.
 *
 * Its main collaborators are the Angular {@link FormBuilder} (for the reactive
 * form) and the generated {@link NotificationService} (for persisting the
 * notification definition).
 */
@Component({
  selector: 'cbc-add-notifications',
  templateUrl: './add-notifications.component.html',
  styleUrl: './add-notifications.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    UsersPickerComponent,
    InlineSelectComponent,
    TranslocoModule,
  ],
})
export class AddNotificationsComponent implements OnInit {
  /** Reactive forms builder used to create the notification wizard form. */
  private readonly fb = inject(FormBuilder);
  /** Generated API service used to persist notification definitions. */
  private readonly notificationService = inject(NotificationService);

  /**
   * Two-way bound model controlling the visibility of the modal wizard.
   * `true` shows the modal, `false` hides it.
   */
  public showModal = model(false);
  /** Required input: the node the notification definitions are created for. */
  readonly node = input.required<ModelNode>();
  /** Required input: the identifier of the interest group context. */
  readonly ig = input.required<string>();
  /**
   * Emitted when the wizard finishes, either after a cancellation or after an
   * attempt to add notifications, carrying the resulting {@link ActionEmitterResult}.
   */
  readonly finished = output<ActionEmitterResult>();

  /** Flag indicating that a notification-creation request is in progress. */
  public readonly adding = signal(false);
  /**
   * Map of the notification definitions being configured, keyed by authority
   * identifier (user id or group name).
   */
  public newNotifModel: { [authority: string]: NotifDef } = {};
  /** Reactive form backing the user/profile picker of the wizard. */
  public newNotificationForm!: FormGroup;

  /**
   * View model derived from {@link newNotifModel}, used to render the list of
   * configured authorities and their notification state.
   */
  public configModel: AuthConfig[] = [];

  /**
   * Angular lifecycle hook that initializes the reactive form used by the
   * wizard.
   */
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

  /**
   * Cancels the wizard: hides the modal, clears the current configuration and
   * form state, and emits a `CANCELED` {@link ActionEmitterResult} of type
   * {@link ActionType.ADD_NOTIFICATIONS}.
   */
  public cancelWizard() {
    this.showModal.set(false);
    this.newNotifModel = {};
    this.newNotificationForm.controls.invitedUsersOrProfiles.setValue(null);
    const res: ActionEmitterResult = {};
    res.type = ActionType.ADD_NOTIFICATIONS;
    res.result = ActionResult.CANCELED;
    this.configModel = [];
    this.finished.emit(res);
  }

  /**
   * Builds a {@link NotificationDefinition} from {@link newNotifModel},
   * separating user and profile entries, and posts it to the backend for the
   * current node via {@link NotificationService.postNotification}.
   *
   * On success the configuration is cleared and a `SUCCEED` result is emitted;
   * on failure a `FAILED` result is emitted. In all cases a
   * {@link ActionEmitterResult} of type {@link ActionType.ADD_NOTIFICATIONS}
   * is emitted through {@link finished}.
   *
   * @returns A promise that resolves once the request completes and the result
   * has been emitted.
   */
  public async addNotifications() {
    this.adding.set(true);
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
          if (body?.users) {
            body.users.push(ndu);
          }
        } else {
          const ndp: NotificationDefinitionProfiles = {};
          ndp.notifications = this.newNotifModel[newPermKey].notifications;
          ndp.profile = authTmp as Profile;
          if (body?.profiles) {
            body.profiles.push(ndp);
          }
        }
      }
      const node = this.node();
      if (node.id) {
        await this.notificationService.postNotificationAsync({
          id: node.id,
          notificationDefinition: body,
        });
        this.newNotifModel = {};
        res.result = ActionResult.SUCCEED;
      }
    } catch (error) {
      console.error(error);
      res.result = ActionResult.FAILED;
    }
    this.adding.set(false);
    this.finished.emit(res);
  }

  /**
   * Adds the users/profiles currently selected in the form to
   * {@link newNotifModel} (defaulting each to `ALLOWED`), resets the picker
   * form, and refreshes {@link configModel} for rendering.
   */
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

  /**
   * Computes the display label for an authority.
   *
   * @param authority The authority to describe, either a {@link User} or a
   * {@link Profile}.
   * @returns The user's full name (`firstname lastname`) when the authority is
   * a user, otherwise the profile's name.
   */
  getAuthorityDisplay(authority: User | Profile): string {
    if ((authority as User).userId) {
      return `${(authority as User).firstname} ${(authority as User).lastname}`;
    }
    return (authority as Profile).name as string;
  }

  /**
   * Removes an authority from the configured notification definitions and
   * refreshes {@link configModel}.
   *
   * @param authorityKey The key (user id or group name) of the authority to
   * remove from {@link newNotifModel}.
   */
  public removeNotification(authorityKey: string) {
    this.configModel = [];
    delete this.newNotifModel[authorityKey];
    this.configModel = this.getNotifModel();
  }

  /**
   * Projects {@link newNotifModel} into an array of `{ authKey, authValue }`
   * entries suitable for iteration in the template.
   *
   * @returns The list of configured authorities paired with their
   * {@link NotifDef} value.
   */
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

  /**
   * Indicates whether the "Add" action should be shown.
   *
   * @returns `true` when at least one authority has been configured in
   * {@link newNotifModel}, otherwise `false`.
   */
  public isShowAddButton(): boolean {
    return Object.keys(this.newNotifModel).length > 0;
  }

  /**
   * Toggles the notification state of a configured authority between
   * `ALLOWED` and `DENIED`, then refreshes {@link configModel}.
   *
   * @param key The key (user id or group name) of the authority whose
   * notification state should be switched.
   */
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
