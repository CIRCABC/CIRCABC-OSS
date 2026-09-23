import { inject, Pipe, PipeTransform } from '@angular/core';

import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';

/**
 * Pipe (`cbcIfOrRoles`) that performs a logical OR between an existing boolean
 * condition and a permission check on a given node.
 *
 * It returns `true` when either the incoming `value` is already `true`, or when
 * the current user holds any of the required permissions on the provided node.
 * This is typically used in templates to conditionally show or enable UI when a
 * base condition is met OR the user has one of the accepted roles/permissions.
 *
 * Permission resolution is delegated to the {@link PermissionEvaluator}.
 */
@Pipe({
  name: 'cbcIfOrRoles',
})
export class IfOrRolesPipe implements PipeTransform {
  /**
   * Collaborator used to evaluate whether the current user holds the requested
   * permissions on a given node.
   */
  private readonly permissionEvaluator = inject(PermissionEvaluator);

  /**
   * Combines a base boolean condition with a node-scoped permission check.
   *
   * @param value The base condition; if already `true`, the result is `true`
   * regardless of permissions.
   * @param args A tuple of `[node, dynamicPermissions, staticPermissions]`
   * where `node` is the target {@link ModelNode} and the two permission arrays
   * hold the {@link AllPermission} values that satisfy the check.
   * @returns `true` if `value` is `true` or the current user has any of the
   * supplied permissions on `node`; otherwise `false`.
   */
  transform(
    value: boolean,
    args: [ModelNode, AllPermission[], AllPermission[]]
  ): boolean {
    return (
      value ||
      this.permissionEvaluator.hasAnyOfPermissions(args[0], args[1], args[2])
    );
  }
}
