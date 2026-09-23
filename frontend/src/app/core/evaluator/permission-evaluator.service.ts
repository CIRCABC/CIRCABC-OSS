import { Service } from '@angular/core';
import {
  AgendaPermissions,
  agendaPermissionKeys,
} from 'app/core/evaluator/agenda-permissions';
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
  InterestGroup,
  Node as ModelNode,
  PermissionDefinition,
} from 'app/core/generated/circabc';

/**
 * Root-scoped service that evaluates the effective permissions carried by
 * CIRCABC nodes and interest groups.
 *
 * The backend exposes permissions as a map of permission-key/value pairs on
 * {@link ModelNode} and {@link InterestGroup} objects (typically the string
 * `'ALLOWED'` when a permission is granted). This service centralises the
 * logic that translates the strongly-typed permission enums
 * ({@link LibraryPermissions}, {@link DirectoryPermissions},
 * {@link NewsgroupsPermissions} and {@link AgendaPermissions}) into simple
 * boolean checks that UI components and guards can consume.
 *
 * Key collaborators:
 * - The permission enums and their `*PermissionKeys` helpers used to map an
 *   enum value back to the string key present on a node's `permissions` map.
 * - The generated CIRCABC API models ({@link ModelNode},
 *   {@link InterestGroup}, {@link PermissionDefinition}).
 */
@Service()
export class PermissionEvaluatorService {
  /** Marker value used by the backend to indicate a granted permission. */
  private readonly ALLOWED = 'ALLOWED';

  /**
   * Determines whether the current user administers the given interest group
   * in any of its service areas (library, directory or agenda/events).
   *
   * @param interestGroup - The interest group whose permissions are evaluated.
   * @returns `true` if the user is a library, directory or events
   * administrator for the group; otherwise `false`.
   */
  public isGroupAdmin(interestGroup: InterestGroup): boolean {
    return (
      this.isLibAdmin(interestGroup as ModelNode) ||
      this.isDirAdmin(interestGroup) ||
      this.isEveAdmin(interestGroup as ModelNode)
    );
  }

  /**
   * Checks whether the given user owns the node.
   *
   * @param node - The node to inspect.
   * @param userName - The login name to compare against the node owner.
   * @returns `true` if `userName` matches the node's `owner` property;
   * otherwise `false`.
   */
  public isOwner(node: ModelNode, userName: string): boolean {
    return userName === node?.properties?.owner;
  }

  /**
   * Checks whether the library administrator permission is granted on the node.
   *
   * @param node - The node whose library permissions are evaluated.
   * @returns `true` if {@link LibraryPermissions.LibAdmin} is allowed.
   */
  public isLibAdmin(node: ModelNode): boolean {
    return this.isLibraryPermissionAllowed(node, LibraryPermissions.LibAdmin);
  }

  /**
   * Checks whether the library full-edit permission is granted on the node.
   *
   * @param node - The node whose library permissions are evaluated.
   * @returns `true` if {@link LibraryPermissions.LibFullEdit} is allowed.
   */
  public isLibFullEdit(node: ModelNode): boolean {
    return this.isLibraryPermissionAllowed(
      node,
      LibraryPermissions.LibFullEdit
    );
  }

  /**
   * Checks whether the node grants "manage own" library permissions or any
   * higher edit/admin level.
   *
   * @param node - The node whose library permissions are evaluated.
   * @returns `true` if any of `LibManageOwn`, `LibEditOnly`, `LibFullEdit` or
   * `LibAdmin` is allowed; otherwise `false`.
   */
  public isLibManageOwnOrHigher(node: ModelNode): boolean {
    return this.isAnyOfLibraryPermissionAllowed(node, [
      LibraryPermissions.LibManageOwn,
      LibraryPermissions.LibEditOnly,
      LibraryPermissions.LibFullEdit,
      LibraryPermissions.LibAdmin,
    ]);
  }

  /**
   * Checks whether the node grants full-edit or admin library permissions.
   *
   * @param node - The node whose library permissions are evaluated.
   * @returns `true` if either {@link LibraryPermissions.LibFullEdit} or
   * {@link LibraryPermissions.LibAdmin} is allowed; otherwise `false`.
   */
  public isLibAdminOrFullEdit(node: ModelNode): boolean {
    return this.isAnyOfLibraryPermissionAllowed(node, [
      LibraryPermissions.LibFullEdit,
      LibraryPermissions.LibAdmin,
    ]);
  }

