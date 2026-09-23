/**
 * Represents a single node in a hierarchical tree view (e.g. the folder
 * tree used by the treeview component).
 *
 * A `TreeNode` holds the display metadata for the node it represents, tracks
 * its expanded/collapsed UI state, and references its child nodes, allowing a
 * nested tree structure to be built and rendered.
 */
export class TreeNode {
  // node properties

  /** Human-readable label displayed for this node. */
  name: string;

  /** Optional descriptive text associated with this node. Defaults to an empty string. */
  description: string;

  /** Unique identifier of the underlying node (typically the backend node reference). */
  nodeId: string;

  /** Creation date of the underlying node, when available. */
  created?: Date;

  /** Whether this node is currently expanded in the tree view. Defaults to `false`. */
  expanded = false;

  /** Child nodes nested under this node. Defaults to an empty array. */
  children: TreeNode[] = [];

  /**
   * Whether this node has sub-folders (children that can be loaded/expanded).
   * Used to decide if an expand affordance should be shown. Defaults to `false`.
   */
  hasSubFolders = false;

  /**
   * Creates a new tree node with the given label and identifier.
   *
   * @param name The human-readable label for the node.
   * @param nodeId The unique identifier of the underlying node.
   */
  constructor(name: string, nodeId: string) {
    this.name = name;
    this.nodeId = nodeId;
    this.description = '';
  }

  /**
   * Toggles the expanded/collapsed state of this node in the tree view.
   */
  toggleExpand() {
    this.expanded = !this.expanded;
  }
}
