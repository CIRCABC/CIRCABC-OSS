import { Pipe, PipeTransform } from '@angular/core';

import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { assertDefined } from 'app/core/asserts';

/**
 * This Pipe has to be used only in *ngIf directives
 * It is used to chain any kind of test to a permission check
 * Example
 * <a *ngIf="myTest() | cbcIfRoleGE: [ nodeToTest, 'LibAdmin']">
 * The boolean formula is myTest() && cbcIfRoleGE
 */
@Pipe({
  name: 'cbcIfRoleGE',
})
export class IfRoleGePipe implements PipeTransform {
  constructor(private permissionEvaluator: PermissionEvaluator) {}

  transform(
    value: boolean,
    args: [ModelNode | undefined, AllPermission, AllPermission[]]
  ): boolean {
    assertDefined(args[0]);
    return (
      value &&
      this.permissionEvaluator.hasStrongerPermission(args[0], args[1], args[2])
    );
  }
}
