import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  directoryPermissionKeys,
  DirectoryPermissions,
} from 'app/core/evaluator/directory-permissions';
import {
  libraryPermissionKeys,
  LibraryPermissions,
} from 'app/core/evaluator/library-permissions';
import {
  newsGroupPermissionKeys,
  NewsgroupsPermissions,
} from 'app/core/evaluator/newsgroups-permissions';
import {
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
  PagedShares,
  PermissionDefinition,
  PermissionDefinitionPermissionsProfiles,
  PermissionService,
  Profile,
  ProfileService,
  Share,
  SpaceService,
  UserService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getErrorTranslation, getSuccessTranslation } from 'app/core/util';
import { ShareSpaceComponent } from 'app/group/library/manage-space-sharing/share-space/share-space.component';
import { InlineDeleteComponent } from 'app/shared/delete/inline-delete.component';
import { ModalDeleteComponent } from 'app/shared/delete/modal-delete/modal-delete.component';
import { IfRoleGEDirective } from 'app/shared/directives/ifrolege.directive';
import { HintComponent } from 'app/shared/hint/hint.component';
import { HorizontalLoaderComponent } from 'app/shared/loader/horizontal-loader.component';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { ReponsiveSubMenuComponent } from 'app/shared/reponsive-sub-menu/reponsive-sub-menu.component';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { firstValueFrom } from 'rxjs';
import { AddPermissionsComponent } from './add/add-permissions.component';
import { DropdownComponent } from './dropdown/dropdown.component';

@Component({
  selector: 'cbc-permissions',
  templateUrl: './permissions.component.html',
  styleUrl: './permissions.component.scss',
  preserveWhitespaces: true,
  imports: [
    HorizontalLoaderComponent,
    ReponsiveSubMenuComponent,
    RouterLink,
    IfRoleGEDirective,
    DropdownComponent,
    HintComponent,
    SpinnerComponent,
    InlineDeleteComponent,
    AddPermissionsComponent,
    ShareSpaceComponent,
    ModalDeleteComponent,
    I18nPipe,
    TranslocoModule,
  ],
})
export class PermissionsComponent implements OnInit {
  public loading = false;
  public perms!: PermissionDefinition;
  public currentNode!: ModelNode;
  public processing = false;
  public deletingUserPermissions = false;
  public deletingProfilePermissions = false;
  public showAddModal = false;
  public currentIgId!: string;
  public currentIg!: InterestGroup;
  public from!: string;
  public nodeId!: string;

  public showShareSpaceModal = false;
  public showModalDelete = false;
  public shares!: Share[];
  public igId!: string | undefined;
  public currentPermission!: string | undefined;

  public currentItem = '';
  public currentItemKind = '';

  public profilePermissionMap!: { [key: string]: string };
  public userPermissionMap!: { [key: string]: string };
  public profiles: Profile[] = [];
  public libAdmin: string[] = [];
  public ownerName = '';

  constructor(
    private permissionService: PermissionService,
    private uiMessageService: UiMessageService,
    private translateService: TranslocoService,
    private route: ActivatedRoute,
    private nodesService: NodesService,
    private spaceService: SpaceService,
    private groupService: InterestGroupService,
    private router: Router,
    private profileService: ProfileService,
    private userService: UserService
  ) {}

  async ngOnInit() {
    this.route.params.subscribe(async (params) => {
      this.currentIgId = params.id;
      await this.loadNode(params);
      await this.loadProfiles(params);
      await this.getOwnerName();
    });

    this.route.queryParams.subscribe((params) => {
      if (params.from) {
        this.from = params.from;
      }
    });
  }

  private async loadProfiles(params: { [key: string]: string }) {
    this.loading = true;
    if (params.id) {
      try {
        this.profiles = await firstValueFrom(
          this.profileService.getProfiles(params.id)
        );
      } catch (_error) {
        this.profiles = [];
      }

      this.libAdmin = [];
      this.profiles.forEach((item) => {
        if (item.permissions) {
          if (item.permissions.library === 'LibAdmin') {
            this.libAdmin.push(item.name as string);
          }
        }
      });
    }
    this.loading = false;
  }

