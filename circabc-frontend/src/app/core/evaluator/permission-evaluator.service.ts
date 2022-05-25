import { Injectable } from '@angular/core';
import { AgendaPermissions } from 'app/core/evaluator/agenda-permissions';
import { DirectoryPermissions } from 'app/core/evaluator/directory-permissions';
import { LibraryPermissions } from 'app/core/evaluator/library-permissions';
import { NewsgroupsPermissions } from 'app/core/evaluator/newsgroups-permissions';
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

  public isOwner(node: ModelNode, userName: string): boolean {
    if (node && node.properties && node.properties.owner) {
      return userName === node.properties.owner;
    } else {
      return false;
    }
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
        if (
          node.permissions[LibraryPermissions[libraryPermission]] ===
          this.ALLOWED
        ) {
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
          if (
            node.permissions[LibraryPermissions[permission]] === this.ALLOWED
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
        result =
          interestGroup.permissions.directory ===
          DirectoryPermissions[directoryPermissions];
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
        if (
          node.permissions[NewsgroupsPermissions[newsgroupsPermissions]] ===
          this.ALLOWED
        ) {
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
        if (
          node.permissions[AgendaPermissions[agendaPermissions]] ===
          this.ALLOWED
        ) {
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
      LibraryPermissions[LibraryPermissions.LibNoAccess],
      LibraryPermissions[LibraryPermissions.LibAccess],
      LibraryPermissions[LibraryPermissions.LibManageOwn],
      LibraryPermissions[LibraryPermissions.LibFullEdit],
      LibraryPermissions[LibraryPermissions.LibAdmin],
    ];
  }

  public getLibraryContentPermissions() {
    return [
      LibraryPermissions[LibraryPermissions.LibNoAccess],
      LibraryPermissions[LibraryPermissions.LibAccess],
      LibraryPermissions[LibraryPermissions.LibEditOnly],
      LibraryPermissions[LibraryPermissions.LibFullEdit],
      LibraryPermissions[LibraryPermissions.LibAdmin],
    ];
  }

  public getLibraryFolderPermissions() {
    return [
      LibraryPermissions[LibraryPermissions.LibNoAccess],
      LibraryPermissions[LibraryPermissions.LibAccess],
      LibraryPermissions[LibraryPermissions.LibEditOnly],
      LibraryPermissions[LibraryPermissions.LibManageOwn],
      LibraryPermissions[LibraryPermissions.LibFullEdit],
      LibraryPermissions[LibraryPermissions.LibAdmin],
    ];
  }

  public geNewsgroupsPermissions() {
    return [
      NewsgroupsPermissions[NewsgroupsPermissions.NwsNoAccess],
      NewsgroupsPermissions[NewsgroupsPermissions.NwsAccess],
      NewsgroupsPermissions[NewsgroupsPermissions.NwsPost],
      NewsgroupsPermissions[NewsgroupsPermissions.NwsModerate],
      NewsgroupsPermissions[NewsgroupsPermissions.NwsAdmin],
    ];
  }
}
