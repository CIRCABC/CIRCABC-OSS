export class TreeNode {
  // node properties
  name: string;
  description: string;
  nodeId: string;
  created?: Date;
  expanded = false;
  children: TreeNode[] = [];
  hasSubFolders = false;

  constructor(name: string, nodeId: string) {
    this.name = name;
    this.nodeId = nodeId;
    this.description = '';
  }

  toggleExpand() {
    this.expanded = !this.expanded;
  }
}