  public async loadNode(params: { [id: string]: string }) {
    this.loading = true;
    this.nodeId = params.nodeId;
    if (this.nodeId) {
      this.currentNode = await firstValueFrom(
        this.nodesService.getNode(this.nodeId)
      );
      this.perms = await firstValueFrom(
        this.permissionService.getPermissions(this.nodeId)
      );

      this.prepareUserFilteredPermissions();
      this.prepareProfileFilteredPermissions();

      if (this.isFolder()) {
        await this.loadShares();
      }
    }
    if (this.currentIgId) {
      this.currentIg = await firstValueFrom(
        this.groupService.getInterestGroup(this.currentIgId)
      );
    }

    this.loading = false;
  }

  public shouldDisablePermissionSetting(): boolean {
    if (this.currentIg === undefined) {
      return false;
    }

    // Find the key name for the DirNoAccess value
    const dirNoAccessKey = directoryPermissionKeys.find(
      (key) =>
        DirectoryPermissions[key as keyof typeof DirectoryPermissions] ===
        DirectoryPermissions.DirNoAccess
    );

    // Find the key name for the LibManageOwn value
    const libManageOwnKey = libraryPermissionKeys.find(
      (key) =>
        LibraryPermissions[key as keyof typeof LibraryPermissions] ===
        LibraryPermissions.LibManageOwn
    );

    // Find the key name for the NwsPost value
    const nwsPostKey = newsGroupPermissionKeys.find(
      (key) =>
        NewsgroupsPermissions[key as keyof typeof NewsgroupsPermissions] ===
        NewsgroupsPermissions.NwsPost
    );

    return (
      this.currentIg.permissions.directory === dirNoAccessKey &&
      (this.currentIg.permissions.library === libManageOwnKey ||
        this.currentIg.permissions.newsgroup === nwsPostKey)
    );
  }

  public async toggleInheritance() {
    const body: PermissionDefinition = {
      inherited: !this.perms.inherited,
      permissions: this.perms.permissions,
    };

    this.processing = true;
    if (this.currentNode?.id) {
      this.perms = await firstValueFrom(
        this.permissionService.putPermission(this.currentNode.id, body)
      );

      this.prepareUserFilteredPermissions();
      this.prepareProfileFilteredPermissions();
    }
    this.processing = false;
    this.setPermissionMsg();
  }

  public setPermissionMsg() {
    const text = this.translateService.translate(
      getSuccessTranslation(ActionType.SET_PERMISSION)
    );
    if (text) {
      this.uiMessageService.addSuccessMessage(text, true);
    }
  }

  async addPermissionFinished(result: ActionEmitterResult) {
    if (result.result === ActionResult.CANCELED) {
      this.showAddModal = false;
    } else if (result.result === ActionResult.SUCCEED) {
      this.showAddModal = false;

      if (this.currentNode.id) {
        this.perms = await firstValueFrom(
          this.permissionService.getPermissions(this.currentNode.id)
        );

        this.prepareUserFilteredPermissions();
        this.prepareProfileFilteredPermissions();
      }
    }
  }

  prepareDeletePermissionEntry(item: string, itemKind: string) {
    this.currentItem = item;
    this.currentItemKind = itemKind;
    this.showModalDelete = true;
  }

  async deletePreparedPermissionEntry() {
    try {
      if (this.currentItemKind === 'user') {
        this.deletingUserPermissions = true;
        await this.deleteUserPermissions(this.currentItem);
        this.deletingUserPermissions = false;
      } else if (this.currentItemKind === 'group') {
        this.deletingProfilePermissions = true;
        await this.deleteProfilePermissions(this.currentItem);
        this.deletingProfilePermissions = false;
      }

      const text = this.translateService.translate(
        getSuccessTranslation(ActionType.DELETE_PERMISSION)
      );
      this.uiMessageService.addSuccessMessage(text, true);
    } catch (_error) {
      const text = this.translateService.translate(
        getErrorTranslation(ActionType.DELETE_PERMISSION)
      );
      this.uiMessageService.addErrorMessage(text, true);
    }

    this.currentItem = '';
    this.currentItemKind = '';
    this.showModalDelete = false;
  }

  cancelPreparedPermissionEntry() {
    this.currentItem = '';
    this.currentItemKind = '';
    this.showModalDelete = false;
  }

