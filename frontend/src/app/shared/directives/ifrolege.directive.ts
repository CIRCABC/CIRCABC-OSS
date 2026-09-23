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
import { assertDefined } from 'app/core/asserts';
import { PermissionEvaluator } from 'app/core/evaluator/permission-evaluator';
import { AllPermission } from 'app/core/evaluator/permissions';
import { Node as ModelNode } from 'app/core/generated/circabc';
/**
 * Structural directive that conditionally renders its host template only when
 * the current user holds the specified permission on a node, or any permission
 * that is considered stronger than it.
 *
 * The permission comparison is delegated to {@link PermissionEvaluator}, which
 * decides whether the user's effective permission on the node is greater than
 * or equal to (GE) the requested permission, taking into account the provided
 * ranking of stronger permissions.
 *
 * The directive re-evaluates its condition both on initialization and whenever
 * its bound input changes, adding or removing the embedded view accordingly.
 *
 * @example
 *  <div *cbcIfRoleGE="[node, 'LibManageOwn', strongerPermissions]">
 *    bla bla
 *  </div>
 * The `<div>bla bla</div>` is shown only if the user has at least the
 * `LibManageOwn` permission on `node`.
 */
@Directive({
  selector: '[cbcIfRoleGE]',
})
export class IfRoleGEDirective implements OnInit, OnChanges {
  /** Reference to the template that this structural directive controls. */
  private readonly templateRef = inject<TemplateRef<unknown>>(TemplateRef);
  /** Container used to create or clear the embedded view of the template. */
  private readonly viewContainer = inject(ViewContainerRef);
  /** Collaborator that evaluates whether the user meets the required permission. */
  private readonly permissionEvaluator = inject(PermissionEvaluator);

  /**
   * Required input driving the directive, provided as a tuple:
   * - index 0: the {@link ModelNode} to evaluate permissions against (may be
   *   `undefined` before it is resolved);
   * - index 1: the minimum {@link AllPermission} required to render the template;
   * - index 2: the list of {@link AllPermission} values that are considered
   *   stronger than the required permission.
   */
  readonly cbcIfRoleGE =
    input.required<[ModelNode | undefined, AllPermission, AllPermission[]]>();

  /**
   * Angular lifecycle hook. Performs the initial permission evaluation and
   * renders the embedded view when the user has the required permission (or a
   * stronger one) on the node.
   *
   * @throws Error if the node at index 0 of the input tuple is not defined.
   */
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

  /**
   * Angular lifecycle hook. Re-evaluates the permission condition whenever the
   * `cbcIfRoleGE` input changes and both the node and the required permission
   * are present, clearing and recreating the embedded view as needed.
   *
   * @param changes The set of changed inputs for this directive.
   */
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
