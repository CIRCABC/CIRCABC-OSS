import { Pipe, PipeTransform } from '@angular/core';

import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';

/**
 * This Pipe has to be used only in *ngIf directives
 * It is used to chain any kind of test to a permission check
 * Example
 * <a *ngIf="myTest() | cbcIfRole: [nodeToTest, 'LibAdmin']">
 * The boolean formula is myTest() && cbcIfRole
 */
@Pipe({
  name: 'cbcIfRole',
})
export class IfRolePipe implements PipeTransform {
  constructor(private permissionEvaluator: PermissionEvaluator) {}

  transform(
    value: boolean,
    args: [ModelNode, AllPermission, AllPermission[]]
  ): boolean {
    return (
      value && this.permissionEvaluator.hasPermission(args[0], args[1], args[2])
    );
  }
}