  async deletePermission(authority: string, permission: string) {
    if (this.currentNode.id) {
      await firstValueFrom(
        this.permissionService.deletePermission(
          this.currentNode.id,
          authority,
          permission
        )
      );
      this.perms = await firstValueFrom(
        this.permissionService.getPermissions(this.currentNode.id)
      );

      this.prepareUserFilteredPermissions();
      this.prepareProfileFilteredPermissions();
    }
  }

  async deleteUserPermissions(authority: string) {
    if (this.currentNode.id) {
      const permissions = this.splitUserPermissions(authority);

      for (const permission of permissions) {
        await firstValueFrom(
          this.permissionService.deletePermission(
            this.currentNode.id,
            authority,
            permission
          )
        );
      }

      this.perms = await firstValueFrom(
        this.permissionService.getPermissions(this.currentNode.id)
      );

      this.prepareUserFilteredPermissions();
    }
  }

  async deleteProfilePermissions(authority: string) {
    if (this.currentNode.id) {
      // DIGITCIRCABC-4766 DOT in URL is breaking alfresco webscript. It is rejected by webscript
      const encodeedAuthority = authority.replace('.', '%2E');

      await firstValueFrom(
        this.permissionService.clearPermissions(
          this.currentNode.id,
          encodeedAuthority
        )
      );

      this.perms = await firstValueFrom(
        this.permissionService.getPermissions(this.currentNode.id)
      );

      this.prepareProfileFilteredPermissions();
    }
  }

  isProfilePermissionDeletable(
    perm: PermissionDefinitionPermissionsProfiles
  ): boolean {
    return (
      perm.profile !== undefined &&
      perm.profile.groupName !== 'GROUP_EVERYONE' &&
      !this.perms.inherited
    );
  }

  public hasUserPermissionDeletable(userId: string): boolean {
    if (this.perms.permissions?.users) {
      const result = this.perms.permissions.users.find(
        (userEntry) =>
          userEntry?.user !== undefined &&
          userEntry.user.userId === userId &&
          userEntry.inherited !== true
      );

      if (result !== undefined) {
        return true;
      }
    }

    return false;
  }

  public hasProfilePermissionDeletable(groupName: string): boolean {
    if (this.perms.permissions?.profiles) {
      const result = this.perms.permissions.profiles.find(
        (profileEntry) =>
          profileEntry?.profile !== undefined &&
          profileEntry.profile.groupName === groupName &&
          profileEntry.inherited !== true
      );

      if (result !== undefined) {
        return true;
      }
    }

    return false;
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
    const nodeId = this.isFile()
      ? this.currentNode.parentId
      : this.currentNode.id;
    this.router.navigate(['../../library', nodeId], {
      relativeTo: this.route,
    });
  }

  public getNodeId() {
    if (this.currentNode) {
      return this.currentNode.id;
    }

    return '';
  }

  // used to ignore default permissions of Visibility or Category access / admin
  prepareProfileFilteredPermissions() {
    this.profilePermissionMap = {};
    const modelProfiles = this.perms.permissions.profiles;

    if (modelProfiles !== undefined) {
      for (const perm of modelProfiles) {
        if (
          perm.permission &&
          perm.permission.indexOf('Visibility') === -1 &&
          perm.permission.indexOf('Category') === -1 &&
          perm.permission.indexOf('Dir') === -1
        ) {
          const permType = perm;

          if (
            permType?.profile &&
            permType.permission &&
            permType.profile.groupName
          ) {
            if (
              Object.keys(this.profilePermissionMap).indexOf(
                permType.profile.groupName
              ) < 0
            ) {
              this.profilePermissionMap[permType.profile.groupName] =
                permType.permission;
            } else {
              this.profilePermissionMap[permType.profile.groupName] = `${
                this.profilePermissionMap[permType.profile.groupName]
              }-${permType.permission}`;
            }
          }
        }
      }
    }
  }

  // used to ignore default permissions of Visibility or Category access / admin
  prepareUserFilteredPermissions() {
    this.userPermissionMap = {};
    const modelUsers = this.perms.permissions.users;

    if (modelUsers !== undefined) {
      for (const perm of modelUsers) {
        if (
          perm.permission &&
          perm.permission.indexOf('Visibility') === -1 &&
          perm.permission.indexOf('Category') === -1 &&
          perm.permission.indexOf('Dir') === -1
        ) {
          const permType = perm;

          if (permType?.user && permType.permission && permType.user.userId) {
            if (
              Object.keys(this.userPermissionMap).indexOf(
                permType.user.userId
              ) < 0
            ) {
              this.userPermissionMap[permType.user.userId] =
                permType.permission;
            } else {
              this.userPermissionMap[permType.user.userId] = `${
                this.userPermissionMap[permType.user.userId]
              }-${permType.permission}`;
            }
          }
        }
      }
    }
  }