  /**
   * Checks whether basic library access is granted on the node.
   *
   * @param node - The node whose library permissions are evaluated.
   * @returns `true` if {@link LibraryPermissions.LibAccess} is allowed.
   */
  public isLibAccess(node: ModelNode): boolean {
    return this.isLibraryPermissionAllowed(node, LibraryPermissions.LibAccess);
  }

  /**
   * Checks whether the node explicitly denies library access.
   *
   * @param node - The node whose library permissions are evaluated.
   * @returns `true` if {@link LibraryPermissions.LibNoAccess} is allowed.
   */
  public isLibNoAccess(node: ModelNode): boolean {
    return this.isLibraryPermissionAllowed(
      node,
      LibraryPermissions.LibNoAccess
    );
  }

  /**
   * Resolves a single library permission enum value against the node's
   * permission map.
   *
   * @param node - The node whose `permissions` map is inspected.
   * @param libraryPermission - The library permission to look up.
   * @returns `true` if the corresponding permission key on the node equals
   * {@link ALLOWED}; otherwise `false` (including when the node or its
   * permissions are undefined).
   */
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

  /**
   * Resolves a set of library permission enum values against the node's
   * permission map, returning as soon as one of them is granted.
   *
   * @param node - The node whose `permissions` map is inspected.
   * @param libraryPermissions - The list of library permissions to look up.
   * @returns `true` if at least one of the permissions is {@link ALLOWED};
   * otherwise `false` (including when the node or its permissions are
   * undefined).
   */
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

  /**
   * Checks whether directory administrator permission is granted for the
   * interest group.
   *
   * @param interestGroup - The interest group whose directory permission is
   * evaluated.
   * @returns `true` if {@link DirectoryPermissions.DirAdmin} applies.
   */
  public isDirAdmin(interestGroup: InterestGroup): boolean {
    return this.isDirPermission(interestGroup, DirectoryPermissions.DirAdmin);
  }

  /**
   * Checks whether the interest group grants the "manage members" directory
   * permission.
   *
   * @param interestGroup - The interest group whose directory permission is
   * evaluated.
   * @returns `true` if {@link DirectoryPermissions.DirManageMembers} applies.
   */
  public isDirManageMembers(interestGroup: InterestGroup): boolean {
    return this.isDirPermission(
      interestGroup,
      DirectoryPermissions.DirManageMembers
    );
  }

  /**
   * Compares the interest group's `directory` permission against a directory
   * permission enum value.
   *
   * @param interestGroup - The interest group whose `permissions.directory`
   * value is inspected.
   * @param directoryPermissions - The directory permission to match.
   * @returns `true` if the group's directory permission key equals the given
   * permission; otherwise `false` (including when the group or its
   * permissions are undefined).
   */
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

  /**
   * Checks whether the user may post in the given newsgroup/forum node.
   *
   * Posting is implied by the post, moderate or admin newsgroup permissions.
   *
   * @param node - The newsgroup node whose permissions are evaluated.
   * @returns `true` if any of `NwsPost`, `NwsModerate` or `NwsAdmin` is
   * allowed; otherwise `false`.
   */
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

  /**
   * Resolves a single newsgroup permission enum value against the node's
   * permission map.
   *
   * @param node - The node whose `permissions` map is inspected.
   * @param newsgroupsPermissions - The newsgroup permission to look up.
   * @returns `true` if the corresponding permission key on the node equals
   * {@link ALLOWED}; otherwise `false` (including when the node or its
   * permissions are undefined).
   */
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

  /**
   * Checks whether the user may moderate the newsgroup (moderate or admin
   * permission).
   *
   * @param node - The newsgroup node whose permissions are evaluated.
   * @returns `true` if either `NwsAdmin` or `NwsModerate` is allowed;
   * otherwise `false`.
   */
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

