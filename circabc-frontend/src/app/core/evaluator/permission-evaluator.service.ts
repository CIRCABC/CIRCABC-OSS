import { Injectable } from '@angular/core';
import {
  agendaPermissionKeys,
  AgendaPermissions,
} from 'app/core/evaluator/agenda-permissions';
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
  Node as ModelNode,
  PermissionDefinition,
} from 'app/core/generated/circabc';

@Injectable({
  providedIn: 'root',
})
export class PermissionEvaluatorService {
  private ALLOWED = 'ALLOWED';

  public isGroupAdmin(interestGroup: InterestGroup): boolean {
    return (
      this.isLibAdmin(interestGroup as ModelNode) ||
      this.isDirAdmin(interestGroup) ||
      this.isEveAdmin(interestGroup as ModelNode)
    );
  }

  public isOwner(node: ModelNode, userName: string): boolean {
    return userName === node?.properties?.owner;
  }

  public isLibAdmin(node: ModelNode): boolean {
    return this.isLibraryPermissionAllowed(node, LibraryPermissions.LibAdmin);
  }

  public isLibFullEdit(node: ModelNode): boolean {
    return this.isLibraryPermissionAllowed(
      node,
      LibraryPermissions.LibFullEdit
    );
  }

  public isLibManageOwnOrHigher(node: ModelNode): boolean {
    return this.isAnyOfLibraryPermissionAllowed(node, [
      LibraryPermissions.LibManageOwn,
      LibraryPermissions.LibEditOnly,
      LibraryPermissions.LibFullEdit,
      LibraryPermissions.LibAdmin,
    ]);
  }

  public isLibAdminOrFullEdit(node: ModelNode): boolean {
    return this.isAnyOfLibraryPermissionAllowed(node, [
      LibraryPermissions.LibFullEdit,
      LibraryPermissions.LibAdmin,
    ]);
  }

  public isLibAccess(node: ModelNode): boolean {
    return this.isLibraryPermissionAllowed(node, LibraryPermissions.LibAccess);
  }

  public isLibNoAccess(node: ModelNode): boolean {
    return this.isLibraryPermissionAllowed(
      node,
      LibraryPermissions.LibNoAccess
    );
  }

  private isLibraryPermissionAllowed(
    node: ModelNode,
    libraryPermission: LibraryPermissions
  ): boolean {
    let result = false;

    if (node !== undefined) {
      if (node.permissions !== undefined) {
        // Get the string key name for the permission value
        const permissionKey = libraryPermissionKeys.find(
          (key) =>
            LibraryPermissions[key as keyof typeof LibraryPermissions] ===
            libraryPermission
        );

        if (permissionKey && node.permissions[permissionKey] === this.ALLOWED) {
          result = true;
        }
      }
    }
    return result;
  }

  private isAnyOfLibraryPermissionAllowed(
    node: ModelNode,
    libraryPermissions: LibraryPermissions[]
  ): boolean {
    let result = false;

    if (node !== undefined) {
      if (node.permissions !== undefined) {
        for (const permission of libraryPermissions) {
          // Get the string key name for the permission value
          const permissionKey = libraryPermissionKeys.find(
            (key) =>
              LibraryPermissions[key as keyof typeof LibraryPermissions] ===
              permission
          );

          if (
            permissionKey &&
            node.permissions[permissionKey] === this.ALLOWED
          ) {
            result = true;
            break;
          }
        }
      }
    }
    return result;
  }

  public isDirAdmin(interestGroup: InterestGroup): boolean {
    return this.isDirPermission(interestGroup, DirectoryPermissions.DirAdmin);
  }

  public isDirManageMembers(interestGroup: InterestGroup): boolean {
    return this.isDirPermission(
      interestGroup,
      DirectoryPermissions.DirManageMembers
    );
  }

  private isDirPermission(
    interestGroup: InterestGroup,
    directoryPermissions: DirectoryPermissions
  ): boolean {
    let result = false;
    if (interestGroup !== undefined) {
      if (interestGroup.permissions !== undefined) {
        // Get the string key name for the permission value
        const permissionKey = directoryPermissionKeys.find(
          (key) =>
            DirectoryPermissions[key as keyof typeof DirectoryPermissions] ===
            directoryPermissions
        );

        result = interestGroup.permissions.directory === permissionKey;
      }
    }

    return result;
  }