  public isFile(): boolean {
    let result = false;
    if (this.currentNode?.type !== undefined) {
      result = this.currentNode.type.indexOf('folder') === -1;
    }
    return result;
  }

  public isFolder(): boolean {
    let result = false;
    if (this.currentNode?.type !== undefined) {
      result = this.currentNode.type.indexOf('folder') !== -1;
    }
    return result;
  }

  public isLink(): boolean {
    if (this.currentNode?.type && this.currentNode.properties) {
      return this.currentNode.properties.isUrl === 'true';
    }
    return false;
  }

  isSharedSpaceLink(): boolean {
    if (this.currentNode?.type) {
      return this.currentNode.type.indexOf('folderlink') !== -1;
    }
    return false;
  }

  // shared spaces

  private async loadShares() {
    if (this.nodeId !== undefined && this.isFolder()) {
      this.loading = true;
      const result: PagedShares = await firstValueFrom(
        this.spaceService.getShareSpaces(this.nodeId, 0, 1)
      );
      this.shares = result.data;
      this.loading = false;
    }
  }

  public async removeShare(share: Share) {
    if (this.isFolder()) {
      await firstValueFrom(
        this.spaceService.deleteShareSpace(this.nodeId, share.igId as string)
      );
      await this.loadShares();
    }
  }

  public hasShares(): boolean {
    return this.shares !== undefined && this.shares.length > 0;
  }

  public async refreshShares(result: ActionEmitterResult) {
    if (this.isFolder()) {
      this.igId = undefined;
      this.currentPermission = undefined;
      this.showShareSpaceModal = false;
      if (result.result === ActionResult.SUCCEED) {
        await this.loadShares();
      }
    }
  }

  public editSharePermission(share: Share) {
    if (this.isFolder()) {
      this.igId = share.igId;
      this.currentPermission = share.permission;
      this.showShareSpaceModal = true;
    }
  }

  public addPermission(isShare: boolean): void {
    if (this.isFolder()) {
      if (isShare) {
        this.showShareSpaceModal = true;
      } else {
        this.showAddModal = true;
      }
    }
  }

  public showAddPermissionModal() {
    if (!this.shouldDisablePermissionSetting()) {
      this.showAddModal = true;
    }
  }

  public getMapKeys(obj: {}) {
    return Object.keys(obj);
  }

  public getProfileTitle(groupName: string) {
    if (this.perms.permissions?.profiles) {
      const result = this.perms.permissions.profiles.find(
        (profileEntry) =>
          profileEntry?.profile !== undefined &&
          profileEntry.profile.groupName === groupName
      );

      if (result?.profile) {
        return result.profile.title;
      }
      return undefined;
    }
    return undefined;
  }

  public getDisplayUser(userid: string) {
    if (this.perms.permissions?.users) {
      const result = this.perms.permissions.users.find(
        (userEntry) =>
          userEntry?.user !== undefined && userEntry.user.userId === userid
      );

      if (result?.user) {
        return `${result.user.firstname} ${result.user.lastname}`;
      }
      return undefined;
    }
    return undefined;
  }

  public splitProfilePermissions(groupName: string) {
    return this.splitPermissions(this.profilePermissionMap[groupName]);
  }

  public splitUserPermissions(userId: string) {
    return this.splitPermissions(this.userPermissionMap[userId]);
  }

  public splitPermissions(permissions: string) {
    if (permissions.indexOf('-') > -1) {
      return permissions.split('-');
    }
    return [permissions];
  }

  isForumNewsgroupsName(name: string, type: string) {
    return name === 'Newsgroups' && type.includes('forums');
  }

  public async getOwnerName() {
    if (this.currentNode.properties) {
      const user = await firstValueFrom(
        this.userService.getUser(this.currentNode.properties.owner)
      );
      this.ownerName = `${user.firstname}  ${user.lastname}`;
    }
  }
}
