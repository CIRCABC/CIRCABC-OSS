import { inject, Pipe, PipeTransform } from '@angular/core';

import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';

/**
 * Pure pipe (selector `cbcIfRole`) intended to be used within `@if`
 * blocks to combine an arbitrary boolean test with a permission check.
 *
 * The pipe short-circuits: it returns the result of ANDing the incoming
 * boolean value with a permission evaluation performed against a given node.
 * This lets templates express the boolean formula `myTest() && cbcIfRole`
 * in a single expression.
 *
 * @example
 * ```html
 * @if (myTest() | cbcIfRole: [nodeToTest, 'LibAdmin']) {
 *   <a>...</a>
 * }
 * ```
 *
 * @remarks
 * Delegates the permission evaluation to {@link PermissionEvaluator}.
 */
@Pipe({
  name: 'cbcIfRole',
})
export class IfRolePipe implements PipeTransform {
  /**
   * Collaborator responsible for evaluating whether a node grants a given
   * permission (or set of permissions) to the current user.
   */
  private readonly permissionEvaluator = inject(PermissionEvaluator);

  /**
   * Combines the incoming boolean value with a permission check on a node.
   *
   * @param value - The result of a preceding boolean test; the left-hand side
   * of the AND. When `false`, the permission check is not the deciding factor.
   * @param args - Tuple describing the permission check:
   * - `args[0]` ({@link ModelNode}): the node whose permissions are evaluated.
   * - `args[1]` ({@link AllPermission}): the primary required permission.
   * - `args[2]` ({@link AllPermission}[]): additional permissions to consider.
   * @returns `true` when `value` is truthy AND the permission evaluator grants
   * the requested permission(s) on the node; otherwise `false`.
   */
  transform(
    value: boolean,
    args: [ModelNode, AllPermission, AllPermission[]]
  ): boolean {
    return (
      value && this.permissionEvaluator.hasPermission(args[0], args[1], args[2])
    );
  }
}
