import { Pipe, PipeTransform } from '@angular/core';

import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';

/**
 * This Pipe has to be used only in *ngIf directives
 * It is used to chain any kind of test to a permission check
 * Example
 * <a *ngIf="myTest() | cbcIfRoles: [ nodeToTest, ['LibAdmin', 'LibManageOwn']]">
 * The boolean formula is myTest() && cbcIfRoles
 */
@Pipe({
  name: 'cbcIfRoles',
})
export class IfRolesPipe implements PipeTransform {
  constructor(private permissionEvaluator: PermissionEvaluator) {}

  transform(
    value: boolean,
    args: [ModelNode, AllPermission[], AllPermission[]]
  ): boolean {
    return (
      value &&
      this.permissionEvaluator.hasAnyOfPermissions(args[0], args[1], args[2])
    );
  }
}
