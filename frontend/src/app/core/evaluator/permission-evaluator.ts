import { inject, Service } from '@angular/core';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';

/**
 * Root-level service that evaluates whether the current user holds a given
 * permission on a CIRCABC node.
 *
 * CIRCABC permissions are hierarchical: a "stronger" permission implicitly
 * grants all the weaker permissions in the same service area (library, news
 * group, events, directory and information). This service centralises that
 * hierarchy and exposes helpers to test single permissions, sets of
 * permissions, and "stronger-or-equal" permissions against a node's granted
 * permission set.
 *
 * Some permissions are only effective when the current user owns the node
 * (owner permissions). In those cases the evaluator additionally checks node
 * ownership via the {@link LoginService}.
 *
 * Key collaborators:
 * - {@link LoginService}: resolves the current username to determine node
 *   ownership.
 * - {@link AllPermission}: the union of all supported permission identifiers.
 * - {@link ModelNode}: the node whose granted permissions are inspected.
 */
@Service()
export class PermissionEvaluator {
  /** Login service used to resolve the current username for ownership checks. */
  private readonly loginService = inject(LoginService);

  /** Sentinel value stored on a node's permission map when a permission is granted. */
  private readonly ALLOWED = 'ALLOWED';
  /**
   * Maps each permission to the list of permissions that are equal to or
   * stronger than it (i.e. permissions that implicitly satisfy it). Populated
   * once at construction time via {@link loadStrongerPermissions}.
   */
  private readonly strongerPermissions: Map<AllPermission, AllPermission[]> =
    new Map();

  /**
   * Builds the permission hierarchy map by loading the stronger-permission
   * relationships for every service area.
   */
  constructor() {
    this.loadStrongerPermissions();
  }

  /**
   * Populates {@link strongerPermissions} with the hierarchies for all
   * service areas (library, news group, events, directory and information).
   */
  private loadStrongerPermissions(): void {
    this.loadLibraryStorngerPermission();
    this.loadNewsGroupStrongerPermission();
    this.loadEventsStrongerPermission();
    this.loadDirectoryStrongerPermissions();
    this.loadInformationStrongerPermissions();
  }

  /**
   * Registers the information-service permission hierarchy, from the weakest
   * ("InfNoAccess") to the strongest ("InfAdmin").
   */
  private loadInformationStrongerPermissions() {
    this.strongerPermissions.set('InfNoAccess', [
      'InfAdmin',
      'InfManage',
      'InfAccess',
      'InfNoAccess',
    ]);
    this.strongerPermissions.set('InfAccess', [
      'InfAdmin',
      'InfManage',
      'InfAccess',
    ]);
    this.strongerPermissions.set('InfManage', ['InfAdmin', 'InfManage']);
    this.strongerPermissions.set('InfAdmin', ['InfAdmin']);
  }

  /**
   * Registers the directory-service permission hierarchy, from the weakest
   * ("DirNoAccess") to the strongest ("DirAdmin").
   */
  private loadDirectoryStrongerPermissions() {
    this.strongerPermissions.set('DirNoAccess', [
      'DirAdmin',
      'DirManageMembers',
      'DirAccess',
      'DirNoAccess',
    ]);
    this.strongerPermissions.set('DirAccess', [
      'DirAdmin',
      'DirManageMembers',
      'DirAccess',
    ]);
    this.strongerPermissions.set('DirManageMembers', [
      'DirAdmin',
      'DirManageMembers',
    ]);
    this.strongerPermissions.set('DirAdmin', ['DirAdmin']);
  }

  /**
   * Registers the events-service permission hierarchy, from the weakest
   * ("EveNoAccess") to the strongest ("EveAdmin").
   */
  private loadEventsStrongerPermission() {
    this.strongerPermissions.set('EveNoAccess', [
      'EveAdmin',
      'EveAccess',
      'EveNoAccess',
    ]);
    this.strongerPermissions.set('EveAccess', ['EveAdmin', 'EveAccess']);
    this.strongerPermissions.set('EveAdmin', ['EveAdmin']);
  }

  /**
   * Registers the news-group-service permission hierarchy, from the weakest
   * ("NwsNoAccess") to the strongest ("NwsAdmin").
   */
  private loadNewsGroupStrongerPermission() {
    this.strongerPermissions.set('NwsNoAccess', [
      'NwsAdmin',
      'NwsModerate',
      'NwsPost',
      'NwsAccess',
      'NwsNoAccess',
    ]);
    this.strongerPermissions.set('NwsAccess', [
      'NwsAdmin',
      'NwsModerate',
      'NwsPost',
      'NwsAccess',
    ]);
    this.strongerPermissions.set('NwsPost', [
      'NwsAdmin',
      'NwsModerate',
      'NwsPost',
    ]);
    this.strongerPermissions.set('NwsModerate', ['NwsAdmin', 'NwsModerate']);
    this.strongerPermissions.set('NwsAdmin', ['NwsAdmin']);
  }

