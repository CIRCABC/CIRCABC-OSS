import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  linkedSignal,
  model,
  output,
  resource,
  signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { TranslocoModule } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  Node as ModelNode,
  PermissionDefinition,
  PermissionDefinitionPermissionsProfiles,
  PermissionDefinitionPermissionsUsers,
  PermissionService,
  Profile,
  User,
} from 'app/core/generated/circabc';
import {
  AuthConfig,
  PermDef,
} from 'app/group/permissions/add/permission-definition-model';
import { InlineSelectComponent } from 'app/shared/inline-select/inline-select.component';
import { ModalComponent } from 'app/shared/modal/modal.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { UsersPickerComponent } from 'app/shared/users/users-picker.component';

/**
 * Modal-based wizard component that lets an administrator grant permissions
 * on a given node to one or more authorities (individual users or access
 * profiles) within an interest group.
 *
 * The component renders a {@link ModalComponent} containing a
 * {@link UsersPickerComponent} for selecting the authorities to invite and,
 * per selected authority, an {@link InlineSelectComponent} for choosing the
 * permission level to assign. The set of assignable permission levels is
 * derived from the target node's service and type (library content, library
 * folder or newsgroup) via the {@link PermissionEvaluatorService}.
 *
 * On confirmation the accumulated assignments are persisted through the
 * {@link PermissionService} and the outcome is reported back to the parent
 * through the {@link AddPermissionsComponent.finished} output.
 *
 * Key collaborators: {@link PermissionService} (read/write permissions),
 * {@link PermissionEvaluatorService} (available permission sets),
 * {@link I18nPipe} (localised profile titles) and Angular's
 * {@link FormBuilder} (reactive form backing the picker).
 */
