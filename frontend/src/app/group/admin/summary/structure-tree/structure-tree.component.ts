import {
  ChangeDetectionStrategy,
  Component,
  forwardRef,
  input,
} from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';
import { type StructureNode } from 'app/group/admin/summary/structure-tree/structure-node';

/**
 * Renders a single {@link StructureNode} and, recursively, its descendants as a
 * nested hierarchical tree in the group administration summary view.
 *
 * The component references itself in its `imports` array (via `forwardRef`) so
 * that each node's children are rendered by further instances of this same
 * component, producing an arbitrarily deep tree. The `isRoot` flag lets the
 * template distinguish the top-most invocation from nested ones (for example to
 * adjust styling or wrapping markup).
 */
@Component({
  selector: 'cbc-structure-tree',
  templateUrl: './structure-tree.component.html',
  styleUrl: './structure-tree.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [forwardRef(() => StructureTreeComponent), TranslocoModule],
})
export class StructureTreeComponent {
  /**
   * Required input holding the {@link StructureNode} to render at this level of
   * the tree, including its name and any nested children.
   */
  tree = input.required<StructureNode>();
  /**
   * Whether this instance represents the root of the tree. Defaults to `true`;
   * recursive child instances set it to `false` so the template can treat the
   * outermost node differently from nested nodes.
   */
  readonly isRoot = input(true);
}
