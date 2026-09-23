import {
  Directive,
  inject,
  input,
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
 * Structural directive that conditionally renders its host element based on
 * the current user's permissions for a given node.
 *
 * It delegates the permission check to {@link PermissionEvaluator} and only
 * embeds the associated template view when the user holds the required
 * permission. The view is re-evaluated whenever the bound input changes.
 *
 * @example
 *  <div *cbcIfRole="[node,'LibAdmin']">
 *    bla bla
 *  </div>
 * show <div> bla bla </div> if node has permission LibAdmin
 *
 */
@Directive({
  selector: '[cbcIfRole]',
})
export class IfRoleDirective implements OnInit, OnChanges {
  /** Template of the host element to conditionally render. */
  private readonly templateRef = inject<TemplateRef<unknown>>(TemplateRef);
  /** View container used to create or clear the embedded template view. */
  private readonly viewContainer = inject(ViewContainerRef);
  /** Collaborator that resolves whether the user holds a given permission. */
  private readonly permissionEvaluator = inject(PermissionEvaluator);

  /**
   * Required input driving the directive, provided as a tuple of:
   * - the {@link ModelNode} to evaluate permissions against,
   * - the required {@link AllPermission},
   * - an array of {@link AllPermission} used as additional/fallback permissions.
   */
  readonly cbcIfRole =
    input.required<[ModelNode, AllPermission, AllPermission[]]>();

  /**
   * Lifecycle hook that performs the initial permission check. Clears any
   * existing view and embeds the template only if the user has the required
   * permission for the bound node.
   */
  ngOnInit() {
    this.viewContainer.clear();
    if (
      this.permissionEvaluator.hasPermission(
        this.cbcIfRole()[0],
        this.cbcIfRole()[1],
        this.cbcIfRole()[2]
      )
    ) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    }
  }

  /**
   * Lifecycle hook that re-evaluates the permission whenever the `cbcIfRole`
   * input changes. When both a node and a required permission are present in
   * the new value, it clears the current view and re-embeds the template only
   * if the permission check succeeds.
   *
   * @param changes - The set of changed inputs for this directive.
   */
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
