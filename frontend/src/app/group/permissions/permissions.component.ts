import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  resource,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import {
  DirectoryPermissions,
  directoryPermissionKeys,
} from 'app/core/evaluator/directory-permissions';
import {
  LibraryPermissions,
  libraryPermissionKeys,
} from 'app/core/evaluator/library-permissions';
import {
  NewsgroupsPermissions,
  newsGroupPermissionKeys,
} from 'app/core/evaluator/newsgroups-permissions';
import {
  InterestGroupService,
  Node as ModelNode,
  NodesService,
  PermissionDefinition,
  PermissionDefinitionPermissionsProfiles,
  PermissionDefinitionPermissionsUsers,
  PermissionService,
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
import { AddPermissionsComponent } from './add/add-permissions.component';
import { DropdownComponent } from './dropdown/dropdown.component';

/**
 * Standalone Angular component (`cbc-permissions`) that renders the permission
 * management screen for a library/forum node within an interest group.
 *
 * The component displays the current node's permission definition — split into
 * user-level and profile (group) permissions — and lets administrators toggle
 * permission inheritance, add new permission entries, delete existing ones and
 * (for folders) manage shared-space relationships with other interest groups.
 *
 * The target node and interest group are resolved from the current route
 * parameters (`id` for the interest group, `nodeId` for the node). It
 * collaborates with several backend services to load and mutate state:
 * - {@link PermissionService} — read/write node permission definitions
 * - {@link NodesService} — load the current node
 * - {@link SpaceService} — manage shared spaces for folders
 * - {@link InterestGroupService} — load the current interest group
 * - {@link ProfileService} — load the group's access profiles
 * - {@link UserService} — resolve the node owner's display name
 * - {@link UiMessageService} — surface success/error notifications
 * - {@link TranslocoService} — translate notification messages
 */
@Component({
  selector: 'cbc-permissions',
  templateUrl: './permissions.component.html',
  styleUrl: './permissions.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
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
export class PermissionsComponent {
  private readonly permissionService = inject(PermissionService);
  private readonly uiMessageService = inject(UiMessageService);
  private readonly translateService = inject(TranslocoService);
  private readonly route = inject(ActivatedRoute);
  private readonly nodesService = inject(NodesService);
  private readonly spaceService = inject(SpaceService);
  private readonly groupService = inject(InterestGroupService);
  private readonly router = inject(Router);
  private readonly profileService = inject(ProfileService);
  private readonly userService = inject(UserService);

  /** The current route params, as a signal. */
  private readonly routeParams = toSignal(this.route.params);
  /** The current route query params, as a signal. */
  private readonly routeQueryParams = toSignal(this.route.queryParams);

  /** Identifier of the current interest group, taken from the route `id` param. */
  public readonly currentIgId = computed(() => this.routeParams()?.id ?? '');
  /** Identifier of the current node, taken from the route `nodeId` param. */
  public readonly nodeId = computed(() => this.routeParams()?.nodeId ?? '');
  /** Origin of the navigation (`library`, `forum` or `topic`), used by {@link goBack}. */
  public readonly from = computed(() => this.routeQueryParams()?.from);

  /**
   * Resource loading the current node, its permission definition and (for
   * folders) its shared spaces in one round-trip chain, keyed by
   * {@link nodeId}. Errors are logged and swallowed (no dedicated error UI
   * exists for this section), matching the previous behaviour.
   */
  private readonly nodeDataResource = resource({
    params: () => this.nodeId() || undefined,
    loader: async ({ params: id }) => {
      try {
        const currentNode = await this.nodesService.getNodeAsync({ id });
        const perms = await this.permissionService.getPermissionsAsync({
          id,
        });
        const shares = currentNode.type?.includes('folder')
          ? (
              await this.spaceService.getShareSpacesAsync({
                id,
                limit: 0,
                page: 1,
              })
            ).data
          : undefined;
        return { currentNode, perms, shares };
      } catch (error) {
        console.error('problem retrieving the node permissions', error);
        return undefined;
      }
    },
  });

  /** The node whose permissions are being managed. */
  public readonly currentNode = computed(
    () => this.nodeDataResource.value()?.currentNode as ModelNode
  );
  /** The permission definition (users, profiles and inheritance flag) for the current node. */
  public readonly perms = computed(
    () => this.nodeDataResource.value()?.perms as PermissionDefinition
  );
  /** Shared spaces (with other interest groups) for the current folder node. */
  public readonly shares = computed(
    () => this.nodeDataResource.value()?.shares ?? []
  );

  /**
   * Resource loading the current interest group, keyed by {@link currentIgId}.
   */
  private readonly currentIgResource = resource({
    params: () => this.currentIgId() || undefined,
    loader: ({ params: id }) => this.groupService.getInterestGroupAsync({ id }),
  });

  /** The current interest group, loaded from {@link InterestGroupService}. */
  public readonly currentIg = this.currentIgResource.value;

  /**
   * Resource loading the interest group's access profiles, keyed by
   * {@link currentIgId}. Errors are swallowed (no dedicated error UI exists
   * for this section) so the screen remains usable.
   */
  private readonly profilesResource = resource({
    params: () => this.currentIgId() || undefined,
    loader: async ({ params: id }) => {
      try {
        return await this.profileService.getProfilesAsync({ id });
      } catch (error) {
        console.error(error);
        return [];
      }
    },
  });

  /** Access profiles defined for the current interest group. */
  public readonly profiles = computed(
    () => this.profilesResource.value() ?? []
  );

  /** Names of profiles that hold the `LibAdmin` library permission. */
  public readonly libAdmin = computed(() =>
    this.profiles()
      .filter((item) => item.permissions?.library === 'LibAdmin')
      .map((item) => item.name as string)
  );

  /**
   * Resource resolving the display name (first + last) of the current node's
   * owner. Mirrors {@link nodeDataResource}'s loading/error state via
   * `chain`, and stays idle until an owner is known.
   */
  private readonly ownerNameResource = resource({
    params: ({ chain }) =>
      chain(this.nodeDataResource)?.currentNode.properties?.owner,
    loader: ({ params: userId }) => this.userService.getUserAsync({ userId }),
  });

  /** Display name (first + last) of the node owner. */
  public readonly ownerName = computed(() => {
    const user = this.ownerNameResource.value();
    return user ? `${user.firstname}  ${user.lastname}` : '';
  });

  /** True while node/permission/profile data is being (re)loaded. */
  public readonly loading = computed(
    () => this.nodeDataResource.isLoading() || this.profilesResource.isLoading()
  );

  /** True while a permission mutation (e.g. inheritance toggle) is in progress. */
  public processing = false;
  /** True while user permissions are being deleted. */
  public deletingUserPermissions = false;
  /** True while profile (group) permissions are being deleted. */
  public deletingProfilePermissions = false;
  /** Controls visibility of the "add permission" modal. */
  public showAddModal = false;

  /** Controls visibility of the share-space modal. */
  public showShareSpaceModal = false;
  /** Controls visibility of the delete-confirmation modal. */
  public showModalDelete = false;
  /** Interest group id of the share currently being edited, if any. */
  public igId!: string | undefined;
  /** Permission of the share currently being edited, if any. */
  public currentPermission!: string | undefined;

  /** Authority (user id or group name) of the permission entry queued for deletion. */
  public currentItem = '';
  /** Kind of the queued permission entry: `'user'` or `'group'`. */
  public currentItemKind = '';

  /** Map of profile group name to a `-`-joined string of its filtered permissions. */
  public readonly profilePermissionMap = computed(() =>
    this.buildProfilePermissionMap(this.perms())
  );
  /** Map of user id to a `-`-joined string of its filtered permissions. */
  public readonly userPermissionMap = computed(() =>
    this.buildUserPermissionMap(this.perms())
  );

  /**
   * Determines whether permission settings should be disabled for the current
   * interest group. Settings are disabled when the group grants no directory
   * access combined with library manage-own or newsgroup post rights.
   *
   * @returns `true` if permission setting should be disabled, otherwise `false`.
   */
  public shouldDisablePermissionSetting(): boolean {
    const currentIg = this.currentIg();
    if (currentIg === undefined) {
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
      currentIg.permissions.directory === dirNoAccessKey &&
      (currentIg.permissions.library === libManageOwnKey ||
        currentIg.permissions.newsgroup === nwsPostKey)
    );
  }

  /**
   * Toggles inheritance of permissions for the current node, persists the
   * change, refreshes the permission maps and shows a success message.
   *
   * @returns A promise that resolves once the update completes.
   */
  public async toggleInheritance() {
    const currentPerms = this.perms();
    const body: PermissionDefinition = {
      inherited: !currentPerms.inherited,
      permissions: currentPerms.permissions,
    };

    this.processing = true;
    const nodeId = this.currentNode()?.id;
    if (nodeId) {
      const perms = await this.permissionService.putPermissionAsync({
        id: nodeId,
        permissionDefinition: body,
      });

      this.nodeDataResource.value.update((data) => data && { ...data, perms });
    }
    this.processing = false;
    this.setPermissionMsg();
  }

  /** Displays a translated success notification for the set-permission action. */
  public setPermissionMsg() {
    const text = this.translateService.translate(
      getSuccessTranslation(ActionType.SET_PERMISSION)
    );
    if (text) {
      this.uiMessageService.addSuccessMessage(text, true);
    }
  }

  /**
   * Handles the result emitted by the add-permission modal. Closes the modal
   * and, on success, reloads the node permissions and rebuilds the maps.
   *
   * @param result - Outcome emitted by the add-permission component.
   * @returns A promise that resolves once handling completes.
   */
  async addPermissionFinished(result: ActionEmitterResult) {
    if (result.result === ActionResult.CANCELED) {
      this.showAddModal = false;
    } else if (result.result === ActionResult.SUCCEED) {
      this.showAddModal = false;

      const nodeId = this.currentNode()?.id;
      if (nodeId) {
        const perms = await this.permissionService.getPermissionsAsync({
          id: nodeId,
        });

        this.nodeDataResource.value.update(
          (data) => data && { ...data, perms }
        );
      }
    }
  }

  /**
   * Stores the permission entry targeted for deletion and opens the
   * delete-confirmation modal.
   *
   * @param item - Authority (user id or group name) to delete.
   * @param itemKind - Kind of entry: `'user'` or `'group'`.
   */
  prepareDeletePermissionEntry(item: string, itemKind: string) {
    this.currentItem = item;
    this.currentItemKind = itemKind;
    this.showModalDelete = true;
  }

  /**
   * Deletes the previously prepared permission entry (user or profile), shows a
   * success or error notification and resets the deletion state.
   *
   * @returns A promise that resolves once the deletion attempt completes.
   */
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
    } catch (error) {
      console.error(error);
      const text = this.translateService.translate(
        getErrorTranslation(ActionType.DELETE_PERMISSION)
      );
      this.uiMessageService.addErrorMessage(text, true);
    }

    this.currentItem = '';
    this.currentItemKind = '';
    this.showModalDelete = false;
  }

  /** Clears the queued permission entry and closes the delete-confirmation modal. */
  cancelPreparedPermissionEntry() {
    this.currentItem = '';
    this.currentItemKind = '';
    this.showModalDelete = false;
  }

  /**
   * Deletes a single permission for an authority on the current node, then
   * reloads the permission definition and rebuilds the maps.
   *
   * @param authority - The user id or group name to remove the permission from.
   * @param permission - The permission to remove.
   * @returns A promise that resolves once the deletion completes.
   */
  async deletePermission(authority: string, permission: string) {
    const nodeId = this.currentNode()?.id;
    if (nodeId) {
      await this.permissionService.deletePermissionAsync({
        id: nodeId,
        authority,
        permission,
      });
      const perms = await this.permissionService.getPermissionsAsync({
        id: nodeId,
      });

      this.nodeDataResource.value.update((data) => data && { ...data, perms });
    }
  }

  /**
   * Removes all (filtered) permissions held by a user on the current node, then
   * reloads the permission definition and rebuilds the user map.
   *
   * @param authority - The user id whose permissions should be removed.
   * @returns A promise that resolves once all deletions complete.
   */
  async deleteUserPermissions(authority: string) {
    const nodeId = this.currentNode()?.id;
    if (nodeId) {
      const permissions = this.splitUserPermissions(authority);

      for (const permission of permissions) {
        await this.permissionService.deletePermissionAsync({
          id: nodeId,
          authority,
          permission,
        });
      }

      const perms = await this.permissionService.getPermissionsAsync({
        id: nodeId,
      });

      this.nodeDataResource.value.update((data) => data && { ...data, perms });
    }
  }

  /**
   * Clears all permissions held by a profile (group) on the current node, then
   * reloads the permission definition and rebuilds the profile map.
   *
   * The authority is URL-encoded to work around a webscript issue where a dot
   * in the URL is rejected by Alfresco (DIGITCIRCABC-4766).
   *
   * @param authority - The group name whose permissions should be cleared.
   * @returns A promise that resolves once the operation completes.
   */
  async deleteProfilePermissions(authority: string) {
    const nodeId = this.currentNode()?.id;
    if (nodeId) {
      // DIGITCIRCABC-4766 DOT in URL is breaking alfresco webscript. It is rejected by webscript
      const encodeedAuthority = authority.replace('.', '%2E');

      await this.permissionService.clearPermissionsAsync({
        id: nodeId,
        authority: encodeedAuthority,
      });

      const perms = await this.permissionService.getPermissionsAsync({
        id: nodeId,
      });

      this.nodeDataResource.value.update((data) => data && { ...data, perms });
    }
  }

  /**
   * Determines whether a profile permission entry can be deleted. Entries for
   * the `GROUP_EVERYONE` profile and inherited permissions are not deletable.
   *
   * @param perm - The profile permission entry to evaluate.
   * @returns `true` if the entry is deletable, otherwise `false`.
   */
  isProfilePermissionDeletable(
    perm: PermissionDefinitionPermissionsProfiles
  ): boolean {
    return (
      perm.profile !== undefined &&
      perm.profile.groupName !== 'GROUP_EVERYONE' &&
      !this.perms().inherited
    );
  }

  /**
   * Checks whether the given user has at least one non-inherited (deletable)
   * permission on the current node.
   *
   * @param userId - The user id to check.
   * @returns `true` if a deletable user permission exists, otherwise `false`.
   */
  public hasUserPermissionDeletable(userId: string): boolean {
    const perms = this.perms();
    if (perms.permissions?.users) {
      const result = perms.permissions.users.find(
        (userEntry) =>
          userEntry?.user?.userId === userId && userEntry.inherited !== true
      );

      if (result !== undefined) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks whether the given profile (group) has at least one non-inherited
   * (deletable) permission on the current node.
   *
   * @param groupName - The group name to check.
   * @returns `true` if a deletable profile permission exists, otherwise `false`.
   */
  public hasProfilePermissionDeletable(groupName: string): boolean {
    const perms = this.perms();
    if (perms.permissions?.profiles) {
      const result = perms.permissions.profiles.find(
        (profileEntry) =>
          profileEntry?.profile?.groupName === groupName &&
          profileEntry.inherited !== true
      );

      if (result !== undefined) {
        return true;
      }
    }

    return false;
  }

  /**
   * Navigates back to the originating view (library details, forum or topic)
   * based on the `from` query parameter.
   *
   * @returns A promise that resolves once navigation is triggered.
   */
  public async goBack() {
    const currentNode = this.currentNode();
    if (this.from() === 'library') {
      this.router.navigate(['../../library', currentNode.id, 'details'], {
        relativeTo: this.route,
      });
    } else if (this.from() === 'forum') {
      this.router.navigate(['../../forum', currentNode.id], {
        relativeTo: this.route,
      });
    } else if (this.from() === 'topic') {
      this.router.navigate(['../../forum/topic', currentNode.id], {
        relativeTo: this.route,
      });
    }
  }

  /**
   * Navigates back to the library folder containing the current node (the node
   * itself if it is a folder, otherwise its parent).
   *
   * @returns A promise that resolves once navigation is triggered.
   */
  public async goBackToFolder() {
    const currentNode = this.currentNode();
    const nodeId = this.isFile() ? currentNode.parentId : currentNode.id;
    this.router.navigate(['../../library', nodeId], {
      relativeTo: this.route,
    });
  }

  /**
   * Returns the current node id, or an empty string if no node is loaded.
   *
   * @returns The current node id or `''`.
   */
  public getNodeId() {
    const currentNode = this.currentNode();
    if (currentNode) {
      return currentNode.id;
    }

    return '';
  }

  /**
   * Builds a profile permission map from the given permission definition,
   * ignoring default Visibility, Category and Dir permissions and joining
   * multiple permissions per profile with `-`.
   *
   * @param perms - The permission definition to derive the map from.
   * @returns A map of profile group name to a `-`-joined permission string.
   */
  // used to ignore default permissions of Visibility or Category access / admin
  private buildProfilePermissionMap(perms: PermissionDefinition | undefined): {
    [key: string]: string;
  } {
    const map: { [key: string]: string } = {};
    const modelProfiles = perms?.permissions.profiles;

    if (modelProfiles) {
      for (const perm of modelProfiles) {
        if (this.isValidPermission(perm.permission)) {
          this.addProfilePermission(map, perm);
        }
      }
    }

    return map;
  }

  /**
   * Determines whether a permission should be displayed, filtering out default
   * Visibility, Category and Dir permissions.
   *
   * @param permission - The permission name to test.
   * @returns `true` if the permission is a valid, displayable one.
   */
  private isValidPermission(permission: string | undefined): boolean {
    if (!permission) return false;
    return !(
      permission.includes('Visibility') ||
      permission.includes('Category') ||
      permission.includes('Dir')
    );
  }

  /**
   * Adds a single profile permission to the given map, appending to any
   * existing permissions for the same group with a `-` separator.
   *
   * @param map - The profile permission map to mutate.
   * @param perm - The profile permission entry to add.
   */
  private addProfilePermission(
    map: { [key: string]: string },
    perm: PermissionDefinitionPermissionsProfiles
  ) {
    const profile = perm.profile;
    if (!(profile?.groupName && perm.permission)) return;

    const groupName = profile.groupName;
    if (map[groupName]) {
      map[groupName] += `-${perm.permission}`;
    } else {
      map[groupName] = perm.permission;
    }
  }

  /**
   * Builds a user permission map from the given permission definition,
   * ignoring default Visibility, Category and Dir permissions and joining
   * multiple permissions per user with `-`.
   *
   * @param perms - The permission definition to derive the map from.
   * @returns A map of user id to a `-`-joined permission string.
   */
  // used to ignore default permissions of Visibility or Category access / admin
  private buildUserPermissionMap(perms: PermissionDefinition | undefined): {
    [key: string]: string;
  } {
    const map: { [key: string]: string } = {};
    const modelUsers = perms?.permissions.users;

    if (modelUsers !== undefined) {
      for (const perm of modelUsers) {
        if (this.isValidPermission(perm.permission)) {
          this.addUserPermission(map, perm);
        }
      }
    }

    return map;
  }

  /**
   * Adds a single user permission to the given map, appending to any existing
   * permissions for the same user with a `-` separator.
   *
   * @param map - The user permission map to mutate.
   * @param perm - The user permission entry to add.
   */
  private addUserPermission(
    map: { [key: string]: string },
    perm: PermissionDefinitionPermissionsUsers
  ) {
    const user = perm.user;
    if (!(user?.userId && perm.permission)) return;

    const userId = user.userId;
    if (map[userId]) {
      map[userId] += `-${perm.permission}`;
    } else {
      map[userId] = perm.permission;
    }
  }

  /**
   * Determines whether the current node is a file (i.e. not a folder).
   *
   * @returns `true` if the node is not a folder.
   */
  public isFile(): boolean {
    return !this.currentNode()?.type?.includes('folder');
  }

  /**
   * Determines whether the current node is a folder.
   *
   * @returns `true` if the node type includes `folder`.
   */
  public isFolder(): boolean {
    return this.currentNode()?.type?.includes('folder') ?? false;
  }

  /**
   * Determines whether the current node is a URL link.
   *
   * @returns `true` if the node's `isUrl` property is `'true'`.
   */
  public isLink(): boolean {
    const currentNode = this.currentNode();
    if (currentNode?.type && currentNode.properties) {
      return currentNode.properties.isUrl === 'true';
    }
    return false;
  }

  /**
   * Determines whether the current node is a shared-space link (folder link).
   *
   * @returns `true` if the node type includes `folderlink`.
   */
  isSharedSpaceLink(): boolean {
    const currentNode = this.currentNode();
    if (currentNode?.type) {
      return currentNode.type.includes('folderlink');
    }
    return false;
  }

  // shared spaces

  /**
   * Removes a shared space from the current folder node and reloads the shares.
   *
   * @param share - The share to remove.
   * @returns A promise that resolves once the share is removed.
   */
  public async removeShare(share: Share) {
    if (this.isFolder()) {
      await this.spaceService.deleteShareSpaceAsync({
        id: this.nodeId(),
        sharedIGId: share.igId as string,
      });
      this.nodeDataResource.reload();
    }
  }

  /**
   * Indicates whether the current node has any shared spaces.
   *
   * @returns `true` if at least one share exists.
   */
  public hasShares(): boolean {
    return this.shares().length > 0;
  }

  /**
   * Handles the result emitted by the share-space modal. Resets the share edit
   * state, closes the modal and reloads shares on success.
   *
   * @param result - Outcome emitted by the share-space component.
   * @returns A promise that resolves once handling completes.
   */
  public async refreshShares(result: ActionEmitterResult) {
    if (this.isFolder()) {
      this.igId = undefined;
      this.currentPermission = undefined;
      this.showShareSpaceModal = false;
      if (result.result === ActionResult.SUCCEED) {
        this.nodeDataResource.reload();
      }
    }
  }

  /**
   * Opens the share-space modal pre-populated to edit the given share's
   * permission.
   *
   * @param share - The share whose permission should be edited.
   */
  public editSharePermission(share: Share) {
    if (this.isFolder()) {
      this.igId = share.igId;
      this.currentPermission = share.permission;
      this.showShareSpaceModal = true;
    }
  }

  /**
   * Opens either the share-space modal or the add-permission modal (folders
   * only).
   *
   * @param isShare - When `true` opens the share modal, otherwise the add
   * permission modal.
   */
  public addPermission(isShare: boolean): void {
    if (this.isFolder()) {
      if (isShare) {
        this.showShareSpaceModal = true;
      } else {
        this.showAddModal = true;
      }
    }
  }

  /**
   * Opens the add-permission modal unless permission setting is disabled for the
   * current interest group.
   */
  public showAddPermissionModal() {
    if (!this.shouldDisablePermissionSetting()) {
      this.showAddModal = true;
    }
  }

  /**
   * Returns the keys of the given object; used by the template to iterate
   * permission maps.
   *
   * @param obj - The object whose keys are requested.
   * @returns An array of the object's own enumerable keys.
   */
  public getMapKeys(obj: {}) {
    return Object.keys(obj);
  }

  /**
   * Resolves the display title of a profile from the current permission
   * definition.
   *
   * @param groupName - The group name of the profile.
   * @returns The profile title, or `undefined` if not found.
   */
  public getProfileTitle(groupName: string) {
    const perms = this.perms();
    if (perms.permissions?.profiles) {
      const result = perms.permissions.profiles.find(
        (profileEntry) => profileEntry?.profile?.groupName === groupName
      );

      if (result?.profile) {
        return result.profile.title;
      }
      return undefined;
    }
    return undefined;
  }

  /**
   * Resolves the display name (first + last) of a user from the current
   * permission definition.
   *
   * @param userid - The user id.
   * @returns The user's full name, or `undefined` if not found.
   */
  public getDisplayUser(userid: string) {
    const perms = this.perms();
    if (perms.permissions?.users) {
      const result = perms.permissions.users.find(
        (userEntry) => userEntry?.user?.userId === userid
      );

      if (result?.user) {
        return `${result.user.firstname} ${result.user.lastname}`;
      }
      return undefined;
    }
    return undefined;
  }

  /**
   * Splits the aggregated profile permissions for a group into an array.
   *
   * @param groupName - The group name to look up in {@link profilePermissionMap}.
   * @returns An array of individual permission names.
   */
  public splitProfilePermissions(groupName: string) {
    return this.splitPermissions(this.profilePermissionMap()[groupName]);
  }

  /**
   * Splits the aggregated user permissions for a user into an array.
   *
   * @param userId - The user id to look up in {@link userPermissionMap}.
   * @returns An array of individual permission names.
   */
  public splitUserPermissions(userId: string) {
    return this.splitPermissions(this.userPermissionMap()[userId]);
  }

  /**
   * Splits a `-`-joined permission string into an array of permission names.
   *
   * @param permissions - The permission string to split.
   * @returns An array with each permission, or a single-element array when no
   * separator is present.
   */
  public splitPermissions(permissions: string) {
    if (permissions.includes('-')) {
      return permissions.split('-');
    }
    return [permissions];
  }

  /**
   * Determines whether an entry represents the forum's Newsgroups node.
   *
   * @param name - The entry name.
   * @param type - The entry type.
   * @returns `true` if the name is `Newsgroups` and the type includes `forums`.
   */
  isForumNewsgroupsName(name: string, type: string) {
    return name === 'Newsgroups' && type.includes('forums');
  }
}
