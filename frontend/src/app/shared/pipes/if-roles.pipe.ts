import { inject, Pipe, PipeTransform } from '@angular/core';

import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';

/**
 * This Pipe has to be used only in `@if` blocks
 * It is used to chain any kind of test to a permission check
 *
 * @example
 * ```html
 * @if (myTest() | cbcIfRoles: [nodeToTest, ['LibAdmin', 'LibManageOwn']]) {
 *   <a>...</a>
 * }
 * ```
 * The boolean formula is myTest() && cbcIfRoles
 */
@Pipe({
  name: 'cbcIfRoles',
})
export class IfRolesPipe implements PipeTransform {
  /**
   * Collaborator used to evaluate whether the current user holds the
   * required permissions on a given node.
   */
  private readonly permissionEvaluator = inject(PermissionEvaluator);

  /**
   * Combines an arbitrary boolean expression with a node-level permission
   * check, producing the logical AND of the two.
   *
   * @param value The result of the caller's own boolean test (the left-hand
   * side of the boolean formula).
   * @param args A tuple describing the permission check:
   * `[node, permissions, altPermissions]`, where `node` is the node to test
   * and the two permission arrays are passed to
   * {@link PermissionEvaluator.hasAnyOfPermissions}.
   * @returns `true` when `value` is truthy and the user holds any of the
   * required permissions on the node; otherwise `false`.
   */
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