@Component({
  selector: 'cbc-add-permissions',
  templateUrl: './add-permissions.component.html',
  styleUrl: './add-permissions.component.scss',
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
export class AddPermissionsComponent {
  /** API client used to read the current permissions and persist new ones. */
  private readonly permissionService = inject(PermissionService);
  /** Factory used to build the reactive form backing the users/profiles picker. */
  private readonly fb = inject(FormBuilder);
  /** Resolves the set of permission levels applicable to the target node. */
  private readonly permEvalService = inject(PermissionEvaluatorService);
  /** Pipe used to resolve the localised display title of an access profile. */
  private readonly i18nPipe = inject(I18nPipe);

  /** Required input: the node whose permissions are being edited. */
  readonly node = input.required<ModelNode>();
  /** Required input: the identifier of the interest group the node belongs to. */
  readonly ig = input.required<string>();

  /** Two-way model controlling the visibility of the wizard modal. */
  readonly showModal = model(false);
  /**
   * Whether the newly created permissions should mark the node as inheriting
   * permissions from its parent. Defaults to `true`.
   */
  readonly inherited = input(true);
  /**
   * Emitted when the wizard completes (either cancelled or after an attempt to
   * add permissions), carrying the resulting {@link ActionEmitterResult}.
   */
  readonly finished = output<ActionEmitterResult>();

  /**
   * Loads the current {@link PermissionDefinition} for the target node from
   * the backend. Idle (loader not invoked) while the node has no id yet.
   */
  private readonly permsResource = resource({
    params: () => this.node().id || undefined,
    loader: ({ params: id }) =>
      this.permissionService.getPermissionsAsync({ id }),
  });

  /**
   * The current permission definition loaded for / persisted on the node.
   * Derived from {@link permsResource} but locally writable so
   * {@link addPermissions} can update it with the freshly persisted
   * definition after a successful save.
   */
  public readonly perms = linkedSignal(() => this.permsResource.value());

  /** Reactive form holding the users/profiles selected in the picker. */
  public readonly newPermissionsForm: FormGroup = this.fb.group(
    {
      invitedUsersOrProfiles: [],
    },
    {
      updateOn: 'change',
    }
  );
  /** Flag indicating an add-permissions request is in progress. */
  public readonly adding = signal(false);
  /**
   * Pending assignments keyed by authority identifier (user id or group name),
   * each mapping to the {@link PermDef} describing the authority and chosen
   * permission level.
   */
  public newPermsModel: { [authority: string]: PermDef } = {};
  /** View model derived from {@link newPermsModel} used to render the list of pending assignments. */
  public configModel: AuthConfig[] = [];
  /** Cached list of permission levels applicable to the current node. */
  public permissionCache: string[] = [];
  /** Whether the contextual help layout is currently shown. */
  public showHelpLayout = false;
  /** Available permission level options presented to the user, derived from the target node. */
  public readonly options = computed(() => this.determineNodePermissionSet());

  /**
   * Cancels the wizard without persisting any changes. Clears the picker
   * selection and pending assignments and emits a
   * {@link ActionResult.CANCELED} result through {@link finished}.
   */
  cancelWizard() {
    const res: ActionEmitterResult = {};
    res.type = ActionType.ADD_PERMISSIONS;
    res.result = ActionResult.CANCELED;
    this.newPermissionsForm.controls.invitedUsersOrProfiles.setValue(null);
    this.newPermsModel = {};
    this.finished.emit(res);
  }

  /**
   * Determines and caches the set of permission levels applicable to the
   * current node based on its service (library vs. newsgroup) and, for the
   * library, whether the node is content or a folder. The result is cached in
   * {@link permissionCache} so it is only computed once.
   *
   * @returns The list of applicable permission level identifiers.
   */
  determineNodePermissionSet(): string[] {
    const node = this.node();
    if (node.permissions && this.permissionCache.length === 0) {
      const isInLibrary = node.service === 'library';

      if (isInLibrary) {
        if (this.isContent()) {
          this.permissionCache =
            this.permEvalService.getLibraryContentPermissions();
        } else {
          this.permissionCache =
            this.permEvalService.getLibraryFolderPermissions();
        }
      } else {
        this.permissionCache = this.permEvalService.geNewsgroupsPermissions();
      }
    }
    return this.permissionCache;
  }

  /**
   * Indicates whether the target node represents a content item (as opposed to
   * a folder), based on its Alfresco type ending in `}content`.
   *
   * @returns `true` if the node is a content item, otherwise `false`.
   */
  isContent(): boolean {
    return this.node().type?.endsWith('}content') === true;
  }

  /**
   * Adds the authorities currently selected in the picker to the pending
   * assignments ({@link newPermsModel}), defaulting each to the first
   * applicable permission level, then resets the picker and refreshes the
   * {@link configModel} view model.
   */
  assign() {
    this.configModel = [];
    for (const auth of this.newPermissionsForm.value.invitedUsersOrProfiles) {
      const permDef: PermDef = {
        authority: auth,
        permission: this.determineNodePermissionSet()[0],
      };
      if (auth.userId) {
        this.newPermsModel[auth.userId] = permDef;
      } else {
        this.newPermsModel[auth.groupName] = permDef;
      }
    }
    this.newPermissionsForm.reset();
    this.configModel = this.getPermModel();
  }

  /**
   * Builds the {@link AuthConfig} view model list from the pending assignments
   * held in {@link newPermsModel}.
   *
   * @returns An array of `{ authKey, authValue }` entries, one per pending
   * assignment.
   */
  getPermModel() {
    const result = [];

    for (const newPermKey of Object.keys(this.newPermsModel)) {
      result.push({
        authKey: newPermKey,
        authValue: this.newPermsModel[newPermKey],
      });
    }

    return result;
  }

  /**
   * Computes the human-readable display label for an authority. For users this
   * is their first and last name; for profiles it is the localised title, or
   * the profile name as a fallback.
   *
   * @param authority The user or profile to render.
   * @returns The display string for the authority.
   */
  getAuthorityDisplay(authority: User | Profile): string {
    if ((authority as User).userId) {
      return `${(authority as User).firstname}  ${
        (authority as User).lastname
      }`;
    }
    const profileTitle = this.i18nPipe.transform((authority as Profile).title);
    if (profileTitle !== '' && profileTitle !== undefined) {
      return profileTitle;
    }
    return (authority as Profile).name as string;
  }

  /**
   * Removes a pending assignment identified by its authority key and refreshes
   * the {@link configModel} view model.
   *
   * @param key The authority key (user id or group name) to remove.
   */
  removePerm(key: string) {
    this.configModel = [];
    delete this.newPermsModel[key];
    this.configModel = this.getPermModel();
  }

  /**
   * Persists all pending assignments to the node. Splits the assignments into
   * user- and profile-based permission entries, builds the
   * {@link PermissionDefinition} body (respecting the {@link inherited} input)
   * and submits it via {@link PermissionService.putPermission}. On success the
   * pending state is cleared. Regardless of outcome, a corresponding
   * {@link ActionEmitterResult} is emitted through {@link finished}.
   *
   * Failures during persistence are caught and reported as
   * {@link ActionResult.FAILED} rather than propagated.
   *
   * @returns A promise that resolves once the operation and result emission
   * complete.
   */
  async addPermissions() {
    this.adding.set(true);

    const res: ActionEmitterResult = {};
    res.type = ActionType.ADD_PERMISSIONS;

    try {
      const body: PermissionDefinition = {
        inherited: this.inherited(),
        permissions: {
          profiles: [],
          users: [],
        },
      };

      for (const newPermKey of Object.keys(this.newPermsModel)) {
        const authTmp = this.newPermsModel[newPermKey].authority;
        if ((authTmp as User).userId) {
          const pdpu: PermissionDefinitionPermissionsUsers = {};
          pdpu.permission = this.newPermsModel[newPermKey].permission;
          pdpu.user = authTmp as User;
          if (body?.permissions?.users) {
            body.permissions.users.push(pdpu);
          }
        } else {
          const pdpp: PermissionDefinitionPermissionsProfiles = {};
          pdpp.permission = this.newPermsModel[newPermKey].permission;
          pdpp.profile = authTmp as Profile;
          if (body?.permissions?.profiles) {
            body.permissions.profiles.push(pdpp);
          }
        }
      }
      const node = this.node();
      if (node.id) {
        this.perms.set(
          await this.permissionService.putPermissionAsync({
            id: node.id,
            permissionDefinition: body,
          })
        );
        res.result = ActionResult.SUCCEED;
        this.newPermissionsForm.reset();
        this.newPermsModel = {};
      }
    } catch (error) {
      console.error(error);
      res.result = ActionResult.FAILED;
    }
    this.adding.set(false);
    this.finished.emit(res);
  }

  /**
   * Indicates whether the "add" action button should be shown, i.e. whether at
   * least one pending assignment exists.
   *
   * @returns `true` when there are pending assignments, otherwise `false`.
   */
  public isShowAddButton() {
    return Object.keys(this.newPermsModel).length > 0;
  }

  /**
   * Updates the permission level of an existing pending assignment and
   * refreshes the {@link configModel} view model.
   *
   * @param authKey The authority key (user id or group name) of the assignment.
   * @param value The newly selected permission level.
   */
  public updateModel(authKey: string, value: string) {
    this.configModel = [];
    const perm = this.newPermsModel[authKey];
    perm.permission = value;
    this.newPermsModel[authKey] = perm;
    this.configModel = this.getPermModel();
  }

  /**
   * Toggles the visibility of the contextual help layout.
   */
  public toggleHelp() {
    this.showHelpLayout = !this.showHelpLayout;
  }
}
