import { Component, Input } from '@angular/core';
import { StructureNode } from 'app/group/admin/summary/structure-tree/structure-node';

@Component({
  selector: 'cbc-structure-tree',
  templateUrl: './structure-tree.component.html',
  styleUrls: ['./structure-tree.component.scss'],
  preserveWhitespaces: true,
})
export class StructureTreeComponent {
  @Input()
  tree!: StructureNode;
  @Input()
  isRoot = true;
}