  public isNewsgroupPost(node: ModelNode): boolean {
    return (
      this.isNewsgroupsPermissionAllowed(node, NewsgroupsPermissions.NwsPost) ||
      this.isNewsgroupsPermissionAllowed(
        node,
        NewsgroupsPermissions.NwsModerate
      ) ||
      this.isNewsgroupsPermissionAllowed(node, NewsgroupsPermissions.NwsAdmin)
    );
  }

  private isNewsgroupsPermissionAllowed(
    node: ModelNode,
    newsgroupsPermissions: NewsgroupsPermissions
  ): boolean {
    let result = false;

    if (node !== undefined) {
      if (node.permissions !== undefined) {
        // Get the string key name for the permission value
        const permissionKey = newsGroupPermissionKeys.find(
          (key) =>
            NewsgroupsPermissions[key as keyof typeof NewsgroupsPermissions] ===
            newsgroupsPermissions
        );

        if (permissionKey && node.permissions[permissionKey] === this.ALLOWED) {
          result = true;
        }
      }
    }
    return result;
  }

  public isNewsgroupModerate(node: ModelNode): boolean {
    return (
      this.isNewsgroupsPermissionAllowed(
        node,
        NewsgroupsPermissions.NwsAdmin
      ) ||
      this.isNewsgroupsPermissionAllowed(
        node,
        NewsgroupsPermissions.NwsModerate
      )
    );
  }

  public isNewsgroupAdmin(node: ModelNode): boolean {
    return this.isNewsgroupsPermissionAllowed(
      node,
      NewsgroupsPermissions.NwsAdmin
    );
  }

  public canModerateNewsgroup(node: ModelNode): boolean {
    return (
      this.isNewsgroupsPermissionAllowed(
        node,
        NewsgroupsPermissions.NwsModerate
      ) || this.isNewsgroupAdmin(node)
    );
  }

  public canDeleteForum(node: ModelNode): boolean {
    if (node === undefined) {
      return false;
    }
    return node.permissions !== undefined
      ? node.permissions.DeleteForum === 'ALLOWED'
      : false;
  }

  private isAgendaPermissionAllowed(
    node: ModelNode,
    agendaPermissions: AgendaPermissions
  ): boolean {
    let result = false;

    if (node !== undefined) {
      if (node.permissions !== undefined) {
        // Get the string key name for the permission value
        const permissionKey = agendaPermissionKeys.find(
          (key) =>
            AgendaPermissions[key as keyof typeof AgendaPermissions] ===
            agendaPermissions
        );

        if (permissionKey && node.permissions[permissionKey] === this.ALLOWED) {
          result = true;
        }
      }
    }
    return result;
  }

  public isEveAdmin(node: ModelNode): boolean {
    return this.isAgendaPermissionAllowed(node, AgendaPermissions.EveAdmin);
  }

  public hasGuestProfileAccess(
    perms: PermissionDefinition,
    noAccessPermission: string
  ): boolean {
    if (perms !== null && perms !== undefined) {
      // check to see whether the guest profile is set and if it is, if it has access
      const result = perms.permissions.profiles?.find(
        (profileEntry) =>
          profileEntry.permission !== noAccessPermission &&
          profileEntry !== undefined &&
          profileEntry.profile !== undefined &&
          profileEntry.profile.name === 'guest'
      );

      return result !== undefined;
    }

    return false;
  }

  public getLibraryPermissions() {
    return [
      'LibNoAccess',
      'LibAccess',
      'LibManageOwn',
      'LibFullEdit',
      'LibAdmin',
    ];
  }

  public getLibraryContentPermissions() {
    return [
      'LibNoAccess',
      'LibAccess',
      'LibEditOnly',
      'LibFullEdit',
      'LibAdmin',
    ];
  }

  public getLibraryFolderPermissions() {
    return [
      'LibNoAccess',
      'LibAccess',
      'LibEditOnly',
      'LibManageOwn',
      'LibFullEdit',
      'LibAdmin',
    ];
  }

  public geNewsgroupsPermissions() {
    return ['NwsNoAccess', 'NwsAccess', 'NwsPost', 'NwsModerate', 'NwsAdmin'];
  }
}
