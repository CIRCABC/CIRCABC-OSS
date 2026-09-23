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
 * Structural directive that conditionally renders its host template based on the
 * permissions a user holds on a given node.
 *
 * The directive delegates the actual permission check to {@link PermissionEvaluator}.
 * The embedded view is created only when the user has *any* of the supplied
 * permissions; otherwise nothing is rendered. The condition is re-evaluated whenever
 * the bound input changes (see {@link IfRolesDirective.ngOnChanges}).
 *
 * @example
 * ```html
 * <div *cbcIfRoles="[node, ['LibAdmin', 'LibManageOwn']]">
 *   bla bla
 * </div>
 * ```
 * The `<div>bla bla</div>` is shown when `node` grants the permission
 * `LibAdmin` or `LibManageOwn`.
 */
@Directive({
  selector: '[cbcIfRoles]',
})
export class IfRolesDirective implements OnInit, OnChanges {
  /** Reference to the embedded template that this directive controls. */
  private readonly templateRef = inject<TemplateRef<unknown>>(TemplateRef);
  /** Container used to create or clear the embedded view for the template. */
  private readonly viewContainer = inject(ViewContainerRef);
  /** Collaborator that resolves whether a node grants a set of permissions. */
  private readonly permissionEvaluator = inject(PermissionEvaluator);

  /**
   * Required input bound to the `cbcIfRoles` selector.
   *
   * The tuple carries:
   * - index `0`: the {@link ModelNode} whose permissions are evaluated;
   * - index `1`: the primary list of {@link AllPermission} values to match;
   * - index `2`: an additional list of {@link AllPermission} values to match.
   *
   * The template is rendered when the node grants any permission from either list.
   */
  readonly cbcIfRoles =
    input.required<[ModelNode, AllPermission[], AllPermission[]]>();

  /**
   * Angular lifecycle hook. Performs the initial permission check and renders the
   * embedded view when the node grants any of the requested permissions.
   *
   * @returns Nothing.
   */
  ngOnInit() {
    this.viewContainer.clear();
    if (
      this.permissionEvaluator.hasAnyOfPermissions(
        this.cbcIfRoles()[0],
        this.cbcIfRoles()[1],
        this.cbcIfRoles()[2]
      )
    ) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    }
  }

  /**
   * Angular lifecycle hook. Re-evaluates the permission check whenever the
   * `cbcIfRoles` input changes and refreshes the rendered view accordingly.
   *
   * The view container is cleared and the embedded view re-created only when the
   * new value provides both a node and a primary permission list, and the node
   * grants any of the requested permissions.
   *
   * @param changes The set of input changes reported by Angular.
   * @returns Nothing.
   */
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
