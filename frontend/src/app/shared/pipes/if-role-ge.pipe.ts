import { inject, Pipe, PipeTransform } from '@angular/core';

import { assertDefined } from 'app/core/asserts';
import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';

/**
 * Pure Angular pipe (`cbcIfRoleGE`) that combines an arbitrary boolean
 * expression with a permission check, intended to be used inside `@if`
 * blocks.
 *
 * The pipe evaluates the logical AND between the piped input value and the
 * result of a permission comparison performed by the {@link PermissionEvaluator}.
 * This lets templates chain any custom test with a role/permission gate.
 *
 * @example
 * ```html
 * @if (myTest() | cbcIfRoleGE: [nodeToTest, 'LibAdmin']) {
 *   <a>...</a>
 * }
 * ```
 * The resulting boolean formula is `myTest() && cbcIfRoleGE(...)`.
 *
 * @remarks
 * Relies on {@link PermissionEvaluator} as its key collaborator to determine
 * whether the current user's permission on the given node is stronger than or
 * equal to the required permission.
 */
@Pipe({
  name: 'cbcIfRoleGE',
})
export class IfRoleGePipe implements PipeTransform {
  /**
   * Collaborator used to evaluate whether the user's effective permission on a
   * node meets or exceeds the required permission.
   */
  private readonly permissionEvaluator = inject(PermissionEvaluator);

  /**
   * Transforms the piped boolean value by ANDing it with a permission check.
   *
   * @param value - The boolean result of the caller's own test (the left-hand
   * side of the logical AND).
   * @param args - A tuple describing the permission check:
   * - `args[0]`: the {@link ModelNode} to test permissions against (must be
   *   defined at runtime);
   * - `args[1]`: the {@link AllPermission} required as the baseline;
   * - `args[2]`: an array of {@link AllPermission} values used by the evaluator
   *   to determine the permission ordering.
   * @returns `true` only when `value` is `true` and the user holds a permission
   * on the node that is stronger than or equal to the required permission;
   * otherwise `false`.
   * @throws If `args[0]` (the node) is `undefined`, as enforced by
   * {@link assertDefined}.
   */
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
