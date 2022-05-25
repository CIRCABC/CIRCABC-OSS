import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewChild,
} from '@angular/core';
import { Node as ModelNode } from 'app/core/generated/circabc';
import { TreeNode } from 'app/shared/treeview/tree-node';
import { TreeNodeComponent } from 'app/shared/treeview/tree-node.component';

@Component({
  selector: 'cbc-tree-view',
  templateUrl: './tree-view.component.html',
  preserveWhitespaces: true,
})
export class TreeViewComponent {
  @ViewChild(TreeNodeComponent)
  treeNodeComponent!: TreeNodeComponent;
  @Input()
  root!: TreeNode;
  @Input()
  displayedPath: ModelNode[] = [];
  @Input()
  service: 'library' | 'newsgroups' = 'library';
  @Input()
  folderId: string | undefined;
  @Input()
  disabled = false;
  @Input()
  showSelector = true;
  @Input()
  flagNewDays = -1;
  @Output()
  readonly selectedNodeEmitter = new EventEmitter<TreeNode>();
  @Output()
  readonly clickedNodeEmitter = new EventEmitter<TreeNode>();

  async reload() {
    if (this.treeNodeComponent) {
      await this.treeNodeComponent.reload();
    }
  }

  selectNode(node: TreeNode) {
    this.selectedNodeEmitter.emit(node);
  }

  clickNode(node: TreeNode) {
    this.clickedNodeEmitter.emit(node);
  }
}
