import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
  viewChild,
} from '@angular/core';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeNodeComponent } from 'app/shared/treeview/tree-node.component';

/**
 * Root component of the CIRCABC tree view (`cbc-tree-view`).
 *
 * Renders a hierarchical, expandable folder tree by delegating the rendering
 * of the tree structure to a single root {@link TreeNodeComponent}. It acts as
 * the public entry point for embedding a folder tree in a page (for example the
 * document library or the newsgroups browser) and forwards user interactions
 * (node selection and node clicks) to the host component through its outputs.
 *
 * The component is configured through its {@link TreeNode} `root` input and a
 * set of behavioural inputs, and exposes a {@link reload} method to refresh the
 * underlying tree on demand.
 */
@Component({
  selector: 'cbc-tree-view',
  templateUrl: './tree-view.component.html',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TreeNodeComponent],
})
export class TreeViewComponent {
  /**
   * Reference to the root {@link TreeNodeComponent} instance rendered in the
   * template. Used to trigger a reload of the whole tree. Required, so it is
   * guaranteed to be resolved once the view has been initialised.
   */
  readonly treeNodeComponent = viewChild.required(TreeNodeComponent);

  /**
   * The root {@link TreeNode} that seeds the tree. This mandatory input defines
   * the top of the hierarchy that will be rendered and expanded.
   */
  readonly root = input.required<TreeNode>();

  /**
   * The path of nodes (from the root down to the current location) that should
   * be expanded/highlighted when the tree is displayed. Defaults to an empty
   * array (no pre-expanded path).
   */
  readonly displayedPath = input<ModelNode[]>([]);

  /**
   * The service context the tree is browsing, which influences how nodes are
   * loaded and rendered. Either `'library'` (document library) or
   * `'newsgroups'`. Defaults to `'library'`.
   */
  readonly service = input<'library' | 'newsgroups'>('library');

  /**
   * Optional identifier of the folder that should be treated as the currently
   * selected/active folder within the tree.
   */
  readonly folderId = input<string>();

  /**
   * Whether the tree is disabled (nodes cannot be interacted with). Defaults to
   * `false`.
   */
  readonly disabled = input(false);

  /**
   * Whether the selection control (selector) is shown next to each node,
   * allowing the user to pick a node. Defaults to `true`.
   */
  readonly showSelector = input(true);

  /**
   * Number of days during which a node is considered "new" and flagged
   * accordingly in the UI. A value of `-1` disables the "new" flagging.
   * Defaults to `-1`.
   */
  readonly flagNewDays = input(-1);

  /**
   * Emits the {@link TreeNode} that was selected by the user (for example via
   * the selector control).
   */
  readonly selectedNodeEmitter = output<TreeNode>();

  /**
   * Emits the {@link TreeNode} that was clicked by the user (navigation intent
   * on the node itself).
   */
  readonly clickedNodeEmitter = output<TreeNode>();

  /**
   * Reloads the tree by delegating to the root {@link TreeNodeComponent}.
   *
   * If the root node component has not yet been resolved, the call is a no-op.
   *
   * @returns A promise that resolves once the underlying tree node has finished
   * reloading.
   */
  async reload() {
    const treeNodeComponent = this.treeNodeComponent();
    if (treeNodeComponent) {
      await treeNodeComponent.reload();
    }
  }

  /**
   * Handles a node selection by re-emitting it through
   * {@link selectedNodeEmitter}.
   *
   * @param node The {@link TreeNode} that was selected.
   */
  selectNode(node: TreeNode) {
    this.selectedNodeEmitter.emit(node);
  }

  /**
   * Handles a node click by re-emitting it through {@link clickedNodeEmitter}.
   *
   * @param node The {@link TreeNode} that was clicked.
   */
  clickNode(node: TreeNode) {
    this.clickedNodeEmitter.emit(node);
  }
}