  /**
   * Checks whether newsgroup administrator permission is granted on the node.
   *
   * @param node - The newsgroup node whose permissions are evaluated.
   * @returns `true` if {@link NewsgroupsPermissions.NwsAdmin} is allowed.
   */
  public isNewsgroupAdmin(node: ModelNode): boolean {
    return this.isNewsgroupsPermissionAllowed(
      node,
      NewsgroupsPermissions.NwsAdmin
    );
  }

  /**
   * Checks whether the user can moderate the newsgroup via the moderate or
   * admin permission.
   *
   * @param node - The newsgroup node whose permissions are evaluated.
   * @returns `true` if `NwsModerate` is allowed or the user is a newsgroup
   * admin; otherwise `false`.
   */
  public canModerateNewsgroup(node: ModelNode): boolean {
    return (
      this.isNewsgroupsPermissionAllowed(
        node,
        NewsgroupsPermissions.NwsModerate
      ) || this.isNewsgroupAdmin(node)
    );
  }

  /**
   * Checks whether the user may delete the forum represented by the node.
   *
   * @param node - The forum node whose `DeleteForum` permission is inspected.
   * @returns `true` if the node's `DeleteForum` permission equals `'ALLOWED'`;
   * `false` when the node is undefined or the permission is not granted.
   */
  public canDeleteForum(node: ModelNode): boolean {
    if (node === undefined) {
      return false;
    }
    return node.permissions?.DeleteForum === 'ALLOWED';
  }

  /**
   * Resolves a single agenda permission enum value against the node's
   * permission map.
   *
   * @param node - The node whose `permissions` map is inspected.
   * @param agendaPermissions - The agenda permission to look up.
   * @returns `true` if the corresponding permission key on the node equals
   * {@link ALLOWED}; otherwise `false` (including when the node or its
   * permissions are undefined).
   */
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

  /**
   * Checks whether events/agenda administrator permission is granted on the
   * node.
   *
   * @param node - The node whose agenda permissions are evaluated.
   * @returns `true` if {@link AgendaPermissions.EveAdmin} is allowed.
   */
  public isEveAdmin(node: ModelNode): boolean {
    return this.isAgendaPermissionAllowed(node, AgendaPermissions.EveAdmin);
  }

  /**
   * Determines whether the "guest" profile has been granted access in the
   * given permission definition.
   *
   * @param perms - The permission definition containing the profile entries.
   * @param noAccessPermission - The permission value that represents "no
   * access"; a guest entry with this value is treated as not having access.
   * @returns `true` if a profile entry named `guest` exists with a permission
   * other than `noAccessPermission`; otherwise `false` (including when `perms`
   * is null or undefined).
   */
  public hasGuestProfileAccess(
    perms: PermissionDefinition,
    noAccessPermission: string
  ): boolean {
    if (perms !== null && perms !== undefined) {
      // check to see whether the guest profile is set and if it is, if it has access
      const result = perms.permissions.profiles?.find(
        (profileEntry) =>
          profileEntry.permission !== noAccessPermission &&
          profileEntry?.profile?.name === 'guest'
      );

      return result !== undefined;
    }

    return false;
  }

  /**
   * Returns the ordered list of library permission level keys applicable to a
   * group/space.
   *
   * @returns An array of library permission key names, from lowest to highest
   * access level.
   */
  public getLibraryPermissions() {
    return [
      'LibNoAccess',
      'LibAccess',
      'LibManageOwn',
      'LibFullEdit',
      'LibAdmin',
    ];
  }

  /**
   * Returns the ordered list of library permission level keys applicable to
   * content (documents).
   *
   * @returns An array of library permission key names, from lowest to highest
   * access level.
   */
  public getLibraryContentPermissions() {
    return [
      'LibNoAccess',
      'LibAccess',
      'LibEditOnly',
      'LibFullEdit',
      'LibAdmin',
    ];
  }

  /**
   * Returns the ordered list of library permission level keys applicable to
   * folders.
   *
   * @returns An array of library permission key names, from lowest to highest
   * access level.
   */
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

  /**
   * Returns the ordered list of newsgroup permission level keys.
   *
   * @returns An array of newsgroup permission key names, from lowest to
   * highest access level.
   */
  public geNewsgroupsPermissions() {
    return ['NwsNoAccess', 'NwsAccess', 'NwsPost', 'NwsModerate', 'NwsAdmin'];
  }
}
