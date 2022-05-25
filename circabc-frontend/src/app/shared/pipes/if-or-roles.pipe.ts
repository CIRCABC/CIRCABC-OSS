import { Pipe, PipeTransform } from '@angular/core';

import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';

@Pipe({
  name: 'cbcIfOrRoles',
})
export class IfOrRolesPipe implements PipeTransform {
  constructor(private permissionEvaluator: PermissionEvaluator) {}

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
