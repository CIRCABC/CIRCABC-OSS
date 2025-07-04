import {
  Directive,
  OnChanges,
  OnInit,
  SimpleChanges,
  TemplateRef,
  ViewContainerRef,
  input,
} from '@angular/core';
import { assertDefined } from 'app/core/asserts';
import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';
/**
 * Dispalay ui element is user has given permission or some stronger permission
 * @example
 *  <div *cbcIfGERole="[node,'LibManageOwn']">
 *    bla bla
 *  </div>
 * show <div> bla bla </div> if node has at least permission LibManageOwn
 *
 */
@Directive({
  selector: '[cbcIfRoleGE]',
})
export class IfRoleGEDirective implements OnInit, OnChanges {
  readonly cbcIfRoleGE =
    input.required<[ModelNode | undefined, AllPermission, AllPermission[]]>();

  constructor(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private permissionEvaluator: PermissionEvaluator
  ) {}

  ngOnInit() {
    const cbcIfRoleGE = this.cbcIfRoleGE();
    assertDefined(cbcIfRoleGE[0]);
    this.viewContainer.clear();
    if (
      this.permissionEvaluator.hasStrongerPermission(
        cbcIfRoleGE[0],
        cbcIfRoleGE[1],
        cbcIfRoleGE[2]
      )
    ) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.cbcIfRoleGE) {
      if (
        changes.cbcIfRoleGE.currentValue[0] &&
        changes.cbcIfRoleGE.currentValue[1]
      ) {
        this.viewContainer.clear();
        if (
          this.permissionEvaluator.hasStrongerPermission(
            changes.cbcIfRoleGE.currentValue[0],
            changes.cbcIfRoleGE.currentValue[1],
            changes.cbcIfRoleGE.currentValue[2]
          )
        ) {
          this.viewContainer.createEmbeddedView(this.templateRef);
        }
      }
    }
  }
}
