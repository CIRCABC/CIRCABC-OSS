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
 *  Dispalay ui element is user has any of given permissions
 *  @example
 *  <div *cbcIfRoles="[node,['LibAdmin','LibManageOwn']]">
 *    bla bla
 *  </div>
 *  show <div> bla bla </div> if node has permission LibAdmin or LibManageOwn
 *
 */
@Directive({ selector: '[cbcIfRoles]' })
export class IfRolesDirective implements OnInit, OnChanges {
  @Input()
  cbcIfRoles!: [ModelNode, AllPermission[], AllPermission[]];

  constructor(
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private permissionEvaluator: PermissionEvaluator
  ) {}

  ngOnInit() {
    this.viewContainer.clear();
    if (
      this.permissionEvaluator.hasAnyOfPermissions(
        this.cbcIfRoles[0],
        this.cbcIfRoles[1],
        this.cbcIfRoles[2]
      )
    ) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.cbcIfRoles) {
      if (
        changes.cbcIfRoles.currentValue[0] &&
        changes.cbcIfRoles.currentValue[1]
      ) {
        this.viewContainer.clear();
        if (
          this.permissionEvaluator.hasAnyOfPermissions(
            changes.cbcIfRoles.currentValue[0],
            changes.cbcIfRoles.currentValue[1],
            changes.cbcIfRoles.currentValue[2]
          )
        ) {
          this.viewContainer.createEmbeddedView(this.templateRef);
        }
      }
    }
  }
}