  /**
   * Registers the library-service permission hierarchy, from the weakest
   * ("LibNoAccess") to the strongest ("LibAdmin").
   */
  private loadLibraryStorngerPermission() {
    this.strongerPermissions.set('LibNoAccess', [
      'LibAdmin',
      'LibFullEdit',
      'LibManageOwn',
      'LibEditOnly',
      'LibAccess',
      'LibNoAccess',
    ]);
    this.strongerPermissions.set('LibAccess', [
      'LibAdmin',
      'LibFullEdit',
      'LibManageOwn',
      'LibEditOnly',
      'LibAccess',
    ]);
    this.strongerPermissions.set('LibManageOwn', [
      'LibAdmin',
      'LibFullEdit',
      'LibManageOwn',
    ]);
    this.strongerPermissions.set('LibEditOnly', [
      'LibAdmin',
      'LibFullEdit',
      'LibManageOwn',
      'LibEditOnly',
    ]);
    this.strongerPermissions.set('LibFullEdit', ['LibAdmin', 'LibFullEdit']);
    this.strongerPermissions.set('LibAdmin', ['LibAdmin']);
  }

  /**
   * Checks whether the given permission is granted on the node.
   *
   * When `ownerPermissions` is non-empty, permissions listed there are only
   * considered granted if the current user owns the node. A permission not in
   * `ownerPermissions` is granted whenever the node's permission map marks it
   * as {@link ALLOWED}; if it is not explicitly allowed but owner permissions
   * are being checked, it is granted only when the user owns the node.
   *
   * @param node The node whose granted permissions are inspected.
   * @param permission The permission to test.
   * @param ownerPermissions Permissions that additionally require node
   *   ownership; pass an empty array to skip ownership checks.
   * @returns `true` if the current user effectively holds the permission on
   *   the node, otherwise `false`.
   */
  public hasPermission(
    node: ModelNode,
    permission: AllPermission,
    ownerPermissions: AllPermission[]
  ): boolean {
    const checkOwner: boolean = ownerPermissions.length > 0;
    let result = false;
    if (node !== undefined) {
      if (node.permissions !== undefined) {
        if (node.permissions[permission] === this.ALLOWED) {
          if (checkOwner && ownerPermissions.includes(permission)) {
            result = this.isOwner(node);
          } else {
            result = true;
          }
        } else if (checkOwner) {
          result = this.isOwner(node);
        }
      }
    }
    return result;
  }
  /**
   * Checks whether the node grants at least one of the given permissions.
   *
   * @param node The node whose granted permissions are inspected.
   * @param permissions The permissions to test; the first one that matches
   *   short-circuits the evaluation.
   * @param ownerPermissions Permissions that additionally require node
   *   ownership; pass an empty array to skip ownership checks.
   * @returns `true` if any of the permissions is effectively held on the
   *   node, otherwise `false`.
   */
  public hasAnyOfPermissions(
    node: ModelNode,
    permissions: AllPermission[],
    ownerPermissions: AllPermission[]
  ): boolean {
    let result = false;

    if (node !== undefined) {
      if (node.permissions !== undefined) {
        for (const permission of permissions) {
          if (this.hasPermission(node, permission, ownerPermissions)) {
            result = true;
            break;
          }
        }
      }
    }
    return result;
  }

  /**
   * Checks whether the node grants the given permission or any permission
   * that is stronger than it within the same service area.
   *
   * @param node The node whose granted permissions are inspected.
   * @param permission The baseline permission to test against.
   * @param ownerPermissions Permissions that additionally require node
   *   ownership; pass an empty array to skip ownership checks.
   * @returns `true` if the node grants an equal-or-stronger permission,
   *   otherwise `false`.
   * @throws Error If the permission has no registered hierarchy (propagated
   *   from {@link getStrongerPermissions}).
   */
  public hasStrongerPermission(
    node: ModelNode,
    permission: AllPermission,
    ownerPermissions: AllPermission[]
  ): boolean {
    return this.hasAnyOfPermissions(
      node,
      this.getStrongerPermissions(permission),
      ownerPermissions
    );
  }

  /**
   * Returns the list of permissions that are equal to or stronger than the
   * given one, according to the loaded hierarchy.
   *
   * @param permission The permission whose stronger-or-equal set is requested.
   * @returns The permissions that satisfy the given permission.
   * @throws Error If the permission is not registered in the hierarchy.
   */
  private getStrongerPermissions(permission: AllPermission): AllPermission[] {
    const result = this.strongerPermissions.get(permission);
    if (result) {
      return result;
    }
    throw new Error(`unsupported permission type ${permission}`);
  }

  /**
   * Determines whether the current user owns the given node by comparing the
   * logged-in username with the node's `owner` property.
   *
   * @param node The node whose ownership is checked.
   * @returns `true` if the current user is the node owner, otherwise `false`.
   */
  private isOwner(node: ModelNode): boolean {
    const userName = this.loginService.getCurrentUsername();

    return userName === node?.properties?.owner;
  }
}
