/**
 * Represents a single node in a hierarchical structure tree used to display
 * the organisational layout of a group's administration summary.
 *
 * Each node carries a display name and an ordered list of child nodes,
 * allowing the tree to be nested to an arbitrary depth.
 */
export interface StructureNode {
  /** Human-readable label shown for this node in the structure tree. */
  name: string;
  /**
   * Direct descendants of this node. An empty array denotes a leaf node
   * with no children.
   */
  children: StructureNode[];
}
