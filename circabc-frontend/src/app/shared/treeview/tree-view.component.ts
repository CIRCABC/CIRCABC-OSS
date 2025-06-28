import { Component, viewChild, output, input } from '@angular/core';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeNodeComponent } from 'app/shared/treeview/tree-node.component';

@Component({
  selector: 'cbc-tree-view',
  templateUrl: './tree-view.component.html',
  preserveWhitespaces: true,
  imports: [TreeNodeComponent],
})
export class TreeViewComponent {
  readonly treeNodeComponent = viewChild.required(TreeNodeComponent);
  readonly root = input.required<TreeNode>();
  readonly displayedPath = input<ModelNode[]>([]);
  readonly service = input<'library' | 'newsgroups'>('library');
  readonly folderId = input<string>();
  readonly disabled = input(false);
  readonly showSelector = input(true);
  readonly flagNewDays = input(-1);
  readonly selectedNodeEmitter = output<TreeNode>();
  readonly clickedNodeEmitter = output<TreeNode>();

  async reload() {
    const treeNodeComponent = this.treeNodeComponent();
    if (treeNodeComponent) {
      await treeNodeComponent.reload();
    }
  }

  selectNode(node: TreeNode) {
    this.selectedNodeEmitter.emit(node);
  }

  clickNode(node: TreeNode) {
    this.clickedNodeEmitter.emit(node);
  }
}
