import { Injectable } from '@angular/core';
import { Node as ModelNode } from 'app/core/generated/circabc';

import { AllPermission } from 'app/core/evaluator/permissions';
import { LoginService } from 'app/core/login.service';

@Injectable({
  providedIn: 'root',
})
export class PermissionEvaluator {
  private ALLOWED = 'ALLOWED';
  private strongerPermissions: Map<AllPermission, AllPermission[]> = new Map();

  constructor(private loginService: LoginService) {
    this.loadStrongerPermissions();
  }

  private loadStrongerPermissions(): void {
    this.loadLibraryStorngerPermission();
    this.loadNewsGroupStrongerPermission();
    this.loadEventsStrongerPermission();
    this.loadDirectoryStrongerPermissions();
    this.loadInformationStrongerPermissions();
  }

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

  private loadEventsStrongerPermission() {
    this.strongerPermissions.set('EveNoAccess', [
      'EveAdmin',
      'EveAccess',
      'EveNoAccess',
    ]);
    this.strongerPermissions.set('EveAccess', ['EveAdmin', 'EveAccess']);
    this.strongerPermissions.set('EveAdmin', ['EveAdmin']);
  }

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

  private getStrongerPermissions(permission: AllPermission): AllPermission[] {
    const result = this.strongerPermissions.get(permission);
    if (result) {
      return result;
    } else {
      throw new Error(`unsupported permission type ${permission}`);
    }
  }

  private isOwner(node: ModelNode): boolean {
    const userName = this.loginService.getCurrentUsername();
    if (node && node.properties && node.properties.owner) {
      return userName === node.properties.owner;
    } else {
      return false;
    }
  }
}
