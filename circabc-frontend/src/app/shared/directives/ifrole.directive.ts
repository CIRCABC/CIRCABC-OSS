import {
  Directive,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  TemplateRef,
  ViewContainerRef,
} from '@angular/core';
import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';
/**
 * Dispalay ui element is user has given permission
 * @example
 *  <div *cbcIfRole="[node,'LibAdmin']">
 *    bla bla
 *  </div>
 * show <div> bla bla </div> if node has permission LibAdmin
 *
 */
@Directive({ selector: '[cbcIfRole]' })
export class IfRoleDirective implements OnInit, OnChanges {
  @Input()
  cbcIfRole!: [ModelNode, AllPermission, AllPermission[]];

  constructor(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private permissionEvaluator: PermissionEvaluator
  ) {}

  ngOnInit() {
    this.viewContainer.clear();
    if (
      this.permissionEvaluator.hasPermission(
        this.cbcIfRole[0],
        this.cbcIfRole[1],
        this.cbcIfRole[2]
      )
    ) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.cbcIfRole) {
      if (
        changes.cbcIfRole.currentValue[0] &&
        changes.cbcIfRole.currentValue[1]
      ) {
        this.viewContainer.clear();
        if (
          this.permissionEvaluator.hasPermission(
            changes.cbcIfRole.currentValue[0],
            changes.cbcIfRole.currentValue[1],
            changes.cbcIfRole.currentValue[2]
          )
        ) {
          this.viewContainer.createEmbeddedView(this.templateRef);
        }
      }
    }
  }
}
