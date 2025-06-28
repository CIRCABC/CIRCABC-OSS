import {
  animate,
  state,
  style,
  transition,
  trigger,
} from '@angular/animations';
import { Component, Input, OnInit, output, input } from '@angular/core';
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
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-add-permissions',
  templateUrl: './add-permissions.component.html',
  styleUrl: './add-permissions.component.scss',
  preserveWhitespaces: true,
  animations: [
    trigger('helpLayoutVisibility', [
      state(
        'inactive',
        style({
          visibility: 'hidden',
          opacity: 0,
        })
      ),
      state(
        'active',
        style({
          visibility: 'visible',
          opacity: 1,
        })
      ),
      transition('inactive => active', animate('300ms ease-in')),
      transition('active => inactive', animate('100ms ease-out')),
    ]),
  ],
  imports: [
    ModalComponent,
    ReactiveFormsModule,
    UsersPickerComponent,
    InlineSelectComponent,
    TranslocoModule,
  ],
})
export class AddPermissionsComponent implements OnInit {
  readonly node = input.required<ModelNode>();
  readonly ig = input.required<string>();
  // TODO: Skipped for migration because:
  //  Your application code writes to the input. This prevents migration.
  @Input()
  showModal = false;
  readonly inherited = input(true);
  readonly finished = output<ActionEmitterResult>();

  public perms!: PermissionDefinition;
  public newPermissionsForm!: FormGroup;
  public adding = false;
  public newPermsModel: { [authority: string]: PermDef } = {};
  public configModel: AuthConfig[] = [];
  public permissionCache: string[] = [];
  public showHelpLayout = false;
  public options: string[] = [];

  constructor(
    private permissionService: PermissionService,
    private fb: FormBuilder,
    private permEvalService: PermissionEvaluatorService,
    private i18nPipe: I18nPipe
  ) {}

  async ngOnInit() {
    this.permissionCache = [];
    this.newPermissionsForm = this.fb.group(
      {
        invitedUsersOrProfiles: [],
      },
      {
        updateOn: 'change',
      }
    );

    this.configModel = [];
    this.options = this.determineNodePermissionSet();

    const node = this.node();
    if (node?.id) {
      this.perms = await firstValueFrom(
        this.permissionService.getPermissions(node.id)
      );
    }
  }

  cancelWizard() {
    const res: ActionEmitterResult = {};
    res.type = ActionType.ADD_PERMISSIONS;
    res.result = ActionResult.CANCELED;
    this.newPermissionsForm.controls.invitedUsersOrProfiles.setValue(null);
    this.newPermsModel = {};
    this.finished.emit(res);
  }

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

  isContent(): boolean {
    return this.node().type?.endsWith('}content') === true;
  }

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

  removePerm(key: string) {
    // eslint-disable-next-line
    this.configModel = [];
    delete this.newPermsModel[key];
    this.configModel = this.getPermModel();
  }

  async addPermissions() {
    this.adding = true;

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
        this.perms = await firstValueFrom(
          this.permissionService.putPermission(node.id, body)
        );
        res.result = ActionResult.SUCCEED;
        this.newPermissionsForm.reset();
        this.newPermsModel = {};
      }
    } catch (_error) {
      res.result = ActionResult.FAILED;
    }
    this.adding = false;
    this.finished.emit(res);
  }

  public isShowAddButton() {
    return Object.keys(this.newPermsModel).length > 0;
  }

  public updateModel(authKey: string, value: string) {
    this.configModel = [];
    const perm = this.newPermsModel[authKey];
    perm.permission = value;
    this.newPermsModel[authKey] = perm;
    this.configModel = this.getPermModel();
  }

  public toggleHelp() {
    this.showHelpLayout = !this.showHelpLayout;
  }
}
